package miniventure.game.world.levelgen.noise;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

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
		
		NoiseConfiguration mountains = new NoiseConfiguration(new Coherent2DNoiseFunction(2, 2))
			.modify(NoiseModifier.combine(new Coherent2DNoiseFunction(50, 8), 1))
			.modify(NoiseModifier.combine(new Coherent2DNoiseFunction(32, 6), 1))
			.modify(NoiseModifier.combine(NoiseGenerator.ISLAND_MASK, 2));
		
		NoiseConfiguration volcano = new NoiseConfiguration(new Coherent2DNoiseFunction(30, 2))
			.modify(NoiseModifier.combine(NoiseGenerator.ISLAND_MASK, 1));
		
		NoiseConfiguration starter = new NoiseConfiguration(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1}))
			.modify(NoiseModifier.FILL_VALUE_RANGE)
			.modify(NoiseModifier.multiply(NoiseGenerator.ISLAND_MASK, 2))
			.modify(NoiseModifier.FILL_VALUE_RANGE);
		
		
		// display end result
		final boolean isCount = true;
		// final boolean isCount = Boolean.parseBoolean(args[3]);
		final float[] thresholds;
		//noinspection ConstantConditions
		if(isCount) {
			final int count = 12;
			// final int count = new Integer(args[4]);
			thresholds = new float[count-1];
			for(int i = 0; i < thresholds.length; i++)
				thresholds[i] = (i+1)*1f/thresholds.length;
		}
		else thresholds = new float[] {.2f};
		// else thresholds = parseFloats(args[4]);
		
		Random rand = new Random();
		
		boolean more = true;
		while(more) more = displayNoise(width, height, scale, starter.get2DNoise(rand.nextLong(), width, height), thresholds, false);
	}
	
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
