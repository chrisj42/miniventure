package miniventure.game.network;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

import miniventure.game.chat.InfoMessage;
import miniventure.game.chat.InfoMessageLine;
import miniventure.game.item.EquipmentType;
import miniventure.game.item.ItemDataTag;
import miniventure.game.item.ItemStack;
import miniventure.game.texture.FetchableTextureHolder;
import miniventure.game.texture.ItemTextureSource;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.Version;
import miniventure.game.util.customenum.SerialEnumMap;
import miniventure.game.util.function.Action;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.Point;
import miniventure.game.world.Taggable.Tag;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.Entity.EntityTag;
import miniventure.game.world.entity.EntityRenderer;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;
import miniventure.game.world.entity.mob.player.Player.Stat;
import miniventure.game.world.entity.particle.ActionType;
import miniventure.game.world.entity.particle.ParticleData;
import miniventure.game.world.level.Level;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.Tile.TileTag;
import miniventure.game.world.tile.TileStack.TileData;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.island.IslandType;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import com.esotericsoftware.kryo.Kryo;

import org.jetbrains.annotations.Nullable;

public interface GameProtocol {
	
	int PORT = 8405;
	String HOST = "Host"; // name of host player; used only when joining a game in the same JVM. Other players are not allowed to specify this name.
	int objectBufferSize = 2_000_000;
	int clientWriteBufferSize = 2_000_000;
	int serverWriteBufferSize = clientWriteBufferSize * 3;
	
	boolean lag = false;
	int lagMin = lag?10:0, lagMax = lag?100:0;
	
	static void forPacket(Object packet, DatalessRequest type, Action response) {
		if(type.equals(packet)) response.act();
	}
	static void forPacket(Object packet, DatalessRequest type, Action response, ValueAction<Runnable> syncFunc) {
		if(type.equals(packet)) {
			if(syncFunc != null)
				syncFunc.act(response::act);
			else
				response.act();
		}
	}
	static <T> void forPacket(Object packet, Class<T> type, ValueAction<T> response) {
		if(type.isAssignableFrom(packet.getClass())) response.act(type.cast(packet));
	}
	static <T> void forPacket(Object packet, Class<T> type, ValueAction<T> response, ValueAction<Runnable> syncFunc) {
		if(type.isAssignableFrom(packet.getClass())) {
			if(syncFunc != null)
				syncFunc.act(() -> response.act(type.cast(packet)));
			else
				response.act(type.cast(packet));
		}
	}
	
	static void registerClasses(Kryo kryo) {
		kryo.register(Version.class);
		
		registerNestedClasses(kryo, GameProtocol.class);
		registerNestedClasses(kryo, ParticleData.class, true);
		
		kryo.register(TileData.class);
		kryo.register(TileData[].class);
		kryo.register(TileData[][].class);
		// kryo.register(ChunkData.class);
		// kryo.register(ChunkData[].class);
		kryo.register(IslandType.class);
		kryo.register(IslandReference[].class);
		kryo.register(Tag.class);
		kryo.register(EntityTag.class);
		kryo.register(TileTag.class);
		kryo.register(Direction.class);
		kryo.register(ActionType.class);
		kryo.register(Stat.class);
		kryo.register(InfoMessageLine.class);
		kryo.register(InfoMessageLine[].class);
		kryo.register(InfoMessage.class);
		kryo.register(TileTypeEnum.class);
		kryo.register(ItemTextureSource.class);
		kryo.register(SerialItem[].class);
		kryo.register(SerialItemStack[].class);
		kryo.register(SerialRecipe[].class);
		kryo.register(CursorHighlight.class);
		kryo.register(EquipmentType.class);
		
		kryo.register(String[].class);
		kryo.register(String[][].class);
		kryo.register(int[].class);
		kryo.register(Integer.class);
		kryo.register(Integer[].class);
		
		kryo.register(Vector2.class);
		kryo.register(Point.class);
		kryo.register(Point[].class);
	}
	
	class Login {
		public final String username;
		public final Version version;
		
		private Login() { this(null, null); }
		public Login(String username, Version version) {
			this.username = username;
			this.version = version;
		}
	}
	
	class LoginFailure {
		public final String message;
		
		private LoginFailure() { this(null); }
		public LoginFailure(String msg) { this.message = msg; }
	}
	
	enum DatalessRequest {
		Respawn,
		// Tile,
		Clear_Console, // server sends to player to clear console; is a server command only to make it fit in with the others; could be implemented entirely client-side otherwise.
		Level_Loading, // notifies the client that a level must be loaded before they can be added to it, allowed a loading message to be shown client-side.
		Level_Ready, // when the server has sent all the entities to the client.
		Death, // player died, show respawn screen.
		Recipes // client asking for recipes they can make without a crafter
	}
	
	// pings always end up being started from the server.
	// start is the time that the request was sent.
	class Ping {
		public final long start;
		public final String source; // the name of the player who requested the ping. Null if initiated in server console.
		
		private Ping() { this(0, null); }
		public Ping(String source) { this(System.nanoTime(), source); }
		public Ping(long start, String source) {
			this.start = start;
			this.source = source;
		}
	}
	
	// this is sent by the client as a command, a single line. Chat messages are already prepended with "msg ". The server sends this whenever a player says something.
	class Message {
		public final String msg;
		public final String color;
		
		private Message() { this(null, (String)null); }
		// public Message(String msg) { this(msg, 0); }
		public Message(String msg, Color color) { this(msg, color.toString()); }
		private Message(String msg, String color) {
			this.msg = msg;
			this.color = color;
		}
	}
	
	// client asking for autocomplete
	class TabRequest {
		public final String manualText;
		public final int tabIndex;
		
		private TabRequest() { this(null, 0); }
		public TabRequest(String manualText, int tabIndex) {
			this.manualText = manualText;
			this.tabIndex = tabIndex;
		}
	}
	
	// server response to autocomplete request
	class TabResponse {
		public final String manualText; // used to check against the client text again; if it's different, then it means that the client has abandoned the tab request, and so this response will be ignored.
		public final String completion;
		// there will also be output, but that ought to be taken care of separately.
		
		private TabResponse() { this(null, null); }
		public TabResponse(String manualText, String completion) {
			this.manualText = manualText;
			this.completion = completion;
		}
	}
	
	class WorldData {
		public final float gameTime, daylightOffset;
		public final boolean doDaylightCycle;
		
		private WorldData() { this(0, 0, true); }
		public WorldData(float gameTime, float daylightOffset, boolean doDaylightCycle) {
			this.gameTime = gameTime;
			this.daylightOffset = daylightOffset;
			this.doDaylightCycle = doDaylightCycle;
		}
	}
	
	// for server to send level data to the client. Sets the client to a loading screen, which stays open until a SpawnData is sent. A SpawnData packet will always clear a loading screen.
	class LevelData {
		public final int levelId;
		public final TileData[][] tiles;
		
		// for client
		public LevelData() { this(0, null); }
		// for server
		public LevelData(Level level) { this(level.getLevelId(), level.getTileData(false)); }
		public LevelData(int levelId, TileData[][] tiles) {
			this.levelId = levelId;
			this.tiles = tiles;
		}
	}
	
	// used in MapRequest below; holds general data about a single island.
	class IslandReference {
		public final int levelId;
		public final IslandType type; // gen parameter
		
		private IslandReference() { this(0, null); }
		public IslandReference(int levelId, IslandType type) {
			this.levelId = levelId;
			this.type = type;
		}
	}
	
	// for server to transmit world layout to client, so it can display a world map.
	class MapRequest {
		public final IslandReference[] islands;
		
		// for client use only
		public MapRequest() { this(null); }
		// for server use only
		public MapRequest(IslandReference[] islands) {
			this.islands = islands;
		}
	}
	
	class LevelChange {
		public final int levelid;
		
		private LevelChange() { this(0); }
		public LevelChange(int levelid) {
			this.levelid = levelid;
		}
	}
	
	class TileUpdate {
		public final TileData tileData;
		public final int levelId;
		public final int x;
		public final int y;
		public final TileTypeEnum updatedType;
		
		private TileUpdate() { this(null, 0, 0, 0, null); }
		public TileUpdate(Tile tile, TileTypeEnum updatedType) { this(tile, tile.getLocation(), updatedType); }
		private TileUpdate(Tile tile, Point pos, TileTypeEnum updatedType) { this(new TileData(tile, false), tile.getLevel().getLevelId(), pos.x, pos.y, updatedType); }
		public TileUpdate(TileData data, int levelId, int x, int y, TileTypeEnum updatedType) {
			tileData = data;
			this.levelId = levelId;
			this.x = x;
			this.y = y;
			this.updatedType = updatedType;
		}
	}
	
	// sent by client to ask for a chunk. Server assumes level to be the client's current level.
	/*class ChunkRequest {
		public final int x;
		public final int y;
		
		private ChunkRequest() { this(0, 0); }
		public ChunkRequest(Point p) { this(p.x, p.y); }
		public ChunkRequest(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}*/
	
	class SpawnData {
		public final EntityAddition playerData;
		public final InventoryUpdate inv;
		public final Integer[] stats;
		
		private SpawnData() { this(null, null, null); }
		public SpawnData(EntityAddition playerData, InventoryUpdate inv, Integer[] stats) {
			this.playerData = playerData;
			this.inv = inv;
			this.stats = stats;
		}
	}
	
	
	class ParticleAddition {
		public final ParticleData particleData;
		public final PositionUpdate positionUpdate;
		
		private ParticleAddition() { this(null, null); }
		public ParticleAddition(ParticleData particleData, PositionUpdate positionUpdate) {
			this.positionUpdate = positionUpdate;
			this.particleData = particleData;
		}
	}
	
	class EntityAddition {
		public final PositionUpdate positionUpdate;
		public final SpriteUpdate spriteUpdate;
		public final int eid;
		public final boolean permeable;
		public final String descriptor;
		public final boolean canFloat;
		public final boolean cutHeight;
		
		private EntityAddition() { this(0, null, null, false, false, null, false); }
		public EntityAddition(Entity e) { this(e, e.getClass().getSimpleName().replace("Server", "")); }
		public EntityAddition(Entity e, String descriptor) { this(e.getId(), new PositionUpdate(e), new SpriteUpdate(e), e.isFloating(), e.isPermeable(), descriptor, e instanceof Mob); }
		public EntityAddition(int eid, PositionUpdate positionUpdate, SpriteUpdate spriteUpdate, boolean canFloat, boolean permeable, String descriptor, boolean cutHeight) {
			this.eid = eid;
			this.positionUpdate = positionUpdate;
			this.spriteUpdate = spriteUpdate;
			this.canFloat = canFloat;
			this.permeable = permeable;
			this.descriptor = descriptor;
			this.cutHeight = cutHeight;
		}
	}
	
	class EntityRemoval {
		public final int eid;
		
		private EntityRemoval() { this(0); }
		public EntityRemoval(Entity e) { this(e.getId()); }
		public EntityRemoval(int eid) {
			this.eid = eid;
		}
	}
	
	// sent by server (and client in response to server ping) to make sure the client is still connected.
	// however, might not be needed since I'm using kryonet.
	//class Ping {}
	
	/*
		The server sends to the clients entity position, and new entity animations.
		
		The client moves itself at a speed previously set by the server, and sends to the server which direction they are going, and where they ended up. If the server finds that the client's position according to it differs from the received position by more than a quarter tile, then it sends back a player update to correct the client's position.
	 */
	
	// the server won't receive this, only the client. The entity is updated even if the entity given by the tag is the client player.
	class EntityUpdate {
		public final PositionUpdate positionUpdate; // can be null
		public final SpriteUpdate spriteUpdate; // can be null
		public final EntityTag tag;
		
		private EntityUpdate() { this(null, null, null); }
		public EntityUpdate(EntityTag tag, PositionUpdate positionUpdate, SpriteUpdate spriteUpdate) {
			this.tag = tag;
			this.positionUpdate = positionUpdate;
			this.spriteUpdate = spriteUpdate;
		}
		
		@Override public String toString() { return "EntityUpdate("+positionUpdate+","+spriteUpdate+")"; }
	}
	
	class MobUpdate {
		public final EntityTag tag;
		public final Direction newDir;
		
		private MobUpdate() { this(null, null); }
		public MobUpdate(EntityTag tag, Direction newDir) {
			this.tag = tag;
			this.newDir = newDir;
		}
	}
	
	// sent by the server every 10 seconds or so with all entities in the 5x5 chunks surrounding the client. The client can use this to validate all the entities that it has loaded; if there are any listed that it doesn't have loaded (and the chunk it's on is loaded), it can send back an entity request to get it loaded. Also, if it finds that there are entities it has loaded, which aren't present in the list, it can unload them.
	class EntityValidation {
		public final int levelId;
		public final int[] ids;
		
		private EntityValidation() { this(0, null); }
		public EntityValidation(Level level, Entity... excluded) {
			this.levelId = level.getLevelId();
			// get all entities in level
			LinkedList<Entity> entities = new LinkedList<>(level.getEntities());
			// remove excluded entities
			entities.removeAll(Arrays.asList(excluded));
			// map to ids
			ids = ArrayUtils.mapArray(entities.toArray(), int.class, int[].class, e -> ((Entity)e).getId());
		}
		public EntityValidation(int levelId, int[] ids) {
			this.levelId = levelId;
			this.ids = ids;
		}
	}
	
	// sent by the client when it receives an entity update that is in the loaded chunks, but isn't loaded for some reason.
	class EntityRequest {
		public int eid;
		
		private EntityRequest() { this(0); }
		public EntityRequest(int eid) {
			this.eid = eid;
		}
	}
	
	// sent in EntityUpdate
	class SpriteUpdate {
		public final String[] rendererData;
		
		private SpriteUpdate() { this((String[])null); }
		public SpriteUpdate(Entity e) { this(e.getRenderer()); }
		public SpriteUpdate(EntityRenderer renderer) { this(EntityRenderer.serialize(renderer)); }
		public SpriteUpdate(String[] rendererData) {
			this.rendererData = rendererData;
		}
		
		@Override public String toString() { return "SpriteUpdate("+Arrays.toString(rendererData)+')'; }
	}
	
	// sent in EntityUpdate, EntityAddition, ParticleAddition.
	class PositionUpdate {
		public final float x, y, z;
		public final Integer levelId; // should never be null, actually, because it is always on one level or another, and if not, then it's not in the game, aka removed. An entity removal would be sent rather than a position update. However, it's a bit complicated to change now, so I'll leave it...
		
		private PositionUpdate() { this(null, 0, 0, 0); }
		public PositionUpdate(Entity e) { this(e.getLevel(), e.getLocation()); }
		public PositionUpdate(Level level, Vector2 pos) { this(level, new Vector3(pos, 0)); }
		public PositionUpdate(Level level, Vector3 pos) { this(level==null?null:level.getLevelId(), pos); }
		public PositionUpdate(Integer levelId, Vector3 pos) { this(levelId, pos.x, pos.y, pos.z); }
		public PositionUpdate(Integer levelId, float x, float y, float z) {
			this.levelId = levelId;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public boolean variesFrom(Entity entity) { return variesFrom(entity, entity.getLevel()); }
		private boolean variesFrom(Entity entity, Level level) { return variesFrom(entity.getPosition(), level==null?null:level.getLevelId()); }
		public boolean variesFrom(Vector3 pos, Integer levelId) { return variesFrom(new Vector2(pos.x, pos.y), levelId); }
		public boolean variesFrom(Vector2 pos, Integer levelId) {
			if(pos.dst(x, y) > 0.25) return true;
			return !Objects.equals(levelId, this.levelId);
		}
		
		public Vector3 getPos() { return new Vector3(x, y, z); }
		
		@Override public String toString() {
			return "PositionUpdate("+x+","+y+","+z+",lvl"+levelId+")";
		}
		
		public String toString(WorldManager world) {
			if(levelId == null) return toString();
			return "PositionUpdate("+x+','+y+','+z+",lvl"+levelId+')';
		}
	}
	
	class MovementRequest {
		public final PositionUpdate startPos; // where the player was before movement. The server will update to this as long as it is a valid position.
		public final float xd, yd, zd; // this is useful to the server because it can do things like getting hurt by touching something that the player can't actually run into.
		public final PositionUpdate endPos; // where the player ended up after movement. If this doesn't match where the server thinks the player should have ended up, it will end back a position update to correct it.
		
		private MovementRequest() { this(null, 0, 0, 0, null); }
		public MovementRequest(PositionUpdate startPos, Vector2 moveDist, PositionUpdate endPos) { this(startPos, moveDist.x, moveDist.y, 0, endPos); }
		public MovementRequest(PositionUpdate startPos, Vector3 moveDist, PositionUpdate endPos) { this(startPos, moveDist.x, moveDist.y, moveDist.z, endPos); }
		public MovementRequest(PositionUpdate startPos, float xd, float yd, float zd, PositionUpdate endPos) {
			//System.out.println("made movement request, starting "+);
			this.startPos = startPos;
			this.xd = xd;
			this.yd = yd;
			this.zd = zd;
			this.endPos = endPos;
		}
		
		public Vector3 getMoveDist() { return new Vector3(xd, yd, zd); }
	}
	
	// sent by client to interact or attack.
	class InteractRequest {
		public final boolean attack;
		public final Vector2 actionPos;
		// public final PositionUpdate playerPosition;
		public final Direction dir;
		public final int hotbarIndex;
		
		private InteractRequest() { this(false, null,/* null,*/ null, 0); }
		public InteractRequest(boolean attack, Vector2 actionPos/*, PositionUpdate playerPosition*/, Direction dir, int hotbarIndex) {
			this.attack = attack;
			this.actionPos = actionPos;
			// this.playerPosition = playerPosition;
			this.dir = dir;
			this.hotbarIndex = hotbarIndex;
		}
	}
	
	// sent by server to tell clients to use blinking rendering.
	class Hurt {
		public final float power; // this is 0 to 1, used to determine knockback. (It's really the percent of health taken off.)
		public final Tag<? extends WorldObject> source, target;
		
		private Hurt() { this(null, null, 0); }
		public Hurt(Tag<? extends WorldObject> source, Tag<? extends WorldObject> target, float healthPercent) {
			this.source = source;
			this.target = target;
			this.power = healthPercent;
		}
	}
	
	// sent by the client when a player hurts themself.
	class SelfHurt {
		public int dmg;
		
		private SelfHurt() { this(0); }
		public SelfHurt(int dmg) {
			this.dmg = dmg;
		}
	}
	
	// a stat changed; sent by both the client and the server. When the client sends it, the amount is the total, but when the server sends it to a client, it is a delta.
	// TODO change this; server should be the only one sending these, and they should be absolute values; the server should be handling the stats.
	class StatUpdate {
		public final Stat stat;
		public final int amount;
		
		private StatUpdate() { this(null, 0); }
		public StatUpdate(Stat stat, int amt) {
			this.stat = stat;
			this.amount = amt;
		}
	}
	
	// sent by client to drop an item from the inventory
	class ItemDropRequest {
		public final int index;
		public final boolean all;
		
		private ItemDropRequest() { this(0, false); }
		public ItemDropRequest(int index, boolean all) {
			this.index = index;
			this.all = all;
		}
	}
	
	// sent client -> server when a player changes equipment
	class EquipRequest {
		public final EquipmentType equipmentType;
		public final int invIdx;
		public final boolean equip;
		
		private EquipRequest() { this(null, 0, false); }
		public EquipRequest(EquipmentType equipmentType, int invIdx, boolean equip) {
			this.equipmentType = equipmentType;
			this.invIdx = invIdx;
			this.equip = equip;
		}
	}
	
	class InventoryMovement {
		public final int oldIdx;
		public final int newIdx;
		
		private InventoryMovement() { this(0, 0); }
		public InventoryMovement(int oldIdx, int newIdx) {
			this.oldIdx = oldIdx;
			this.newIdx = newIdx;
		}
	}
	
	// server -> client to update inventory and equipped items.
	class InventoryUpdate {
		public final SerialItemStack[] inventory; // the item list
		@Nullable
		public final SerialItem[] equipment; // equipped items
		// public final String[] hotbarData;
		
		private InventoryUpdate() { this(null, null); }
		public InventoryUpdate(SerialItemStack[] inventory, @Nullable SerialItem[] equipment) {
			this.inventory = inventory;
			this.equipment = equipment;
			// this.hotbarData = hotbarData;
		}
	}
	
	class SerialItemTexture {
		public final ItemTextureSource textureSource;
		public final String textureName;
		
		private SerialItemTexture() { this(null, null); }
		public SerialItemTexture(FetchableTextureHolder texture) { this(texture.source, texture.tex.name); }
		public SerialItemTexture(ItemTextureSource textureSource, String textureName) {
			this.textureSource = textureSource;
			this.textureName = textureName;
		}
		
		public FetchableTextureHolder getTexture() {
			return textureSource.get(textureName);
		}
	}
	
	// sent server -> client; for items added to the inventory
	class SerialItem {
		
		// public final ItemSerialType type;
		public final String name;
		public final SerialItemTexture texture;
		public final CursorHighlight highlightMode;
		public final String data;
		
		// @Nullable public Integer durability;
		// @Nullable public EquipmentType equipmentType;
		// @Nullable public SerialItemTexture cursorTexture;
		
		private SerialItem() { this(null, null, null, null); }
		public SerialItem(String name, SerialItemTexture texture, CursorHighlight highlightMode, String data) {
			this.name = name;
			this.texture = texture;
			this.highlightMode = highlightMode;
			this.data = data;
		}
	}
	
	class SerialItemStack {
		public final SerialItem item;
		public final int count;
		
		private SerialItemStack() { this(null, 0); }
		public SerialItemStack(SerialItem item, int count) {
			this.item = item;
			this.count = count;
		}
	}
	
	// sent client -> server; when received, server will stop sending InventoryUpdates and resume sending HotbarUpdates. It will also use the given array to update its hotbar options. If the given array is null, then this means to start sending InventoryUpdates rather than HotbarUpdates.
	// the server sends back a HotbarUpdate after getting this to make sure both it and the client are on the same page and the client is displaying the right items.
	/*class InventoryRequest {
		// the likelihood of this being necessary is very small. If it becomes necessary I will implement it.
		// public final int requestID; // to prevent confusion regarding late responses
		// public final int[] hotbar;
		
		public InventoryRequest() {  }
		// public InventoryRequest(int[] hotbar) {
		// 	this.hotbar = hotbar;
		// }
	}*/
	
	// sent by server to update clients' inventory, after dropping items and attacking/interacting.
	// sent server -> client; holds ItemStack data for all hotbar items, and fill percent of inventory.
	/*class HotbarUpdate {
		public final float fillPercent; // % filled of the inventory
		public final String[][] itemStacks; // the item list
		
		private HotbarUpdate() { this((String[][])null, 0); }
		public HotbarUpdate(ItemStack[] itemStacks, float fillPercent) {
			this.fillPercent = fillPercent;
			this.itemStacks = new String[itemStacks.length][];
			for(int i = 0; i < this.itemStacks.length; i++)
				this.itemStacks[i] = itemStacks[i].serialize();
		}
		public HotbarUpdate(String[][] itemStacks, float fillPercent) {
			this.itemStacks = itemStacks;
			this.fillPercent = fillPercent;
		}
	}*/
	
	class RecipeStockUpdate {
		public final String[] inventoryItemNames;
		public final int[] inventoryItemCounts;
		
		private RecipeStockUpdate() { this(null, null); }
		public RecipeStockUpdate(ItemStack[] inventory) {
			inventoryItemNames = new String[inventory.length];
			inventoryItemCounts = new int[inventory.length];
			for(int i = 0; i < inventory.length; i++) {
				inventoryItemNames[i] = inventory[i].item.getName();
				inventoryItemCounts[i] = inventory[i].count;
			}
		}
		public RecipeStockUpdate(String[] inventoryItemNames, int[] inventoryItemCounts) {
			this.inventoryItemNames = inventoryItemNames;
			this.inventoryItemCounts = inventoryItemCounts;
		} 
	}
	
	// called by the client with no parameters for object items, server responds with filled parameters. Server does the initial send for item recipes since they are always activated by crafters.
	// client call gives appropriate hammer recipes, and also any item hand recipes.
	class RecipeUpdate {
		public final SerialRecipe[] recipes;
		public final RecipeStockUpdate stockUpdate;
		
		public RecipeUpdate() { this(null, null); }
		public RecipeUpdate(SerialRecipe[] recipes, RecipeStockUpdate stockUpdate) {
			this.recipes = recipes;
			this.stockUpdate = stockUpdate;
		}
	}
	
	class SerialRecipe {
		public final int setOrdinal;
		public final int recipeIndex;
		public final SerialItemStack result;
		public final SerialItemStack[] costs;
		
		private SerialRecipe() { this(0, 0, null, (SerialItemStack[])null); }
		/*public SerialRecipe(int setOrdinal, int recipeIndex, ItemStack result, ItemStack[] costs) {
			this(setOrdinal, recipeIndex, result.serialize(), costs, false);
		}
		public SerialRecipe(int setOrdinal, int recipeIndex, Item resultObjectAsItem, ItemStack[] costs) {
			this(setOrdinal, recipeIndex, resultObjectAsItem.serialize(), costs, true);
		}*/
		/*public SerialRecipe(int setOrdinal, int recipeIndex, SerialItemStack result, ItemStack[] costs) {
			this.setOrdinal = setOrdinal;
			this.recipeIndex = recipeIndex;
			this.result = result;
			// this.isBlueprint = isBlueprint;
			this.costs = new SerialItemStack[costs.length];
			for(int i = 0; i < costs.length; i++)
				this.costs[i] = costs[i].serialize();
		}*/
		/*public SerialRecipe(int setOrdinal, int recipeIndex, SerialItem result, SerialItemStack[] costs) {
			this(setOrdinal, recipeIndex, new SerialItemStack(result, 0), costs);
		}*/
		public SerialRecipe(int setOrdinal, int recipeIndex, SerialItemStack result, SerialItemStack[] costs) {
			this.setOrdinal = setOrdinal;
			this.recipeIndex = recipeIndex;
			this.result = result;
			this.costs = costs;
			// this.isBlueprint = isBlueprint;
		}
	}
	
	class RecipeSelectionRequest {
		public final boolean isItem;
		public final int setOrdinal;
		public final int recipeIndex;
		
		private RecipeSelectionRequest() { this(false, 0, 0); }
		public RecipeSelectionRequest(boolean isItem, int setOrdinal, int recipeIndex) {
			this.isItem = isItem;
			this.setOrdinal = setOrdinal;
			this.recipeIndex = recipeIndex;
		}
	}
	
	// sent client -> server when crafting an object on a tile, instead of an item in the inventory
	/*class BuildRequest {
		public final int setOrdinal;
		public final int recipeIndex;
		
		public final Vector2 tilePos;
		
		private BuildRequest() { this(0, 0, null); }
		public BuildRequest(int setOrdinal, int recipeIndex, Vector2 tilePos) {
			this.setOrdinal = setOrdinal;
			this.recipeIndex = recipeIndex;
			this.tilePos = tilePos;
		}
	}*/
	
	class SoundRequest {
		public final String sound;
		
		private SoundRequest() { this(null); }
		public SoundRequest(String sound) {
			this.sound = sound;
		}
	}
	
	static void registerNestedClasses(Kryo kryo, Class<?> containerClass) { registerNestedClasses(kryo, containerClass, false); }
	static void registerNestedClasses(Kryo kryo, Class<?> containerClass, boolean registerContainer) {
		if(registerContainer)
			kryo.register(containerClass);
		Class<?>[] classes = containerClass.getDeclaredClasses();
		for(Class<?> clazz: classes)
			kryo.register(clazz);
	}
	
	static void registerClassesInPackage(Kryo kryo, String packageName, boolean recursive) {
		String sep = File.separator;
		String path = new File("").getAbsolutePath() + sep + ".."+sep+"src"+sep;
		path += packageName.replaceAll("\\.", sep);
		registerClassesInPackage(kryo, packageName, new File(path), recursive); // should now point to the folder.
	}
	static void registerClassesInPackage(Kryo kryo, String packageName, File parent, boolean recursive) {
		File[] classes = parent.listFiles((dir, name) -> name.endsWith(".java"));
		File[] subpackages = parent.listFiles(File::isDirectory);
		if(classes == null)
			return;
		
		for(File file: classes) {
			String className = file.getName();
			className = className.substring(0, className.indexOf("."));
			try {
				Class<?> clazz = Class.forName(packageName+"."+className);
				kryo.register(clazz);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		if(recursive && subpackages != null)
			for(File subpack: subpackages)
				registerClassesInPackage(kryo, packageName+"."+subpack.getName(), subpack, true);
	}
}
