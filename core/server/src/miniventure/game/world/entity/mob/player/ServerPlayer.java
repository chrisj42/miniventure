package miniventure.game.world.entity.mob.player;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

import miniventure.game.GameCore;
import miniventure.game.item.*;
import miniventure.game.item.MaterialQuality;
import miniventure.game.item.ToolItem.ToolType;
import miniventure.game.network.GameProtocol;
import miniventure.game.network.GameProtocol.*;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.server.GameServer;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.util.function.Action;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.Boundable;
import miniventure.game.world.Point;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.EntityDataSet;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerPlayer extends ServerMob implements Player {
	
	private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
	
	// saved spawn location; you slept in a bed, etc. This is checked when adding the player to a level. If there is no location, then the player is spawned randomly on the default level. If the location (and adjacent tiles) are not spawnable, then the player is spawned randomly on the saved level (assuming it's valid).
	private Point spawnLoc;
	private int spawnLevel = 0;
	
	
	@NotNull private final ServerPlayerInventory invManager;
	@NotNull private final ServerInventory inventory;
	@Nullable private HammerType equippedHammer;
	
	private final String name;
	
	public ServerPlayer(@NotNull ServerWorld world, String name) {
		super(world, "player", Stat.Health.initial);
		
		this.name = name;
		invManager = new ServerPlayerInventory();
		inventory = invManager.getInv();
		reset();
	}
	
	protected ServerPlayer(@NotNull ServerWorld world, EntityDataSet allData, final Version version, ValueAction<EntityDataSet> modifier) {
		super(world, allData, version, data -> {
			data.get("mob").add("sprite", "player");
		});
		SerialHashMap data = allData.get("player");
		
		name = data.get("name");
		invManager = new ServerPlayerInventory();
		inventory = invManager.getInv();
		equippedHammer = null;
		
		stats.put(Stat.Health, getHealth());
		stats.put(Stat.Hunger, data.get("hunger", Integer::parseInt));
		stats.put(Stat.Stamina, data.get("stamina", Integer::parseInt));
		// stats.put(Stat.Armor, Integer.parseInt(data.get(3)));
		// todo remove "equippedHammer", see todo notes
		if(!data.get("tool").equals("null"))
			equippedHammer = data.get("tool", HammerType::valueOf);
		
		if(data.get("sloc").equals("null"))
			spawnLoc = null;
		else
			spawnLoc = data.get("sloc", Point::new);
		
		spawnLevel = data.get("slvl", Integer::parseInt);
		
		invManager.loadItems(MyUtils.parseLayeredString(data.get("inv")), version);
	}
	
	@Override
	public EntityDataSet save() {
		EntityDataSet allData = super.save();
		allData.get("mob").remove("sprite");
		
		SerialHashMap data = new SerialHashMap();
		data.add("name", name);
		data.add("hunger", getStat(Stat.Hunger));
		data.add("stamina", getStat(Stat.Stamina));
		data.add("tool", equippedHammer);
		data.add("sloc", spawnLoc == null ? null : spawnLoc.serialize());
		data.add("slvl", spawnLevel);
		data.add("inv", MyUtils.encodeStringArray(invManager.save()));
		
		allData.put("player", data);
		return allData;
	}
	
	public String getName() { return name; }
	
	// use this instead of creating a new player.
	@Override
	public void reset() {
		for(Stat stat: Stat.values)
			stats.put(stat, stat.initial);
		
		super.reset();
		
		invManager.reset();
		equippedHammer = null;
		
		if(GameCore.debug) {
			System.out.println("adding debug items to player inventory");
			inventory.addItem(new ToolItem(ToolType.Shovel, MaterialQuality.Superior));
			inventory.addItem(new ToolItem(ToolType.Pickaxe, MaterialQuality.Superior));
			// inventory.addItem(TileItemType.Door);
			for(int i = 0; i < 7; i++)
				inventory.addItem(PlaceableItemType.Torch.get());
			for(int i = 0; i < 7; i++)
				inventory.addItem(ResourceType.Log.get());
			for(int i = 0; i < 7; i++)
				inventory.addItem(FoodType.Cooked_Meat.get());
		}
	}
	
	public SpawnData getSpawnData() {
		return new SpawnData(new EntityAddition(this), invManager.getUpdate(), saveStats());
	}
	
	// private InventoryUpdate inventory.getUpdate() { return new InventoryUpdate(inventory.serialize()); }
	// public HotbarUpdate getHotbarUpdate() { return new HotbarUpdate(hands.serialize(), inventory.getPercentFilled()); }
	
	private <T> void forPacket(Object packet, DatalessRequest type, boolean sync, Action response) {
		GameProtocol.forPacket(packet, type, response, sync ? getWorld()::postRunnable : null);
	}
	private <T> void forPacket(Object packet, Class<T> type, boolean sync, ValueAction<T> response) {
		GameProtocol.forPacket(packet, type, response, sync ? getWorld()::postRunnable : null);
	}
	
	// this allows a lot of the packet handling that deals with inner workings of the ServerPlayer to occur right within the ServerPlayer class, and not require it to have a bunch of public methods that only GameServer ever uses.
	@Override
	public void handlePlayerPackets(@NotNull Object packet, @NotNull PacketPipeWriter connection) {
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
				connection.send(new PositionUpdate(this));
			else
				moveTo(move.endPos.getPos());
			// note that the server will always have the say when it comes to which level the player should be on.
		});
		
		forPacket(packet, InteractRequest.class, true, r -> {
			// if(r.playerPosition.variesFrom(client))
			// 	connection.send(new PositionUpdate(client)); // fix the player's position
			doInteract(r.dir, r.actionPos, getHeldItem(r.hotbarIndex), r.hotbarIndex, r.attack);
		});
		
		forPacket(packet, ItemDropRequest.class, true, drop -> {
			if(!dropItem(drop)) {
				// drop failed, i.e. client allowed it when it shouldn't have; update client inv
				connection.send(invManager.getUpdate());
			}
			// if successful, do nothing, because client will have pre-maturely removed the item itself.
		});
		
		forPacket(packet, InventoryMovement.class, true, req -> {
			inventory.moveItem(req.oldIdx, req.newIdx);
		});
		
		forPacket(packet, EquipRequest.class, true, req -> {
			if(req.equip)
				invManager.equipItem(req.equipmentType, req.invIdx);
			else
				invManager.unequipItem(req.equipmentType, req.invIdx);
		});
		
		forPacket(packet, DatalessRequest.Recipes, true, () -> connection.send(new RecipeUpdate(
			equippedHammer == null ? HammerType.getHandRecipes() : equippedHammer.getRecipes(),
			new RecipeStockUpdate(inventory.getItemStacks())
		)));
		
		forPacket(packet, CraftRequest.class, true, req -> {
			ItemRecipeSet set = ItemRecipeSet.values[req.setOrdinal];
			ItemRecipe recipe = set.getRecipe(req.recipeIndex);
			GameCore.debug("server got craft request for "+recipe.getResult().item);
			Integer left = recipe.tryCraft(inventory);
			if(left != null) {
				ServerLevel level = getLevel();
				if(level != null)
					for(int i = 0; i < left; i++)
						level.dropItem(recipe.getResult().item, getPosition(), null);
			}
			//getHands().validate();
			connection.send(invManager.getUpdate(false));
			connection.send(new RecipeStockUpdate(inventory.getItemStacks()));
		});
		
		forPacket(packet, BuildRequest.class, true, req -> {
			ObjectRecipeSet set = ObjectRecipeSet.values[req.setOrdinal];
			ObjectRecipe recipe = set.getRecipe(req.recipeIndex);
			
			ServerLevel level = getLevel();
			if(level == null)
				return;
			ServerTile tile = (ServerTile) level.getTile(req.tilePos);
			if(tile == null)
				return;
			
			if(recipe.tryCraft(tile, this, inventory)) {
				// success, update inv
				connection.send(invManager.getUpdate(false));
			}
		});
		
		forPacket(packet, DatalessRequest.Respawn, true, () -> {
			world.respawnPlayer(this);
			connection.send(getSpawnData());
		});
		
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
	private Array<WorldObject> getInteractionQueue(Vector2 center) {
		Array<WorldObject> objects = new Array<>();
		
		// get level, and don't interact if level is not found
		ServerLevel level = getLevel();
		if(level == null) return objects;
		
		Rectangle interactionBounds = getInteractionRect(center);
		
		objects.addAll(level.getOverlappingEntities(interactionBounds, this));
		Boundable.sortByDistance(objects, getCenter());
		
		Tile tile = level.getTile(center);
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
	private void doInteract(Direction dir, Vector2 actionPos, ServerItem heldItem, int index, boolean attack) {
		setDirection(dir);
		
		if(getStat(Stat.Stamina) < heldItem.getStaminaUsage())
			return;
		
		ServerLevel level = getLevel();
		
		Result result = Result.NONE;
		for(WorldObject obj: getInteractionQueue(actionPos)) {
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
			ActionType actionType = result.success ? ActionType.SLASH : ActionType.PUNCH;
			
			TextureHolder tex = GameCore.entityAtlas.getRegion(actionType.getSpriteName(dir));
			Vector2 offset = getSize();
			offset.y = Mob.unshortenSprite(offset.y);
			offset.scl(0.5f);
			// if(dir == Direction.LEFT)
			// 	offset.x += tex.width / (float) Tile.SIZE;
			// if(dir == Direction.DOWN)
			// 	offset.y += tex.height / (float) Tile.SIZE;
			
			Vector2 pos = getCenter().add(dir.getVector().scl(offset));
			
			getServer().broadcastParticle(new ActionParticleData(actionType, dir), level, pos);
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
		
		// this is already done above
		// if(!GameCore.debug)
		// 	changeStat(Stat.Stamina, -item.getStaminaUsage());
		
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
		getServer().sendToPlayer(this, invManager.getUpdate(false));
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
	
	public int getSpawnLevel() { return spawnLevel == 0 ? getWorld().getDefaultLevel() : spawnLevel; }
	
	public ValueAction<ServerLevel> respawnPositioning() {
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
