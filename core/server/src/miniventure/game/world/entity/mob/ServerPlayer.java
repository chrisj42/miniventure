package miniventure.game.world.entity.mob;

import java.util.Arrays;
import java.util.EnumMap;

import miniventure.game.GameProtocol.InventoryUpdate;
import miniventure.game.GameProtocol.StatUpdate;
import miniventure.game.item.Hands;
import miniventure.game.item.Inventory;
import miniventure.game.item.Item;
import miniventure.game.item.ServerHands;
import miniventure.game.server.ServerCore;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.Boundable;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.particle.ActionParticle.ActionType;
import miniventure.game.world.entity.particle.TextParticle;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerPlayer extends ServerMob implements Player {
	
	private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
	
	@NotNull private final ServerHands hands;
	private Inventory inventory;
	
	public ServerPlayer() {
		super("player", Stat.Health.initial);
		
		hands = new ServerHands(this);
		reset();
	}
	
	protected ServerPlayer(String[][] allData, Version version) {
		super(Arrays.copyOfRange(allData, 0, allData.length-1), version);
		String[] data = allData[allData.length-1];
		
		hands = new ServerHands(this);
		reset();
		
		stats.put(Stat.Health, getHealth());
		stats.put(Stat.Hunger, Integer.parseInt(data[0]));
		stats.put(Stat.Stamina, Integer.parseInt(data[1]));
		//stats.put(Stat.Armor, Integer.parseInt(data[2]));
		
		inventory.loadItems(MyUtils.parseLayeredString(data[2]));
		hands.loadItem(MyUtils.parseLayeredString(data[3]));
	}
	
	@Override
	public Array<String[]> save() {
		Array<String[]> data = super.save();
		
		data.add(new String[] {
			getStat(Stat.Hunger)+"",
			getStat(Stat.Stamina)+"",
			MyUtils.encodeStringArray(inventory.save()),
			MyUtils.encodeStringArray(hands.save())
		});
		
		return data;
	}
	
	// use this instead of creating a new player.
	public void reset() {
		for(Stat stat: Stat.values)
			stats.put(stat, stat.initial);
		
		super.reset();
		
		hands.reset();
		inventory = new Inventory(INV_SIZE, hands);
	}
	
	public int getStat(@NotNull Stat stat) {
		return stats.get(stat);
	}
	public int changeStat(@NotNull Stat stat, int amt) {
		int prevVal = stats.get(stat);
		stats.put(stat, Math.max(0, Math.min(stat.max, stats.get(stat) + amt)));
		if(stat == Stat.Health && amt > 0)
			regenHealth(amt);
		
		int change = stats.get(stat) - prevVal;
		
		if(amt != 0) {
			ServerCore.getServer().sendToPlayer(this, new StatUpdate(stat, amt));
			
			if(stat == Stat.Hunger && amt > 0) {
				Level level = getLevel();
				if(level != null)
					level.addEntity(new TextParticle(amt + "", Color.CORAL), getCenter(), true);
			}
		}
		
		return change;
	}
	
	public void loadStat(StatUpdate update) {
		Stat stat = Stat.values[update.statIndex];
		stats.put(stat, update.amount);
		if(stat == Stat.Health) setHealth(update.amount);
	}
	
	public boolean moveTo(float x, float y, float z) { return moveTo(new Vector3(x, y, z)); }
	public boolean moveTo(Vector3 pos) { return move(pos.cpy().sub(getLocation())); }
	
	@Override
	public Integer[] saveStats() { return Stat.save(stats); }
	
	/// These two methods are ONLY to be accessed by GameScreen, so far.
	@NotNull @Override
	public Hands getHands() { return hands; }
	@Override
	public Inventory getInventory() { return inventory; }
	
	public boolean takeItem(@NotNull Item item) {
		boolean success = false;
		if(hands.addItem(item))
			success = true;
		else
			success = inventory.addItem(item, 1) == 1;
		
		if(success)
			ServerCore.getServer().sendToPlayer(this, new InventoryUpdate(this));
		
		return success;
	}
	
	@NotNull
	private Array<WorldObject> getInteractionQueue() {
		Array<WorldObject> objects = new Array<>();
		
		// get level, and don't interact if level is not found
		Level level = getWorld().getEntityLevel(this);
		if(level == null) return objects;
		
		Rectangle interactionBounds = getInteractionRect();
		
		objects.addAll(level.getOverlappingEntities(interactionBounds, this));
		Boundable.sortByDistance(objects, getCenter());
		
		Tile tile = level.getClosestTile(interactionBounds);
		if(tile != null)
			objects.add(tile);
		
		return objects;
	}
	
	public void attack() {
		if(!hands.hasUsableItem()) return;
		
		Level level = getLevel();
		Item heldItem = hands.getUsableItem();
		
		boolean success = false;
		for(WorldObject obj: getInteractionQueue()) {
			if (heldItem.attack(obj, this)) {
				success = true;
				break;
			}
		}
		
		if(!heldItem.isUsed())
			changeStat(Stat.Stamina, -1); // for trying...
		
		if (level != null) {
			if(success)
				level.addEntity(ActionType.SLASH.get(getDirection()), getCenter().add(getDirection().getVector().scl(getSize().scl(0.5f))), true);
			else
				level.addEntity(ActionType.PUNCH.get(getDirection()), getInteractionRect().getCenter(new Vector2()), true);
		}
	}
	
	public void interact() {
		if(!hands.hasUsableItem()) return;
		
		Item heldItem = hands.getUsableItem();
		
		boolean success = false;
		for(WorldObject obj: getInteractionQueue()) {
			if (heldItem.interact(obj, this)) {
				success = true;
				break;
			}
		}
		
		if(!success)
			// none of the above interactions were successful, do the reflexive use.
			heldItem.interact(this);
		
		if(!heldItem.isUsed())
			changeStat(Stat.Stamina, -1); // for trying...
	}
	
	@Override
	public boolean attackedBy(WorldObject source, @Nullable Item item, int dmg) {
		if(super.attackedBy(source, item, dmg)) {
			int health = stats.get(Stat.Health);
			if (health == 0) return false;
			changeStat(Stat.Health, -dmg);
			// here is where I'd make a death chest, and show the death screen.
			return true;
		}
		return false;
	}
}
