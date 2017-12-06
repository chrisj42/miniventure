package miniventure.game.world;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import miniventure.game.world.tile.TileType;

public class LevelGenerator {
	
	enum Biome {
		OCEAN(TileType.WATER),
		
		FOREST(
			TileType.TREE/*,
			TileType.GRASS,
			TileType.GRASS,
			TileType.GRASS,
			TileType.GRASS,
			TileType.TREE,
			TileType.GRASS,
			TileType.TREE,
			TileType.GRASS,
			TileType.GRASS*/
		),
		
		DESERT(TileType.SAND),
		
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
		
		
		public static final Biome[] values = Biome.values();
	}
	
	/*private static final Biome[] biomes = {
		*//*Biome.FOREST,
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
		Biome.MOUNTAIN*//*
		//Biome.OCEAN,
		//Biome.OCEAN,
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
		TileType.SAND,
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
	};*/
	
	static TileType[] generateLevel(int width, int height) {
		return generateLevel(new Random().nextLong(), width, height);
	}
	static TileType[] generateLevel(long seed, int width, int height) {
		Random seedPicker = new Random(seed);
		long biomeSeed = seedPicker.nextLong();
		long detailSeed = seedPicker.nextLong();
		//long oceanSeed = seedPicker.nextLong();
		
		float[] biomeData = generateTerrain(biomeSeed, width, height);
		
		float[] rawTerrainValues = generateTerrain(detailSeed, width, height);
		//float[] oceanTerrainValues = generateTerrain(oceanSeed, width, height, 50, 40, 30, 20, 10, 5, 1);
		TileType[] tiles = new TileType[width*height];
		int[] biomeSets = new int[width*height];
		Arrays.fill(biomeSets, -1);
		int curBiome = 0;
		
		for(int i = 0; i < biomeSets.length; i++) {
			int x = i / height, y = i % height;
			int biomeIdx = getIndex(Biome.values.length, biomeData[i]);
			if(biomeSets[i] == -1) {
				biomeSets[i] = curBiome;
				curBiome++;
			} else if(y > height-10)
				System.out.println("biome already set for " + x+","+y+": "+biomeIdx+" (area "+biomeSets[i]+")");
			if(y < height-1 && getIndex(Biome.values.length, biomeData[i+1]) == biomeIdx)
				biomeSets[i + 1] = biomeSets[i];
			else if(y < height-1 && y > height-10)
				System.out.println("below tile " + x+","+y+" is a different biome; cur="+biomeIdx+" (area:"+biomeSets[i]+"), other="+getIndex(Biome.values.length, biomeData[i+1])+" (area:"+biomeSets[i+1]+")");
			if(x < width-1 && getIndex(Biome.values.length, biomeData[i+height]) == biomeIdx)
				biomeSets[i + height] = biomeSets[i];
			else if(x < width-1 && y > height-10)
				System.out.println("right of tile " + x+","+y+" is a different biome; cur="+biomeIdx+" (area:"+biomeSets[i]+"), other="+getIndex(Biome.values.length, biomeData[i+height])+" (area:"+biomeSets[i+height]+")");
			/*if(y > 0 && getIndex(Biome.values.length, biomeData[i-1]) == biomeIdx)
				biomeSets[i - 1] = biomeSets[i];
			else if(y > 0 && y > height-10)
				System.out.println("above tile " + x+","+y+" is a different biome; cur="+biomeIdx+" (area:"+biomeSets[i]+"), other="+getIndex(Biome.values.length, biomeData[i-1])+" (area:"+biomeSets[i-1]+")");
			if(x > 0 && getIndex(Biome.values.length, biomeData[i-height]) == biomeIdx)
				biomeSets[i - height] = biomeSets[i];
			else if(x > 0 && y > height-10)
				System.out.println("left of tile " + x+","+y+" is a different biome; cur="+biomeIdx+" (area:"+biomeSets[i]+"), other="+getIndex(Biome.values.length, biomeData[i-height])+" (area:"+biomeSets[i-height]+")");*/
		}
		
		System.out.println("biomes made: " + curBiome);
		
		// biome areas are set, now assign a biome to each number
		Biome[] biomes = new Biome[curBiome];
		for(int i = 0; i < biomes.length; i++) {
			biomes[i] = Biome.values[seedPicker.nextInt(Biome.values.length)];
			System.out.println("area " + i + " = " + biomes[i]);
		}
		
		// biomes are set, now assign tile types based on biome
		for(int i = 0; i < tiles.length; i++) {
			//System.out.println("getting biome " + biomeSets[i]);
			TileType[] btiles = biomes[biomeSets[i]].tiles;
			tiles[i] = btiles[getIndex(btiles.length, rawTerrainValues[i])];
		}
		
		// make sure all water bordered by not-water is sand, a beach.
		/*for(int i = 0; i < tiles.length; i++) {
			if(tiles[i] == TileType.WATER) {
				if(i >= height && tiles[i-height] != TileType.WATER)
					tiles[i-height] = TileType.SAND;
				if(i%height > 0 && tiles[i-1] != TileType.WATER)
					tiles[i-1] = TileType.SAND;
				if(i%height < height-1 && tiles[i+1] != TileType.WATER)
					tiles[i+1] = TileType.SAND;
				if(i < height*(width-1) && tiles[i+height] != TileType.WATER)
					tiles[i+height] = TileType.SAND;
			}
		}*/
		
		return tiles;
	}
	
	private static float[] generateTerrain(long seed, int width, int height) {
		return generateTerrain(seed, width, height, 20, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
	}
	private static float[] generateTerrain(long seed, int width, int height, int... postSmoothing) {
		return generateTerrain(seed, width, height, new int[] {1, 2, 4, 8, 16, 32, 64}, postSmoothing);
	}
	private static float[] generateTerrain(long seed, int width, int height, int[] initialSmoothing, int[] postSmoothing) {
		float[] noise = Noise.getWhiteNoise(seed, width*height);
		float[][] noises = Noise.smoothNoise2D(noise, width, height, initialSmoothing);
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
		
		//return new float[][] {smoothNoise, smoothNoise2};
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
		while(true) displayLevelVisually(256, 256, 2);
	}
}
