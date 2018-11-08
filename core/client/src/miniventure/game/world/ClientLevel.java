package miniventure.game.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import miniventure.game.GameProtocol.ChunkRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.ClientTile;
import miniventure.game.world.tile.ClientTileType;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.Tile.TileData;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ClientLevel extends RenderLevel {
	
	@NotNull private ClientWorld world;
	
	private final Map<ClientTile, TileData> tileUpdates = Collections.synchronizedMap(new HashMap<>());
	
	public ClientLevel(@NotNull ClientWorld world, int depth, int width, int height) {
		super(world, depth, width, height);
		this.world = world;
	}
	
	@Override @NotNull
	public ClientWorld getWorld() { return world; }
	
	@Override
	void pruneLoadedChunks() {
		if(getWorld().getMainPlayer() == null) return; // don't prune chunks before the main player is loaded.
		
		super.pruneLoadedChunks();
	}
	
	@Override
	public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {
		applyTileUpdates();
		
		renderSpace = new Rectangle(Math.max(0, renderSpace.x), Math.max(0, renderSpace.y), Math.min(getWidth()-renderSpace.x, renderSpace.width), Math.min(getHeight()-renderSpace.y, renderSpace.height));
		// pass the offset vector to all objects being rendered.
		
		Array<Tile<ClientTileType>> tiles = getOverlappingTiles(renderSpace);
		Array<Entity> entities = getOverlappingEntities(renderSpace);
		
		if(ClientCore.getScreen() instanceof RespawnScreen)
			entities.removeValue(ClientCore.getWorld().getMainPlayer(), true);
		
		render(tiles, entities, batch, delta, posOffset);
	}
	
	@SuppressWarnings("unchecked")
	private void applyTileUpdates() {
		Entry<ClientTile, TileData>[] tilesToUpdate;
		HashSet<Tile> spriteUpdates = new HashSet<>();
		synchronized (tileUpdates) {
			tilesToUpdate = tileUpdates.entrySet().toArray((Entry<ClientTile, TileData>[]) new Entry[tileUpdates.size()]);
			tileUpdates.clear();
		}
		for(Entry<ClientTile, TileData> entry: tilesToUpdate) {
			entry.getKey().apply(entry.getValue());
			spriteUpdates.add(entry.getKey());
			spriteUpdates.addAll(entry.getKey().getAdjacentTiles(true));
		}
		
		for(Tile t: spriteUpdates)
			((ClientTile)t).updateSprites();
	}
	
	public void serverUpdate(ClientTile tile, TileData data) {
		synchronized (tileUpdates) {
			tileUpdates.put(tile, data);
		}
	}
	
	@Override
	public ClientTile getTile(float x, float y) { return getTile(x, y, false); }
	public ClientTile getTile(float x, float y, boolean loadIfNull) {
		ClientTile tile = (ClientTile) super.getTile(x, y);
		if(tile == null && loadIfNull)
			loadChunk(Chunk.getCoords(x, y));
		
		return tile;
	}
	
	@Override
	protected void loadChunk(Point chunkCoord) {
		//System.out.println("Client requesting chunk "+chunkCoord);
		ClientCore.getClient().send(new ChunkRequest(chunkCoord));
	}
	
	@Override
	protected void unloadChunk(Point chunkCoord) {
		Chunk chunk = getLoadedChunk(chunkCoord);
		if(chunk == null) return; // already unloaded
		
		//System.out.println("Client unloading chunk "+chunkCoord);
		
		for(Entity e: entityChunks.keySet().toArray(new Entity[entityChunks.size()]))
			if(entityChunks.get(e).equals(chunkCoord))
				e.remove();
		
		loadedChunks.access(chunks -> chunks.remove(chunkCoord));
	}
	
	
}
