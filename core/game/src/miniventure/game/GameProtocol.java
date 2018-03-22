package miniventure.game;

import java.io.File;

import miniventure.game.item.Hands;
import miniventure.game.item.Inventory;
import miniventure.game.item.ItemStack;
import miniventure.game.util.Version;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.Level;
import miniventure.game.world.Point;
import miniventure.game.world.WorldObject.Tag;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.Entity.EntityTag;
import miniventure.game.world.entity.EntityRenderer;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.Tile.TileData;
import miniventure.game.world.tile.Tile.TileTag;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;

public interface GameProtocol {
	
	int PORT = 8405;
	int writeBufferSize = 16384*2;
	int objectBufferSize = 16384;
	
	static void registerClasses(Kryo kryo) {
		Class<?>[] classes = GameProtocol.class.getDeclaredClasses();
		for(Class<?> clazz: classes)
			kryo.register(clazz);
		
		kryo.register(TileData.class);
		kryo.register(TileData[].class);
		kryo.register(TileData[][].class);
		kryo.register(ChunkData.class);
		kryo.register(ChunkData[].class);
		kryo.register(Version.class);
		kryo.register(Tag.class);
		kryo.register(EntityTag.class);
		kryo.register(TileTag.class);
		
		kryo.register(String[].class);
		kryo.register(int[].class);
		kryo.register(Integer.class);
		kryo.register(Integer[].class);
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
		Respawn
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
		public final InventoryUpdate inventory;
		public final StatUpdate stats;
		
		private SpawnData() { this(null, null, null); }
		public SpawnData(EntityAddition playerData, Player player) { this(playerData, new InventoryUpdate(player), new StatUpdate(player)); }
		public SpawnData(EntityAddition playerData, InventoryUpdate inventory, StatUpdate stats) {
			this.playerData = playerData;
			this.inventory = inventory;
			this.stats = stats;
		}
	}
	
	class EntityAddition {
		public final PositionUpdate positionUpdate;
		public final SpriteUpdate spriteUpdate;
		public final int eid;
		
		private EntityAddition() { this(0, null, null); }
		public EntityAddition(Entity e) { this(e.getId(), new PositionUpdate(e), new SpriteUpdate(e)); }
		public EntityAddition(int eid, PositionUpdate positionUpdate, SpriteUpdate spriteUpdate) {
			this.eid = eid;
			this.positionUpdate = positionUpdate;
			this.spriteUpdate = spriteUpdate;
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
	}
	
	// sent in EntityUpdate
	class PositionUpdate {
		public final float x, y, z;
		public final Integer levelDepth; // can be null
		
		private PositionUpdate() { this(null, 0, 0, 0); }
		public PositionUpdate(Entity e) { this(e, e.getLevel(), e.getLocation()); }
		private PositionUpdate(Entity e, Level level, Vector3 pos) { this(level==null?null:level.getDepth(), pos); }
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
			
			if((levelDepth == null) != (this.levelDepth == null)) return true;
			if(levelDepth != null && !levelDepth.equals(this.levelDepth)) return true;
			
			return false;
		}
	}
	
	// sent in EntityUpdate
	class SpriteUpdate {
		public final String[] rendererData;
		
		private SpriteUpdate() { this((String[])null); }
		public SpriteUpdate(Entity e) { this(e.getRenderer()); }
		public SpriteUpdate(EntityRenderer renderer) { this(renderer.save()); }
		public SpriteUpdate(String[] rendererData) {
			this.rendererData = rendererData;
		}
	}
	
	// sent by client to interact or attack.
	class InteractRequest {
		public final boolean attack;
		public final PositionUpdate playerPosition;
		
		private InteractRequest() { this(false, null); }
		public InteractRequest(boolean attack, PositionUpdate playerPosition) {
			this.attack = attack;
			this.playerPosition = playerPosition;
		}
	}
	
	// sent by server to tell clients to use blinking rendering.
	class Hurt {
		public final int damage;
		public final Tag source, target;
		
		private Hurt() { this(null, null, 0, null); }
		public Hurt(Tag source, Tag target, int damage, String[] attackItem) {
			this.source = source;
			this.target = target;
			this.damage = damage;
		}
	}
	
	// sent by client to tell the server that it wishes to move in the given direction.
	class PlayerMovement {
		public final float xdir;
		public final float ydir;
		
		public final PositionUpdate playerPosition; // where the client thinks it should be, after moving.
		
		private PlayerMovement() { this(0, 0, null); }
		public PlayerMovement(Vector2 dir, PositionUpdate playerPosition) { this(dir.x, dir.y, playerPosition); }
		public PlayerMovement(float xdir, float ydir, PositionUpdate playerPosition) {
			this.xdir = xdir;
			this.ydir = ydir;
			this.playerPosition = playerPosition;
		}
	}
	
	class StatUpdate {
		public final Integer[] stats;
		
		private StatUpdate() { this((Integer[])null); }
		public StatUpdate(Player player) { this(player.saveStats()); }
		public StatUpdate(Integer[] stats) {
			this.stats = stats;
		}
	}
	
	// sent by client to let the server know that a new held item has been selected from the inventory.
	class HeldItemRequest {
		public final ItemStack stack;
		
		private HeldItemRequest() { this((ItemStack)null); }
		public HeldItemRequest(Hands hands) { this(new ItemStack(hands.getUsableItem(), hands.getCount())); }
		public HeldItemRequest(ItemStack stack) {
			this.stack = stack;
		}
	}
	
	// sent by client to drop an item from the inventory
	class ItemDropRequest {
		public final ItemStack stack;
		
		private ItemDropRequest() { this(null); }
		public ItemDropRequest(ItemStack stack) {
			this.stack = stack;
		}
	}
	
	// sent by server to update clients' inventory, after dropping items and attacking/interacting.
	class InventoryUpdate {
		public final String[] inventory;
		public final String[] heldItemStack;
		
		private InventoryUpdate() { this((String[])null, null); }
		public InventoryUpdate(Player player) { this(player.getInventory(), player.getHands()); }
		public InventoryUpdate(Inventory inv, Hands hands) { this(inv.save(), hands.save()); }
		public InventoryUpdate(String[] inventory, String[] heldItemStack) {
			this.inventory = inventory;
			this.heldItemStack = heldItemStack;
		}
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
