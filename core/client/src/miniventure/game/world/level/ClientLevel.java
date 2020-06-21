package miniventure.game.world.level;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import miniventure.game.core.ClientCore;
import miniventure.game.network.GameProtocol.LevelChunk;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.management.ClientWorld;
import miniventure.game.world.tile.ClientTile;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileStack.TileData;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientLevel extends RenderLevel {
	
	@NotNull private ClientWorld world;
	private final boolean[][] loaded;
	
	private final Map<ClientTile, CachedTileUpdate> tileUpdates = Collections.synchronizedMap(new HashMap<>());
	
	public ClientLevel(@NotNull ClientWorld world, int levelId, int width, int height) {
		super(world, levelId, width, height, ClientTile::new);
		this.world = world;
		
		loaded = new boolean[width][height];
	}
	
	@Override @NotNull
	public ClientWorld getWorld() { return world; }
	
	@Override
	public ClientTile getTile(float x, float y) { return (ClientTile) super.getTile(x, y); }
	@Override
	public ClientTile getTile(int x, int y) { return (ClientTile) super.getTile(x, y); }
	
	@Override
	public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {
		applyTileUpdates();
		
		renderSpace = new Rectangle(Math.max(0, renderSpace.x), Math.max(0, renderSpace.y), Math.min(getWidth()-renderSpace.x, renderSpace.width), Math.min(getHeight()-renderSpace.y, renderSpace.height));
		// pass the offset vector to all objects being rendered.
		
		Array<Tile> tiles = getOverlappingTiles(renderSpace);
		Array<Entity> entities = getOverlappingEntities(renderSpace);
		
		if(ClientCore.getScreen() instanceof RespawnScreen)
			entities.removeValue(ClientCore.getWorld().getMainPlayer(), true);
		
		render(tiles, entities, batch, delta, posOffset);
	}
	
	public void setTiles(LevelChunk data) {
		for (int i = 0; i < data.tileData.length; i++) {
			for (int j = 0; j < data.tileData[i].length; j++) {
				getTile(data.offset.x + i, data.offset.y + j).apply(data.tileData[i][j], null);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void applyTileUpdates() {
		Entry<ClientTile, CachedTileUpdate>[] tilesToUpdate;
		HashSet<Tile> spriteUpdates = new HashSet<>();
		synchronized (tileUpdates) {
			tilesToUpdate = tileUpdates.entrySet().toArray((Entry<ClientTile, CachedTileUpdate>[]) new Entry[tileUpdates.size()]);
			tileUpdates.clear();
		}
		for(Entry<ClientTile, CachedTileUpdate> entry: tilesToUpdate) {
			CachedTileUpdate update = entry.getValue();
			entry.getKey().apply(update.newData, update.updatedType);
			spriteUpdates.add(entry.getKey());
			spriteUpdates.addAll(entry.getKey().getAdjacentTiles(true));
		}
		
		for(Tile t: spriteUpdates)
			((ClientTile)t).updateSprites();
	}
	
	// called in GameClient thread.
	public void serverUpdate(ClientTile tile, TileData data, @Nullable TileTypeEnum updatedType) {
		synchronized (tileUpdates) {
			tileUpdates.put(tile, new CachedTileUpdate(data, updatedType));
		}
	}
	
	private static class CachedTileUpdate {
		TileData newData;
		TileTypeEnum updatedType;
		
		private CachedTileUpdate(TileData newData, TileTypeEnum updatedType) {
			this.newData = newData;
			this.updatedType = updatedType;
		}
	}
}
