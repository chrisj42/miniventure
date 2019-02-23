package miniventure.game.world.worldgen.island;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import miniventure.game.util.function.ValueFunction;

// Contains 2 sets of groups: those that matched the condition, and those that didn't.
// Each group consists of tiles (positions) adjacent to 1 or more tiles already in the group (if the tile is not adjacent to any known group, a new group is created).
// Groups hold reference to adjacent groups in the opposite set.
// originally held ProtoTiles, hence the comments talk of tiles; but I have since made this more flexible.
public class PositionGroupMap {
	
	/*
		Overview of usage and interface:
		
		- use a noise map to distinguish land tiles, which happen to come mostly as one large group, and remove groups not connected to the biggest one; also fill in any non-land groups that don't touch the edge of the map.
		
		- use a noise map to distinguish land tiles, which happen to come in small groups, and remove any groups that are not fully contained in the main island shape.
			- get group sets, get group consisting of main island shape, fail any group with at least one tile not contained in the main island group.
		
		- from the above two: from a noise map, group tiles into touching groups that either all match a condition, or all fail a condition.
			- track groups that are adjacent, but in the opposite set.
		- provide these two sets of groups as output.
	 */
	
	@FunctionalInterface
	public interface PositionalFetcher<T> {
		T get(int x, int y);
	}
	
	public final HashSet<PositionGroup> matches;
	public final HashSet<PositionGroup> fails;
	private final GroupedPosition[][] positionData;
	
	private PositionGroupMap(HashSet<PositionGroup> matches, HashSet<PositionGroup> fails, GroupedPosition[][] positionData) {
		this.matches = matches;
		this.fails = fails;
		this.positionData = positionData;
	}
	
	public boolean checkMatched(int x, int y) {
		return positionData[x][y].matches;
	}
	
	public static class GroupedPosition {
		public final int x;
		public final int y;
		private boolean matches;
		private boolean edge;
		private PositionGroup group;
		
		private GroupedPosition(int x, int y, boolean matches, PositionGroup group, boolean edge) {
			this.matches = matches;
			this.group = group;
			
			this.x = x;
			this.y = y;
			this.edge = edge;
		}
	}
	
	public static PositionGroupMap process(final int width, final int height, final PositionalFetcher<Boolean> matchFetcher) {
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
		
		// method of accessing GroupedTiles via coordinate.
		GroupedPosition[][] positionData = new GroupedPosition[width][height];
		// the two sets to which the groups may belong
		final HashSet<PositionGroup> matches = new HashSet<>();
		final HashSet<PositionGroup> fails = new HashSet<>();
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				new PositionGroup(x, y, matchFetcher.get(x, y), width, height, positionData, matches, fails);
		
		// go through the tiles again, and this time create groups by looking at the top and right neighbors of each tile.
		// if match, add them to the current tile's group (or add cur to their group if cur doesn't have a group), if both have a group then merge the smaller one into the larger.
		// if no match, and cur has no group, make group for cur.
		
		// the map of tiles to their group
		// final HashMap<ProtoTile, TileGroup> groupMap = new HashMap<>(tileCount);
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				final GroupedPosition pos = positionData[x][y];
				final boolean curMatch = pos.matches;
				
				ValueFunction<GroupedPosition> neighborCheck = other -> {
					PositionGroup curGroup = pos.group;
					PositionGroup oGroup = other.group;
					
					if(curMatch == other.matches) {
						// merge groups
						PositionGroup bigger = curGroup.size() >= oGroup.size() ? curGroup : oGroup;
						PositionGroup smaller = curGroup == bigger ? oGroup : curGroup;
						for(GroupedPosition switched: bigger.mergeGroup(smaller))
							switched.group = bigger;
					}
					else {
						// add each other as adjacent opposites
						curGroup.adjacentOpposites.add(oGroup);
						oGroup.adjacentOpposites.add(curGroup);
					}
				};
				
				// check tile to the right
				if(x < width-1)
					neighborCheck.act(positionData[x+1][y]);
				// check tile above(?)
				if(y < height-1)
					neighborCheck.act(positionData[x][y+1]);
			}
		}
		
		return new PositionGroupMap(matches, fails, positionData);
	}
	
	public static class PositionGroup {
		// a group of adjacent tiles that all satisfied, or all didn't satisfy, the sorting condition.
		// a given instance of this class does not know whether it satisfied the sorting condition.
		
		private HashSet<GroupedPosition> positions;
		private HashSet<PositionGroup> adjacentOpposites; // if this group has no "holes" in it, then there can only be 1 adjacent opposite. However, each "hole" in the group space would also count as an adjacent opposite.
		
		private HashSet<PositionGroup> ownSet;
		private HashSet<PositionGroup> oppositeSet;
		
		private boolean edge; // true if at least one member tile touches the edge
		
		public PositionGroup(int startX, int startY, boolean starterMatches, int width, int height, GroupedPosition[][] positionData, HashSet<PositionGroup> matchSet, HashSet<PositionGroup> failSet) {
			positions = new HashSet<>();
			
			edge = startX == 0 || startY == 0 || startX == width - 1 || startY == height - 1;
			GroupedPosition pos = new GroupedPosition(startX, startY, starterMatches, this, edge);
			positions.add(pos);
			
			positionData[startX][startY] = pos;
			
			this.ownSet = starterMatches ? matchSet : failSet;
			this.oppositeSet = starterMatches ? failSet : matchSet;
			
			adjacentOpposites = new HashSet<>();
			
			ownSet.add(this);
		}
		
		public int size() { return positions.size(); }
		
		public boolean touchesEdge() { return edge; }
		
		// switches to the opposite group, merging with adjacent opposites and taking on all their adjacent opposites instead. (after removing this group from all their sets)
		// All adjacent groups are then removed from the opposite group, this is removed from the original group, and placed in the opposite group. Adjacent groups' tiles are also obviously added to this group.
		// own and opposite group references are then switched.
		public void switchSet() {
			// first, check that we are where we expect...
			if(!ownSet.contains(this)) {
				System.err.println("PositionGroup not contained in own set, assuming no longer tracked; ignoring switch request.");
				return;
			}
			// System.out.println("switching position group (set count: "+ownSet.size()+"; opp set count: "+oppositeSet.size()+"; adjacents to merge: "+adjacentOpposites.size()+')');
			
			// cache adjacent
			PositionGroup[] toMerge = adjacentOpposites.toArray(new PositionGroup[0]);
			// clear adjacent
			adjacentOpposites.clear();
			
			// switch groups
			ownSet.remove(this);
			oppositeSet.add(this);
			// switch group refs
			HashSet<PositionGroup> temp = ownSet;
			ownSet = oppositeSet;
			oppositeSet = temp;
			
			// run through all tiles and switch "matched" bool
			for(GroupedPosition pos: positions)
				pos.matches = !pos.matches;
			
			// merge adjacents
			PositionGroup biggest = this;
			for(PositionGroup opp: toMerge) {
				opp.adjacentOpposites.remove(this);
				
				PositionGroup smaller = opp;
				if(opp.size() > biggest.size()) {
					smaller = biggest;
					biggest = opp;
				}
				
				for(GroupedPosition pos: biggest.mergeGroup(smaller))
					pos.group = biggest;
			}
		}
		
		// merges contents of other into this group, and removes it from set.
		public Set<GroupedPosition> mergeGroup(PositionGroup other) {
			if(other == this) return Collections.emptySet();
			positions.addAll(other.positions); // add tiles
			
			// the adjacent opposites also track the other group; loop through them and update their lists, by removing refs to other, and adding refs to this where not already present.
			for(PositionGroup otherOp: other.adjacentOpposites) {
				otherOp.adjacentOpposites.remove(other);
				// I *could* do a conditional to see if this group is already in this group's list... but we're dealing with sets here so there's no reason to bother.
				otherOp.adjacentOpposites.add(this);
			}
			
			adjacentOpposites.addAll(other.adjacentOpposites); // add group's adjacent
			ownSet.remove(other); // remove ref
			edge = edge || other.edge; // merge edge flag
			return new HashSet<>(other.positions);
		}
	}
}
