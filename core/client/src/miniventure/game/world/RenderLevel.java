package miniventure.game.world;

import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.RenderTile;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public abstract class RenderLevel extends Level {
	
	protected RenderLevel(@NotNull WorldManager world, int depth, int width, int height) {
		super(world, depth, width, height);
	}
	
	public abstract void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset);
	
	public static void render(Array<Tile> tiles, Array<Entity> entities, SpriteBatch batch, float delta, Vector2 posOffset) {
		// pass the offset vector to all objects being rendered.
		
		Array<WorldObject> objects = new Array<>();
		Array<Tile> under = new Array<>(); // ground tiles
		Array<Entity> over = new Array<>();
		for(Entity e: entities) {
			if(e.isFloating())
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
		under.sort((e1, e2) -> Float.compare(e2.getCenter().y, e1.getCenter().y));
		objects.sort((e1, e2) -> Float.compare(e2.getCenter().y, e1.getCenter().y));
		over.sort((e1, e2) -> Float.compare(e2.getCenter().y, e1.getCenter().y));
		//objects.addAll(entities);
		
		for(WorldObject obj: under)
			obj.render(batch, delta, posOffset);
		for(WorldObject obj: objects)
			obj.render(batch, delta, posOffset);
		for(WorldObject obj: over)
			obj.render(batch, delta, posOffset);
	}
	
	public static Array<Vector3> renderLighting(Array<WorldObject> objects) {
		Array<Vector3> lighting = new Array<>();
		
		for(WorldObject obj: objects) {
			float lightR = obj.getLightRadius();
			if(lightR > 0)
				lighting.add(new Vector3(obj.getCenter(), lightR));
		}
		
		return lighting;
	}
	
	@Override
	public void loadChunk(Chunk newChunk) {
		super.loadChunk(newChunk);
		
		// queue all contained tiles for update
		Tile[][] tiles = newChunk.getTiles();
		for(int i = 0; i < tiles.length; i++) {
			for(int j = 0; j < tiles[i].length; j++) {
				RenderTile t = (RenderTile) tiles[i][j];
				t.updateSprites();
				// update the tiles in adjacent chunks
				int oi = i == 0 ? -1 : i == tiles.length-1 ? 1 : 0;
				int oj = j == 0 ? -1 : j == tiles[i].length-1 ? 1 : 0;
				if(oi != 0) tryUpdate(t, oi, 0); // left/right side
				if(oj != 0) tryUpdate(t, 0, oj); // above/below
				if(oi != 0 && oj != 0) tryUpdate(t, oi, oj); // corner
			}
		}
	}
	private void tryUpdate(RenderTile ref, int ox, int oy) {
		Point p = ref.getLocation();
		RenderTile tile = (RenderTile) getTile(p.x+ox, p.y+oy);
		if(tile != null) tile.updateSprites();
	}
}
