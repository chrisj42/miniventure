package miniventure.game.world.entity;

import java.util.LinkedList;

import miniventure.game.world.management.Level;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class EntitySpawn {
	
	private static LinkedList<EntitySpawn> freeObjects = new LinkedList<>();
	
	public static EntitySpawn get(@NotNull Level level, Vector2 v) {
		return get(level, v.x, v.y);
	}
	public static EntitySpawn get(@NotNull Level level, float x, float y) {
		if(freeObjects.size() == 0)
			return new EntitySpawn(level, x, y);
		return freeObjects.removeFirst().set(level, x, y);
	}
	
	static void free(@NotNull EntitySpawn obj) {
		freeObjects.addLast(obj);
	}
	
	private @NotNull Level level;
	private float x;
	private float y;
	
	private EntitySpawn(@NotNull Level level, float x, float y) {
		this.level = level;
		this.x = x;
		this.y = y;
	}
	
	private EntitySpawn set(@NotNull Level level, float x, float y) {
		this.level = level;
		this.x = x;
		this.y = y;
		return this;
	}
	
	@NotNull
	public Level getLevel() { return level; }
	public float getX() { return x; }
	public float getY() { return y; }
}
