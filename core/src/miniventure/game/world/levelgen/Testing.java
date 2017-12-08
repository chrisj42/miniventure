package miniventure.game.world.levelgen;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

import miniventure.game.world.tile.TileType;

class Testing {
	private static final HashMap<TileType, Color> tileMap = new HashMap<>();
	static {
		tileMap.put(TileType.WATER, Color.BLUE);
		tileMap.put(TileType.TREE, Color.GREEN.darker().darker());
		tileMap.put(TileType.GRASS, Color.GREEN);
		tileMap.put(TileType.ROCK, Color.GRAY);
		tileMap.put(TileType.SAND, Color.YELLOW);
		tileMap.put(TileType.DIRT, Color.ORANGE.darker().darker());
	}
	
	private static void displayLevelVisually(int width, int height, int scale) {
		displayLevelVisually(width, height, scale, new Random().nextLong());
	}
	private static void displayLevelVisually(int width, int height, int scale, long seed) {
		TileType[][] tiles = LevelGenerator.generateLevel(seed, width, height);
		
		Color[][] colors = new Color[width][height];
		for(int x = 0; x < tiles.length; x++)
			for(int y = 0; y < tiles[x].length; y++)
				colors[x][y] = tileMap.get(tiles[x][y]);
		
		displayMap(width, height, colors, scale);
	}
	
	private static void displayNoiseGrayscale(int width, int height, float[][] noise, int scale) {
		Color[][] colors = new Color[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				int col = LevelGenerator.getIndex(256, noise[x][y]);
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
		
		JOptionPane.showMessageDialog(null, viewPanel, "Level", JOptionPane.PLAIN_MESSAGE);
	}
	
	
	public static void main(String[] args) {
		while(true) //testTerrainGen(256, 256, 2);
		displayLevelVisually(20, 16, 32);
	}
	
	private static void testTerrainGen(int width, int height, int scale) { testTerrainGen(width, height, scale, new Random().nextLong()); }
	private static void testTerrainGen(int width, int height, int scale, long seed) {
		displayNoiseGrayscale(width, height, LevelGenerator.generateTerrain(seed, width, height,
			new int[] {32, 16, 2},
			new int[] {16, 8, 4, 2, 1}
		), scale);
	}
}
