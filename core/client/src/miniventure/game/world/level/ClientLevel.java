package miniventure.game.world.level;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import miniventure.game.core.ClientCore;
import miniventure.game.network.GameProtocol.TileAreaUpdate;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.util.pool.RectPool;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.management.ClientWorld;
import miniventure.game.world.tile.ClientTile;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileStack.TileData;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ClientLevel extends RenderLevel {
	
	@NotNull private final ClientWorld world;
	// private final boolean[][] loaded;
	
	private final Map<ClientTile, TileData> tileUpdates = Collections.synchronizedMap(new HashMap<>());
	
	
	public ClientLevel(@NotNull ClientWorld world, LevelId levelId, int width, int height) {
		super(world, levelId, width, height);
		this.world = world;
		
		// loaded = new boolean[width][height];
	}
	
	@Override
	protected Tile makeTile(int x, int y) {
		return new ClientTile(this, x, y);
	}
	
	@Override @NotNull
	public ClientWorld getWorld() { return (ClientWorld) super.getWorld(); }
	
	@Override
	public ClientTile getTile(float x, float y) { return (ClientTile) super.getTile(x, y); }
	@Override
	public ClientTile getTile(int x, int y) { return (ClientTile) super.getTile(x, y); }
	
	@Override
	public void update(float delta) {
		applyTileUpdates();
		super.update(delta);
	}
	
	@Override
	public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {
		renderSpace = RectPool.POOL.obtain(Math.max(0, renderSpace.x), Math.max(0, renderSpace.y), Math.min(getWidth()-renderSpace.x, renderSpace.width), Math.min(getHeight()-renderSpace.y, renderSpace.height));
		// pass the offset vector to all objects being rendered.
		
		Array<Tile> tiles = new Array<>(Tile.class);
		forOverlappingTiles(renderSpace, tiles::add);
		Array<Entity> entities = new Array<>(Entity.class);
		forOverlappingEntities(renderSpace, true, entities::add);
		
		if(ClientCore.getScreen() instanceof RespawnScreen)
			entities.removeValue(ClientCore.getWorld().getMainPlayer(), true);
		
		render(tiles, entities, batch, delta, posOffset);
	}
	
	public void setTiles(TileAreaUpdate data) {
		for (int i = 0; i < data.tileData.length; i++) {
			for (int j = 0; j < data.tileData[i].length; j++) {
				ClientTile tile = getTile(data.offset.x + i, data.offset.y + j);
				tile.setStack(data.tileData[i][j].parseStack(world));
			}
		}
		if(data.updateAdjacent)
			forOverlappingTiles(
					RectPool.POOL.obtain(data.offset.x - 1, data.offset.y - 1,
							data.tileData.length + 2, data.tileData[0].length + 2
					), true, t -> ((ClientTile)t).queueSpriteUpdate()
			);
	}
	
	@SuppressWarnings("unchecked")
	private void applyTileUpdates() {
		Entry<ClientTile, TileData>[] tilesToUpdate;
		// HashSet<Tile> spriteUpdates = new HashSet<>();
		synchronized (tileUpdates) {
			tilesToUpdate = tileUpdates.entrySet().toArray((Entry<ClientTile, TileData>[]) new Entry[tileUpdates.size()]);
			tileUpdates.clear();
		}
		for(Entry<ClientTile, TileData> entry: tilesToUpdate) {
			TileData update = entry.getValue();
			entry.getKey().setStack(update.parseStack(world)/*, update.updatedType*/);
			forAreaTiles(entry.getKey().getLocation(), 1, true, t -> ((ClientTile)t).queueSpriteUpdate());
		}
		
		// for(Tile t: spriteUpdates)
		// 	((ClientTile)t).queueSpriteUpdate();
	}
	
	// called in GameClient thread.
	public void serverUpdate(ClientTile tile, TileData data/*, @Nullable TileTypeEnum updatedType*/) {
		synchronized (tileUpdates) {
			tileUpdates.put(tile, data/*new CachedTileUpdate(data, updatedType)*/);
		}
	}
	
	/*private static class CachedTileUpdate {
		TileData newData;
		TileTypeEnum updatedType;
		
		private CachedTileUpdate(TileData newData, TileTypeEnum updatedType) {
			this.newData = newData;
			this.updatedType = updatedType;
		}
	}*/
}
