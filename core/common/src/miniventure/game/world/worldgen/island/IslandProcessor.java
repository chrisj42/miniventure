package miniventure.game.world.worldgen.island;

// processes a proto-island using some method
// noise maps are not necessarily involved; an island layer may end up using a noise map, but it will create the noise itself, using the seed fetched from the proto-island.
public interface IslandProcessor {
	
	void apply(ProtoLevel level);
	
	/*
		process types that I may need:
		- acts on tiles that satisfy a given condition (basically just a shortcut to foreach with a guard conditional)
		- TileGroupProcess
		
		- island processes don't have to iterate over the entire island or anything, they simply must accept a proto-island; what they do with the island is not specified in this interface.
		- this is how structures work...
	 */
	
	/*
		how about a more modular design?
		- make a new class, tile group
		- this consists of a set of tiles that can be given to other island processes.
	 */
	
}
