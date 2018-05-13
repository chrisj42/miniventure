package miniventure.game.world.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.EntityUpdate;
import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.GameProtocol.SpriteUpdate;
import miniventure.game.server.ServerCore;
import miniventure.game.server.ServerWorld;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.particle.Particle;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ServerEntity extends Entity {
	
	private SpriteUpdate newSprite = null;
	private PositionUpdate newPos = null;
	
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
		//data.add(new String[] {x+"", y+"", z+""});
		return data;
	}
	
	@Override @NotNull
	public ServerWorld getWorld() { return (ServerWorld) super.getWorld(); }
	
	@Override @Nullable
	public ServerLevel getLevel() { return getWorld().getEntityLevel(this); }
	
	@Override
	public boolean isMob() { return this instanceof Mob; }
	
	@Override
	public void update(float delta) {
		if(/*!(this instanceof Particle) && */(newSprite != null || newPos != null)) {
			ServerCore.getServer().broadcast(new EntityUpdate(getTag(), newPos, newSprite), this);
			newPos = null;
			newSprite = null;
		}
		
		if(this instanceof Particle) return; // particles don't interact
		
		ServerLevel level = getLevel();
		if(level == null) return;
		Array<WorldObject> objects = new Array<>();
		objects.addAll(level.getOverlappingEntities(getBounds(), this));
		// we don't want to trigger things like getting hurt by lava until the entity is actually *in* the tile, so we'll only consider the closest one to be "touching".
		Tile tile = level.getClosestTile(getBounds());
		if(tile != null) objects.add(tile);
		
		for(WorldObject obj: objects)
			obj.touching(this);
	}
	
	protected void updateSprite(SpriteUpdate newSprite) { this.newSprite = newSprite; }
	
	public float getZ() { return z; }
	protected void setZ(float z) { this.z = z; }
	
	@Override
	public void moveTo(@NotNull Level level, float x, float y) {
		super.moveTo(level, x, y);
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
