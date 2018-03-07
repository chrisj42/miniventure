package miniventure.game.world.tile;

import miniventure.game.api.APIObjectType;
import miniventure.game.api.PropertyFetcher;
import miniventure.game.api.TypeLoader;
import miniventure.game.item.FoodItem;
import miniventure.game.item.ResourceItem;
import miniventure.game.item.TileItem;
import miniventure.game.item.ToolType;
import miniventure.game.util.MyUtils;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.tile.AnimationProperty.AnimationType;
import miniventure.game.world.tile.DestructibleProperty.PreferredTool;
import miniventure.game.world.tile.DestructibleProperty.RequiredTool;

import org.jetbrains.annotations.NotNull;

public enum TileType implements APIObjectType<TileType, TileProperty> {
	
	HOLE(() -> new TileProperty[] {
			SolidProperty.SOLID,
			new ConnectionProperty(true, TileType.WATER)
	}),
	
	DIRT(() -> new TileProperty[] {
			SolidProperty.WALKABLE,
			new DestructibleProperty(true, new RequiredTool(ToolType.Shovel))
	}),
	
	SAND(() -> new TileProperty[] {
			SolidProperty.WALKABLE,
			new DestructibleProperty(true, new RequiredTool(ToolType.Shovel)),
			new OverlapProperty(true)
	}),
	
	GRASS(() -> new TileProperty[] {
			SolidProperty.WALKABLE,
			new DestructibleProperty(true, new RequiredTool(ToolType.Shovel)),
			new OverlapProperty(true)
	}),
	
	WATER(() -> new TileProperty[] {
			new AnimationProperty(true, AnimationType.RANDOM, 0.2f, AnimationType.SEQUENCE, 1/24f),
			new SpreadUpdateProperty(HOLE),
			new OverlapProperty(true)
	}),
	
	STONE(() -> new TileProperty[] {
			SolidProperty.SOLID,
			new DestructibleProperty(20, new PreferredTool(ToolType.Pickaxe, 5), true)
	}),
	
	DOOR_CLOSED(() -> new TileProperty[] {
			SolidProperty.SOLID,
			new DestructibleProperty(true, new RequiredTool(ToolType.Axe)),
			new AnimationProperty(true, AnimationType.SINGLE_FRAME),
			(InteractableProperty) (p, i, t) -> {
				t.replaceTile(TileType.DOOR_OPEN);
				return true;
			}
	}),
	
	DOOR_OPEN(() -> new TileProperty[] {
			SolidProperty.WALKABLE,
			new AnimationProperty(false, AnimationType.SINGLE_FRAME),
			new TransitionProperty(
				new TransitionAnimation("open", true, 1/24f, DOOR_CLOSED),
				new TransitionAnimation("close", false, 1/24f, DOOR_CLOSED)
			),
			(InteractableProperty) (p, i, t) -> {
				t.replaceTile(DOOR_CLOSED);
				return true;
			},
			new DestructibleProperty(new ItemDrop(TileItem.get(TileType.DOOR_CLOSED)), new RequiredTool(ToolType.Axe))
	}),
	
	TORCH(() -> new TileProperty[] {
			new DestructibleProperty(true),
			(LightProperty) () -> 2,
			new AnimationProperty(false, AnimationType.SEQUENCE, 1/12f),
			new TransitionProperty(new TransitionAnimation("enter", true, 1/12f))
	}),
	
	CACTUS(() -> new TileProperty[] {
			SolidProperty.SOLID,
			new DestructibleProperty(7, null, true),
			new AnimationProperty(false, AnimationType.SINGLE_FRAME),
			(TouchListener) (e, t) -> e.attackedBy(t, null, 1)
	}),
	
	TREE(() -> new TileProperty[] {
			SolidProperty.SOLID,
			new DestructibleProperty(10, new PreferredTool(ToolType.Axe, 2), new ItemDrop(ResourceItem.Log.get()), new ItemDrop(FoodItem.Apple.get())),
			new AnimationProperty(false, AnimationType.SINGLE_FRAME),
			new ConnectionProperty(true)
	});
	
	/*LAVA(() -> new TileProperty[] {
			(TouchListener) (e, t) -> e.attackedBy(t, null, 5),
			new AnimationProperty.RandomFrame(0.1f)
	});*/
	
	/*
		Others:
		wheat, farmland, door, floor, wall, stairs?, sapling, torch, ore, ice, cloud?,
		laser source, laser, mirror, laser receiver.
	 */
	
	private final PropertyFetcher<TileProperty> propertyFetcher;
	TileType(@NotNull PropertyFetcher<TileProperty> fetcher) { this.propertyFetcher = fetcher; }
	
	@Override public TileType getInstance() { return this; }
	@Override public Class<TileType> getTypeClass() { return TileType.class; }
	
	@Override public TileProperty[] getProperties() {
		TileProperty[] props = propertyFetcher.getProperties();
		for(TileProperty p: props)
			p.init(this);
		
		return props;
	}
	
	/* What I've learned:
		Casting with parenthesis works because the generic type is replaced by Object during runtime, or, if you've specified bounds, as specific a class as you can get with the specified bounds.
		But calling (T var).getClass().cast(Tile t) doesn't always work because getClass() returns the actual class of the generic variable, and that may not be compatible with whatever you're trying to cast.
	 */
	
	public static final TileType[] values = TileType.values();
	
	public String getName() { return MyUtils.toTitleCase(name()); }
	
	static {
		TypeLoader.loadType(TileType.class, TileProperty.getDefaultPropertyMap());
	}
}
