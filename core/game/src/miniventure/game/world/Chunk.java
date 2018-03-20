package miniventure.game.world;

import miniventure.game.world.tilenew.Tile;
import miniventure.game.world.tilenew.Tile.TileData;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Chunk {
	
	public static final int SIZE = 32;
	
	@NotNull private Tile[][] tiles;
	public final int width, height;
	public final int chunkX, chunkY;
	
	public Chunk(int chunkX, int chunkY, @NotNull Level level, @NotNull TileType[][][] tileTypes) {
		tiles = new Tile[tileTypes.length][];
		width = tiles.length;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		int height = 0;
		for(int x = 0; x < tiles.length; x++) {
			tiles[x] = new Tile[tileTypes[x].length];
			height = Math.max(height, tiles[x].length);
			for(int y = 0; y < tiles[x].length; y++)
				tiles[x][y] = new Tile(level, chunkX*SIZE + x, chunkY*SIZE + y, tileTypes[x][y]);
		}
		this.height = height;
	}
	public Chunk(@NotNull Level level, @NotNull ChunkData data) {
		this.chunkX = data.chunkX;
		this.chunkY = data.chunkY;
		
		width = data.tileData.length;
		this.tiles = new Tile[width][];
		int height = 0;
		for(int x = 0; x < tiles.length; x++) {
			tiles[x] = new Tile[data.tileData[x].length];
			height = Math.max(height, tiles[x].length);
			for(int y = 0; y < tiles[x].length; y++) {
				TileData tileData = data.tileData[x][y];
				TileType[] types = new TileType[tileData.typeOrdinals.length];
				for(int i = 0; i < types.length; i++)
					types[i] = TileType.values[tileData.typeOrdinals[i]];
				
				tiles[x][y] = new Tile(level, chunkX * SIZE + x, chunkY * SIZE + y, types, tileData.data);
			}
		}
		this.height = height;
	}
	
	@Nullable
	public Tile getTile(int x, int y) {
		if(x < 0 || y < 0 || x >= tiles.length || y >= tiles[x].length)
			return null;
		return tiles[x][y];
	}
	
	@NotNull
	Tile[][] getTiles() { return tiles; }
	
	public static int getCoord(float pos) {
		int worldCoord = MathUtils.floor(pos);
		return worldCoord / SIZE;
	}
	
	public Rectangle getBounds() { return new Rectangle(chunkX*SIZE, chunkY*SIZE, width, height); }
	
	
	public static class ChunkData {
		public final int chunkX, chunkY, levelDepth;
		public final TileData[][] tileData;
		
		private ChunkData() { this(0, 0, 0, null); }
		public ChunkData(int chunkX, int chunkY, int depth, TileData[][] data) {
			this.chunkX = chunkX;
			this.chunkY = chunkY;
			this.levelDepth = depth;
			this.tileData = data;
		}
		public ChunkData(Chunk chunk, Level level) { this(chunk, level.getDepth()); }
		public ChunkData(Chunk chunk, int levelDepth) {
			chunkX = chunk.chunkX;
			chunkY = chunk.chunkY;
			this.levelDepth = levelDepth;
			
			Tile[][] tiles = chunk.getTiles();
			tileData = new TileData[tiles.length][];
			for(int i = 0; i < tiles.length; i++) {
				tileData[i] = new TileData[tiles[i].length];
				for(int j = 0; j < tiles[i].length; j++)
					tileData[i][j] = new TileData(tiles[i][j]);
			}
		}
	}
}
