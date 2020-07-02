package miniventure.game.world.file;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.LevelFetcher;
import miniventure.game.world.level.TileMapData;
import miniventure.game.world.worldgen.level.IslandType;

import org.jetbrains.annotations.NotNull;

public class LevelCache {
	
	/*
		There are two competing purposes for this class:
			- info sent to client about positioning and type of island
			- info used by server to generate a new island / load an existing one
		
		So, the server needs this type info... but also some extra, like seed, and entity/tile data.
		Client needs levelid, location, and levelConfig (which is the island type)
	 */
	
	// public final IslandReference ref;
	public final IslandCache island;
	private final int id;
	
	private final long seed;
	
	// load parameters
	@NotNull
	private Version dataVersion;
	private String[] entityData;
	private TileMapData tileData;
	
	LevelCache(IslandCache parent, int id, long seed) {
		this.island = parent;
		this.id = id;
		this.seed = seed;
		dataVersion = Version.CURRENT;
	}
	
	LevelCache(IslandCache parent, int id, @NotNull Version dataVersion, LinkedList<String> fileData) {
		this.island = parent;
		this.id = id;
		this.dataVersion = dataVersion;
		
		//noinspection MismatchedQueryAndUpdateOfCollection
		SerialHashMap map = new SerialHashMap(fileData.pop());
		// this.islandType = map.get("island", IslandType::valueOf);
		// this.ref = new IslandReference(id, IslandType.valueOf(islandType));
		seed = map.get("seed", Long::parseLong);
		int ec = map.get("ec", Integer::parseInt);
		int width = map.get("w", Integer::parseInt);
		int height = map.get("h", Integer::parseInt);
		
		if(ec < 0) {
			entityData = null;
			tileData = null;
			return;
		}
		
		this.entityData = new String[ec];
		for(int i = 0; i < ec; i++)
			entityData[i] = fileData.pop();
		
		this.tileData = new TileMapData(width, height, dataVersion, fileData);
		// TODO here is where I can check for compressed tiles
		// for(int xp = 0; xp < width; xp++)
		// 	for(int yp = 0; yp < height; yp++)
		// 		tileData[xp][yp] = new TileData(dataVersion, fileData.pop());
	}
	
	List<String> save() { return save(new LinkedList<>()); }
	List<String> save(List<String> data) {
		SerialHashMap map = new SerialHashMap();
		// map.add("island", island.getId());
		map.add("seed", seed);
		map.add("ec", entityData == null ? -1 : entityData.length);
		map.add("w", tileData == null ? 0 : tileData.width);
		map.add("h", tileData == null ? 0 : tileData.height);
		data.add(map.serialize());
		
		if(entityData != null)
			data.addAll(Arrays.asList(entityData));
		
		if(tileData != null)
			tileData.serialize(data);
		
		return data;
	}
	
	public void updateData(String[] entityData, TileMapData tileData) {
		this.entityData = entityData;
		this.tileData = tileData;
		dataVersion = Version.CURRENT;
	}
	
	public boolean generated() { return tileData != null; }
	
	public IslandType getIslandType() { return island.getIslandType(); }
	
	public int getId() { return id; }
	
	// the server is the only one that calls this, since it's the only one that uses this system.
	public Level getLevel(LevelFetcher<?> fetcher) {
		if(generated()) // should basically always be true
			return fetcher.loadLevel(this, dataVersion, tileData, entityData);
		
		return fetcher.makeLevel(this, seed);
	}
}
