package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.particle.Particle;
import miniventure.game.world.tile.data.DataMap;
import miniventure.game.world.tile.data.DataTag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileType {
	
	public enum TileTypeEnum {
		
		HOLE(type -> new TileType(type, DestructionManager.INDESTRUCTIBLE(type), new TileTypeRenderer(type, true, new ConnectionManager(type, RenderStyle.SINGLE_FRAME/*, TileTypeEnum.WATER*/))));
		
		private final TileType tileType; // TODO have a World TileType fetcher that replaces TileTypes with with their specialized Server / Client equivalents. This field is private, and instead there's a method here that takes a WorldManager.
		
		TileTypeEnum(ValueMonoFunction<TileTypeEnum, TileType> typeFetcher) {
			tileType = typeFetcher.get(this);
		}
		
		private static final TileTypeEnum[] values = values();
		public static TileTypeEnum values(int ordinal) { return values[ordinal]; }
		
		public TileType getTileType(@NotNull WorldManager world) {
			return world.getTileTypeFetcher().mapValue(this, tileType);
		}
	}
	
	
	private final TileTypeEnum enumType;
	final DestructionManager destructionManager;
	final TileTypeRenderer renderer;
	final UpdateManager updateManager;
	
	/*
		Renderer
			- opaque?
			- connection behavior - 
			- overlap behavior
			- transitions - entrance/exit; complete with triggers.
			
	 */
	
	TileType(@NotNull TileTypeEnum enumType, DestructionManager destructionManager, TileTypeRenderer renderer) {
		this(enumType, destructionManager, renderer, new UpdateManager());
	}
	TileType(@NotNull TileTypeEnum enumType, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		this.enumType = enumType;
		this.destructionManager = destructionManager;
		this.renderer = renderer;
		this.updateManager = updateManager;
	}
	
	public DataMap getInitialData() { return new DataMap(); }
	
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
	 * @return how long to wait before next call, or 0 for never (until adjacent tile update)
	 */ 
	public float update(@NotNull Tile tile) {
		DataMap dataMap = tile.getDataMap(this);
		
		float now = tile.getWorld().getGameTime();
		float lastUpdate = dataMap.getOrDefault(DataTag.LastUpdate, now);
		float delta = now - lastUpdate;
		dataMap.put(DataTag.LastUpdate, now);
		
		return updateManager.update(tile, delta);
	}
	
	
	public boolean interact(@NotNull Tile tile, Player player, @Nullable Item item) { return false; }
	
	public boolean attacked(@NotNull Tile tile, WorldObject source, @Nullable Item item, int damage) {
		return destructionManager.tileAttacked(tile, source, item, damage);
	}
	
	public boolean touched(@NotNull Tile tile, Entity entity, boolean initial) { return false; }
	
	@Override
	public final boolean equals(Object other) {
		return other instanceof TileType && ((TileType)other).getEnumType().equals(getEnumType());
	}
	@Override
	public final int hashCode() { return enumType.hashCode(); }
}
