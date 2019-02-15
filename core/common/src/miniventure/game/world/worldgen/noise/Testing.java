package miniventure.game.world.worldgen.noise;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import miniventure.game.world.worldgen.island.NoiseTileCondition;
import miniventure.game.world.worldgen.island.ProtoIsland;
import miniventure.game.world.worldgen.island.TileGroupMap;
import miniventure.game.world.worldgen.island.TileGroupMap.TileGroup;

import static miniventure.game.world.worldgen.noise.NoiseModifier.*;
import static miniventure.game.world.worldgen.noise.NoiseModifier.NoiseValueMerger.*;
import static miniventure.game.world.worldgen.noise.NoiseGenerator.islandMask;

/** @noinspection SameParameterValue*/
public class Testing {
	
	/** @noinspection UnnecessaryLocalVariable, unused, RedundantSuppression */
	public static NoiseGenerator getTerrain() {
		// NoiseConfiguration mountains = new NoiseConfiguration(new Coherent2DNoiseFunction(2, 2))
		// 	.modify(NoiseModifier.combine(new Coherent2DNoiseFunction(50, 8), 1))
		// 	.modify(NoiseModifier.combine(new Coherent2DNoiseFunction(32, 6), 1))
		// 	.modify(NoiseModifier.multiply(NoiseGenerator.ISLAND_MASK, 2))
		// 	.modify(NoiseModifier.FILL_VALUE_RANGE);
		
		// Noise features = new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1}); // good for terrain features..?
		
		NoiseGenerator heightMask = islandMask(1).modify(
			FILL_VALUE_RANGE,
			forEach((noise, x, y) -> (float) Math.pow(noise, 2)),
			FILL_VALUE_RANGE
		);
		
		NoiseGenerator terrain = new Coherent2DNoiseFunction(36, 3).modify(
			// combine(new Coherent2DNoiseFunction(36, 3).modifySeed(seed -> seed+1), 0.5f)
			// ,combine(new Coherent2DNoiseFunction(36, 3).modifySeed(seed -> seed+2), 0.5f)
			combine(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1}), .5f)
			,FILL_VALUE_RANGE
			,combine(islandMask(1), MULTIPLY)
			,FILL_VALUE_RANGE
			// ,combine(islandMask(2), heightMask)
		);
		
		NoiseGenerator features = new Coherent2DNoiseFunction(12, 2)
			.modify(FILL_VALUE_RANGE);
		
		return (seed, width, height) -> {
			float[][] noise = terrain.get2DNoise(seed, width, height);
			TileGroupMap groupMap = TileGroupMap.process(new NoiseTileCondition(noise, val -> val >= .2f), new ProtoIsland(width, height, seed));
			
			System.out.println("match groups: "+groupMap.matches.size()+", fail groups: "+groupMap.fails.size());
			
			System.out.println("removing excess matches...");
			// remove all but the biggest match group, and any fail group not touching the edge.
			TileGroup biggestMatch = null;
			// cache the set, because the second for loop will modify during iteration
			TileGroup[] matches = groupMap.matches.toArray(new TileGroup[0]);
			for(TileGroup match: matches)
				biggestMatch = biggestMatch == null || biggestMatch.size() < match.size() ? match : biggestMatch;
			
			for(TileGroup match: matches) {
				if(match != biggestMatch)
					match.switchSet(); // switches group to a fail group, modifying related maps and the match/fail sets; group is combined with any adjacent groups.
			}
			
			System.out.println("removing excess fails...");
			// now for the fail groups not touching the edge
			for(TileGroup fail: groupMap.fails.toArray(new TileGroup[0])) {
				if(!fail.touchesEdge())
					fail.switchSet();
			}
			
			System.out.println("after switching:\nmatch groups: "+groupMap.matches.size()+", fail groups: "+groupMap.fails.size());
			
			forEach((val, x, y) -> groupMap.checkMatched(x, y) ? 1 : 0).modify(seed, noise);
			
			return noise;
		};
	}
	
	public static void main(String[] args) {
		// final int width = new Integer(args[0]);
		// final int height = new Integer(args[1]);
		// final int scale = new Integer(args[2]);
		final int width = 300;
		final int height = 300;
		final int scale = 2;
		
		// float[] thresholds = getThresholds(256);
		float[] thresholds = {.2f/*, .5f, .6f, .7f, .8f, .9f*/};
		Color[] colors = getColors(thresholds.length+1, false); // add 1 to threshold count to do black-gray instead of black-white
		// Color[] colors = {Color.BLACK, Color.DARK_GRAY, Color.DARK_GRAY.brighter()};
		
		Random rand = new Random();
		boolean repeat = true;
		while(repeat)
			repeat = displayNoise(width, height, scale, getTerrain().get2DNoise(rand.nextLong(), width, height), thresholds, colors);
	}
	
	private static boolean displayNoise(int width, int height, int scale, float[][] noise, float[] thresholds, Color[] thresholdColors) {
		
		Color[][] colors = new Color[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				final float val = noise[x][y];
				int colorIdx = thresholds.length; // color array is 1 larger than threshold array
				for(int i = 0; i < thresholds.length; i++) {
					if(val < thresholds[i]) {
						colorIdx = i;
						break;
					}
				}
				colors[x][y] = thresholdColors[colorIdx];
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
	
	private static float[] getThresholds(int sectionCount) {
		float[] thresholds = new float[sectionCount-1];
		for(int i = 0; i < thresholds.length; i++)
			thresholds[i] = (i+1)*1f/sectionCount;
		return thresholds;
	}
	
	// colors are equally spaced in color-space regardless of threshold values.
	private static Color[] getColors(int thresholdCount, boolean color) {
		// the thresholds account for the end of each range, but not the beginning. Hence, the number of colors is 1 greater than the number of thresholds.
		int colorCount = thresholdCount + 1;
		// now we need to know how much to change the color by so that the last color is at the top of the range. The number of changes is 1 less than the number of colors.
		float colorDelta = colorCount == 1 ? 0 : (color?1f:255f) / (colorCount - 1);
		// if there are no thresholds, i.e. only one color, then the delta is invalid and the range will not be covered, hence the color count check.
		
		Color[] colors = new Color[colorCount];
		
		// int col = color?360:255;
		for(int i = 0; i < colors.length; i++) {
			int col = (int) (i*colorDelta);
			colors[i] = color ? Color.getHSBColor((1-col)+.6f, 1, 1) : new Color(col, col, col);
		}
		
		return colors;
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
