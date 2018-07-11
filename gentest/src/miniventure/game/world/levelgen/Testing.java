package miniventure.game.world.levelgen;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

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
	
	public static void main(String[] args) {
		/*
			Testing abilities:
				- view an infinite, scrollable map (zoomable? later)
				
				- customize all noise functions that go into it
					- name the functions
					- seed
					- noise coords per pixel
					- curve count
				
				- specify how to interpret the functions to generate the terrain
					- you can say that a value in one function means you look at another function for the value
					- you start by mapping the values of one function to tiles
						- consists of a list of intervals in the form of floats
							- non-editable intervals for 0 and 1
							- a text field; if you enter a value not in the range of the values next to it, it will turn red, and you will not be able to regen the map until you fix it.
						- a tiletype (or noise function) for each range in between
							- drop-down menu, top is blank; leaving it blank you can't regen the map.
							- radio button for tile type, or other function. can't select yourself, or make a loop
				
				- world size selection
				- world seed selection (noise functions can also optionally have custom seeds)
				- "regen" button
				
			
			to start, you have no noise functions, and your base selector has only one blank entry, between 0 and 1.
			
			
			The various UI components:
				- Grid screen:
					- displays the level map.
					- is separate tab?
				
				- value selector:
					- a bar that goes from 0 to 1, colored according to tiletype color of each section
					- consists of left half of window, horizontally
					- underneath the bar is a row consisting of the following, left to right:
						- bar region selector: drop down that you can also type in if you want (the number and/or name of the tiletype/noise function); displays numerical list of all regions, with tile type (or noise function) next to it in the drop down
						- once bar region has been selected, radio buttons show for tiletype or other function
							- if you select tiletype, dropdown appears (to right) of all the tiletypes
							- if you select noise function, dropdown appears to select function, or, at bottom, create new function
						- as said, third region is the tiletype/noise function dropdown.
				
				- alternate value selector:
					- same bar
					- under bar is same as above, but all regions are shown, one region per row
				
				- noise function customizer
					- is another tab
					- has list of all functions in alphabetical order. Fields for name, seed, curves, and pixels per noise coord
						- for seed, also has a checkbox for "custom"; if not checked, seed field is not editable, and is populated with the seed generated from the global seed.
				
				- noise mapper customizer
					- a noise mapper maps values from a noise function to either a tiletype, or another noise function
				
				
				
				tabs will have red text if there's an error preventing you from regening; pink if error exists but is not used in generation.
				
				you start with a master noise mapper, that gets values from a master noise function.
				- from there, you can specify what to do with each region you create in the master mapper.
				- only noise functions and maps you reference from the master will be checked for correctness before allowing you to regen the map.
				
				
			
		 */
		
		JFrame frame = new JFrame("Level Generation Playground");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		frame.add(new TestPanel());
		frame.pack();
		frame.setVisible(true);
		
		/*Coherent2DNoiseFunction noise = new Coherent2DNoiseFunction(new Random().nextLong(), 100);
		float max = 0, min = 0, avg = 0;
		final int xr = 500, yr = 500;
		for(int x = 0; x < xr; x++) {
			for(int y = 0; y < yr; y++) {
				float val = noise.getValue(x, y);
				//System.out.println("value at coord " + x + ',' + y + ": " + val);
				if(x == 0 && y == 0) {
					max = val;
					min = val;
				} else {
					max = Math.max(max, val);
					min = Math.min(min, val);
				}
				avg += val;
			}
		}
		avg /= xr * yr;
		System.out.println("min val = "+min);
		System.out.println("max val = "+max);
		System.out.println("avg val = "+avg);*/
	}
	
	public static Color blendColors(Color... colors) {
		if(colors.length == 0) return Color.BLACK;
		
		int r=0, g=0, b=0, a=0;
		int count = 0;
		for(Color c: colors) {
			if(c == null) continue;
			count++;
			r += c.getRed();
			g += c.getGreen();
			b += c.getBlue();
			a += c.getAlpha();
		}
		if(count == 0) return Color.BLACK;
		r/=count;
		g/=count;
		b/=count;
		a/=count;
		return new Color(r, g, b, a);
	}
	public static Color invertColor(Color c) {
		return new Color((c.getRed()+128)%255, (c.getGreen()+128)%255, (c.getBlue()+128)%255);
	}
}
