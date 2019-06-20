package miniventure.game.world.file;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import miniventure.game.GameCore;
import miniventure.game.network.GameProtocol.IslandReference;
import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.world.Point;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.LevelFetcher;
import miniventure.game.world.tile.TileStack.TileData;
import miniventure.game.world.worldgen.island.IslandType;

public class LevelCache {
	
	/*
		There are two competing purposes for this class:
			- info sent to client about positioning and type of island
			- info used by server to generate a new island / load an existing one
		
		So, the server needs this type info... but also some extra, like seed, and entity/tile data.
		Client needs levelid, location, and levelConfig (which is the island type)
	 */
	
	public final IslandReference island;
	
	// gen parameter
	private final long seed;
	
	// load parameters
	private Version dataVersion;
	private String[] entityData;
	private TileData[][] tileData;
	
	LevelCache(int levelId, Point location, long seed, IslandType islandType) {
		this.island = new IslandReference(levelId, location, islandType);
		this.seed = seed;
		dataVersion = GameCore.VERSION;
	}
	
	LevelCache(Version dataVersion, LinkedList<String> fileData) {
		//noinspection MismatchedQueryAndUpdateOfCollection
		SerialHashMap map = new SerialHashMap(fileData.pop());
		int id = Integer.parseInt(map.get("id"));
		int x = Integer.parseInt(map.get("x"));
		int y = Integer.parseInt(map.get("y"));
		String islandName = map.get("island");
		this.island = new IslandReference(id, new Point(x, y), IslandType.valueOf(islandName));
		seed = Long.parseLong(map.get("seed"));
		int ec = Integer.parseInt(map.get("ec"));
		int width = Integer.parseInt(map.get("w"));
		int height = Integer.parseInt(map.get("h"));
		
		if(ec < 0) {
			entityData = null;
			tileData = null;
			return;
		}
		
		this.entityData = new String[ec];
		this.tileData = new TileData[width][height];
		
		for(int i = 0; i < ec; i++)
			entityData[i] = fileData.pop();
		
		// TODO here is where I can check for compressed tiles
		for(int xp = 0; xp < width; xp++)
			for(int yp = 0; yp < height; yp++)
				tileData[xp][yp] = new TileData(dataVersion, fileData.pop());
	}
	
	List<String> save() { return save(new LinkedList<>()); }
	List<String> save(List<String> data) {
		SerialHashMap map = new SerialHashMap();
		map.add("id", island.levelId);
		map.add("x", island.location.x);
		map.add("y", island.location.y);
		map.add("island", island.type.name());
		map.add("seed", seed);
		map.add("ec", entityData == null ? -1 : entityData.length);
		map.add("w", tileData == null ? 0 : tileData.length);
		map.add("h", tileData == null ? 0 : tileData[0].length);
		data.add(map.serialize());
		
		if(entityData != null)
			data.addAll(Arrays.asList(entityData));
		
		if(tileData != null)
			for(TileData[] col: tileData)
				for(TileData td: col)
					data.add(td.serialize());
		
		return data;
	}
	
	public void updateData(String[] entityData, TileData[][] tileData) {
		this.entityData = entityData;
		this.tileData = tileData;
		dataVersion = GameCore.VERSION;
	}
	
	public boolean generated() { return tileData != null; }
	
	// the server is the only one that calls this, since it's the only one that uses this system.
	public Level getLevel(LevelFetcher fetcher) {
		if(generated()) // should basically always be true
			return fetcher.loadLevel(dataVersion, island.levelId, tileData, entityData);
		
		return fetcher.makeLevel(island.levelId, island.type);
	}
}
