package miniventure.game.world.worldgen.noise;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Testing {
	
	/** @noinspection UnnecessaryLocalVariable, unused, RedundantSuppression */
	public static NoiseConfiguration getTerrain() {
		// NoiseConfiguration mountains = new NoiseConfiguration(new Coherent2DNoiseFunction(2, 2))
		// 	.modify(NoiseModifier.combine(new Coherent2DNoiseFunction(50, 8), 1))
		// 	.modify(NoiseModifier.combine(new Coherent2DNoiseFunction(32, 6), 1))
		// 	.modify(NoiseModifier.multiply(NoiseGenerator.ISLAND_MASK, 2))
		// 	.modify(NoiseModifier.FILL_VALUE_RANGE);
		
		// Noise features = new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1}); // good for terrain features..?
		
		NoiseConfiguration terrain = new NoiseConfiguration(new Coherent2DNoiseFunction(36, 3))
			.modify(NoiseModifier.combine(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1}), 1))
			.modify(NoiseModifier.FILL_VALUE_RANGE)
			.modify(NoiseModifier.multiply(NoiseGenerator.islandMask(1)))
			// .modify(NoiseModifier.FILL_VALUE_RANGE)
			// .modify(NoiseModifier.combine(NoiseGenerator.islandMask(1), .25f))
		;
		
		NoiseConfiguration features = new NoiseConfiguration(new Coherent2DNoiseFunction(12, 2))
		// NoiseConfiguration trees = new NoiseConfiguration(features)
			.modify(NoiseModifier.FILL_VALUE_RANGE)
		;
		
		NoiseConfiguration heightMask = new NoiseConfiguration(NoiseGenerator.BLANK)
			.modify(NoiseModifier.multiply(NoiseGenerator.islandMask(2)))
			// .modify(NoiseModifier.FILL_VALUE_RANGE)
		;
		
		return features;
	}
	
	public static void main(String[] args) {
		// final int width = new Integer(args[0]);
		// final int height = new Integer(args[1]);
		// final int scale = new Integer(args[2]);
		final int width = 300;
		final int height = 300;
		final int scale = 2;
		
		// float[] thresholds = getThresholds(32);
		float[] thresholds = {.05f, .95f};
		Color[] colors = {Color.GRAY, Color.GREEN, Color.GREEN.darker()};
		
		Random rand = new Random();
		boolean repeat = true;
		while(repeat)
			// repeat = displayNoise(width, height, scale, getTerrain().get2DNoise(rand.nextLong(), width, height), thresholds, false);
			repeat = displayNoise(width, height, scale, getTerrain().get2DNoise(rand.nextLong(), width, height), thresholds, colors);
	}
	
	/** @noinspection SameParameterValue*/
	private static boolean displayNoise(int width, int height, int scale, float[][] noise, float[] thresholds, boolean color) {
		int sectionCount = thresholds.length+1;
		float sectionSize = (color?360f:255f) / sectionCount;
		
		Color[] colors = new Color[sectionCount];
		
		// int col = color?360:255;
		for(int i = 0; i < colors.length; i++) {
			int col = (int) (i*sectionSize);
			colors[i] = color ? Color.getHSBColor((1-col/360f)+.6f, 1, 1) : new Color(col, col, col);
		}
		
		return displayNoise(width, height, scale, noise, thresholds, colors);
	}
	
	private static boolean displayNoise(int width, int height, int scale, float[][] noise, float[] thresholds, Color[] thresholdColors) {
		
		Color[][] colors = new Color[width][height];
		final Color maxColor = thresholdColors[thresholdColors.length-1];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				float val = noise[x][y];
				Color color = maxColor;
				for(int i = 0; i < thresholds.length; i++) {
					if(val < thresholds[i]) {
						color = thresholdColors[i];
						break;
					}
				}
				colors[x][y] = color;
			}
		}
		
		return displayMap(width, height, scale, colors);
	}
	
	public static boolean displayMap(int width, int height, int scale, Color[][] colors) {
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
	
	private static float[] getThresholds(int colorCount) {
		float[] thresholds = new float[colorCount-1];
		for(int i = 0; i < thresholds.length; i++)
			thresholds[i] = (i+1)*1f/thresholds.length;
		return thresholds;
	}
	
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
}
