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
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.mob.Player.PlayerUpdate;
import miniventure.game.world.entity.mob.Player.Stat;
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
		kryo.register(PlayerUpdate.class);
		kryo.register(Tag.class);
		kryo.register(EntityTag.class);
		kryo.register(TileTag.class);
		
		kryo.register(String[].class);
		kryo.register(int[].class);
		kryo.register(Integer.class);
		kryo.register(Integer[].class);
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
	
	class SpawnData {
		public final String playerData;
		public final int eid;
		
		private SpawnData() { this((String)null, 0); }
		public SpawnData(Player player) { this(Entity.serialize(player), player.getId()); }
		public SpawnData(String playerData, int eid) {
			this.playerData = playerData;
			this.eid = eid;
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
	
	class EntityAddition {
		public final int eid;
		public final String data;
		public final Integer levelDepth;
		
		private EntityAddition() { this(null, 0, null); }
		public EntityAddition(Entity e) { this(e, e.getLevel()); }
		private EntityAddition(Entity e, Level level) { this(Entity.serialize(e), e.getId(), level == null ? null : level.getDepth()); }
		public EntityAddition(String data, int eid, Integer levelDepth) {
			this.data = data;
			this.eid = eid;
			this.levelDepth = levelDepth;
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
	
	class Ping {
		// this is the occasional ping sent by each client, to make sure things like position are still accurate.
		public final float x, y;
		
		private Ping() { this(0, 0); }
		public Ping(Player player) { this(player.getPosition()); }
		public Ping(Vector2 pos) { this(pos.x, pos.y); }
		public Ping(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}
	
	class Movement {
		public final float x, y, z;
		public final int eid;
		public final Integer levelDepth;
		
		private Movement() { this(0, null, 0, 0, 0); }
		public Movement(Entity e) { this(e, e.getLevel(), new Vector3(e.getPosition(), e.getZ())); }
		private Movement(Entity e, Level level, Vector3 pos) { this(e.getId(), level==null?null:level.getDepth(), pos); }
		public Movement(int eid, Integer depth, Vector3 pos) { this(eid, depth, pos.x, pos.y, pos.z); }
		public Movement(int eid, Integer depth, float x, float y, float z) {
			this.levelDepth = depth;
			this.x = x;
			this.y = y;
			this.z = z;
			this.eid = eid;
		}
	}
	
	class InteractRequest {
		public final boolean attack;
		public final PlayerUpdate update;
		
		private InteractRequest() { this(false, null); }
		public InteractRequest(boolean attack, PlayerUpdate update) {
			this.attack = attack;
			this.update = update;
		}
	}
	
	class StatChange {
		public final int stat, amt;
		
		private StatChange() { this(0, 0); }
		public StatChange(Stat stat, int amt) { this(stat.ordinal(), amt); }
		public StatChange(int statOrd, int amt) {
			this.stat = statOrd;
			this.amt = amt;
		}
	}
	
	class Hurt {
		public final int damage;
		public final String[] attackItem;
		public final Tag source, target;
		
		private Hurt() { this(null, null, 0, null); }
		public Hurt(Tag source, Tag target, int damage, String[] attackItem) {
			this.source = source;
			this.target = target;
			this.damage = damage;
			this.attackItem = attackItem;
		}
	}
	
	class ItemDropRequest {
		public final ItemStack stack;
		
		private ItemDropRequest() { this(null); }
		public ItemDropRequest(ItemStack stack) {
			this.stack = stack;
		}
	}
	
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
