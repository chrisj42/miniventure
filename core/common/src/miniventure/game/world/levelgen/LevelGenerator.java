package miniventure.game.world.levelgen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;

import miniventure.game.world.tile.TileTypeEnum;

import static miniventure.game.world.tile.TileTypeEnum.*;

public class LevelGenerator {
	
	// TO-DO I don't really know why, but things get messed up when you go really far out, and have big numbers for your coordinates, even though it's nowhere near the number limit. I want to fix this, but I have no idea how, and I have a feeling that it is going to be more complicated than I think, and I really don't want to deal with it. So, for now, I'm just going to use a considerably smaller value, in the range of 10,000s. It's plenty big, honestly, so it'll just have to do for now until whenever I decide to try and figure out the issue.
	// public static final int MAX_WORLD_SIZE = (int) (Math.sqrt(Math.min(Integer.MAX_VALUE, Float.MAX_VALUE)));
	
	private final NoiseMapper noiseMapper;
	public final int levelWidth, levelHeight;
	private final int noiseOffX = 0, noiseOffY = 0; // todo bring back for validation when new algo is made? unless algo doesn't need it...
	
	public LevelGenerator(long seed) { this(seed, 0, 0); }
	public LevelGenerator(long seed, int width, int height) { this(seed, width, height, DEFAULT_MAPPER()); }
	// public LevelGenerator(long seed, int width, int height, boolean ensureMuchLand) { this(seed, width, height, DEFAULT_MAPPER(), ensureMuchLand); }
	public LevelGenerator(long seed, int width, int height, NoiseMapper mapper) {// this(seed, width, height, mapper, false); }
	// public LevelGenerator(long seed, int width, int height, NoiseMapper mapper, boolean ensureMuchLand) { this(seed, width, height, mapper, true, ensureMuchLand); }
	// public LevelGenerator(long seed, int width, int height, NoiseMapper mapper, boolean ensureLand, boolean ensureMuchLand) {
		this.noiseMapper = mapper;
		
		// if(width < 0 || width > MAX_WORLD_SIZE || height < 0 || height > MAX_WORLD_SIZE)
		// 	throw new IllegalArgumentException("illegal world size; dimensions of world must be non-negative and not greater than " + MAX_WORLD_SIZE + "; given dimensions: " + width+","+height);
		
		// if(width == 0) width = MAX_WORLD_SIZE;
		// if(height == 0) height = MAX_WORLD_SIZE;
		
		levelWidth = width;
		levelHeight = height;
		
		noiseMapper.setSeedRecursively(new Random(seed));
		
		/*if(!ensureLand) {
			noiseOffX = 0;
			noiseOffY = 0;
			return;
		}
		
		boolean valid = false;
		// Rectangle area = new Rectangle();
		// getSpawnArea(area);
		
		if(!ensureMuchLand) {
			int noiseOff = 0;
			while(!valid) {
				// try and find land
				for(int xo = 0; xo < levelWidth; xo++) {
					for(int yo = 0; yo < levelHeight; yo++) {
						if(noiseMapper.getTileType(xo + noiseOff, yo + noiseOff) != TileTypeEnum.WATER) {
							valid = true;
							break;
						}
					}
					if(valid) break;
				}
				if(!valid)
					noiseOff += 2;
			}
			
			noiseOffX = noiseOff;
			noiseOffY = noiseOff;
		}
		else {
			int noiseOffX = 0;
			int noiseOffY = 0;
			
			while(!valid) {
				// try and find land
				int landCountUL = 0;
				int landCountUR = 0;
				int landCountBL = 0;
				int landCountBR = 0;
				int landCountTotal = 0;
				for(int xo = 0; xo < levelWidth; xo++) {
					for(int yo = 0; yo < levelHeight; yo++) {
						if(noiseMapper.getTileType(xo + noiseOffX, yo + noiseOffY) != TileTypeEnum.WATER) {
							landCountTotal++;
							if(xo < area.width/2) {
								if(yo < area.height/2)
									landCountBL++;
								else
									landCountUL++;
							} else {
								if(yo < area.height/2)
									landCountBR++;
								else
									landCountUR++;
							}
							break;
						}
					}
				}
				
				if(landCountTotal < area.width * area.height / 100) {
					boolean moved = false;
					if(landCountBL + landCountBR < landCountUL + landCountUR) {
						// go up
						noiseOffY += area.height / 2;
						moved = true;
					}
					if(landCountBL + landCountUL < landCountBR + landCountUR) {
						// go right
						noiseOffX += area.width / 2;
						moved = true;
					}
					
					if(!moved && landCountTotal < area.width * area.height / 100) {
						noiseOffX += area.width * Math.random();
						noiseOffY += area.height * Math.random();
					}
				}
				else valid = true;
			}
			
			this.noiseOffX = noiseOffX;
			this.noiseOffY = noiseOffY;
		}*/
	}
	
	/*public Rectangle getSpawnArea(Rectangle rect) {
		int width = getDim(levelWidth);
		int height = getDim(levelHeight);
		return rect.set((levelWidth -width)/2f, (levelHeight -height)/2f, width, height);
	}*/
	/*private int getDim(int worldDim) {
		// the idea here is to go from the spawn area being the whole map at 100 tiles across or less, to the center 1000 tiles after 4000, which is a quarter of the width at that point.
		float div = MyUtils.mapFloat(MyUtils.clamp(worldDim, 100, worldDim), 100, Math.max(worldDim, 4000), 1, Math.max(4, worldDim/1000));
		return (int) (worldDim/div);
	}*/
	
	/*public TileTypeEnum[][][] generateChunk(final int x, final int y) {
		int width = Chunk.SIZE, height = Chunk.SIZE;
		if(x * width + width >= worldWidth)
			width = worldWidth - x*width;
		if(y * height + height >= worldHeight)
			height = worldHeight - y*height;
		
		if(width <= 0 || height <= 0)
			throw new IndexOutOfBoundsException("Requested chunk is outside world bounds; chunkX="+x+", chunkY="+y+". Max x requested: "+((x+1)*Chunk.SIZE-1)+", max y requested: "+((y+1)*Chunk.SIZE-1)+". Actual world size: ("+worldWidth+","+worldHeight+")");
		
		return generateTiles(x, y, width, height);
	}*/
	
	public TileTypeEnum[][][] generateTiles() {
		TileTypeEnum[][][] tiles = new TileTypeEnum[levelWidth][levelHeight][];
		// int xt = x * Chunk.SIZE;
		// int yt = y * Chunk.SIZE;
		
		for(int x = 0; x < tiles.length; x++) {
			for(int y = 0; y < tiles[x].length; y++) {
				// int xp = xt+xo, yp = yt+yo;
				tiles[x][y] = generateTile(x, y);
			}
		}
		
		return tiles;
	}
	
	public TileTypeEnum getTileType(int x, int y) {
		if(x < 0 || y < 0 || x >= levelWidth || y >= levelHeight)
			throw new IllegalArgumentException("Requested tile is outside world bounds; x="+x+", y="+y+". Actual world size: ("+levelWidth+','+levelHeight+')');
		
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
		
		return types.toArray(new TileTypeEnum[0]);
	}
	
	private static final EnumMap<TileTypeEnum, TileTypeEnum> underTiles = new EnumMap<TileTypeEnum, TileTypeEnum>(TileTypeEnum.class) {{
		put(WATER, HOLE);
		put(SAND, DIRT);
		put(CACTUS, SAND);
		put(CARTOON_TREE, GRASS);
		put(PINE_TREE, GRASS);
		put(DARK_TREE, DIRT);
		put(POOF_TREE, GRASS);
		put(DIRT, HOLE);
		put(GRASS, DIRT);
		put(STONE, DIRT);
		put(RUBY_ORE, DIRT);
		put(TUNGSTEN_ORE, DIRT);
		put(IRON_ORE, DIRT);
		put(COAL_ORE, DIRT);
		put(FLINT, GRASS);
		put(SNOW, GRASS);
		// put(TORCH, DIRT);
	}};
	
	private static NoiseMapper DEFAULT_MAPPER() {
		
		NamedNoiseFunction continentNoise = new NamedNoiseFunction("Continent Noise", 500, 0);
		NamedNoiseFunction landNoise = new NamedNoiseFunction("Land Noise", 300, 3);
		NamedNoiseFunction biomeNoise = new NamedNoiseFunction("Biome Noise", 120, 2);
		NamedNoiseFunction detailNoise = new NamedNoiseFunction("Detail Noise", 12, 2);
		NamedNoiseFunction islandNoise = new NamedNoiseFunction("Island Noise", 24, 2);
		NamedNoiseFunction cactusNoise = new NamedNoiseFunction("Cactus Noise", 4, 2);
		
		
		NoiseMapper plainsBiome = new NoiseMapper("Plains", detailNoise)
			.addRegion(FLINT, .03f)
			.addRegion(GRASS, 8)
			.addRegion(CARTOON_TREE, 1)
			;
		
		NoiseMapper desertBiome = new NoiseMapper("Desert", cactusNoise)
			.addRegion(CACTUS, 1)
			.addRegion(SAND, 10)
			;
		
		NoiseMapper snowBiome = new NoiseMapper("Snow", cactusNoise)
			.addRegion(SNOW, 2)
			.addRegion(PINE_TREE, .5f)
			.addRegion(SNOW, 8)
			.addRegion(DARK_TREE, .5f)
			;
		
		NoiseMapper ores = new GroupNoiseMapper("Ores", detailNoise)
			.addRegion(STONE, .3f)
			.addRegion(IRON_ORE, 1f)
			.addRegion(COAL_ORE, 2f)
			// .addRegion(FLINT, 1f)
			.addRegion(TUNGSTEN_ORE, .75f)
			.addRegion(RUBY_ORE, .5f)
			;
		
		NoiseMapper canyonBiome = new NoiseMapper("Canyons", detailNoise)
			.addRegion(STONE, 8)
			.addRegion(ores, 1)
			.addRegion(STONE, 8)
			.addRegion(ores, 1)
			.addRegion(STONE, 8)
			.addRegion(ores, 1)
			.addRegion(STONE, 8)
			.addRegion(FLINT, 1)
			.addRegion(GRASS, 10)
			.addRegion(PINE_TREE, 1)
			.addRegion(GRASS, 10)
			.addRegion(STONE, 8)
			.addRegion(ores, 1)
			.addRegion(STONE, 8)
			.addRegion(ores, 1)
			.addRegion(STONE, 8)
			.addRegion(ores, 1)
			.addRegion(STONE, 8)
			;
		
		NoiseMapper mountainBiome = new NoiseMapper("Mountains", biomeNoise)
			.addRegion(canyonBiome, 1.5f)
			.addRegion(STONE, .5f)
			.addRegion(ores, .5f)
			.addRegion(STONE, .5f)
			.addRegion(canyonBiome, 4f)
			.addRegion(ores, .25f)
			.addRegion(STONE, .5f)
			.addRegion(ores, .5f)
			.addRegion(STONE, .5f)
			.addRegion(canyonBiome, 1.5f)
			.addRegion(snowBiome, 1)
			;
		
		
		NoiseMapper oceanBiome = new NoiseMapper("Ocean", biomeNoise)
			.addRegion(WATER, 1)
			;
		
		NoiseMapper deepOceanBiome = new NoiseMapper("Deep Ocean", islandNoise)
			.addRegion(WATER, 10)
			.addRegion(SAND, 1)
			;
		
		
		
		NoiseMapper landTerrain = new NoiseMapper("Land", landNoise)
			.addRegion(desertBiome, 4)
			.addRegion(plainsBiome, 8)
			.addRegion(mountainBiome, 6)
			;
		
		NoiseMapper marshBiome = new GroupNoiseMapper("Marsh", detailNoise)
			.addRegion(DIRT, .1f)
			.addRegion(WATER, .1f)
			.addRegion(DARK_TREE, .1f)
			.addRegion(GRASS, .1f)
			;
		
		NoiseMapper border = new NoiseMapper("Beach Type", continentNoise)
			.addRegion(SAND, 1)
			.addRegion(marshBiome, 1)
			;
		
		NoiseMapper biomeCategories = new NoiseMapper("Surface Type", continentNoise)
			.addRegion(oceanBiome, 0.5f)
			.addRegion(border, 0.05f)
			.addRegion(landTerrain, 1f)
			.addRegion(border, 0.05f)
			.addRegion(oceanBiome, .35f)
			.addRegion(deepOceanBiome, 1f)
			;
		
		//noinspection UnnecessaryLocalVariable
		NoiseMapper rivers = new NoiseMapper("Rivers", biomeNoise)
			.addRegion(biomeCategories, 1)
			.addRegion(WATER, .07f)
			.addRegion(biomeCategories, 1.1f)
			;
		
		return rivers;
	}
	
	private static NoiseMapper ISLAND_MAPPER() {
		NamedNoiseFunction continentNoise = new NamedNoiseFunction("Continent Noise", 500, 2);
		NamedNoiseFunction biomeNoise = new NamedNoiseFunction("Biome Noise", 120, 2);
		NamedNoiseFunction detailNoise = new NamedNoiseFunction("Detail Noise", 12, 2);
		NamedNoiseFunction islandNoise = new NamedNoiseFunction("Island Noise", 24, 2);
		NamedNoiseFunction cactusNoise = new NamedNoiseFunction("Cactus Noise", 4, 2);
		
		
		NoiseMapper plainsBiome = new NoiseMapper("Plains", detailNoise)
			.addRegion(GRASS, 4)
			// .addRegion(WATER, 1f)
			.addRegion(GRASS, 3)
			.addRegion(CARTOON_TREE, 1)
			;
		
		NoiseMapper desertBiome = new NoiseMapper("Desert", cactusNoise)
			.addRegion(CACTUS, 1)
			.addRegion(SAND, 10)
			.addRegion(CARTOON_TREE, .3f)
			;
		
		NoiseMapper canyonBiome = new NoiseMapper("Canyons", detailNoise)
			.addRegion(STONE, 20)
			.addRegion(GRASS, 10)
			.addRegion(PINE_TREE, 1)
			.addRegion(GRASS, 10)
			.addRegion(STONE, 20)
			;
		
		
		NoiseMapper mountainBiome = new NoiseMapper("Mountains", biomeNoise)
			.addRegion(STONE, 1)
			.addRegion(canyonBiome, 4.5f)
			.addRegion(STONE, 1)
			;
		
		NoiseMapper snowBiome = new GroupNoiseMapper("Snow", detailNoise)
			.addRegion(SNOW, .6f)
			.addRegion(PINE_TREE, .1f)
			// .addRegion(SNOW, 1)
			.addRegion(GRASS, .1f)
			// .addRegion(TREE_, .25f)
			// .addRegion(GRASS, .25f)
			// .addRegion(WATER, .1f)
			// .addRegion(SNOW, 1)
			.addRegion(DARK_TREE, .1f)
			// .addRegion(SNOW, 1)
			;
		
		/*NoiseMapper volcanoBiome = new NoiseMapper("Volcano", continentNoise)
			.addRegion(SAND, 1)
			.addRegion(DIRT, 1)
			.addRegion(STONE, 3)
			.addRegion(TORCH, .5f)
			.addRegion(STONE, 3)
			.addRegion(DIRT, 1)
			.addRegion(SAND, 1);
		*/
		
		NoiseMapper marshBiome = new GroupNoiseMapper("Marsh", detailNoise)
			.addRegion(DIRT, .1f)
			.addRegion(WATER, .1f)
			.addRegion(DARK_TREE, .1f)
			.addRegion(GRASS, .1f)
			// .addRegion(DIRT, 1)
			// .addRegion(WATER, .75f)
			// .addRegion(GRASS, .4f)
			// .addRegion(DARK_TREE, .3f)
			// .addRegion(DIRT, 1)
			// .addRegion(WATER, .75f)
			// .addRegion(GRASS, .4f)
			// .addRegion(DARK_TREE, .3f)
			// .addRegion(WATER, .75f)
			;
		
		NoiseMapper oceanBiome = new NoiseMapper("Ocean", biomeNoise)
			.addRegion(WATER, 1)
			;
		
		NoiseMapper deepOceanBiome = new NoiseMapper("Deep Ocean", islandNoise)
			.addRegion(WATER, 10)
			.addRegion(SAND, 1)
			;
		
		
		/*NoiseMapper biomeCategories = new NoiseMapper("Surface Type", continentNoise)
			.addRegion(oceanBiome, 0.5f)
			.addRegion(SAND, 0.05f)
			.addRegion(landTerrain, 0.8f)
			.addRegion(SAND, 0.05f)
			.addRegion(oceanBiome, 2.5f);
		*/
		
		//noinspection UnnecessaryLocalVariable
		NoiseMapper terrain = new GroupNoiseMapper("Terrain", continentNoise)
			.addRegion(oceanBiome, .05f) // this one is special, it's the filler
			.addRegion(deepOceanBiome, .2f)
			.addRegion(desertBiome, .1f)
			.addRegion(plainsBiome, .1f)
			.addRegion(mountainBiome, .1f)
			.addRegion(snowBiome, .1f)
			.addRegion(marshBiome, .1f)
			;
		
		return terrain;
	}
}
