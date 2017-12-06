package miniventure.game.world;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import miniventure.game.world.tile.TileType;

public class LevelGenerator {
	
	enum Biome {
		OCEAN(TileType.WATER),
		
		FOREST(
			new int[] {1},
			new int[] {1},
			TileType.TREE,
			TileType.GRASS,
			TileType.GRASS,
			TileType.GRASS,
			TileType.GRASS,
			TileType.TREE,
			TileType.GRASS,
			TileType.TREE,
			TileType.GRASS,
			TileType.GRASS
		),
		
		DESERT(TileType.SAND),
		
		PLAINS(
			TileType.GRASS // maybe flowers later
		),
		
		MOUNTAIN(
			TileType.HOLE
		);
		
		public final TileType[] tiles;
		private final int[] samplePeriods;
		private final int[] postSmoothing;
		
		Biome(TileType... tiles) { this(null, null, tiles); }
		Biome(int[] samplePeriods, int[] postSmoothing, TileType... tiles) {
			this.tiles = tiles;
			this.samplePeriods = samplePeriods;
			this.postSmoothing = postSmoothing;
		}
		
		public float[] generateTerrain(long seed, int width, int height) {
			if(samplePeriods == null && postSmoothing == null)
				return LevelGenerator.generateTerrain(seed, width, height);
			if(samplePeriods == null)
				return LevelGenerator.generateTerrain(seed, width, height, postSmoothing);
			
			return LevelGenerator.generateTerrain(seed, width, height, samplePeriods, postSmoothing);
		}
		
		public static final Biome[] values = Biome.values();
	}
	
	static TileType[][] generateLevel(int width, int height) {
		return generateLevel(new Random().nextLong(), width, height);
	}
	static TileType[][] generateLevel(long seed, int width, int height) {
		Random seedPicker = new Random(seed);
		long biomeSeed = seedPicker.nextLong();
		
		EnumMap<Biome, float[]> biomeTerrain = new EnumMap<>(Biome.class);
		for(Biome b: Biome.values)
			biomeTerrain.put(b, b.generateTerrain(seedPicker.nextLong(), width, height));
		
		float[] biomeData = generateTerrain(biomeSeed, width, height);
		
		TileType[][] tiles = new TileType[width][height];
		
		int[][] biomeAreas = new int[width][height];
		int[][] biomes = new int[width][height];
		
		for(int i = 0; i < biomeData.length; i++)
			biomes[i/height][i%height] = getIndex(Biome.values.length, biomeData[i]);
		
		for(int i = 0; i < biomeAreas.length; i++)
			Arrays.fill(biomeAreas[i], -1);
		
		int curArea = -1;
		Stack<Point> inArea = new Stack<>();
		Stack<Point> diffArea = new Stack<>();
		diffArea.push(new Point(0, 0));
		
		while(diffArea.size() > 0) {
			Point newArea = diffArea.pop(); // get a point in the next area.
			while(diffArea.size() > 0 && biomeAreas[newArea.x][newArea.y] != -1) // insure that the point is still considered new (could have been put multiple times in this stack).
				newArea = diffArea.pop();
			
			if(biomeAreas[newArea.x][newArea.y] != -1)
				continue; // can't find any unset tiles anywhere, we are done.
			
			curArea++; // set the area number of the new area.
			
			inArea.push(newArea); // add the tile to the new area.
			
			while(inArea.size() > 0) {
				Point next = inArea.pop(); // get the next tile in the current area.
				biomeAreas[next.x][next.y] = curArea; // set this tile to the current area
				checkAreaTiles(biomeAreas, biomes, next.x, next.y, inArea, diffArea); // check surrounding tiles, and add them to appropriate stacks. 
			}
		}
		
		System.out.println("biomes made: " + (curArea+1));
		// biome areas are set, now assign a biome to each number
		Biome[] areaBiomes = new Biome[curArea+1];
		for(int i = 0; i < areaBiomes.length; i++)
			areaBiomes[i] = Biome.values[seedPicker.nextInt(Biome.values.length)];
		
		// biomes are set, now assign tile types based on biome
		for(int x = 0; x < tiles.length; x++) {
			for(int y = 0; y < tiles[x].length; y++) {
				Biome biome = areaBiomes[biomeAreas[x][y]];
				//Biome biome = Biome.FOREST;
				tiles[x][y] = biome.tiles[getIndex(biome.tiles.length, biomeTerrain.get(biome)[x*height + y])];
				//tiles[x][y] = Biome.values[biomes[x][y]].tiles[0];
			}
		}
		
		// make sure all water bordered by not-water is sand, a beach.
		for(int x = 0; x < tiles.length; x++) {
			for(int y = 0; y < tiles[x].length; y++) {
				if (tiles[x][y] == TileType.WATER) {
					if (x > 0 && tiles[x-1][y] != TileType.WATER)
						tiles[x-1][y] = TileType.SAND;
					if (y > 0 && tiles[x][y-1] != TileType.WATER)
						tiles[x][y-1] = TileType.SAND;
					if (x < width-1 && tiles[x+1][y] != TileType.WATER)
						tiles[x+1][y] = TileType.SAND;
					if (y < height-1 && tiles[x][y+1] != TileType.WATER)
						tiles[x][y+1] = TileType.SAND;
				}
			}
		}
		
		return tiles;
	}
	
	private static void checkAreaTiles(int[][] biomeAreas, int[][] biomes, int x, int y, Stack<Point> inArea, Stack<Point> diffArea) {
		// it is a given that the current tile is already marked
		
		for(int xi = Math.max(0, x-1); xi <= Math.min(x+1, biomeAreas.length-1); xi++) {
			for(int yi = Math.max(0, y-1); yi <= Math.min(y+1, biomeAreas[xi].length-1); yi++) {
				//if(xi == x && yi == y) continue;
				if(biomeAreas[xi][yi] == -1) {
					Point p = new Point(xi, yi);
					if(biomes[xi][yi] == biomes[x][y])
						inArea.push(p); // add it to the list of tiles in this area.
					else
						diffArea.push(p); // add it to the list of tiles outside this area.
				}
			}
		}
	}
	
	private static float[] generateTerrain(long seed, int width, int height) {
		return generateTerrain(seed, width, height, 20, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
	}
	private static float[] generateTerrain(long seed, int width, int height, int... postSmoothing) {
		return generateTerrain(seed, width, height, new int[] {1, 2, 4, 8, 16, 32, 64}, postSmoothing);
	}
	private static float[] generateTerrain(long seed, int width, int height, int[] samplePeriods, int[] postSmoothing) {
		float[] noise = Noise.getWhiteNoise(seed, width*height);
		float[][] noises = Noise.smoothNoise2D(noise, width, height, samplePeriods);
		float[] smoothNoise = Noise.addNoiseWeighted(noises);
		//smoothNoise = Noise.smoothNoise2DProgressive(smoothNoise, width, height, 50, 40, 30, 20, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
		smoothNoise = Noise.smoothNoise2DProgressive(smoothNoise, width, height, postSmoothing);
		
		/*for(int i = 0; i < smoothNoise.length; i++) {
			int x = i/height;
			int y = i % height;
			float xd = Math.abs(x/(width-1.0f)*2 - 1);
			float yd = Math.abs(y/(height-1.0f)*2 - 1);
			double dist = Math.max(xd, yd);
			dist = Math.pow(Math.pow(Math.pow(dist, 4), 4), 4);
			//dist = Math.pow(dist, 4);
			//System.out.println("noise before dist: " + smoothNoise[i]);
			smoothNoise[i] = Noise.map(smoothNoise[i] - (float)dist, -1, 1, 0, 1);
			//System.out.println("after: " + smoothNoise[i]);
		}*/
		
		return Noise.map(smoothNoise, 0, 1);
	}
	
	private static int getIndex(int arrayLength, float val) {
		return Math.min(arrayLength-1, (int) (val*arrayLength));
	}
	
	private static final HashMap<TileType, Color> tileMap = new HashMap<>();
	static {
		tileMap.put(TileType.WATER, Color.BLUE);
		tileMap.put(TileType.TREE, Color.GREEN.darker().darker());
		tileMap.put(TileType.GRASS, Color.GREEN);
		tileMap.put(TileType.HOLE, Color.GRAY);
		tileMap.put(TileType.SAND, Color.YELLOW);
		tileMap.put(TileType.DIRT, Color.ORANGE.darker().darker());
	}
	
	private static void displayLevelVisually(int width, int height, int scale) {
		displayLevelVisually(width, height, scale, new Random().nextLong());
	}
	private static void displayLevelVisually(int width, int height, int scale, long seed) {
		BufferedImage image = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		
		TileType[][] tiles = generateLevel(seed, width, height);
		
		for(int x = 0; x < tiles.length; x++) {
			for(int y = 0; y < tiles[x].length; y++) {
				//float val = noise[i]; // should be a value between 0 and 1
				g.setColor(tileMap.get(tiles[x][y]));
				g.fillRect(x * scale, y * scale, scale, scale);
			}
		}
		
		JPanel viewPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() { return new Dimension(image.getWidth(), image.getHeight()); }
			@Override
			protected void paintComponent(Graphics g) { g.drawImage(image, 0, 0, null); }
		};
		
		JOptionPane.showMessageDialog(null, viewPanel, "Level", JOptionPane.PLAIN_MESSAGE);
	}
	
	
	public static void main(String[] args) {
		while(true) displayLevelVisually(256, 256, 2);
	}
}
