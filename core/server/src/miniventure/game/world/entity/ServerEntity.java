package miniventure.game.world.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map.Entry;

import miniventure.game.network.GameProtocol.EntityUpdate;
import miniventure.game.network.GameProtocol.PositionUpdate;
import miniventure.game.network.GameProtocol.SpriteUpdate;
import miniventure.game.network.GameServer;
import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.util.pool.RectPool;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ServerEntity extends Entity {
	
	private SpriteUpdate newSprite = null;
	private PositionUpdate newPos = null;
	
	public ServerEntity(@NotNull ServerWorld world) {
		super(world, false);
	}
	
	protected ServerEntity(@NotNull ServerWorld world, EntityDataSet data, final Version version, ValueAction<EntityDataSet> modifier) {
		this(world);
		modifier.act(data);
		
		data.setPrefix("e");
		x = data.get("x", Float::parseFloat);
		y = data.get("y", Float::parseFloat);
		z = data.get("z", Float::parseFloat);
	}
	
	public EntityDataSet save() {
		EntityDataSet data = new EntityDataSet();
		
		data.setPrefix("e");
		data.add("x", x);
		data.add("y", y);
		data.add("z", z);
		
		return data;
	}
	
	@Override @NotNull
	public ServerWorld getWorld() { return (ServerWorld) super.getWorld(); }
	
	@NotNull
	public GameServer getServer() { return getWorld().getServer(); }
	
	@Override @Nullable
	public ServerLevel getLevel() { return getWorld().getEntityLevel(this); }
	
	@Override
	public boolean isMob() { return this instanceof Mob; }
	
	@Override
	public void update(float delta) {
		if(newSprite != null || newPos != null) {
			getServer().broadcastLocal(getLevel(), this, new EntityUpdate(getTag(), newPos, newSprite));
			newPos = null;
			newSprite = null;
		}
		
		if(isFloating()) return; // floating entities don't interact
		
		ServerLevel level = getLevel();
		if(level == null) return;
		Array<WorldObject> objects = new Array<>();
		
		Rectangle bounds = getBounds();
		
		level.forOverlappingEntities(bounds, this, objects::add);
		// we don't want to trigger things like getting hurt by lava until the entity is actually *in* the tile, so we'll only consider the closest one to be "touching".
		Tile tile = level.getTile(bounds);
		if(tile != null) objects.add(tile);
		
		for(WorldObject obj: objects)
			obj.touching(this);
		
		// get the entity back on the map if they somehow end up on a null tile
		if(tile == null)
			moveTo(level.getClosestTile(bounds).getCenter(), true);
		
		RectPool.POOL.free(bounds);
	}
	
	protected void updateSprite(SpriteUpdate newSprite) { this.newSprite = newSprite; }
	
	@Override
	public void moveTo(float x, float y, float z) {
		super.moveTo(x, y, z);
		
		newPos = new PositionUpdate(this);
	}
	
	@Override
	void touchTile(Tile tile) {
		tile.touchedBy(this); // NOTE: I can see this causing an issue if you move too fast; it will "touch" tiles that could be far away, if the player will move there next frame.
	}
	
	@Override
	void touchEntity(Entity entity) {
		if(!entity.touchedBy(this))
			this.touchedBy(entity); // to make sure something has a chance to happen, but it doesn't happen twice.
	}
	
	public String serialize() { return serialize(this); }
	
	public static String serialize(ServerEntity e) {
		EntityDataSet data = e.save();
		
		// SerialHashMap allData = new SerialHashMap();
		// for(Entry<String, SerialHashMap> entry: data.entrySet())
		// 	allData.put(entry.getKey(), entry.getValue().serialize());
		
		data.setPrefix("");
		data.add("class", e.getClass().getCanonicalName().replace(Entity.class.getPackage().getName()+'.', ""));
		
		return data.serialize();
	}
	
	public static ServerEntity deserialize(@NotNull ServerWorld world, String data, @NotNull Version version) {
		EntityDataSet map = new EntityDataSet(data);
		
		String entityType = map.remove("class");
		
		// EntityDataSet map = new EntityDataSet();
		// for(Entry<String, String> entry: map.entrySet()) {
		// 	map.put(entry.getKey(), new SerialHashMap(entry.getValue()));
		// }
		
		ServerEntity entity = null;
		
		try {
			Class<?> clazz = Class.forName(Entity.class.getPackage().getName()+'.'+entityType);
			
			Class<? extends ServerEntity> entityClass = clazz.asSubclass(ServerEntity.class);
			
			entity = deserialize(world, entityClass, map, version);
			
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return entity;
	}
	public static <T extends ServerEntity> T deserialize(@NotNull ServerWorld world, Class<T> clazz, EntityDataSet data, @NotNull Version version) {
		T newEntity = null;
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor(ServerWorld.class, EntityDataSet.class, Version.class, ValueAction.class);
			constructor.setAccessible(true);
			newEntity = constructor.newInstance(world, data, version, (ValueAction<EntityDataSet>)(allData -> {}));
		} catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		
		return newEntity;
	}
}
