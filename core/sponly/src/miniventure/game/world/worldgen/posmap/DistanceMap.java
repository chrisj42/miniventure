package miniventure.game.world.worldgen.posmap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;

import miniventure.game.world.Point;
import miniventure.game.world.worldgen.posmap.PositionalFetcher.PositionalCheck;

public class DistanceMap {
	
	private final int[][] matchDistances;
	private final HashMap<Integer, PositionSet> setsByDistance;
	private final int maxDistance;
	
	private final int width, height;
	// private final Point[][] pointObjects;
	
	public DistanceMap(int width, int height, PositionalCheck matchFetcher) {
		matchDistances = new int[width][height];
		// pointObjects = new Point[width][height];
		this.width = width;
		this.height = height;
		
		setsByDistance = new HashMap<>((int)Math.sqrt(width*height));
		
		PositionSet distSet = new PositionSet(0);
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				matchDistances[x][y] = -1;
				if(matchFetcher.get(x, y))
					distSet.add(x, y);
			}
		}
		
		while(distSet.positions.size() > 0) {
			setsByDistance.put(distSet.distance, distSet);
			
			PositionSet nextSet = new PositionSet(distSet.distance + 1);
			
			for(Point curPoint: distSet.positions) {
				for(int x = Math.max(0, curPoint.x-1); x <= Math.min(width-1, curPoint.x+1); x++) {
					for(int y = Math.max(0, curPoint.y-1); y <= Math.min(height-1, curPoint.y+1); y++) {
						if(x == curPoint.x && y == curPoint.y) continue;
						nextSet.add(x, y);
					}
				}
			}
			
			distSet = nextSet;
		}
		
		maxDistance = distSet.distance - 1;
	}
	
	public int getMaxDistance() { return maxDistance; }
	
	public int getDistance(int x, int y) {
		// if(!valid(x, y)) return -1;
		return matchDistances[x][y];
	}
	
	public void forEach(int distance, Consumer<Point> action) {
		PositionSet distSet = setsByDistance.get(distance);
		if(distSet == null) return;
		distSet.positions.forEach(action);
	}
	
	// iterator for group at same distance
	public void forEach(Consumer<Point> action, int... distances) {
		for(int dist: distances)
			forEach(dist, action);
	}
	
	public void forEachInRange(int minDist, int maxDist, Consumer<Point> action) {
		if(minDist > maxDistance || maxDist < 0) return; // could check for max < min but I'll let that handle itself
		int[] distances = new int[maxDist-minDist+1];
		for(int i = 0; i < distances.length; i++)
			distances[i] = minDist + i;
		forEach(action, distances);
	}
	
	private boolean valid(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	/*private Point at(int x, int y) {
		return pointObjects[x][y];
	}*/
	
	private class PositionSet {
		final int distance;
		final LinkedList<Point> positions;
		
		PositionSet(int distance) {
			this.distance = distance;
			positions = new LinkedList<>();
		}
		
		void add(int x, int y) {
			int cur = matchDistances[x][y];
			if(cur >= 0 && cur <= distance)
				return; // already part of a closer distance set
			positions.add(new Point(x, y));
			matchDistances[x][y] = distance;
		}
	}
}
