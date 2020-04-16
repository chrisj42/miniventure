package miniventure.game.world.worldgen;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import miniventure.game.world.Point;
import miniventure.game.world.worldgen.noise.Testing;

import com.badlogic.gdx.math.Vector2;

public class WorldGenTest {
	
	// use Testing.displayMap()
	
	private static final int MAX_ISLAND_PLACEMENT_ATTEMPTS = 10000; // maximum number of times a new island will attempt to be placed before giving up and starting over with a fresh map.
	
	/*
		data used:
		- min/max distance to center island
		- min/max distance to nearest island
	 */
	
	private static class IslandReq {
		private final int minCenterDist;
		private final int maxCenterDist;
		private final int centerDistRange;
		private final int minIslandDist;
		private final int maxIslandDist;
		private final int islandDistRange;
		
		private IslandReq(int minCenterDist, int maxCenterDist, int minIslandDist, int maxIslandDist) {
			this.minCenterDist = minCenterDist;
			this.maxCenterDist = maxCenterDist;
			centerDistRange = maxCenterDist - minCenterDist;
			this.minIslandDist = minIslandDist;
			this.maxIslandDist = maxIslandDist;
			islandDistRange = maxIslandDist - minIslandDist;
		}
		
		static IslandReq[] getArray(int[] minCenterDist, int[] maxCenterDist,
									int[] minIslandDist, int[] maxIslandDist) {
			IslandReq[] islands = new IslandReq[minCenterDist.length];
			for(int i = 0; i < islands.length; i++)
				islands[i] = new IslandReq(minCenterDist[i], maxCenterDist[i], minIslandDist[i], maxIslandDist[i]);
			return islands;
		}
		static IslandReq[] getArray(int[] minCenterDist, float centerDistMultiplier,
									int[] minIslandDist, int[] maxIslandDist) {
			return getArray(minCenterDist, multiply(minCenterDist, centerDistMultiplier), minIslandDist, maxIslandDist);
		}
		static IslandReq[] getArray(int[] minCenterDist, int[] maxCenterDist,
									int[] minIslandDist, float islandDistMultiplier) {
			return getArray(minCenterDist, maxCenterDist, minIslandDist, multiply(minIslandDist, islandDistMultiplier));
		}
		static IslandReq[] getArray(int[] minCenterDist, float centerDistMultiplier,
									int[] minIslandDist, float islandDistMultiplier) {
			return getArray(minCenterDist, multiply(minCenterDist, centerDistMultiplier),
							minIslandDist, multiply(minIslandDist, islandDistMultiplier)
			);
		}
		
		private static int[] multiply(int[] values, float num) {
			int[] ar = new int[values.length];
			for(int i = 0; i < ar.length; i++) {
				ar[i] = Math.round(values[i] * num);
			}
			return ar;
		}
	}
	
	public static void main(String[] args) {
		boolean repeat;
		do repeat = displayWorldMap(IslandReq.getArray(
			new int[] {30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30},
			new int[] {31, 40, 40, 40, 70, 70, 70, 70, 100, 100, 130, 140, 140, 150},
			new int[] {25, 35, 35, 35, 65, 65, 65, 65, 90, 90, 120, 130, 130, 140},
			new int[] {35, 45, 45, 45, 80, 80, 80, 85, 100, 105, 140, 140, 150, 155}
			),
			4, 2);
		while(repeat);
	}
	
	private static boolean displayWorldMap(final IslandReq[] islandRequirements, final int islandPixelSize, final int scale) {
		final Color background = Color.GRAY;
		
		HashMap<Point, Color> islands;
		do {
			System.out.println("gen islands");
			islands = genMap(islandRequirements);
		}
		while(islands == null);
		
		int minX = 0, maxX = 0, minY = 0, maxY = 0;
		for(Point p: islands.keySet()) {
			minX = Math.min(minX, p.x);
			maxX = Math.max(maxX, p.x);
			minY = Math.min(minY, p.y);
			maxY = Math.max(maxY, p.y);
		}
		
		int absMaxX = Math.max(Math.abs(minX), maxX);
		int absMaxY = Math.max(Math.abs(minY), maxY);
		int absMax = Math.max(absMaxX, absMaxY);
		
		absMax += islandPixelSize + absMax/5;
		
		maxX = maxY = absMax;
		minX = minY = -absMax;
		
		final int dim = absMax * 2;
		final int width = dim, height = dim;
		
		Color[][] colors = new Color[width][height];
		for(int x = 0; x < colors.length; x++)
			Arrays.fill(colors[x], background);
		
		//noinspection KeySetIterationMayUseEntrySet
		for(Point p: islands.keySet()) {
			final Color color = islands.get(p);
			for(int x = p.x - minX - islandPixelSize/2; x <= p.x - minX + islandPixelSize/2; x++) {
				if(x < 0 || x >= width) continue;
				for(int y = p.y - minY - islandPixelSize/2; y <= p.y - minY + islandPixelSize/2; y++) {
					if(y < 0 || y >= height) continue;
					colors[x][y] = color;
				}
			}
		}
		
		return Testing.displayMap(width, height, scale, colors);
	}
	
	private static HashMap<Point, Color> genMap(IslandReq[] islandRequirements) {
		// determine the range of center distances present, for proper coloring.
		int min = islandRequirements[0].minCenterDist;
		int max = islandRequirements[0].maxCenterDist;
		for(int i = 1; i < islandRequirements.length; i++) {
			min = Math.min(min, islandRequirements[i].minCenterDist);
			max = Math.max(max, islandRequirements[i].maxCenterDist);
		}
		// final int range = max - min;
		
		Point[] islandLocations = new Point[islandRequirements.length+1];
		islandLocations[0] = new Point(0, 0);
		HashSet<Point> pointSet = new HashSet<>(islandRequirements.length+1);
		
		for(int i = islandRequirements.length-1; i >= 0; i--) {
		// for(int i = 0; i < islandRequirements.length; i++) {
			final IslandReq curIsland = islandRequirements[i];
			final int minDist = curIsland.minCenterDist;
			final int maxDist = curIsland.maxCenterDist;
			System.out.println("placing island "+(i+1)+" of "+islandRequirements.length + " between "+minDist+" and "+maxDist);
			Point islandPos = placeNewIsland(pointSet, curIsland);
			if(islandPos == null)
				return null; // islands cannot reasonably fit in in this configuration
			// get the color based on the distance to nearest island
			// float thresholdRelativeSize = (maxDist - min) * (280/360f) / range;
			// Color color = Color.getHSBColor(thresholdRelativeSize, 1, 1);
			// islandData.put(islandPos, color);
			islandLocations[i+1] = islandPos;
			pointSet.add(islandPos);
		}
		
		System.out.println(Arrays.toString(islandLocations));
		
		// find min dist between each island
		float[] minDists = new float[islandLocations.length];
		for(int i = 0; i < islandLocations.length-1; i++) {
			for(int j = i+1; j < islandLocations.length; j++) {
				float dist = Vector2.dst(islandLocations[i].x, islandLocations[i].y, islandLocations[j].x, islandLocations[j].y);
				minDists[i] = minDists[i] == 0 ? dist : Math.min(minDists[i], dist);
				minDists[j] = minDists[j] == 0 ? dist : Math.min(minDists[j], dist);
			}
		}
		float minDist = 0, maxDist = 0;
		for(int i = 0; i < minDists.length; i++) {
			float dist = minDists[i];
			minDist = minDist == 0 ? dist : Math.min(minDist, dist);
			maxDist = maxDist == 0 ? dist : Math.max(maxDist, dist);
		}
		
		System.out.println(Arrays.toString(minDists));
		System.out.println("total min: "+minDist+", total max: "+maxDist);
		
		// now that the minDists have been calculated, set the colors
		HashMap<Point, Color> islandData = new HashMap<>(islandRequirements.length+1);
		
		for(int i = 0; i < minDists.length; i++) {
			float distRelative = (minDists[i] - minDist) / 2 / (maxDist - minDist);
			Color color = Color.getHSBColor(distRelative, 1, 1);
			islandData.put(islandLocations[i], color);
		}
		
		// islandData.put(new Point(0, 0), Color.BLACK);
		
		return islandData;
	}
	
	private static Point placeNewIsland(final HashSet<Point> existingPositions, final IslandReq island) {
		Random rand = new Random();
		Point islandPos = null;
		int attempts = 0;
		Vector2 v = new Vector2(1, 1);
		do {
			attempts++;
			if(attempts > MAX_ISLAND_PLACEMENT_ATTEMPTS) {
				System.out.println("exceeded max number of placement attempts");
				break;
			}
			
			float curDist = island.centerDistRange - (float)Math.sqrt(rand.nextFloat() * island.centerDistRange) + island.minCenterDist;
			// float curDist = rand.nextFloat() * island.centerDistRange + island.minCenterDist;
			float angle = rand.nextFloat() * 360;
			v.setLength(curDist).setAngle(angle);
			
			// System.out.println("attempt "+attempts+" of island placement with center dist "+curDist+" and angle "+angle+"; vector: "+v);
			
			islandPos = new Point(Math.round(v.x), Math.round(v.y));
			
		} while(!validateIsland(existingPositions, islandPos, island));
		
		if(attempts > MAX_ISLAND_PLACEMENT_ATTEMPTS)
			return null;
		else
			return islandPos;
	}
	
	private static boolean validateIsland(Set<Point> existing, Point islandPos, IslandReq island) {
		if(existing.contains(islandPos)) {
			// System.out.println(islandPos+" already exists");
			return false;
		}
		
		double minDist = 0;
		boolean first = true;
		for(Point p: existing) {
			double dist = Math.hypot(p.x - islandPos.x, p.y - islandPos.y);
			if(dist < island.minIslandDist) {
				// System.out.println("island too close: "+dist+" below minimum "+island.minIslandDist);
				return false;
			}
			
			if(first) {
				first = false;
				minDist = dist;
			} else
				minDist = Math.min(minDist, dist);
		}
		
		if(minDist > island.maxIslandDist) {
			// System.out.println("island neighbors are too far away; "+minDist+" is greater than limit "+island.maxIslandDist);
			return false;
		}
		
		return true;
	}
	
}
