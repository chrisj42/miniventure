package miniventure.game.world;

import miniventure.game.GameProtocol.ChunkRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;
import miniventure.game.world.entity.Entity;

import org.jetbrains.annotations.NotNull;

public class ClientLevel extends Level {
	
	@NotNull private ClientWorld world;
	
	public ClientLevel(@NotNull ClientWorld world, int depth, int width, int height) {
		super(world, depth, width, height);
		this.world = world;
	}
	
	@Override @NotNull
	public ClientWorld getWorld() { return world; }
	
	@Override
	void loadChunk(Point chunkCoord) { ClientCore.getClient().send(new ChunkRequest(chunkCoord)); }
	
	@Override
	void unloadChunk(Point chunkCoord) {
		Chunk chunk = loadedChunks.get(chunkCoord);
		if(chunk == null) return; // already unloaded
		
		for(Entity e: entityChunks.keySet().toArray(new Entity[entityChunks.size()]))
			if(entityChunks.get(e).equals(chunk))
				e.remove();
		
		loadedChunks.remove(chunkCoord);
	}
}
