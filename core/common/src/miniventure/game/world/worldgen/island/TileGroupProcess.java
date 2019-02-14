package miniventure.game.world.worldgen.island;

public class TileGroupProcess {
	
	/*
		Overview of usage and interface:
		
		- use a noise map to distinguish land tiles, which happen to come mostly as one large group, and remove groups not connected to the biggest one; also fill in any non-land groups that don't touch the edge of the map.
		
		- use a noise map to distinguish land tiles, which happen to come in small groups, and remove any groups that are not fully contained in the main island shape.
			- get group sets, get group consisting of main island shape, fail any group with at least one tile not contained in the main island group.
		
		- from the above two: from a noise map, group tiles into touching groups that either all match a condition, or all fail a condition.
			- track groups that are adjacent, but in the opposite set.
		- provide these two sets of groups as output.
	 */
	
	private final TileCondition condition;
	
	public TileGroupProcess(TileCondition condition) {
		this.condition = condition;
	}
	
	
}
