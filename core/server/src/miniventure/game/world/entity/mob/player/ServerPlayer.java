package miniventure.game.world.entity.mob.player;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.HotbarUpdate;
import miniventure.game.GameProtocol.InventoryAddition;
import miniventure.game.GameProtocol.InventoryUpdate;
import miniventure.game.GameProtocol.StatUpdate;
import miniventure.game.item.Inventory;
import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.server.GameServer;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.Boundable;
import miniventure.game.world.Point;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.ClassDataList;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.mob.ServerMob;
import miniventure.game.world.entity.particle.ActionType;
import miniventure.game.world.entity.particle.ParticleData.ActionParticleData;
import miniventure.game.world.entity.particle.ParticleData.TextParticleData;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.ServerTile;
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
	
	// saved spawn location; you slept in a bed, etc. This is checked when adding the player to a level. If there is no location, then the player is spawned randomly on the default level. If the location (and adjacent tiles) are not spawnable, then the player is spawned randomly on the saved level (assuming it's valid).
	private Point spawnLoc;
	private int spawnLevel = 0;
	
	
	@NotNull private final ServerHands hands;
	@NotNull private final Inventory inventory;
	
	private final String name;
	
	public ServerPlayer(@NotNull ServerWorld world, String name) {
		super(world, "player", Stat.Health.initial);
		
		this.name = name;
		inventory = new Inventory(INV_SIZE);
		hands = new ServerHands(this);
		reset();
	}
	
	protected ServerPlayer(@NotNull ServerWorld world, ClassDataList allData, final Version version, ValueFunction<ClassDataList> modifier) {
		super(world, allData, version, modifier);
		ArrayList<String> data = allData.get(2);
		
		name = data.get(0);
		inventory = new Inventory(INV_SIZE);
		hands = new ServerHands(this, MyUtils.parseLayeredString(data.get(6)));
		
		stats.put(Stat.Health, getHealth());
		stats.put(Stat.Hunger, Integer.parseInt(data.get(1)));
		stats.put(Stat.Stamina, Integer.parseInt(data.get(2)));
		// stats.put(Stat.Armor, Integer.parseInt(data.get(3)));
		
		if(data.get(3).equals("null"))
			spawnLoc = null;
		else {
			String[] pos = data.get(3).split(";");
			spawnLoc = new Point(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
		}
		spawnLevel = Integer.parseInt(data.get(4));
		
		inventory.loadItems(MyUtils.parseLayeredString(data.get(5)));
	}
	
	@Override
	public ClassDataList save() {
		ClassDataList allData = super.save();
		ArrayList<String> data = new ArrayList<>(Arrays.asList(
			name,
			String.valueOf(getStat(Stat.Hunger)),
			String.valueOf(getStat(Stat.Stamina)),
			spawnLoc == null ? "null" : spawnLoc.x+";"+spawnLoc.y,
			String.valueOf(spawnLevel),
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
	
	public InventoryUpdate getInventoryUpdate() { return new InventoryUpdate(inventory.serialize(), hands.toInventoryIndex()); }
	public HotbarUpdate getHotbarUpdate() { return new HotbarUpdate(hands.serialize(), inventory.getPercentFilled()); }
	
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
			getServer().sendToPlayer(this, new StatUpdate(stat, amt));
			
			if(stat == Stat.Hunger && change > 0) {
				ServerLevel level = getLevel();
				if(level != null)
					getServer().broadcastParticle(new TextParticleData(String.valueOf(amt), Color.CORAL), this);
			}
		}
		
		return change;
	}
	
	public void loadStat(StatUpdate update) {
		stats.put(update.stat, update.amount);
		if(update.stat == Stat.Health) setHealth(update.amount);
	}
	
	/*@Override
	public void update(float delta) {
		super.update(delta);
	}*/
	
	// public boolean moveTo(float x, float y, float z) { return moveTo(new Vector3(x, y, z)); }
	// public boolean moveTo(Vector3 pos) { return move(pos.cpy().sub(getLocation())); }
	
	@Override
	public Integer[] saveStats() { return Stat.save(stats); }
	
	/// These two methods are ONLY to be accessed by GameScreen, so far.
	@NotNull
	public ServerHands getHands() { return hands; }
	@NotNull
	public Inventory getInventory() { return inventory; }
	
	public boolean takeItem(@NotNull ServerItem item) {
		if(inventory.addItem(item)) {
			if(inventory.getCount(item) == 1) // don't add to hotbar if it already existed in inventory
				hands.addItem(item); // add to open hotbar slot if it exists
			GameServer server = getServer();
			server.playEntitySound("pickup", this, false);
			
			if(server.isInventoryMode(this))
				server.sendToPlayer(this, new InventoryAddition(item));
			else
				server.sendToPlayer(this, getHotbarUpdate());
			return true;
		}
		
		return false;
	}
	
	@NotNull
	private Array<WorldObject> getInteractionQueue() {
		Array<WorldObject> objects = new Array<>();
		
		// get level, and don't interact if level is not found
		ServerLevel level = getLevel();
		if(level == null) return objects;
		
		Rectangle interactionBounds = getInteractionRect();
		
		objects.addAll(level.getOverlappingEntities(interactionBounds, this));
		Boundable.sortByDistance(objects, getCenter());
		
		Tile tile = level.getTile(interactionBounds);
		if(tile != null)
			objects.add(tile);
		
		return objects;
	}
	
	// this method gets called by GameServer, so in order to ensure it doesn't mix badly with server world updates, we'll post it as a runnable to the server world update thread.
	public void doInteract(Direction dir, int index, boolean attack) {
		getWorld().postRunnable(() -> {
			setDirection(dir);
			ServerItem heldItem = hands.getHeldItem(index);
			if(getStat(Stat.Stamina) < heldItem.getStaminaUsage())
				return;
			
			ServerLevel level = getLevel();
			
			Result result = Result.NONE;
			for(WorldObject obj: getInteractionQueue()) {
				if(attack)
					result = heldItem.attack(obj, this);
				else
					result = heldItem.interact(obj, this);
				
				if(result.success)
					break;
			}
			
			if(!attack && !result.success)
				// none of the above interactions were successful, do the reflexive use.
				result = heldItem.interact(this);
			
			if (attack && level != null) {
				if(result.success)
					getServer().broadcastParticle(
						new ActionParticleData(ActionType.SLASH, getDirection()),
						level,
						getCenter().add(getDirection().getVector().scl(getSize().scl(0.5f, Mob.unshortenSprite(0.5f))))
					);
				else
					getServer().broadcastParticle(
						new ActionParticleData(ActionType.PUNCH, getDirection()),
						level,
						getInteractionRect().getCenter(new Vector2())
					);
			}
			
			if(result == Result.USED)
				changeStat(Stat.Stamina, -heldItem.getStaminaUsage());
			else
				changeStat(Stat.Stamina, -1); // for trying...
			
			if(!result.success)
				// this sound is of an empty swing; successful interaction sounds are taken care of elsewhere.
				getServer().playEntitySound("swing", this);
			
			if(result == Result.USED && !GameCore.debug)
				hands.resetItemUsage(heldItem, index);
		});
	}
	
	@Override
	public Result attackedBy(WorldObject source, @Nullable Item item, int dmg) {
		Result res = super.attackedBy(source, item, dmg);
		if(res.success) {
			int health = stats.get(Stat.Health);
			if (health == 0) return Result.NONE; // TO-DO here is where I'd make a death chest, and show the death screen.
			changeStat(Stat.Health, getHealth() - health);
		}
		return res;
	}
	
	@Override
	public void die() { getWorld().despawnPlayer(this); }
	
	@Override
	public boolean maySpawn(TileTypeEnum type) {
		return super.maySpawn(type)/* && type != TileTypeEnum.SAND*/;
	}
	
	public int getSpawnLevel() { return spawnLevel; }
	
	public ValueFunction<ServerLevel> respawnPositioning() {
		return level -> {
			if(spawnLoc != null) {
				ServerTile spawnTile = level.getTile(spawnLoc.x, spawnLoc.y);
				if(maySpawn(spawnTile.getType().getTypeEnum())) {
					moveTo(spawnTile);
					return;
				}
				for(Tile tile: level.getAreaTiles(spawnLoc, 1, false)) {
					if(maySpawn(tile.getType().getTypeEnum())) {
						moveTo(tile);
						return;
					}
				}
				
				// spawn location obstructed.
				spawnLoc = null; // player loses their saved spawn location.
				getServer().sendMessage(null, this, "Spawn location is obstructed");
			}
			
			Tile tile = level.getSpawnTile(this);
			if(tile != null) {
				spawnLoc = tile.getLocation();
				moveTo(tile);
			}
			else
				System.err.println("Server: error spawning player "+this+" on level "+level+", no spawnable tiles found. Spawning player anyway at current position: "+getCenter());
		};
	}
	
	@Override
	public String toString() {
		InetSocketAddress address = getServer().getPlayerAddress(this);
		return "ServerPlayer["+name+(address==null?']':" at "+address+']');
	}
}
