package miniventure.game.world.levelgen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;

import miniventure.game.util.MyUtils;
import miniventure.game.world.Chunk;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import com.badlogic.gdx.math.Rectangle;

import static miniventure.game.world.tile.TileType.TileTypeEnum.*;

public class LevelGenerator {
	
	// TO-DO I don't really know why, but things get messed up when you go really far out, and have big numbers for your coordinates, even though it's nowhere near the number limit. I want to fix this, but I have no idea how, and I have a feeling that it is going to be more complicated than I think, and I really don't want to deal with it. So, for now, I'm just going to use a considerably smaller value, in the range of 10,000s. It's plenty big, honestly, so it'll just have to do for now until whenever I decide to try and figure out the issue.
	public static final int MAX_WORLD_SIZE = (int) (Math.sqrt(Math.min(Integer.MAX_VALUE, Float.MAX_VALUE)));
	
	private final NoiseMapper noiseMapper;
	public final int worldWidth, worldHeight;
	private final int noiseOffX, noiseOffY;
	
	public LevelGenerator(long seed) { this(seed, 0, 0); }
	public LevelGenerator(long seed, int width, int height) { this(seed, width, height, DEFAULT_MAPPER()); }
	public LevelGenerator(long seed, int width, int height, NoiseMapper mapper) { this(seed, width, height, mapper, true); }
	public LevelGenerator(long seed, int width, int height, NoiseMapper mapper, boolean ensureLand) {
		this.noiseMapper = mapper;
		
		if(width < 0 || width > MAX_WORLD_SIZE || height < 0 || height > MAX_WORLD_SIZE)
			throw new IllegalArgumentException("illegal world size; dimensions of world must be non-negative and not greater than " + MAX_WORLD_SIZE + "; given dimensions: " + width+","+height);
		
		if(width == 0) width = MAX_WORLD_SIZE;
		if(height == 0) height = MAX_WORLD_SIZE;
		
		worldWidth = width;
		worldHeight = height;
		
		noiseMapper.setSeedRecursively(new Random(seed));
		
		if(!ensureLand) {
			noiseOffX = 0;
			noiseOffY = 0;
			return;
		}
		
		boolean valid = false;
		Rectangle area = new Rectangle();
		getSpawnArea(area);
		int noiseOff = 0;
		while(!valid) {
			// try and find land
			for(int xo = 0; xo < area.width; xo++) {
				for(int yo = 0; yo < area.height; yo++) {
					if(noiseMapper.getTileType((int)area.x+xo+noiseOff, (int)area.y+yo) != TileTypeEnum.WATER) {
						valid = true;
						break;
					}
				}
				if(valid) break;
			}
			if(!valid)
				noiseOff += area.width;
		}
		
		this.noiseOffX = noiseOff;
		noiseOffY = 0;
	}
	
	public Rectangle getSpawnArea(Rectangle rect) {
		int width = getDim(worldWidth);
		int height = getDim(worldHeight);
		return rect.set((worldWidth-width)/2, (worldHeight-height)/2, width, height);
	}
	private int getDim(int worldDim) {
		// the idea here is to go from the spawn area being the whole map at 100 tiles across or less, to the center 1000 tiles after 4000, which is a quarter of the width at that point.
		float div = MyUtils.mapFloat(MyUtils.clamp(worldDim, 100, worldDim), 100, Math.max(worldDim, 4000), 1, Math.max(4, worldDim/1000));
		return (int) (worldDim/div);
	}
	
	public TileTypeEnum[][][] generateChunk(final int x, final int y) {
		int width = Chunk.SIZE, height = Chunk.SIZE;
		if(x * width + width >= worldWidth)
			width = worldWidth - x*width;
		if(y * height + height >= worldHeight)
			height = worldHeight - y*height;
		
		if(width <= 0 || height <= 0)
			throw new IndexOutOfBoundsException("Requested chunk is outside world bounds; chunkX="+x+", chunkY="+y+". Max x requested: "+((x+1)*Chunk.SIZE-1)+", max y requested: "+((y+1)*Chunk.SIZE-1)+". Actual world size: ("+worldWidth+","+worldHeight+")");
		
		return generateTiles(x, y, width, height);
	}
	
	public TileTypeEnum[][][] generateTiles(int x, int y, int width, int height) {
		TileTypeEnum[][][] tiles = new TileTypeEnum[width][height][];
		int xt = x * Chunk.SIZE;
		int yt = y * Chunk.SIZE;
		
		for(int xo = 0; xo < tiles.length; xo++) {
			for(int yo = 0; yo < tiles[xo].length; yo++) {
				int xp = xt+xo, yp = yt+yo;
				tiles[xo][yo] = generateTile(xp, yp);
			}
		}
		
		return tiles;
	}
	
	public TileTypeEnum getTileType(int x, int y) {
		if(x < 0 || y < 0 || x >= worldWidth || y >= worldHeight)
			throw new IllegalArgumentException("Requested tile is outside world bounds; x="+x+", y="+y+". Actual world size: ("+worldWidth+","+worldHeight+")");
		
		return noiseMapper.getTileType(x+noiseOffX, y+noiseOffY);
	}
	public TileTypeEnum[] generateTile(int x, int y) {
		return getTile(getTileType(x, y));
	}
	
	private TileTypeEnum[] getTile(TileTypeEnum curType) {
		ArrayList<TileTypeEnum> types = new ArrayList<>();
		while(curType != null) {
			types.add(0, curType);
			curType = underTiles.get(curType);
		}
		
		return types.toArray(new TileTypeEnum[types.size()]);
	}
	
	private static final EnumMap<TileTypeEnum, TileTypeEnum> underTiles = new EnumMap<TileTypeEnum, TileTypeEnum>(TileTypeEnum.class) {{
		put(WATER, HOLE);
		put(SAND, DIRT);
		put(CACTUS, SAND);
		put(TREE_CARTOON, GRASS);
		put(TREE_PINE, GRASS);
		put(TREE_DARK, GRASS);
		put(TREE_POOF, GRASS);
		put(DIRT, HOLE);
		put(GRASS, DIRT);
		put(STONE, DIRT);
		put(TORCH, DIRT);
	}};
	
	private static NoiseMapper DEFAULT_MAPPER() {
		
		NamedNoiseFunction continentNoise = new NamedNoiseFunction("Continent Noise", 500, 0);
		NamedNoiseFunction landNoise = new NamedNoiseFunction("Land Noise", 300, 3);
		NamedNoiseFunction biomeNoise = new NamedNoiseFunction("Biome Noise", 120, 2);
		NamedNoiseFunction detailNoise = new NamedNoiseFunction("Detail Noise", 12, 2);
		NamedNoiseFunction islandNoise = new NamedNoiseFunction("Island Noise", 24, 2);
		NamedNoiseFunction cactusNoise = new NamedNoiseFunction("Cactus Noise", 4, 2);
		
		
		NoiseMapper plainsBiome = new NoiseMapper("Plains", detailNoise)
			.addRegion(GRASS, 8)
			.addRegion(TREE_CARTOON, 1);
		
		NoiseMapper desertBiome = new NoiseMapper("Desert", cactusNoise)
			.addRegion(CACTUS, 1)
			.addRegion(SAND, 10);
		
		NoiseMapper canyonBiome = new NoiseMapper("Canyons", detailNoise)
			.addRegion(STONE, 20)
			.addRegion(GRASS, 10)
			.addRegion(TREE_PINE, 1)
			.addRegion(GRASS, 10)
			.addRegion(STONE, 20);
		
		
		NoiseMapper mountainBiome = new NoiseMapper("Mountains", biomeNoise)
			.addRegion(STONE, 1)
			.addRegion(canyonBiome, 4.5f)
			.addRegion(STONE, 1);
		
		
		NoiseMapper oceanBiome = new NoiseMapper("Ocean", biomeNoise)
			.addRegion(WATER, 1);
		
		NoiseMapper deepOceanBiome = new NoiseMapper("Deep Ocean", islandNoise)
			.addRegion(WATER, 10)
			.addRegion(SAND, 1);
		
		
		
		NoiseMapper landTerrain = new NoiseMapper("Land", landNoise)
			.addRegion(desertBiome, 4)
			.addRegion(plainsBiome, 8)
			.addRegion(mountainBiome, 6);
		
		
		NoiseMapper biomeCategories = new NoiseMapper("Surface Type", continentNoise)
			.addRegion(oceanBiome, 0.5f)
			.addRegion(SAND, 0.05f)
			.addRegion(landTerrain, 1f)
			.addRegion(SAND, 0.05f)
			.addRegion(oceanBiome, .35f)
			.addRegion(deepOceanBiome, 1f);
		
		//noinspection UnnecessaryLocalVariable
		NoiseMapper rivers = new NoiseMapper("Rivers", biomeNoise)
			.addRegion(biomeCategories, 1)
			.addRegion(WATER, .07f)
			.addRegion(biomeCategories, 1.1f);
		
		return rivers;
	}
	
	private static NoiseMapper ISLAND_MAPPER() {
		NamedNoiseFunction continentNoise = new NamedNoiseFunction("Continent Noise", 500, 2);
		NamedNoiseFunction biomeNoise = new NamedNoiseFunction("Biome Noise", 120, 2);
		NamedNoiseFunction detailNoise = new NamedNoiseFunction("Detail Noise", 12, 2);
		NamedNoiseFunction islandNoise = new NamedNoiseFunction("Island Noise", 24, 2);
		NamedNoiseFunction cactusNoise = new NamedNoiseFunction("Cactus Noise", 4, 2);
		
		
		NoiseMapper plainsBiome = new NoiseMapper("Plains", detailNoise)
			.addRegion(GRASS, 8)
			.addRegion(TREE_CARTOON, 1);
		
		NoiseMapper desertBiome = new NoiseMapper("Desert", cactusNoise)
			.addRegion(CACTUS, 1)
			.addRegion(SAND, 10);
		
		NoiseMapper canyonBiome = new NoiseMapper("Canyons", detailNoise)
			.addRegion(STONE, 20)
			.addRegion(GRASS, 10)
			.addRegion(TREE_PINE, 1)
			.addRegion(GRASS, 10)
			.addRegion(STONE, 20);
		
		
		NoiseMapper mountainBiome = new NoiseMapper("Mountains", biomeNoise)
			.addRegion(STONE, 1)
			.addRegion(canyonBiome, 4.5f)
			.addRegion(STONE, 1);
		
		NoiseMapper snowBiome = new NoiseMapper("Snow", biomeNoise)
			.addRegion(SNOW, 1)
			.addRegion(GRASS, 1)
			.addRegion(SNOW, 1);
		
		NoiseMapper volcanoBiome = new NoiseMapper("Volcano", continentNoise)
			.addRegion(SAND, 1)
			.addRegion(DIRT, 1)
			.addRegion(STONE, 3)
			.addRegion(TORCH, .5f)
			.addRegion(STONE, 3)
			.addRegion(DIRT, 1)
			.addRegion(SAND, 1);
		
		NoiseMapper marshBiome = new NoiseMapper("Marsh", detailNoise)
			.addRegion(WATER, .75f)
			.addRegion(DIRT, 1)
			.addRegion(TREE_DARK, .3f)
			.addRegion(WATER, .75f);
		
		NoiseMapper oceanBiome = new NoiseMapper("Ocean", islandNoise)
			.addRegion(WATER, 10)
			.addRegion(SAND, 1);
		
		//noinspection UnnecessaryLocalVariable
		NoiseMapper terrain = new GroupNoiseMapper("Terrain", continentNoise)
			.addRegion(oceanBiome, 0.1f)
			.addRegion(desertBiome, .1f)
			.addRegion(plainsBiome, .1f)
			.addRegion(mountainBiome, .1f)
			.addRegion(volcanoBiome, .1f)
			.addRegion(snowBiome, .1f)
			.addRegion(marshBiome, .1f);
		
		return terrain;
	}
}
