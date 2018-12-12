package miniventure.game;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import miniventure.game.chat.InfoMessage;
import miniventure.game.chat.InfoMessageLine;
import miniventure.game.item.Item;
import miniventure.game.item.ItemStack;
import miniventure.game.util.Version;
import miniventure.game.world.Boundable;
import miniventure.game.world.Chunk;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.Level;
import miniventure.game.world.Point;
import miniventure.game.world.Taggable.Tag;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.Entity.EntityTag;
import miniventure.game.world.entity.EntityRenderer;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.mob.player.Player.Stat;
import miniventure.game.world.entity.particle.ActionType;
import miniventure.game.world.entity.particle.ParticleData;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.Tile.TileData;
import miniventure.game.world.tile.Tile.TileTag;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;

public interface GameProtocol {
	
	int PORT = 8405;
	int writeBufferSize = 16384*3;
	int objectBufferSize = 16384*2;
	
	boolean lag = false;
	int lagMin = lag?10:0, lagMax = lag?100:0;
	
	static void registerClasses(Kryo kryo) {
		registerNestedClasses(kryo, GameProtocol.class);
		registerNestedClasses(kryo, ParticleData.class, true);
		
		kryo.register(TileData.class);
		kryo.register(TileData[].class);
		kryo.register(TileData[][].class);
		kryo.register(ChunkData.class);
		kryo.register(ChunkData[].class);
		kryo.register(Version.class);
		kryo.register(Tag.class);
		kryo.register(EntityTag.class);
		kryo.register(TileTag.class);
		kryo.register(Direction.class);
		kryo.register(ActionType.class);
		kryo.register(Stat.class);
		kryo.register(InfoMessageLine.class);
		kryo.register(InfoMessageLine[].class);
		kryo.register(InfoMessage.class);
		kryo.register(SerialRecipe[].class);
		
		kryo.register(String[].class);
		kryo.register(String[][].class);
		kryo.register(int[].class);
		kryo.register(Integer.class);
		kryo.register(Integer[].class);
		
		kryo.register(Point.class);
		kryo.register(Point[].class);
	}
	
	@FunctionalInterface
	// T = Request class
	interface RequestResponse<T> {
		void respond(T request);
	}
	
	default <T> void forPacket(Object packet, Class<T> type, RequestResponse<T> response) {
		if(type.isAssignableFrom(packet.getClass()))
			response.respond(type.cast(packet));
	}
	
	enum DatalessRequest {
		Respawn, Tile, Clear_Console
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
	
	class LevelData {
		public final int width;
		public final int height;
		public final int depth;
		
		private LevelData() { this(0, 0, 0); }
		public LevelData(Level level) { this(level.getWidth(), level.getHeight(), level.getDepth()); }
		public LevelData(int width, int height, int depth) {
			this.width = width;
			this.height = height;
			this.depth = depth;
		}
	}
	
	class TileUpdate {
		public final TileData tileData;
		public final int levelDepth;
		public final int x;
		public final int y;
		
		private TileUpdate() { this(null, 0, 0, 0); }
		public TileUpdate(Tile tile) { this(tile, tile.getLocation()); }
		private TileUpdate(Tile tile, Point pos) { this(new TileData(tile), tile.getLevel().getDepth(), pos.x, pos.y); }
		public TileUpdate(TileData data, int levelDepth, int x, int y) {
			tileData = data;
			this.levelDepth = levelDepth;
			this.x = x;
			this.y = y;
		}
	}
	
	// sent by client to ask for a chunk. Server assumes level to be the client's current level.
	class ChunkRequest {
		public final int x;
		public final int y;
		
		private ChunkRequest() { this(0, 0); }
		public ChunkRequest(Point p) { this(p.x, p.y); }
		public ChunkRequest(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	class SpawnData {
		public final EntityAddition playerData;
		public final HotbarUpdate hotbar;
		public final Integer[] stats;
		
		private SpawnData() { this(null, null, null); }
		public SpawnData(EntityAddition playerData, HotbarUpdate hotbar, Integer[] stats) {
			this.playerData = playerData;
			this.hotbar = hotbar;
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
		public final int levelDepth;
		public final int[] ids;
		public final Point[] chunks;
		
		private EntityValidation() { this(0, null, null); }
		public EntityValidation(Level level, Rectangle area, Entity... excluded) {
			this.levelDepth = level.getDepth();
			
			Array<Entity> entities = level.getOverlappingEntities(area, excluded);
			ids = new int[entities.size];
			chunks = new Point[entities.size];
			
			for(int i = 0; i < entities.size; i++) {
				ids[i] = entities.get(i).getId();
				chunks[i] = Chunk.getCoords(entities.get(i).getCenter());
			}
		}
		public EntityValidation(int levelDepth, int[] ids, Point[] chunks) {
			this.levelDepth = levelDepth;
			this.ids = ids;
			this.chunks = chunks;
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
		public final Integer levelDepth; // should never be null, actually, because it is always on one level or another, and if not, then it's not in the game, aka removed. An entity removal would be sent rather than a position update. However, it's a bit complicated to change now, so I'll leave it...
		
		private PositionUpdate() { this(null, 0, 0, 0); }
		public PositionUpdate(Entity e) { this(e.getLevel(), e.getLocation()); }
		public PositionUpdate(Level level, Vector2 pos) { this(level, new Vector3(pos, 0)); }
		public PositionUpdate(Level level, Vector3 pos) { this(level==null?null:level.getDepth(), pos); }
		public PositionUpdate(Integer depth, Vector3 pos) { this(depth, pos.x, pos.y, pos.z); }
		public PositionUpdate(Integer depth, float x, float y, float z) {
			this.levelDepth = depth;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public boolean variesFrom(Entity entity) { return variesFrom(entity, entity.getLevel()); }
		private boolean variesFrom(Entity entity, Level level) { return variesFrom(entity.getPosition(), level==null?null:level.getDepth()); }
		public boolean variesFrom(Vector3 pos, Integer levelDepth) { return variesFrom(new Vector2(pos.x, pos.y), levelDepth); }
		public boolean variesFrom(Vector2 pos, Integer levelDepth) {
			if(pos.dst(x, y) > 0.25) return true;
			return !Objects.equals(levelDepth, this.levelDepth);
		}
		
		public Vector3 getPos() { return new Vector3(x, y, z); }
		
		@Override public String toString() {
			return "PositionUpdate("+x+","+y+","+z+",lvl"+levelDepth+")";
		}
		
		public String toString(WorldManager world) {
			if(levelDepth == null) return toString();
			Vector2 pos = Boundable.toLevelCoords(world.getLevel(levelDepth), new Vector2(x, y));
			return "PositionUpdate("+pos.x+","+pos.y+","+z+",lvl"+levelDepth+")";
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
		public final PositionUpdate playerPosition;
		public final Direction dir;
		public final int hotbarIndex;
		
		private InteractRequest() { this(false, null, null, 0); }
		public InteractRequest(boolean attack, PositionUpdate playerPosition, Direction dir, int hotbarIndex) {
			this.attack = attack;
			this.playerPosition = playerPosition;
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
		public final boolean hotbar; // if true, the index is translated to an inventory slot.
		public final int index;
		public final boolean all;
		
		private ItemDropRequest() { this(false, 0, false); }
		public ItemDropRequest(boolean hotbar, int index, boolean all) {
			this.hotbar = hotbar;
			this.index = index;
			this.all = all;
		}
	}
	
	// sent server -> client; special packet for when the client has the inventory screen open, which is communicated through an InventoryRequest with no hotbar array. This packet includes all the inventory items and the inventory indices of all the hotbar items.
	class InventoryUpdate {
		public final String[][] itemStacks; // the item list
		public final int[] hotbar; // only not null the first time
		
		private InventoryUpdate() { this((String[][])null, null); }
		public InventoryUpdate(ItemStack[] inventory, int[] hotbar) {
			this.hotbar = hotbar;
			itemStacks = new String[inventory.length][];
			for(int i = 0; i < inventory.length; i++)
				itemStacks[i] = inventory[i].serialize();
		}
		public InventoryUpdate(String[][] itemStacks, int[] hotbar) {
			this.itemStacks = itemStacks;
			this.hotbar = hotbar;
		}
	}
	
	// sent server -> client; for any items added to the inventory after server sends InventoryUpdate, and before server receives InventoryRequest.
	class InventoryAddition {
		public final String[] newItem;
		
		private InventoryAddition() { this((String[])null); }
		public InventoryAddition(Item item) { this(item.serialize()); }
		public InventoryAddition(String[] item) {
			newItem = item;
		}
	}
	
	// sent client -> server; when received, server will stop sending InventoryUpdates and resume sending HotbarUpdates. It will also use the given array to update its hotbar options. If the given array is null, then this means to start sending InventoryUpdates rather than HotbarUpdates.
	// the server sends back a HotbarUpdate after getting this to make sure both it and the client are on the same page and the client is displaying the right items.
	class InventoryRequest {
		// the likelihood of this being necessary is very small. If it becomes necessary I will implement it.
		// public final int requestID; // to prevent confusion regarding late responses
		public final int[] hotbar;
		
		private InventoryRequest() { this(null); }
		public InventoryRequest(int[] hotbar) {
			this.hotbar = hotbar;
		}
	}
	
	// sent by server to update clients' inventory, after dropping items and attacking/interacting.
	// sent server -> client; holds ItemStack data for all hotbar items, and fill percent of inventory.
	class HotbarUpdate {
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
	}
	
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
	
	// called by the client with no parameters, server responds with filled parameters.
	class RecipeRequest {
		public final SerialRecipe[] recipes;
		public final RecipeStockUpdate stockUpdate;
		
		public RecipeRequest() { this(null, null); }
		public RecipeRequest(SerialRecipe[] recipes, RecipeStockUpdate stockUpdate) {
			this.recipes = recipes;
			this.stockUpdate = stockUpdate;
		}
	}
	
	class SerialRecipe {
		public final String[] result;
		public final String[][] costs;
		
		private SerialRecipe() { this((String[])null, null); }
		public SerialRecipe(ItemStack result, ItemStack[] costs) {
			this.result = result.serialize();
			this.costs = new String[costs.length][];
			for(int i = 0; i < costs.length; i++)
				this.costs[i] = costs[i].serialize();
		}
		public SerialRecipe(String[] result, String[][] costs) {
			this.result = result;
			this.costs = costs;
		}
	}
	
	class CraftRequest {
		public final int recipeIndex; // TO-DO this only works because there is only one array of recipes; it'll have to be changed later.
		
		private CraftRequest() { this(0); }
		public CraftRequest(int recipeIndex) {
			this.recipeIndex = recipeIndex;
		}
	}
	
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
