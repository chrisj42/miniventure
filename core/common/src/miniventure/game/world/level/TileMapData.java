package miniventure.game.world.level;

import java.util.LinkedList;
import java.util.List;

import miniventure.game.util.Version;
import miniventure.game.world.tile.TileData;

public class TileMapData {
	
	public final int width;
	public final int height;
	private final TileData[][] tileData;
	
	public TileMapData(int width, int height, TileData[][] tileData) {
		this.width = width;
		this.height = height;
		this.tileData = tileData;
	}
	public TileMapData(int width, int height, Version dataVersion, LinkedList<String> data) {
		this.width = width;
		this.height = height;
		tileData = new TileData[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tileData[x][y] = new TileData(dataVersion, data.pop());
			}
		}
	}
	
	public void serialize(List<String> list) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				list.add(tileData[x][y].serialize());
			}
		}
	}
	
	public TileData getTileData(int x, int y) {
		return tileData[x][y];
	}
}
