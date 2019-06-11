package miniventure.game.world.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import miniventure.game.network.GameProtocol.EntityUpdate;
import miniventure.game.network.GameProtocol.PositionUpdate;
import miniventure.game.network.GameProtocol.SpriteUpdate;
import miniventure.game.server.GameServer;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ServerEntity extends Entity {
	
	private SpriteUpdate newSprite = null;
	private PositionUpdate newPos = null;
	
	public ServerEntity(@NotNull ServerWorld world) {
		super(world, false);
	}
	
	protected ServerEntity(@NotNull ServerWorld world, ClassDataList allData, final Version version, ValueAction<ClassDataList> modifier) {
		this(world);
		modifier.act(allData);
		// the index here is based on the class count away from ServerEntity in inheritance.
		ArrayList<String> data = allData.get(0);
		x = Float.parseFloat(data.get(0));
		y = Float.parseFloat(data.get(1));
		z = Float.parseFloat(data.get(2));
	}
	
	public ClassDataList save() {
		ClassDataList allData = new ClassDataList();
		ArrayList<String> data = new ArrayList<>(Arrays.asList(
			String.valueOf(x),
			String.valueOf(y),
			String.valueOf(z)
		));
		allData.add(data);
		return allData;
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
			getServer().broadcast(new EntityUpdate(getTag(), newPos, newSprite), this);
			newPos = null;
			newSprite = null;
		}
		
		if(isFloating()) return; // floating entities don't interact
		
		ServerLevel level = getLevel();
		if(level == null) return;
		Array<WorldObject> objects = new Array<>();
		objects.addAll(level.getOverlappingEntities(getBounds(), this));
		// we don't want to trigger things like getting hurt by lava until the entity is actually *in* the tile, so we'll only consider the closest one to be "touching".
		Tile tile = level.getTile(getBounds());
		if(tile != null) objects.add(tile);
		
		for(WorldObject obj: objects)
			obj.touching(this);
		
		// get the entity back on the map if they somehow end up on a null tile
		if(tile == null)
			moveTo(level.getClosestTile(getBounds()).getCenter());
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
		ClassDataList data = e.save();
		
		String[] partEncodedData = new String[data.size()+1];
		for(int i = 0; i < data.size(); i++) {
			partEncodedData[i+1] = MyUtils.encodeStringArray(data.get(i));
		}
		
		partEncodedData[0] = e.getClass().getCanonicalName().replace(Entity.class.getPackage().getName()+".", "");
		
		return MyUtils.encodeStringArray(partEncodedData);
	}
	
	public static ServerEntity deserialize(@NotNull ServerWorld world, String data, Version version) {
		String[] partData = MyUtils.parseLayeredString(data);
		
		ClassDataList map = new ClassDataList();
		for(int i = 1; i < partData.length; i++) {
			map.add(new ArrayList<>(Arrays.asList(
				MyUtils.parseLayeredString(partData[i])
			)));
		}
		
		ServerEntity entity = null;
		
		try {
			Class<?> clazz = Class.forName(Entity.class.getPackage().getName()+"."+partData[0]);
			
			Class<? extends ServerEntity> entityClass = clazz.asSubclass(ServerEntity.class);
			
			entity = deserialize(world, entityClass, map, version);
			
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return entity;
	}
	public static <T extends ServerEntity> T deserialize(@NotNull ServerWorld world, Class<T> clazz, ClassDataList data, Version version) {
		T newEntity = null;
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor(ServerWorld.class, ClassDataList.class, Version.class, ValueAction.class);
			constructor.setAccessible(true);
			newEntity = constructor.newInstance(world, data, version, (ValueAction)(allData -> {}));
		} catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		
		return newEntity;
	}
}
