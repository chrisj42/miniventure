package miniventure.game.world.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.EntityUpdate;
import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.server.ServerCore;
import miniventure.game.server.ServerWorld;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ServerEntity extends Entity {
	
	protected EntityUpdate updateCache = null;
	
	public ServerEntity() {
		super(ServerCore.getWorld());
	}
	
	protected ServerEntity(String[][] data, Version version) {
		super(ServerCore.getWorld());
		x = Float.parseFloat(data[0][0]);
		y = Float.parseFloat(data[0][1]);
		z = Float.parseFloat(data[0][2]);
	}
	
	public Array<String[]> save() {
		Array<String[]> data = new Array<>(String[].class);
		data.add(new String[] {x+"", y+"", z+""});
		return data;
	}
	
	@Override @NotNull
	public ServerWorld getWorld() { return (ServerWorld) super.getWorld(); }
	
	@Override @Nullable
	public ServerLevel getLevel() { return getWorld().getEntityLevel(this); }
	
	@Override
	public void update(float delta) {
		ServerLevel level = getLevel();
		if(level == null) return;
		Array<WorldObject> objects = new Array<>();
		objects.addAll(level.getOverlappingEntities(getBounds(), this));
		Tile tile = level.getClosestTile(getBounds());
		if(tile != null) objects.add(tile);
		
		for(WorldObject obj: objects)
			obj.touching(this);
		
		if(updateCache != null) {
			ServerCore.getServer().broadcast(updateCache, this);
			updateCache = null;
		}
	}
	
	public float getZ() { return z; }
	protected void setZ(float z) { this.z = z; }
	
	public boolean move(float xd, float yd) { return move(xd, yd, 0); }
	public boolean move(float xd, float yd, float zd) {
		Level level = getLevel();
		if(level == null) return false; // can't move if you're not in a level...
		Vector2 movement = new Vector2();
		movement.x = moveAxis(level, true, xd, 0);
		movement.y = moveAxis(level, false, yd, movement.x);
		z += zd;
		boolean moved = !movement.isZero();
		if(moved) {
			moveTo(level, x+movement.x, y+movement.y);
			updateCache = new EntityUpdate(getTag(), new PositionUpdate(this), updateCache == null ? null : updateCache.spriteUpdate);
		}
		return moved;
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
	
	public static String serialize(ServerEntity e) {
		Array<String[]> data = e.save();
		String[][] doubleDataArray = data.shrink();
		
		String[] partEncodedData = new String[doubleDataArray.length+1];
		for(int i = 0; i < doubleDataArray.length; i++) {
			partEncodedData[i+1] = MyUtils.encodeStringArray(doubleDataArray[i]);
		}
		
		partEncodedData[0] = e.getClass().getCanonicalName().replace(Entity.class.getPackage().getName()+".", "");
		
		return MyUtils.encodeStringArray(partEncodedData);
	}
	
	public static ServerEntity deserialize(String data) {
		String[] partData = MyUtils.parseLayeredString(data);
		
		String[][] sepData = new String[partData.length-1][];
		for(int i = 0; i < sepData.length; i++) {
			sepData[i] = MyUtils.parseLayeredString(partData[i+1]);
		}
		
		ServerEntity entity = null;
		
		try {
			Class<?> clazz = Class.forName(Entity.class.getPackage().getName()+"."+partData[0]);
			
			Class<? extends ServerEntity> entityClass = clazz.asSubclass(ServerEntity.class);
			
			entity = deserialize(entityClass, sepData);
			
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return entity;
	}
	public static <T extends ServerEntity> T deserialize(Class<T> clazz, String[][] data) {
		T newEntity = null;
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor(String[][].class, Version.class);
			constructor.setAccessible(true);
			newEntity = constructor.newInstance(data, GameCore.VERSION);
		} catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		
		return newEntity;
	}
}
