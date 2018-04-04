package miniventure.game.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import miniventure.game.GameProtocol.ChunkRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.particle.Particle;
import miniventure.game.world.tile.ClientTile;
import miniventure.game.world.tile.Tile.TileData;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ClientLevel extends Level {
	
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
	
	public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {
		applyTileUpdates();
		
		renderSpace = new Rectangle(Math.max(0, renderSpace.x), Math.max(0, renderSpace.y), Math.min(getWidth()-renderSpace.x, renderSpace.width), Math.min(getHeight()-renderSpace.y, renderSpace.height));
		// pass the offset vector to all objects being rendered.
		
		Array<WorldObject> objects = new Array<>();
		objects.addAll(getOverlappingTiles(renderSpace)); // tiles first
		
		Array<Entity> entities = getOverlappingEntities(renderSpace); // entities second
		entities.sort((e1, e2) -> {
			if(e1 instanceof Particle && !(e2 instanceof Particle))
				return 1;
			if(!(e1 instanceof Particle) && e2 instanceof Particle)
				return -1;
			return Float.compare(e2.getCenter().y, e1.getCenter().y);
		});
		objects.addAll(entities);
		
		for(WorldObject obj: objects)
			obj.render(batch, delta, posOffset);
	}
	
	@SuppressWarnings("unchecked")
	private void applyTileUpdates() {
		Entry<ClientTile, TileData>[] tilesToUpdate;
		synchronized (tileUpdates) {
			tilesToUpdate = tileUpdates.entrySet().toArray((Entry<ClientTile, TileData>[]) new Entry[tileUpdates.size()]);
			tileUpdates.clear();
		}
		for(Entry<ClientTile, TileData> entry: tilesToUpdate) {
			entry.getValue().apply(entry.getKey());
		}
	}
	
	public void serverUpdate(ClientTile tile, TileData data) {
		synchronized (tileUpdates) {
			tileUpdates.put(tile, data);
		}
	}
	
	@Override
	ClientTile createTile(int x, int y, TileType[] types, String[] data) { return new ClientTile(this, x, y, types, data); }
	
	@Override public ClientTile getTile(float x, float y) {
		ClientTile tile = (ClientTile) super.getTile(x, y);
		if(tile == null)
			loadChunk(Chunk.getCoords(x, y));
		
		return tile;
	}
	
	@Override
	void loadChunk(Point chunkCoord) {
		//System.out.println("Client requesting chunk "+chunkCoord);
		ClientCore.getClient().send(new ChunkRequest(chunkCoord));
	}
	
	@Override
	void unloadChunk(Point chunkCoord) {
		Chunk chunk = getLoadedChunk(chunkCoord);
		if(chunk == null) return; // already unloaded
		
		//System.out.println("Client unloading chunk "+chunkCoord);
		
		for(Entity e: entityChunks.keySet().toArray(new Entity[entityChunks.size()]))
			if(entityChunks.get(e).equals(chunkCoord))
				e.remove();
		
		loadedChunks.access(chunks -> chunks.remove(chunkCoord));
	}
	
	
}
