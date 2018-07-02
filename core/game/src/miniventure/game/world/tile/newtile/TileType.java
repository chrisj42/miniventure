package miniventure.game.world.tile.newtile;

import miniventure.game.item.Item;
import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.particle.Particle;
import miniventure.game.world.tile.newtile.data.DataMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileType {
	
	public enum TileTypeEnum {
		
		HOLE(type -> new TileType(type, new DataMap(), DestructionManager.INDESTRUCTIBLE(type), new TileTypeRenderer(type, true, new ConnectionManager(type, RenderStyle.SINGLE_FRAME/*, TileTypeEnum.WATER*/))));
		
		final TileType tileType;
		
		TileTypeEnum(ValueMonoFunction<TileTypeEnum, TileType> typeFetcher) {
			tileType = typeFetcher.get(this);
		}
		
		private static final TileTypeEnum[] values = values();
		public static TileTypeEnum values(int ordinal) { return values[ordinal]; }
	}
	
	
	private final TileTypeEnum enumType;
	private final DataMap idMap; // holds identity info; not tile-specific
	private final DestructionManager destructionManager;
	private final TileTypeRenderer renderer;
	
	/*
		Renderer
			- opaque?
			- connection behavior - 
			- overlap behavior
			- transitions - entrance/exit; complete with triggers.
			
	 */
	
	TileType(@NotNull TileTypeEnum enumType, DataMap idMap, DestructionManager destructionManager, TileTypeRenderer renderer) {
		this.enumType = enumType;
		this.idMap = new DataMap(idMap);
		this.destructionManager = destructionManager;
		
		this.renderer = renderer;
	}
	
	public TileTypeEnum getEnumType() { return enumType; }
	
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
		// Gonna have to figure out how to use DestructibleProperty
		return false;
	}
	
	
	@Override
	public final boolean equals(Object other) {
		return super.equals(other)/* || enumType.equals(other)*/; // the second part would make it inconsistent with equals; other.equals(this) would be false, but this.equals(other) would be true.
	}
	@Override
	public final int hashCode() { return enumType.hashCode(); }
}
