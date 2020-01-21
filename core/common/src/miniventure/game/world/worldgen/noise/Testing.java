package miniventure.game.world.worldgen.noise;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import miniventure.game.util.function.MapFunction;
import miniventure.game.world.worldgen.posmap.DistanceMap;
import miniventure.game.world.worldgen.posmap.PositionGroupMap;
import miniventure.game.world.worldgen.posmap.PositionGroupMap.PositionGroup;

import static miniventure.game.world.worldgen.noise.NoiseGenerator.circleMask;
import static miniventure.game.world.worldgen.noise.NoiseModifier.FILL_VALUE_RANGE;
import static miniventure.game.world.worldgen.noise.NoiseModifier.NoiseValueMerger.MULTIPLY;
import static miniventure.game.world.worldgen.noise.NoiseModifier.combine;

/** @noinspection SameParameterValue*/
public class Testing {
	
	public static NoiseGenerator removeHoles(NoiseGenerator original, float landThresh) {
		return info -> {
			float[][] noise = original.get2DNoise(info);
			
			PositionGroupMap groupMap = PositionGroupMap.process(info.width, info.height,
				(x, y) -> noise[x][y] >= landThresh
			);
			
			// System.out.println("match groups: "+groupMap.matches.size()+", fail groups: "+groupMap.fails.size());
			
			// System.out.println("removing excess matches...");
			// remove all but the biggest match group, and any fail group not touching the edge.
			PositionGroup biggestMatch = null;
			// cache the set, because the second for loop will modify during iteration
			PositionGroup[] matches = groupMap.matches.toArray(new PositionGroup[0]);
			for(PositionGroup match: matches)
				biggestMatch = biggestMatch == null || biggestMatch.size() < match.size() ? match : biggestMatch;
			
			for(PositionGroup match: matches) {
				if(match != biggestMatch)
					match.switchSet(); // switches group to a fail group, modifying related maps and the match/fail sets; group is combined with any adjacent groups.
			}
			
			// System.out.println("removing excess fails...");
			// now for the fail groups not touching the edge
			for(PositionGroup fail: groupMap.fails.toArray(new PositionGroup[0])) {
				if(!fail.touchesEdge())
					fail.switchSet();
			}
			
			// System.out.println("after switching:\nmatch groups: "+groupMap.matches.size()+", fail groups: "+groupMap.fails.size());
			
			NoiseModifier.forEach(noise, (val, x, y) ->
				groupMap.checkMatched(x, y) ? Math.max(landThresh+.01f, val) : Math.min(landThresh-.01f, val)
			);
			
			return noise;
		};
	}
	
	private static NoiseGenerator convertToDistanceMap(NoiseGenerator gen, MapFunction<Float, Boolean> acceptFunc, int... thresholdDistances) {
		return gen.modify(
			(info, noise) -> {
				DistanceMap map = new DistanceMap(info.width, info.height, (x, y) -> acceptFunc.get(noise[x][y]));
				// for each distance interval, set the noise so it decreases with each higher distance.
				// int maxDist = map.getMaxDistance();
				NoiseModifier.forEach(noise, (n, x, y) -> {
					int dist = map.getDistance(x, y);
					int thresh = thresholdDistances.length;
					for(int i = 0; i < thresholdDistances.length; i++) {
						if(dist <= thresholdDistances[i]) {
							thresh = i;
							break;
						}
					}
					return 1 - thresh/(float)thresholdDistances.length;
				});
			}
		);
	}
	
	/** @noinspection UnnecessaryLocalVariable, unused, RedundantSuppression */
	private static NoiseGenerator getTerrainOld() {
		// NoiseConfiguration mountains = new NoiseConfiguration(new Coherent2DNoiseFunction(2, 2))
		// 	.modify(NoiseModifier.combine(new Coherent2DNoiseFunction(50, 8), 1))
		// 	.modify(NoiseModifier.combine(new Coherent2DNoiseFunction(32, 6), 1))
		// 	.modify(NoiseModifier.multiply(NoiseGenerator.ISLAND_MASK, 2))
		// 	.modify(NoiseModifier.FILL_VALUE_RANGE);
		
		// Noise features = new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1}); // good for terrain features..?
		
		NoiseGenerator heightMask = circleMask(1.3f).modify(
			// FILL_VALUE_RANGE,
			// forEach((noise, x, y) -> (float) Math.pow(noise, 2)),
			FILL_VALUE_RANGE
		);
		
		NoiseGenerator terrain = new Coherent2DNoiseFunction(36, 3).modify(
			// combine(new Coherent2DNoiseFunction(36, 3).modifySeed(seed -> seed+1), 0.5f)
			// ,combine(new Coherent2DNoiseFunction(36, 3).modifySeed(seed -> seed+2), 0.5f)
			combine(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1}))
			,FILL_VALUE_RANGE
			,combine(circleMask(1), MULTIPLY)
			// ,FILL_VALUE_RANGE
			,combine(circleMask(1), heightMask)
			// ,combine(heightMask, 0.75f)
			,FILL_VALUE_RANGE
		);
		
		NoiseGenerator terrain2 = new Coherent2DNoiseFunction(84, 3).modify(
			combine(new Coherent2DNoiseFunction(36, 3), MULTIPLY, circleMask(1))
			,combine(circleMask(2), MULTIPLY)
			// ,combine(new Coherent2DNoiseFunction(36, 3).modifySeed(seed -> seed+2), 0.5f)
			// combine(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1}), .5f)
			,FILL_VALUE_RANGE
			// ,FILL_VALUE_RANGE
			,combine(circleMask(1), circleMask(1f).modify(combine(new Coherent2DNoiseFunction(12, 5), MULTIPLY)))
			// ,combine(heightMask, 0.75f)
			// ,FILL_VALUE_RANGE
		);
		
		NoiseModifier reScaler = NoiseModifier.forEach((n, x, y) -> n * 2 - 1);
		MapFunction<Integer, NoiseGenerator> noiseFetcher = cpv -> new Coherent2DNoiseFunction(cpv, 3).modify(reScaler);
		
		return noiseFetcher.get(48).modify(
			NoiseModifier.forEach((noise, x, y) -> Math.abs(noise))
			// combine(noiseFetcher.get(36), NoiseValueMerger.ABS_DIFF)
			,FILL_VALUE_RANGE
			,NoiseModifier.forEach((noise, x, y) -> 1 - noise)
		);
		
		/*return noiseGen.get().modify(
			combine(noiseGen.get(), (n1, n2) -> *//*n1 < .05f || n2 < .1f*//*n1)
		);*/
		/*return NoiseGenerator.MCPlusLevelGen(16).modify(
			combine(NoiseGenerator.MCPlusLevelGen(16), (n1, n2) -> Math.abs(n1 - n2))
			,combine(NoiseGenerator.MCPlusLevelGen(16), (n1, n2) -> Math.abs(n1 - n2))
			,FILL_VALUE_RANGE
		);*/
		
		// NoiseGenerator source = islandMask(1).modify(FILL_VALUE_RANGE);//new Coherent2DNoiseFunction(92, 6);
		// NoiseGenerator angle = new Coherent2DNoiseFunction(100, 0).modify(FILL_VALUE_RANGE);
		// NoiseGenerator distance = new Coherent2DNoiseFunction(80, 0).modify(FILL_VALUE_RANGE);
		
		// return source.modify(NoiseModifier.perturb(angle, distance, 40));
		// return islandMask(10, info -> MIN_RADIUS.get(info)/2);//.modify(getForEach((noise, x, y) -> (float)Math.pow(noise, 16)));
		// return new Coherent2DNoiseFunction(12);
		
		// return new Noise(new int[] {128}, getDescending(0)).modify(FILL_VALUE_RANGE);
		// return removeHoles(terrain, .2f);
		// return terrain;
		
		// return islandShape(info -> MIN_RADIUS.get(info)*.7f, .15f, 2, false, new Coherent2DNoiseFunction(36, 3));
	}
	
	private static NoiseGenerator getTerrain() {
		// return new Coherent2DNoiseFunction(16, 4)
		// 	.modify(NoiseModifier.combine(NoiseGenerator.islandShape, .35f));
		// return islandShape(info -> NoiseGenerator.MIN_RADIUS.get(info)*.8f, .15f, 2, false, new Coherent2DNoiseFunction(36, 3));
		
		// return NoiseGenerator.islandShape;
		return NoiseGenerator.tunnelPattern(
			new Coherent2DNoiseFunction(16),
			new Coherent2DNoiseFunction(32)
		);
		// return NoiseGenerator.getFromFetcher(info -> (x, y) -> 1 - Math.abs(info.width/2f - (x+.5f))/(info.width/2f) * Math.abs(info.height/2f - (y+.5f))/(info.height/2f));
		
		/*return NoiseGenerator.rectMask(3, .8f).modify(
			NoiseModifier.perturb(
				new Coherent2DNoiseFunction(128, 3),
				new Coherent2DNoiseFunction(32, 1),
				60
			)
		);*/
		
		// return new Coherent2DNoiseFunction(16, 1);
		
		// return new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1});
	}
	
	public static void main(String[] args) {
		// final int width = new Integer(args[0]);
		// final int height = new Integer(args[1]);
		// final int scale = new Integer(args[2]);
		final int width = 1200;
		final int height = 900;
		final int scale = 1;
		
		// float[] thresholds = {.08f};
		float[] thresholds = getThresholds(255);
		// float[] thresholds = {.1f};
		// float[] thresholds = getThresholds(10, v -> (float) Math.pow(v, 3));
		// float[] thresholds = {.1f};
		// System.out.println(Arrays.toString(thresholds));
		Color[] colors = getColors(thresholds.length+1, false); // add 1 to threshold count to do black-gray instead of black-white
		// Color[] colors = {Color.GREEN.darker(), Color.GREEN, Color.GREEN.darker()};
		
		Random rand = new Random();
		NoiseGenerator testGen = getTerrain();
		// NoiseGenerator testGen = convertToDistanceMap(getTerrain(), val -> val <= .1f, 0, 5);
		boolean repeat = true;
		while(repeat) {
			repeat = displayNoise(width, height, scale, testGen.get2DNoise(new GenInfo(rand.nextLong(), width, height)), thresholds, colors);
			// repeat = displayNoise(width, height, scale, testGen.get2DNoise(new GenInfo(rand.nextLong(), width, height)), thresholds2, colors2);
		}
			// repeat = IslandType.MENU.displayColorMap(rand.nextLong(), scale);
	}
	
	private static boolean displayNoise(int width, int height, int scale, float[][] noise, float[] thresholds, Color[] thresholdColors) {
		
		Color[][] colors = new Color[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				final float val = noise[x][y];
				int colorIdx = thresholds.length; // color array is 1 larger than threshold array
				for(int i = 0; i < thresholds.length; i++) {
					if(val <= thresholds[i]) {
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
		
		return JOptionPane.showOptionDialog(null, viewPanel, "Noise Map", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] {"Regen", "Exit"}, null) == 0;
	}
	
	private static float[] getThresholds(int sectionCount) { return getThresholds(sectionCount, v -> v); }
	private static float[] getThresholds(int sectionCount, MapFunction<Float, Float> curve) {
		float[] thresholds = new float[sectionCount-1];
		for(int i = 0; i < thresholds.length; i++)
			thresholds[i] = curve.get((i+1)*1f/sectionCount);
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
	
	private static int[] getDescending(int max) {
		int[] n = new int[max+1];
		for(int i = 0; i < n.length; i++) {
			n[max-i] = i+1;
		}
		return n;
	}
}
