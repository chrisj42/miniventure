package miniventure.game.world.worldgen.island;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import miniventure.game.util.function.ValueFunction;

// returned by TileGroupProcess
// Contains 2 sets of groups: those that matched the condition, and those that didn't.
// Each group consists of tiles adjacent to 1 or more tiles already in the group (if the tile is not adjacent to any known group, a new group is created).
// Groups hold reference to adjacent groups in the opposite set.
public class TileGroupMap {
	
	/*
		Overview of usage and interface:
		
		- use a noise map to distinguish land tiles, which happen to come mostly as one large group, and remove groups not connected to the biggest one; also fill in any non-land groups that don't touch the edge of the map.
		
		- use a noise map to distinguish land tiles, which happen to come in small groups, and remove any groups that are not fully contained in the main island shape.
			- get group sets, get group consisting of main island shape, fail any group with at least one tile not contained in the main island group.
		
		- from the above two: from a noise map, group tiles into touching groups that either all match a condition, or all fail a condition.
			- track groups that are adjacent, but in the opposite set.
		- provide these two sets of groups as output.
	 */
	
	public final HashSet<TileGroup> matches;
	public final HashSet<TileGroup> fails;
	private final GroupedTile[][] tileData;
	
	private TileGroupMap(HashSet<TileGroup> matches, HashSet<TileGroup> fails, GroupedTile[][] tileData) {
		this.matches = matches;
		this.fails = fails;
		this.tileData = tileData;
	}
	
	public boolean checkMatched(int x, int y) {
		return tileData[x][y].matches;
	}
	
	public static class GroupedTile {
		public final ProtoTile tile;
		private boolean matches;
		private boolean edge;
		private TileGroup group;
		
		private GroupedTile(ProtoTile tile, boolean matches, TileGroup group, ProtoIsland island) {
			this.tile = tile;
			this.matches = matches;
			this.group = group;
			
			int x = tile.pos.x;
			int y = tile.pos.y;
			this.edge = x == 0 || y == 0 || x == island.width - 1 || y == island.height - 1;
		}
	}
	
	public static TileGroupMap process(TileCondition condition, ProtoIsland island) {
		/*
			- create list buffer for processing tiles, add first
			
			for tile:
				- if cur group equals prev group, skip
				
				- evaluate condition
				
				- if condition matches prev
					- if cur has group, merge cur group into prev group, else add cur to prev group
				- else
					- if cur has group (will be opposite)
					
				
				if prev tile exists (ie not first tile)
					- if cur is in same group as prev, skip
					- if cur matches state of prev
						- if cur has group, merge into prev group, else add cur to prev group;  
					- 
				- check condition
				- if there is no previous tile (ie the first one) then make a new group in the right set, and cache neighbors for checks
				
				- add neighbors to queue
					- skip prev, of course
					- skip neighbors that are in "processed" set; they will have already checked cur
					- skip neighbors added to queue (but not yet processed) that are of the opposite 
		 */
		
		// final int tileCount = island.width * island.height;
		
		// method of accessing GroupedTiles via coordinate.
		GroupedTile[][] tileData = new GroupedTile[island.width][island.height];
		// the two sets to which the groups may belong
		final HashSet<TileGroup> matches = new HashSet<>();
		final HashSet<TileGroup> fails = new HashSet<>();
		
		island.forEach(tile -> new TileGroup(tile, condition.isMatch(tile), island, tileData, matches, fails));
		
		// go through the tiles again, and this time create groups by looking at the top and right neighbors of each tile.
		// if match, add them to the current tile's group (or add cur to their group if cur doesn't have a group), if both have a group then merge the smaller one into the larger.
		// if no match, and cur has no group, make group for cur.
		
		// the map of tiles to their group
		// final HashMap<ProtoTile, TileGroup> groupMap = new HashMap<>(tileCount);
		
		island.forEach(pt -> {
			final GroupedTile tile = tileData[pt.pos.x][pt.pos.y];
			final boolean curMatch = tile.matches;
			
			ValueFunction<GroupedTile> neighborCheck = other -> {
				TileGroup curGroup = tile.group;
				TileGroup oGroup = other.group;
				
				if(curMatch == other.matches) {
					// merge groups
					TileGroup bigger = curGroup.size() >= oGroup.size() ? curGroup : oGroup;
					TileGroup smaller = curGroup == bigger ? oGroup : curGroup;
					for(GroupedTile switched: bigger.mergeGroup(smaller))
						switched.group = bigger;
				}
				else {
					// add each other as adjacent opposites
					curGroup.adjacentOpposites.add(oGroup);
					oGroup.adjacentOpposites.add(curGroup);
				}
			};
			
			// check tile to the right
			if(pt.pos.x < island.width-1)
				neighborCheck.act(tileData[pt.pos.x+1][pt.pos.y]);
			// check tile above(?)
			if(pt.pos.y < island.height-1)
				neighborCheck.act(tileData[pt.pos.x][pt.pos.y+1]);
		});
		
		return new TileGroupMap(matches, fails, tileData);
	}
	
	public static class TileGroup {
		// a group of adjacent tiles that all satisfied, or all didn't satisfy, the sorting condition.
		// a given instance of this class does not know whether it satisfied the sorting condition.
		
		private HashSet<GroupedTile> tiles;
		private HashSet<TileGroup> adjacentOpposites; // if this group has no "holes" in it, then there can only be 1 adjacent opposite. However, each "hole" in the group space would also count as an adjacent opposite.
		
		private HashSet<TileGroup> ownSet;
		private HashSet<TileGroup> oppositeSet;
		
		private boolean edge; // true if at least one member tile touches the edge
		
		public TileGroup(ProtoTile starter, boolean starterMatches, ProtoIsland island, GroupedTile[][] tileData, HashSet<TileGroup> matchSet, HashSet<TileGroup> failSet) {
			tiles = new HashSet<>();
			GroupedTile tile = new GroupedTile(starter, starterMatches, this, island);
			tiles.add(tile);
			edge = tile.edge;
			
			tileData[starter.pos.x][starter.pos.y] = tile;
			
			this.ownSet = starterMatches ? matchSet : failSet;
			this.oppositeSet = starterMatches ? failSet : matchSet;
			
			adjacentOpposites = new HashSet<>();
			
			ownSet.add(this);
		}
		
		public int size() { return tiles.size(); }
		
		public boolean touchesEdge() { return edge; }
		
		// switches to the opposite group, merging with adjacent opposites and taking on all their adjacent opposites instead. (after removing this group from all their sets)
		// All adjacent groups are then removed from the opposite group, this is removed from the original group, and placed in the opposite group. Adjacent groups' tiles are also obviously added to this group.
		// own and opposite group references are then switched.
		public void switchSet() {
			// first, check that we are where we expect...
			if(!ownSet.contains(this)) {
				System.err.println("TileGroup not contained in own set, assuming no longer tracked; ignoring switch request.");
				return;
			}
			// System.out.println("switching tile group (set count: "+ownSet.size()+"; opp set count: "+oppositeSet.size()+"; adjacents to merge: "+adjacentOpposites.size()+')');
			
			// cache adjacent
			TileGroup[] toMerge = adjacentOpposites.toArray(new TileGroup[0]);
			// clear adjacent
			adjacentOpposites.clear();
			
			// switch groups
			ownSet.remove(this);
			oppositeSet.add(this);
			// switch group refs
			HashSet<TileGroup> temp = ownSet;
			ownSet = oppositeSet;
			oppositeSet = temp;
			
			// run through all tiles and switch "matched" bool
			for(GroupedTile tile: tiles)
				tile.matches = !tile.matches;
			
			// merge adjacents
			TileGroup biggest = this;
			for(TileGroup opp: toMerge) {
				opp.adjacentOpposites.remove(this);
				
				TileGroup smaller = opp;
				if(opp.size() > biggest.size()) {
					smaller = biggest;
					biggest = opp;
				}
				
				for(GroupedTile tile: biggest.mergeGroup(smaller))
					tile.group = biggest;
			}
		}
		
		// merges contents of other into this group, and removes it from set.
		public Set<GroupedTile> mergeGroup(TileGroup other) {
			if(other == this) return Collections.emptySet();
			tiles.addAll(other.tiles); // add tiles
			
			// the adjacent opposites also track the other group; loop through them and update their lists, by removing refs to other, and adding refs to this where not already present.
			for(TileGroup otherOp: other.adjacentOpposites) {
				otherOp.adjacentOpposites.remove(other);
				// I *could* do a conditional to see if this group is already in this group's list... but we're dealing with sets here so there's no reason to bother.
				otherOp.adjacentOpposites.add(this);
			}
			
			adjacentOpposites.addAll(other.adjacentOpposites); // add group's adjacent
			ownSet.remove(other); // remove ref
			edge = edge || other.edge; // merge edge flag
			return new HashSet<>(other.tiles);
		}
	}
	
}
