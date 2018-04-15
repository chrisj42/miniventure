package miniventure.game.world;

import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.Tile.TileData;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Chunk implements Boundable {
	
	public static final int SIZE = 20;
	
	@NotNull private final Level level;
	@NotNull private Tile[][] tiles;
	public final int width, height;
	public final int chunkX, chunkY;
	
	public Chunk(int chunkX, int chunkY, @NotNull Level level, @NotNull TileType[][][] tileTypes) {
		this.level = level;
		tiles = new Tile[tileTypes.length][];
		width = tiles.length;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		int height = 0;
		for(int x = 0; x < tiles.length; x++) {
			tiles[x] = new Tile[tileTypes[x].length];
			height = Math.max(height, tiles[x].length);
			for(int y = 0; y < tiles[x].length; y++)
				tiles[x][y] = level.createTile(chunkX*SIZE + x, chunkY*SIZE + y, tileTypes[x][y], TileType.getJoinedInitialData(tileTypes[x][y]));
		}
		this.height = height;
	}
	public Chunk(@NotNull Level level, @NotNull ChunkData data) {
		this.level = level;
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
				
				tiles[x][y] = level.createTile(chunkX * SIZE + x, chunkY * SIZE + y, types, tileData.data);
			}
		}
		this.height = height;
	}
	
	@Nullable
	Tile getTile(int x, int y) {
		if(x < 0 || y < 0 || x >= tiles.length || y >= tiles[x].length)
			return null;
		return tiles[x][y];
	}
	
	@NotNull
	Tile[][] getTiles() { return tiles; }
	
	@NotNull @Override
	public Level getLevel() { return level; }
	
	@NotNull @Override
	public Rectangle getBounds() { return new Rectangle(chunkX*SIZE, chunkY*SIZE, width, height); }
	
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Chunk)) return false;
		Chunk o = (Chunk) other;
		return chunkX == o.chunkX && chunkY == o.chunkY && level.equals(o.level);
	}
	
	@Override
	public int hashCode() { return 31 * chunkX + 17 * chunkY; }
	
	@Override
	public String toString() { return "Chunk("+chunkX+","+chunkY+")"; }
	
	
	public static Point getCoords(Point tileCoords) { return getCoords(tileCoords.x, tileCoords.y); }
	public static Point getCoords(Vector2 pos) { return getCoords(pos.x, pos.y); }
	public static Point getCoords(float x, float y) { return new Point(getCoord(x), getCoord(y)); }
	public static int getCoord(float pos) {
		int worldCoord = MathUtils.floor(pos);
		return worldCoord / SIZE;
	}
	
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
