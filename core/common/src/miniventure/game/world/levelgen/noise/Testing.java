package miniventure.game.world.levelgen.noise;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import miniventure.game.world.levelgen.noise.NoiseConfig.WeightFetcher;

public class Testing {
	
	private static int[] parseInts(String str) {
		String[] strs = str.split(",");
		int[] nums = new int[strs.length];
		for(int i = 0; i < nums.length; i++)
			nums[i] = new Integer(strs[i]);
		return nums;
	}
	private static float[] parseFloats(String str) {
		String[] strs = str.split(",");
		float[] nums = new float[strs.length];
		for(int i = 0; i < nums.length; i++)
			nums[i] = new Float(strs[i]);
		return nums;
	}
	
	public static void main(String[] args) {
		final int width = 150;
		// final int width = new Integer(args[0]);
		final int height = 150;
		// final int height = new Integer(args[1]);
		final int scale = 6;
		// final int scale = new Integer(args[2]);
		
		// final int[] samplePeriods = parseInts(args[3]);
		// final int[] postSmoothing = parseInts(args[4]);
		
		final float maxDist = (float) Math.hypot(width/2f, height/2f);
		WeightFetcher islandMask = (value, x, y) -> {
			float xd = Math.abs(x-width/2f);
			float yd = Math.abs(y-height/2f);
			float dist = (float) Math.hypot(xd, yd);
			float trans = 1 - dist/maxDist;
			return trans;// == 0 ? 0 : (float) Math.pow(trans, 3);
		};
		
		// make height map
		NoiseGenerator noise = new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1});
		NoiseGenerator func = new Coherent2DNoiseFunction(23, 2);
		NoiseGenerator func2 = new Coherent2DNoiseFunction(12, 20);
		NoiseGenerator func3 = new Coherent2DNoiseFunction(50, 8);
		NoiseGenerator func4 = new Coherent2DNoiseFunction(32, 6);
		
		NoiseConfig mountains = new NoiseConfig(width, height, new Coherent2DNoiseFunction(23, 2), true, true);
		mountains.multiply(new Coherent2DNoiseFunction(50, 8));
		mountains.multiply(new Coherent2DNoiseFunction(32, 6));
		mountains.multiply(islandMask);
		mountains.multiply(islandMask);
		
		
		NoiseConfig volcano = new NoiseConfig(width, height, new Coherent2DNoiseFunction(30, 2), true, true);
		// plains.multiply(new Coherent2DNoiseFunction(50, 8));
		// plains.multiply(new Coherent2DNoiseFunction(32, 6));
		// plains.multiply(islandMask);
		volcano.multiply(islandMask);
		
		NoiseConfig starter = new NoiseConfig(width, height, noise, true, true);
		starter.multiply(islandMask);
		starter.multiply(islandMask);
		
		NoiseConfiguration config = new NoiseConfiguration(
			new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1}),
			NoiseModifier.FILL_VALUE_RANGE,
			NoiseModifier.ISLAND_MASK,
			NoiseModifier.FILL_VALUE_RANGE
		);
		
		// NoiseConfig noiseMap = new NoiseConfig(width, height, func, true, false);
		// noiseMap.multiply((WeightFetcher) (value, x, y) -> 1/(float)Math.sqrt(value));
		
		// heightMap.multiply(func2);
		
		// multiply height map by island mask
		// heightMap.multiply(noiseMap);
		// heightMap.multiply(islandMask);
		// heightMap.multiply(islandMask);
		// heightMap.multiply(islandMask);
		// heightMap.multiply((value, x, y) -> value);\
		// heightMap.multiply(func);
		
		// make river map
		// NoiseMap riverMap = new NoiseMap(width, height, new int[] {2,4,8,16,32}, new int[] {32,16,8,4,3,2,1}, true);
		// multiply values to increase separation
		// riverMap.multiply((value, x, y) -> value);
		
		
		// display end result
		final boolean isCount = true;
		// final boolean isCount = Boolean.parseBoolean(args[5]);
		final float[] thresholds;
		//noinspection ConstantConditions
		if(isCount) {
			final int count = 12;
			// final int count = new Integer(args[6]);
			thresholds = new float[count-1];
			for(int i = 0; i < thresholds.length; i++)
				thresholds[i] = (i+1)*1f/thresholds.length;
		}
		else thresholds = new float[] {.2f};
		// else thresholds = parseFloats(args[6]);
		
		Random rand = new Random();
		
		boolean more = true;
		while(more) more = displayNoise(width, height, scale, config.get2DNoise(rand.nextLong(), width, height), thresholds, false);
	}
	
	/*private static float[][] addRivers(int count, float[][] noise) {
		Random rand = new Random();
		for(int i = 0; i < count; i++) {
			int sx, sy;
			boolean sides = rand.nextBoolean();
			int width = noise.length;
			int height = noise[0].length;
			if(sides) {
				sy = rand.nextInt(height);
				sx = rand.nextBoolean() ? 0 : width-1;
			}
			else {
				sx = rand.nextInt(width);
				sy = rand.nextBoolean() ? 0 : height-1;
			}
			
			int favX = sides ? sx == 0 ? 1 : -1 : Math.abs(sx-width/2)<=width/4?0:(int)Math.copySign(1, sx-width/2f);
			int favY = sides ? Math.abs(sy-height/2)<=height/4?0:(int)Math.copySign(1, sy-height/2f) : sy == 0 ? 1 : -1;
			
			addRiver(noise, sx, sy, favX, favY);
		}
		return noise;
	}
	
	private static void addRiver(float[][] noise, int x, int y, int favX, int favY) {
		float val = noise[x][y];
		
		// float leftWeight = x == 0 ? 0 : 
	}*/
	
	private static boolean displayNoise(int width, int height, int scale, float[][] noise, float[] thresholds, boolean color) {
		
		int sectionCount = thresholds.length+1;
		float sectionSize = (color?360f:255f) / sectionCount;
		
		Color[][] colors = new Color[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				float val = noise[x][y];
				int col = color?360:255;
				for(int i = 0; i < thresholds.length; i++) {
					if(val < thresholds[i]) {
						col = (int) (i*sectionSize);
						break;
					}
				}
				//int col = (int) (noise[x][y] * 255);
				colors[x][y] = color ? Color.getHSBColor((1-col/360f)+.6f, 1, 1) : new Color(col, col, col);
			}
		}
		
		return displayMap(width, height, scale, colors);
	}
	
	private static boolean displayMap(int width, int height, int scale, Color[][] colors) {
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
		
		return JOptionPane.showConfirmDialog(null, viewPanel, "Noise", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
	}
}
