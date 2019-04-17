package miniventure.game.world.management;

import miniventure.game.world.file.LevelCache;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.level.ServerLevel;

import org.jetbrains.annotations.NotNull;

public class LevelInitializer extends Thread {
	
	@NotNull
	private final ServerWorld world;
	
	// removals happen in the server update thread, but this thread will save them
	
	LevelInitializer(@NotNull ServerWorld world) {
		super("LevelInitializer");
		this.world = world;
	}
	
	void requestLevel(ServerPlayer player, int levelId) {
		
	}
	
	void unloadLevel(ServerLevel level) {
		
	}
	
	@Override
	public void run() {
		
	}
	
	
	private class LevelRequest {
		final ServerPlayer player;
		final LevelCache cache;
		
		LevelRequest(ServerPlayer player, LevelCache cache) {
			this.player = player;
			this.cache = cache;
		}
	}
}
