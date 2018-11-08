package miniventure.game.world.entity.mob;

import java.util.ArrayList;
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
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.Boundable;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.particle.ActionType;
import miniventure.game.world.entity.ClassDataList;
import miniventure.game.world.entity.particle.ParticleData.ActionParticleData;
import miniventure.game.world.entity.particle.ParticleData.TextParticleData;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerPlayer extends ServerMob implements Player {
	
	private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
	
	@NotNull private final ServerHands hands;
	private Inventory inventory;
	
	private final String name;
	
	public ServerPlayer(String name) {
		super("player", Stat.Health.initial);
		
		this.name = name;
		inventory = new Inventory(INV_SIZE);
		hands = new ServerHands(this);
		reset();
	}
	
	protected ServerPlayer(ClassDataList allData, final Version version, ValueFunction<ClassDataList> modifier) {
		super(allData, version, modifier);
		ArrayList<String> data = allData.get(2);
		
		name = data.get(0);
		hands = new ServerHands(this);
		reset();
		
		stats.put(Stat.Health, getHealth());
		stats.put(Stat.Hunger, Integer.parseInt(data.get(1)));
		stats.put(Stat.Stamina, Integer.parseInt(data.get(2)));
		//stats.put(Stat.Armor, Integer.parseInt(data.get(3)));
		
		inventory.loadItems(MyUtils.parseLayeredString(data.get(3)));
		hands.loadItemShortcuts(MyUtils.parseLayeredString(data.get(4)));
	}
	
	@Override
	public ClassDataList save() {
		ClassDataList allData = super.save();
		ArrayList<String> data = new ArrayList<>(Arrays.asList(
			name,
			String.valueOf(getStat(Stat.Hunger)),
			String.valueOf(getStat(Stat.Stamina)),
			MyUtils.encodeStringArray(inventory.save()),
			MyUtils.encodeStringArray(hands.save())
		));
		
		allData.add(data);
		return allData;
	}
	
	public String getName() { return name; }
	
	// use this instead of creating a new player.
	@Override
	public void reset() {
		for(Stat stat: Stat.values)
			stats.put(stat, stat.initial);
		
		super.reset();
		
		inventory.reset();
		hands.reset();
	}
	
	@Override
	public int getStat(@NotNull Stat stat) {
		return stats.get(stat);
	}
	@Override
	public int changeStat(@NotNull Stat stat, int amt) {
		int prevVal = stats.get(stat);
		stats.put(stat, Math.max(0, Math.min(stat.max, stats.get(stat) + amt)));
		if(stat == Stat.Health && amt > 0)
			regenHealth(amt);
		
		int change = stats.get(stat) - prevVal;
		
		if(amt != 0) {
			ServerCore.getServer().sendToPlayer(this, new StatUpdate(stat, amt));
			
			if(stat == Stat.Hunger && change > 0) {
				ServerLevel level = getLevel();
				if(level != null)
					ServerCore.getServer().broadcastParticle(new TextParticleData(String.valueOf(amt), Color.CORAL), this);
			}
		}
		
		return change;
	}
	
	public void loadStat(StatUpdate update) {
		stats.put(update.stat, update.amount);
		if(update.stat == Stat.Health) setHealth(update.amount);
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		
		hands.resetItemUsage();
	}
	
	// public boolean moveTo(float x, float y, float z) { return moveTo(new Vector3(x, y, z)); }
	// public boolean moveTo(Vector3 pos) { return move(pos.cpy().sub(getLocation())); }
	
	@Override
	public Integer[] saveStats() { return Stat.save(stats); }
	
	@Override
	public void setDirection(@NotNull Direction dir) { super.setDirection(dir); }
	
	/// These two methods are ONLY to be accessed by GameScreen, so far.
	@NotNull @Override
	public Hands getHands() { return hands; }
	@Override
	public Inventory getInventory() { return inventory; }
	
	public boolean takeItem(@NotNull Item item) {
		if(inventory.addItem(item)) {
			hands.addItem(item); // add to open hotbar slot if it exists
			ServerCore.getServer().playEntitySound("pickup", this, false);
			ServerCore.getServer().sendToPlayer(this, new InventoryUpdate(this));
			return true;
		}
		
		return false;
	}
	
	@NotNull
	private Array<WorldObject> getInteractionQueue() {
		Array<WorldObject> objects = new Array<>();
		
		// get level, and don't interact if level is not found
		ServerLevel level = getWorld().getEntityLevel(this);
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
		//System.out.println("server player attacking; item = "+hands.getSelectedItem());
		if(!hands.hasUsableItem()) return; // only ever not true for attacks in the same frame or if not enough stamina
		
		Level level = getLevel();
		Item heldItem = hands.getSelectedItem();
		
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
				ServerCore.getServer().broadcastParticle(
					new ActionParticleData(ActionType.SLASH, getDirection()),
					level,
					getCenter().add(getDirection().getVector().scl(getSize().scl(0.5f)))
				);
			else
				ServerCore.getServer().broadcastParticle(
					new ActionParticleData(ActionType.PUNCH, getDirection()),
					level,
					getInteractionRect().getCenter(new Vector2())
				);
		}
		
		if(!success) // if successful, then the sound will be taken care of. This sound is of an empty swing.
			ServerCore.getServer().playEntitySound("swing", this);
	}
	
	public void interact() {
		//System.out.println("server player interacting; item = "+hands.getSelectedItem());
		if(!hands.hasUsableItem()) return;
		
		Item heldItem = hands.getSelectedItem();
		
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
		
		if(!heldItem.isUsed()) {
			changeStat(Stat.Stamina, -1); // for trying...
			// TODO failed interaction sound
		}
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
	
	@Override
	public void die() { getWorld().despawnPlayer(this); }
	
	@Override
	public boolean maySpawn(TileTypeEnum type) {
		return super.maySpawn(type) && type != TileTypeEnum.SAND;
	}
}
