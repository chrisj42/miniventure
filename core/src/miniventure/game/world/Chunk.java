package miniventure.game.world;

import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Chunk {
	
	public static final int SIZE = 32;
	
	@NotNull private Tile[][] tiles;
	public final int width, height;
	
	public Chunk(int chunkX, int chunkY, @NotNull Level level, @NotNull TileType[][] tileTypes) {
		tiles = new Tile[tileTypes.length][];
		width = tiles.length;
		int height = 0;
		for(int x = 0; x < tiles.length; x++) {
			tiles[x] = new Tile[tileTypes[x].length];
			height = Math.max(height, tiles[x].length);
			for(int y = 0; y < tiles[x].length; y++)
				tiles[x][y] = new Tile(level, chunkX*SIZE + x, chunkY*SIZE + y, tileTypes[x][y]);
		}
		this.height = height;
	}
	
	@Nullable
	public Tile getTile(int x, int y) {
		if(x < 0 || y < 0 || x >= tiles.length || y >= tiles[x].length)
			return null;
		return tiles[x][y];
	}
	
	@NotNull Tile[][] getTiles() { return tiles; }
	
	public static int getCoord(float pos) {
		int worldCoord = MathUtils.floor(pos);
		return worldCoord / SIZE;
	}
}
