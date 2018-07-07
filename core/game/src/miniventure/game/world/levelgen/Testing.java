package miniventure.game.world.levelgen;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

import miniventure.game.world.tile.TileType.TileTypeEnum;

class Testing {
	private static final HashMap<TileTypeEnum, Color> tileMap = new HashMap<>();
	static {
		tileMap.put(TileTypeEnum.WATER, Color.BLUE);
		tileMap.put(TileTypeEnum.TREE_CARTOON, Color.GREEN.darker().darker());
		tileMap.put(TileTypeEnum.CACTUS, Color.GREEN.darker());
		tileMap.put(TileTypeEnum.GRASS, Color.GREEN);
		tileMap.put(TileTypeEnum.STONE, Color.GRAY);
		tileMap.put(TileTypeEnum.SAND, Color.YELLOW);
		//tileMap.put(TileTypeEnum.DIRT, Color.ORANGE.darker().darker());
	}
	
	private static void displayLevelVisually(int width, int height, int scale) {
		displayLevelVisually(width, height, scale, new Random().nextLong());
	}
	private static void displayLevelVisually(int width, int height, int scale, long seed) {
		LevelGenerator gen = new LevelGenerator(seed, 0, 0, 32, 6);
		TileTypeEnum[][][] tiles = gen.generateTiles(0, 0, width, height);
		
		Color[][] colors = new Color[width][height];
		for(int x = 0; x < tiles.length; x++)
			for(int y = 0; y < tiles[x].length; y++)
				colors[x][y] = tileMap.get(tiles[x][y][0]);
		
		displayMap(width, height, colors, scale);
	}
	
	private static void displayNoiseGrayscale(int width, int height, float[][] noise, int scale) {
		Color[][] colors = new Color[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				int col = (int) (noise[x][y] * 255);
				colors[x][y] = new Color(col, col, col);
			}
		}
		
		displayMap(width, height, colors, scale);
	}
	
	private static void displayMap(int width, int height, Color[][] colors, int scale) {
		BufferedImage image = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		
		for(int x = 0; x < colors.length; x++) {
			for(int y = 0; y < colors[x].length; y++) {
				g.setColor(colors[x][y]);
				g.fillRect(x * scale, y * scale, scale, scale);
			}
		}
		
		JPanel viewPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() { return new Dimension(image.getWidth(), image.getHeight()); }
			@Override
			protected void paintComponent(Graphics g) { g.drawImage(image, 0, 0, null); }
		};
		
		JOptionPane.showMessageDialog(null, viewPanel, "Noise", JOptionPane.PLAIN_MESSAGE);
	}
	
	
	public static void main(String[] args) {
		while(true) //testTerrainGen(128, 64, 8, 16, 2);
		displayLevelVisually(128, 64, 8);
	}
	
	/** @noinspection SameParameterValue*/
	private static void testTerrainGen(int width, int height, int scale, int noiseCoordsPerPixel, int numCurves) { testTerrainGen(width, height, scale, new Random().nextLong(), noiseCoordsPerPixel, numCurves); }
	private static void testTerrainGen(int width, int height, int scale, long seed, int noiseCoordsPerPixel, int numCurves) {
		float[][] values = new float[width][height];
		Coherent2DNoiseFunction noise = new Coherent2DNoiseFunction(seed, noiseCoordsPerPixel, numCurves);
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				values[x][y] = noise.getValue(x, y);
		
		displayNoiseGrayscale(width, height, values, scale);
	}
}
