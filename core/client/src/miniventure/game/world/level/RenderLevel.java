package miniventure.game.world.level;

import java.util.Comparator;

import miniventure.game.util.function.ValueAction;
import miniventure.game.util.pool.Vector3Pool;
import miniventure.game.util.pool.VectorPool;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.particle.ActionParticle;
import miniventure.game.world.management.LevelWorldManager;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.island.ProtoLevel;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public abstract class RenderLevel extends Level {
	
	// start times of main animations
	public final LevelDataMap<Float> animStartTimes = new LevelDataMap<>();
	
	/*
	TODO storing tile overlap animation data like this isn't going to work in the current system I think; it would probably help to simplify things first.
		The water animation has been working only because it was synchronous... honestly overlap animations should probably be required to be synchronous.
	 */
	// start times of overlap animations, stored by the tile they come from
	// public final LevelDataMap<Float> overlapAnimStartTimes = new LevelDataMap<>();
	
	protected RenderLevel(@NotNull LevelWorldManager world, LevelId levelId, int width, int height) {
		super(world, levelId, width, height);
	}
	/*protected RenderLevel(@NotNull LevelWorldManager world, LevelId levelId, @NotNull ProtoLevel protoLevel, @NotNull TileMaker tileFetcher) {
		super(world, levelId, protoLevel, tileFetcher);
	}
	
	protected RenderLevel(@NotNull LevelWorldManager world, LevelId levelId, int width, int height, TileFetcher tileFetcher) {
		super(world, levelId, width, height, tileFetcher);
	}*/
	
	@Override @NotNull
	public LevelWorldManager getWorld() { return (LevelWorldManager) super.getWorld(); }
	
	
	
	@Override
	public int getEntityCount() { return getWorld().getEntityTotal(); }
	
	@Override
	public void forEachEntity(ValueAction<Entity> action) {
		getWorld().forEachRegisteredEntity(action);
	}
	
	public abstract void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset);
	
	private static final Comparator<WorldObject> objectSorter = (e1, e2) -> {
		Vector2 e1C = e1.getCenter();
		Vector2 e2C = e2.getCenter();
		final int result = Float.compare(e2C.y, e1C.y);
		VectorPool.POOL.free(e1C);
		VectorPool.POOL.free(e2C);
		return result;
	};
	
	public static void render(Array<Tile> tiles, Array<Entity> entities, SpriteBatch batch, float delta, Vector2 posOffset) {
		// pass the offset vector to all objects being rendered.
		
		Array<WorldObject> objects = new Array<>();
		Array<Tile> under = new Array<>(); // ground tiles
		Array<Entity> over = new Array<>();
		for(Entity e: entities) {
			if(e.isFloating() && !(e instanceof ActionParticle))
				over.add(e);
			else
				objects.add(e);
		}
		for(Tile t: tiles) {
			if(!t.getType().isWalkable()) // used to check if z offset > 0
				objects.add(t);
			else
				under.add(t);
		}
		
		// first, ground tiles
		// then, entities and surface tiles, higher y first
		// then particles
		
		// entities second
		under.sort(objectSorter);
		objects.sort(objectSorter);
		over.sort(objectSorter);
		//objects.addAll(entities);
		
		for(WorldObject obj: under)
			obj.render(batch, delta, posOffset);
		for(WorldObject obj: objects)
			obj.render(batch, delta, posOffset);
		for(WorldObject obj: over)
			obj.render(batch, delta, posOffset);
	}
	
	/*public static Array<Vector3> renderLighting(Array<WorldObject> objects, Array<Vector3> lighting) {
		
		for(WorldObject obj: objects) {
			float lightR = obj.getLightRadius();
			if(lightR > 0) {
				Vector2 center = obj.getCenter();
				lighting.add(Vector3Pool.POOL.obtain(center, lightR));
				VectorPool.POOL.free(center);
			}
		}
		
		return lighting;
	}*/
}
