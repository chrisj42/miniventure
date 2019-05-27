package miniventure.game.world.entity.mob.player;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.GameProtocol.*;
import miniventure.game.item.*;
import miniventure.game.item.ToolItem.Material;
import miniventure.game.item.ToolItem.ToolType;
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
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import com.esotericsoftware.kryonet.Connection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerPlayer extends ServerMob implements Player {
	
	private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
	
	// saved spawn location; you slept in a bed, etc. This is checked when adding the player to a level. If there is no location, then the player is spawned randomly on the default level. If the location (and adjacent tiles) are not spawnable, then the player is spawned randomly on the saved level (assuming it's valid).
	private Point spawnLoc;
	private int spawnLevel = 0;
	
	
	// @NotNull private final ServerHands hands;
	@NotNull private final ServerInventory inventory;
	
	private final String name;
	
	public ServerPlayer(@NotNull ServerWorld world, String name) {
		super(world, "player", Stat.Health.initial);
		
		this.name = name;
		inventory = new ServerInventory(INV_SIZE);
		// hands = new ServerHands(this);
		reset();
	}
	
	protected ServerPlayer(@NotNull ServerWorld world, ClassDataList allData, final Version version, ValueFunction<ClassDataList> modifier) {
		super(world, allData, version, modifier);
		ArrayList<String> data = allData.get(2);
		
		name = data.get(0);
		inventory = new ServerInventory(INV_SIZE);
		// hands = new ServerHands(this, MyUtils.parseLayeredString(data.get(6)));
		
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
			MyUtils.encodeStringArray(inventory.save())
			// MyUtils.encodeStringArray(hands.save())
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
		// hands.reset();
		
		if(GameCore.debug) {
			System.out.println("adding debug items to player inventory");
			inventory.addItem(new ToolItem(ToolType.Shovel, Material.Ruby));
			inventory.addItem(new ToolItem(ToolType.Pickaxe, Material.Ruby));
			inventory.addItem(ServerTileType.getItem(TileTypeEnum.CLOSED_DOOR));
			for(int i = 0; i < 7; i++)
				inventory.addItem(ServerTileType.getItem(TileTypeEnum.TORCH));
			for(int i = 0; i < 7; i++)
				inventory.addItem(ResourceType.Log.get());
			for(int i = 0; i < 7; i++)
				inventory.addItem(FoodType.Cooked_Meat.get());
		}
	}
	
	public SpawnData getSpawnData() {
		return new SpawnData(new EntityAddition(this), getInventoryUpdate(), saveStats());
	}
	
	private InventoryUpdate getInventoryUpdate() { return new InventoryUpdate(inventory.serialize()); }
	// public HotbarUpdate getHotbarUpdate() { return new HotbarUpdate(hands.serialize(), inventory.getPercentFilled()); }
	
	private <T> void forPacket(Object packet, Class<T> type, boolean sync, ValueFunction<T> response) {
		if(sync)
			getWorld().postRunnable(() -> GameProtocol.forPacket(packet, type, response));
		else
			GameProtocol.forPacket(packet, type, response);
	}
	
	// this allows a lot of the packet handling that deals with inner workings of the ServerPlayer to occur right within the ServerPlayer class, and not require it to have a bunch of public methods that only GameServer ever uses.
	@Override
	public void handlePlayerPackets(@NotNull Object packet, @NotNull Connection connection) {
		ServerWorld world = getWorld();
		
		// TODO don't allow client to update server stats
		forPacket(packet, StatUpdate.class, true, this::loadStat);
		
		forPacket(packet, MovementRequest.class, true, move -> {
			/*Vector3 loc = client.getLocation();
			if(move.getMoveDist().len() < 1 && !move.startPos.variesFrom(client)) {
				// move to start pos
				Vector3 start = move.startPos.getPos();
				Vector3 diff = loc.cpy().sub(start);
				client.move(diff);
			}*/
			// move given dist
			Vector3 moveDist = move.getMoveDist();
			if(!GameCore.debug) // TODO replace this static speed check with something that determines the player's speed with respect to their situation.
				moveDist.clamp(0, Math.min(.5f, 2.5f*Player.MOVE_SPEED/Math.min(world.getFPS(), 60))); // the server will not allow the client to move fast (unless in debug mode)
			move(moveDist);
			// compare against given end pos
			if(move.endPos.variesFrom(this))
				connection.sendTCP(new PositionUpdate(this));
			else
				moveTo(move.endPos.getPos());
			// note that the server will always have the say when it comes to which level the player should be on.
		});
		
		forPacket(packet, InteractRequest.class, true, r -> {
			// if(r.playerPosition.variesFrom(client))
			// 	connection.sendTCP(new PositionUpdate(client)); // fix the player's position
			
			doInteract(r.dir, r.hotbarIndex, r.attack);
		});
		
		forPacket(packet, ItemDropRequest.class, true, drop -> {
			if(!dropItem(drop)) {
				// drop failed, i.e. client allowed it when it shouldn't have; update client inv
				connection.sendTCP(getInventoryUpdate());
			}
			// if successful, do nothing, because client will have pre-maturely removed the item itself.
		});
		
		forPacket(packet, InventoryRequest.class, true, req -> {
			connection.sendTCP(getInventoryUpdate());
		});
		
		forPacket(packet, RecipeRequest.class, true, req ->
			connection.sendTCP(new RecipeRequest(
				Recipes.serializeRecipes(),
				new RecipeStockUpdate(inventory.getItemStacks())
			))
		);
		
		forPacket(packet, CraftRequest.class, true, req -> {
			Recipe recipe = Recipes.recipes[req.recipeIndex];
			ServerItem[] left = recipe.tryCraft(inventory);
			if(left != null) {
				ServerLevel level = getLevel();
				if(level != null)
					for(ServerItem item : left)
						level.dropItem(item, getPosition(), null);
			}
			//getHands().validate();
			connection.sendTCP(getInventoryUpdate());
			connection.sendTCP(new RecipeStockUpdate(inventory.getItemStacks()));
		});
		
		if(packet.equals(DatalessRequest.Respawn)) {
			world.postRunnable(() -> {
				world.respawnPlayer(this);
				connection.sendTCP(getSpawnData());
			});
		}
		
		forPacket(packet, SelfHurt.class, true, hurt -> {
			// assumed that the client has hurt itself in some way
			attackedBy(this, null, hurt.dmg);
		});
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
			getServer().sendToPlayer(this, new StatUpdate(stat, amt));
			
			if(stat == Stat.Hunger && change > 0) {
				ServerLevel level = getLevel();
				if(level != null)
					getServer().broadcastParticle(new TextParticleData(String.valueOf(amt), Color.CORAL), this);
			}
		}
		
		return change;
	}
	
	private void loadStat(StatUpdate update) {
		stats.put(update.stat, update.amount);
		if(update.stat == Stat.Health) setHealth(update.amount);
	}
	
	@Override
	public Integer[] saveStats() { return Stat.save(stats); }
	
	
	public boolean takeItem(@NotNull ServerItem item) {
		if(inventory.addItem(item)) {
			// if(inventory.getCount(item) == 1) // don't add to hotbar if it already existed in inventory
			// 	inventory.addItem(item); // add to open hotbar slot if it exists
			GameServer server = getServer();
			server.playEntitySound("pickup", this, false);
			server.sendToPlayer(this, new InventoryAddition(item));
			return true;
		}
		
		return false;
	}
	
	private boolean dropItem(ItemDropRequest drop) {
		ServerLevel level = getLevel();
		if(level == null)
			return false; // cannot drop on non-existent level
		
		ServerItem item = inventory.getItem(drop.index);
		if(item == null)
			return false;
		
		int removed;
		if(drop.all)
			removed = inventory.removeItemStack(item);
		else
			removed = inventory.removeItem(item) ? 1 : 0;
		
		if(removed == 0)
			return false;
		
		// get target pos, which is one tile in front of player.
		Vector2 center = getCenter();
		Vector2 targetPos = center.cpy().add(getDirection().getVector().scl(2)); // adds 2 in the direction of the player.
		for(int i = 0; i < removed; i++)
			level.dropItem(item, true, center, targetPos);
		
		return true;
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
	
	@NotNull
	private ServerItem getHeldItem(int index) {
		ServerItem item = inventory.getItem(index);
		if(item == null) item = HandItem.hand;
		return item;
	}
	
	// this method gets called by GameServer, so in order to ensure it doesn't mix badly with server world updates, we'll post it as a runnable to the server world update thread.
	private void doInteract(Direction dir, int index, boolean attack) {
		setDirection(dir);
		ServerItem heldItem = getHeldItem(index);
		
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
			resetItemUsage(heldItem, index);
	}
	
	private void resetItemUsage(@NotNull ServerItem item, int index) {
		ServerItem newItem = item.getUsedItem();
		
		if(!GameCore.debug)
			changeStat(Stat.Stamina, -item.getStaminaUsage());
		
		// While I could consider this asking for trouble, I'm already stacking items, so any unspecified "individual" data is lost already.
		if(item.equals(newItem)) // this is true for the hand item.
			return; // there is literally zero difference in what the item is now, and what it was before.
		
		// the item has changed (possibly into nothing)
		
		// remove the current item.
		inventory.removeItem(item);
		if(newItem != null) {
			// the item has changed either in metadata or into an entirely separate item.
			// add the new item to the inventory, and then determine what should become the held item: previous or new item.
			// if the new item doesn't fit, then drop it on the ground instead.
			
			if(!inventory.addItem(index, newItem)) {
				// inventory is full, try to drop it on the ground
				ServerLevel level = getLevel();
				if(level != null)
					level.dropItem(newItem, getCenter(), getCenter().add(getDirection().getVector()));
				else // this is a very bad situation, ideally it should never happen. I was considering adding a check for it, but think about it: you're using an item, but not on a level? The only way you're not on a level is if you're travelling between levels, and you're definitely not using items then, you're browsing the level select screen.
					System.err.println("ERROR: could not drop usage-spawn item "+newItem+", ServerLevel for player "+this+" is null. (inventory is also full)");
			}
			/*else if(findItem(newItem) < 0) { // new item not found in hotbar
				// item was successfully added to the inventory; now figure out what to do with the hotbar.
				// this block is only needed to figure out where to put the new item; if it's already in the hotbar then we don't need to do anything. ;)
				
				// check if the original item has run out, in which case the new item should replace it in the hotbar.
				if(!getInv().hasItem(item))
					hotbarItems[index] = newItem;
				else {
					// original item still exists; decide if new item should replace it or not
					if(newItem.getName().equals(item.getName())) {
						// metadata changed, same item, replace stack
						setSlot(index, newItem);
						addItem(item, index); // try and keep the stack in the hotbar
					}
					else // name changed, different item, keep stack
						addItem(newItem); // try-add new item to hotbar like when you normally pick items up
				}
			}*/
		}
		
		// remove old item from hotbar if it's no longer in the inventory
		// if(!inventory.hasItem(item))
		// 	removeItem(item);
		
		// we are never going to be in inventory mode here, because the client has just used an item; items can't be used with a menu open.
		getServer().sendToPlayer(this, getInventoryUpdate());
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
	public void die() {
		getWorld().getServer().sendToPlayer(this, DatalessRequest.Death);
		getWorld().despawnPlayer(this);
	}
	
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
