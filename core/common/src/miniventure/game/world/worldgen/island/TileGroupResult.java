package miniventure.game.world.worldgen.island;

import java.util.HashSet;

// returned by TileGroupProcess
// Contains 2 sets of groups: those that matched the condition, and those that didn't.
// Each group consists of tiles adjacent to 1 or more tiles already in the group (if the tile is not adjacent to any known group, a new group is created).
// Groups hold reference to adjacent groups in the opposite set.
public class TileGroupResult {
	
	private final HashSet<TileGroup> matches;
	private final HashSet<TileGroup> fails;
	
	public TileGroupResult() {
		matches = new HashSet<>();
		fails = new HashSet<>();
	}
	
	static class TileGroup {
		// a group of adjacent tiles that all satisfied, or all didn't satisfy, the sorting condition.
		// a given instance of this class does not know whether it satisfied the sorting condition.
		
		private HashSet<ProtoTile> tiles;
		private HashSet<TileGroup> adjacentOpposites;
		
		public TileGroup(ProtoTile starter) {
			tiles = new HashSet<>();
			tiles.add(starter);
			
			adjacentOpposites = new HashSet<>();
		}
	}
	
}
