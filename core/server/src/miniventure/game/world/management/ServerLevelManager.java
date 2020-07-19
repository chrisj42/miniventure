package miniventure.game.world.management;

import java.nio.file.Path;
import java.util.*;

import miniventure.game.util.MyUtils;
import miniventure.game.util.SyncObj;
import miniventure.game.util.Version;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.file.*;
import miniventure.game.world.level.LevelFetcher;
import miniventure.game.world.level.LevelId;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.worldgen.island.IslandType;

import org.jetbrains.annotations.NotNull;

public class ServerLevelManager implements LevelFetcher<ServerLevel> {
	
	private static final ServerLevel[] EMPTY_LEVEL_ARRAY = new ServerLevel[0];
	
	@NotNull
	private final ServerWorld world;
	
	public final Path worldPath;
	// public final long worldSeed;
	// private final RandomAccessFile lockRef;
	
	private final IslandDataManager[] islandManagers;
	private final SyncObj<Map<LevelId, ServerLevel>> loadedLevels;
	
	ServerLevelManager(@NotNull ServerWorld world, Path worldPath, IslandDataManager[] islandManagers) {
		this.world = world;
		this.worldPath = worldPath;
		// this.worldSeed = data.seed;
		this.islandManagers = islandManagers;
		
		loadedLevels = new SyncObj<>(Collections.synchronizedMap(new HashMap<>()));
	}
	
	IslandDataManager[] getIslandManagers() { return islandManagers; }
	
	public boolean isLevelLoaded(LevelId levelId) {
		return loadedLevels.get(map -> map.containsKey(levelId));
	}
	
	public ServerLevel getLoadedLevel(LevelId levelId) {
		return loadedLevels.get(map -> map.get(levelId));
	}
	
	public ServerLevel[] getLoadedLevels() {
		return loadedLevels.get(map -> map.values().toArray(EMPTY_LEVEL_ARRAY));
	}
	
	public void addLevel(ServerLevel level, ServerPlayer activator) {
		loadedLevels.act(map -> {
			map.put(level.getLevelId(), level);
			world.setEntityLevel(activator, level);
		});
		MyUtils.debug("level "+level.getLevelId()+" is now loaded.");
	}
	
	public void removeLevel(LevelId levelId) {
		loadedLevels.act(map -> map.remove(levelId));
		MyUtils.debug("removed level "+levelId);
	}
	
	@Override
	public ServerLevel fetchLevel(LevelId levelId) {
		final boolean surface = levelId.isSurface();
		final IslandDataManager island = islandManagers[levelId.getIslandId()];
		
		if(island.isGenerated(surface))
			// already generated
			return loadLevel(levelId, Version.CURRENT); // assumed that all levels are up to date
		else
			// need to generate
			return generateLevel(island, surface);
	}
	
	ServerLevel generateLevel(IslandDataManager island, boolean surface) {
		final LevelId levelId = LevelId.getId(island.getIslandId(), surface);
		final IslandType islandType = island.getIslandType();
		final String mapType = surface ? "surface" : "underground";
		
		MyUtils.debug("Server generating "+islandType+' '+mapType+" map for level "+levelId);
		
		ServerLevel level = new ServerLevel(world, levelId,
				islandType.generateLevel(island.getSeed(), levelId.isSurface())
		);
		island.onGenerate(surface);
		return level;
	}
	
	ServerLevel loadLevel(LevelId levelId, @NotNull Version dataVersion) {
		MyUtils.debug("Server loading level "+levelId+" from data");
		try {
			LevelDataSet levelData = WorldFileInterface.loadLevel(worldPath, levelId, dataVersion);
			ServerLevel level = new ServerLevel(world, levelId, levelData.tileData);
			
			for(String e: levelData.entityData)
				level.addEntity(ServerEntity.deserialize(world, e, levelData.dataVersion));
			
			MyUtils.debug("Server finished loading level "+levelId+" (including entities)");
			return level;
		} catch(WorldFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
}
