package miniventure.game.world.worldgen.island;

import java.util.Map;
import java.util.TreeMap;

import miniventure.game.world.worldgen.posmap.DistanceMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileDistanceMap implements IslandProcessor {
	
	public static TileDistanceMapBuilder builder() {
		return new TileDistanceMapBuilder();
	}
	
	// todo move tile condition to builder constructor since these aren't likely to be used anyway
	// todo allow distance value per tile to vary according to a noise function, so for example "5 tiles" would fluctuate a bit; adds more variability to the terrain
	public static class TileDistanceMapBuilder {
		
		private TreeMap<Integer, TileProcessor> distanceProcessors;
		
		private TileDistanceMapBuilder() {
			distanceProcessors = new TreeMap<>();
		}
		
		public TileDistanceMapBuilder atDistance(int distance, @NotNull TileProcessor processor) {
			distanceProcessors.compute(distance, (dist, pro) -> pro == null ? processor : pro.append(processor));
			return this;
		}
		
		public TileDistanceMapBuilder atDistances(int[] distances, @NotNull TileProcessor processor) {
			for(int dist: distances) atDistance(dist, processor);
			return this;
		}
		
		public TileDistanceMapBuilder atRange(int minDist, int maxDist, @NotNull TileProcessor processor) {
			for(int dist = minDist; dist <= maxDist; dist++)
				atDistance(dist, processor);
			return this;
		}
		
		public TileDistanceMap get(@NotNull TileCondition tileAcceptor) {
			return new TileDistanceMap(distanceProcessors, null, tileAcceptor);
		}
		
		public TileDistanceMap get(boolean useTrailingDistances, @NotNull TileProcessor remainingDistances, @NotNull TileCondition tileAcceptor) {
			if(!useTrailingDistances && distanceProcessors.size() > 0) {
				// use all, including trailing
				int highestSpecified = distanceProcessors.lastKey();
				for(int i = 0; i < highestSpecified; i++)
					distanceProcessors.putIfAbsent(i, remainingDistances);
			}
			
			return new TileDistanceMap(distanceProcessors, remainingDistances, tileAcceptor);
		}
	}
	
	
	private final Integer[] distances;
	private final TileProcessor[] processors;
	private final TileProcessor trailingProcessor;
	private final TileCondition tileAcceptor;
	
	private TileDistanceMap(Map<Integer, TileProcessor> distanceProcessors, @Nullable TileProcessor trailingProcessor, TileCondition tileAcceptor) {
		this.trailingProcessor = trailingProcessor;
		this.tileAcceptor = tileAcceptor;
		
		distances = distanceProcessors.keySet().toArray(new Integer[0]);
		processors = new TileProcessor[distances.length];
		for(int i = 0; i < distances.length; i++)
			processors[i] = distanceProcessors.get(distances[i]).copy();
	}
	
	@Override
	public void apply(ProtoLevel level) {
		if(distances.length == 0) return;
		
		DistanceMap distMap = new DistanceMap(level.width, level.height,
			(x, y) -> tileAcceptor.isMatch(level.getTile(x, y))
		);
		
		for(int i = 0; i < distances.length; i++) {
			final TileProcessor processor = processors[i];
			if(processor != null)
				distMap.forEach(distances[i], p -> processor.processTile(level.getTile(p)));
		}
		
		// run processor for distances past the highest given distance
		if(trailingProcessor != null) {
			distMap.forEachInRange(distances[distances.length-1]+1, distMap.getMaxDistance(),
				p -> trailingProcessor.processTile(level.getTile(p))
			);
		}
	}
}
