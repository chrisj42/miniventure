package miniventure.game.world.tile.newtile;

import miniventure.game.item.Item;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.particle.Particle;
import miniventure.game.world.tile.newtile.data.DataMap;
import miniventure.game.world.tile.newtile.data.DataTag;
import miniventure.game.world.tile.newtile.render.TileTypeRenderer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @noinspection MethodMayBeStatic*/
public abstract class TileLayer {
	
	private final TileType type;
	private final DataMap idMap; // holds identity info; not tile-specific
	private final TileTypeRenderer renderer;
	
	/*
		Renderer
			- opaque?
			- connection behavior - 
			- overlap behavior
			- transitions - entrance/exit; complete with triggers.
			
	 */
	
	TileLayer(TileType type, DataMap idMap, TileTypeRenderer renderer) {
		this.type = type;
		this.idMap = new DataMap(idMap);
		
		this.renderer = renderer;
	}
	
	public TileType getType() { return type; }
	
	public float getLightRadius() { return 0; }
	
	public TileTypeRenderer getRenderer() { return renderer; }
	
	public boolean isPermeableBy(Entity e) { return e instanceof Particle; }
	
	/*
		Some update methods. All methods have Tile param in addition to whatever listed.
		can update:
		- behavior/state update
		- (LATER) - sprite update (so it doesn't have to be calculated every frame; animations don't count.)
		
		rendering:
		- get sprite (main/connection)
		- get overlap sprite on (param TileLayer index, in given tile's layer stack, b/c it won't always be the top TileLayer we're overlapping)
		
		interaction:
		- attacked (param WorldObject source; param item (may be null))
		- interaction (param player; param item)
		
	 */
	
	
	/**
	 * Called to update the tile's state in some way, whenever an adjacent tile is updated. It is also called once on tile load to get the first value and determine future calls.
	 * If this returns a value greater than 0, then it is called after at least the specified amount of time has passed. Otherwise, it won't be updated until an adjacent tile is updated.
	 * 
	 * @param tile the tile
	 * @param delta elapsed time since last call
	 * @return how long to wait before next call, or 0 for never (until adjacent tile update)
	 */ 
	public float update(@NotNull Tile tile, float delta) {
		// tile.setData(DataTag.LastUpdate.as(GameCore.getElapsedProgramTime()));
		return 0;
	}
	
	
	public boolean interact(@NotNull Tile tile, Player player, @Nullable Item item) { return false; }
	
	public boolean attacked(@NotNull Tile tile, WorldObject source, @Nullable Item item, int damage) {
		return idMap.computeFrom(DataTag.DestructibleProp, prop -> prop.tileAttacked(tile, source, item, damage), false);
	}
}
