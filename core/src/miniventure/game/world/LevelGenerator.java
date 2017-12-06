package miniventure.game.world;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

import miniventure.game.world.tile.TileType;

public class LevelGenerator {
	
	enum Biome {
		OCEAN(TileType.WATER),
		
		FOREST(
			TileType.GRASS,
			TileType.GRASS,
			TileType.TREE,
			TileType.TREE,
			TileType.TREE,
			TileType.TREE,
			TileType.GRASS,
			TileType.TREE,
			TileType.TREE,
			TileType.GRASS
		),
		
		DESERT(TileType.DIRT),
		
		PLAINS(
			TileType.GRASS // maybe flowers later
		),
		
		MOUNTAIN(
			TileType.HOLE
		);
		
		public final TileType[] tiles;
		
		Biome(TileType... tiles) {
			this.tiles = tiles;
		}
	}
	
	private static final Biome[] biomes = {
		Biome.DESERT,
		Biome.FOREST,
		Biome.DESERT,
		Biome.PLAINS,
		Biome.DESERT,
		Biome.MOUNTAIN,
		Biome.FOREST,
		Biome.PLAINS,
		Biome.FOREST,
		Biome.PLAINS,
		Biome.PLAINS,
		Biome.MOUNTAIN,
		Biome.MOUNTAIN
	};
	
	private static boolean[] ocean = {
		true,
		false,
		false
	};
	
	private static final TileType[] surfaceTiles = {
		TileType.WATER,
		TileType.WATER,
		TileType.WATER,
		TileType.WATER,
		TileType.DIRT,
		TileType.GRASS,
		TileType.GRASS,
		TileType.GRASS,
		TileType.GRASS,
		TileType.HOLE, // represents rock
		TileType.HOLE
	};
	
	private static final boolean[] treeLayout = {
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		true,
		false,
		true,
		false,
		false,
		false,
		true
	};
	
	static TileType[] generateLevel(int width, int height) {
		return generateLevel(new Random().nextLong(), width, height);
	}
	static TileType[] generateLevel(long seed, int width, int height) {
		Random seedPicker = new Random(seed);
		long biomeSeed = seedPicker.nextLong();
		long oceanSeed = seedPicker.nextLong();
		
		float[] biomeData = generateTerrain(biomeSeed, width, height, new int[] {4, 8, 16, 64}, new int[] {50, 40, 30, 10, 5, 1});
		
		float[] rawTerrainValues = generateTerrain(seed, width, height);
		float[] oceanTerrainValues = generateTerrain(oceanSeed, width, height, 50, 40, 30, 20, 10, 5, 1);
		
		TileType[] tiles = new TileType[width*height];
		
		for(int i = 0; i < tiles.length; i++) {
			boolean isOcean = ocean[getIndex(ocean.length, oceanTerrainValues[i])];
			if(isOcean)
				tiles[i] = TileType.WATER;
			else {
				Biome b = biomes[getIndex(biomes.length, biomeData[i])];
				tiles[i] = b.tiles[getIndex(b.tiles.length, rawTerrainValues[i])];
			}
			//boolean placeTree = treeLayout[getIndex(treeLayout.length, treeTerrainValues[i])];
			//if(placeTree && (tiles[i] == TileType.GRASS))
			//	tiles[i] = TileType.TREE;
		}
		
		return tiles;
	}
	
	private static float[] generateTerrain(long seed, int width, int height) {
		return generateTerrain(seed, width, height, 50, 30, 10, 5, 4, 3, 2, 1);
	}
	private static float[] generateTerrain(long seed, int width, int height, int... postSmoothing) {
		return generateTerrain(seed, width, height, new int[] {2, 4, 8, 16, 32}, postSmoothing);
	}
	private static float[] generateTerrain(long seed, int width, int height, int[] initialSmoothing, int[] postSmoothing) {
		float[] noise = Noise.getWhiteNoise(seed, width*height);
		float[][] noises = Noise.smoothNoise2D(noise, width, height, initialSmoothing);
		float[] smoothNoise = Noise.addNoiseWeighted(noises);
		//smoothNoise = Noise.smoothNoise2DProgressive(smoothNoise, width, height, 50, 40, 30, 20, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
		smoothNoise = Noise.smoothNoise2DProgressive(smoothNoise, width, height, postSmoothing);
		
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
		tileMap.put(TileType.DIRT, Color.YELLOW);//Color.ORANGE.darker().darker());
	}
	
	private static void displayLevelVisually(int width, int height, int scale) {
		BufferedImage image = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		
		TileType[] tiles = generateLevel(width, height);
		
		for(int i = 0; i < tiles.length; i++) {
			//float val = noise[i]; // should be a value between 0 and 1
			g.setColor(tileMap.get(tiles[i]));
			g.fillRect((i/height) * scale, (i%height) * scale, scale, scale);
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
		while(true) displayLevelVisually(512, 512, 1);
	}
}
