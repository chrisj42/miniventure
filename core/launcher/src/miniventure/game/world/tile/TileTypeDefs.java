package miniventure.game.world.tile;

import java.awt.Color;

import miniventure.game.item.PlaceableItemType;
import miniventure.game.item.ResourceType;
import miniventure.game.item.ToolItem.ToolType;
import miniventure.game.util.MyUtils;
import miniventure.game.util.param.ParamMap;
import miniventure.game.util.param.Value;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.tile.DestructionManager.PreferredTool;
import miniventure.game.world.tile.DestructionManager.RequiredTool;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.UpdateManager.UpdateAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// todo server transitions (transitions in general) and interaction interface. Also finish migrating properties here.
public enum TileTypeDefs implements ServerTileTypeInterface, ClientTileTypeInterface {
	
	HOLE(true, true),
	
	DIRT(true, true, TP.COLOR.as(new Color(200, 100, 0)),
		TP.DESTRUCT.as(new DestructionManager(
			new ItemDrop(PlaceableItemType.Dirt.get()),
			new RequiredTool(ToolType.Shovel)
		))
	),
	
	SAND(true, true, TP.COLOR.as(Color.YELLOW),
		TP.DESTRUCT.as(new DestructionManager(
			new ItemDrop(PlaceableItemType.Sand.get()),
			new RequiredTool(ToolType.Shovel)
		))
	),
	
	GRASS(true, true, TP.COLOR.as(Color.GREEN),
		TP.UNDER.as(TileTypeEnum.DIRT),
		TP.DESTRUCT.as()
	),
	
	STONE_PATH(true, true,
		TP.DESTRUCT.as()
	), // todo repurpose this to a tile I can use for rocky beaches
	
	SNOW(true, true, TP.COLOR.as(Color.WHITE),
		TP.DESTRUCT.as()
	),
	
	SMALL_STONE(true, true, TP.COLOR.as(Color.LIGHT_GRAY),
		TP.UNDER.as(TileTypeEnum.GRASS),
		TP.DESTRUCT.as()
	),
	
	WATER(true, true, TP.COLOR.as(Color.BLUE.darker()),
		TP.SPEED.as(0.6f)
	),
	
	DOCK(true, false),
	
	COAL_ORE(false, true,
		TP.UNDER.as(TileTypeEnum.DIRT),
		PFactory.oreDestruct(ResourceType.Coal, 25)
	),
	
	IRON_ORE(false, true,
		TP.UNDER.as(TileTypeEnum.DIRT),
		PFactory.oreDestruct(ResourceType.Iron, 35)
	),
	
	TUNGSTEN_ORE(false, true,
		TP.UNDER.as(TileTypeEnum.DIRT),
		PFactory.oreDestruct(ResourceType.Tungsten, 45)
	),
	
	RUBY_ORE(false, true,
		TP.UNDER.as(TileTypeEnum.DIRT),
		PFactory.oreDestruct(ResourceType.Ruby, 60)
	),
	
	STONE(false, true, TP.COLOR.as(Color.GRAY),
		TP.UNDER.as(TileTypeEnum.DIRT),
		TP.DESTRUCT.as(new DestructionManager(40,
			new PreferredTool(ToolType.Pickaxe, 5),
			new ItemDrop(ResourceType.Stone.get(), 2, 3)
		))
	),
	
	STONE_FLOOR(true, true,
		TP.DESTRUCT.as(new DestructionManager(
			new ItemDrop(ResourceType.Stone.get(), 3),
			new RequiredTool(ToolType.Pickaxe)
		))
	),
	
	WOOD_WALL(false, false,
		TP.DESTRUCT.as(new DestructionManager(20,
			new PreferredTool(ToolType.Axe, 3),
			new ItemDrop(ResourceType.Log.get(), 3)
		))
	), // by saying it's not opaque, grass will still connect under it
	
	STONE_WALL(false, false,
		TP.DESTRUCT.as(new DestructionManager(40,
			new PreferredTool(ToolType.Pickaxe, 5),
			new ItemDrop(ResourceType.Stone.get(), 3)
		))
	),
	
	OPEN_DOOR(true, false,
		TP.DESTRUCT.as()
	),
	
	CLOSED_DOOR(false, false,
		TP.DESTRUCT.as()
	),
	
	TORCH(true, false,
		TP.DESTRUCT.as()
	),
	
	CACTUS(false, false, TP.COLOR.as(Color.GREEN.darker().darker()),
		TP.DESTRUCT.as()
	),
	
	CARTOON_TREE(false, false, TP.COLOR.as(Color.GREEN.darker().darker())
	),
	
	DARK_TREE(false, false, TP.COLOR.as(Color.GREEN.darker().darker())
	),
	
	PINE_TREE(false, false, TP.COLOR.as(Color.GREEN.darker().darker())
	),
	
	POOF_TREE(false, false, TP.COLOR.as(Color.GREEN.darker().darker())
		TP.DESTRUCT.as()
	),
	
	AIR(true, false);
	
	private interface PFactory {
		static Value<?> oreDestruct(ResourceType resourceType, int health) {
			return TP.DESTRUCT.as(new DestructionManager(health,
				new PreferredTool(ToolType.Pickaxe, 5),
				new ItemDrop(resourceType.get(), 3, 4)
			));
		}
	}
	
	private final TileTypeEnum tileType;
	private final String formattedName;
	
	private final boolean walkable;
	private final boolean opaque;
	private final float speedRatio;
	private final TileTypeEnum underType;
	
	private final Color color;
	private final float lightRadius;
	
	private final TileTypeRenderer renderer;
	private final SwimAnimation swimAnimation;
	
	private final DestructionManager destructionManager;
	private final UpdateAction updateAction;
	
	TileTypeDefs(boolean walkable, boolean opaque, Value<?>... properties) {
		this.walkable = walkable;
		this.opaque = opaque;
		this.tileType = TileTypeEnum.value(ordinal());
		
		ParamMap map = new ParamMap(properties);
		this.color = map.get(TP.COLOR);
		this.underType = map.get(TP.UNDER);
		this.speedRatio = map.get(TP.SPEED);
		this.lightRadius = map.get(TP.LIGHT);
		
		this.destructionManager = map.get(TP.DESTRUCT);
		this.updateAction = map.get(TP.UPDATE);
		
		this.renderer = map.get(TP.RENDER).get(tileType);
		this.swimAnimation = map.get(TP.SWIM).get(tileType);
		
		this.formattedName = MyUtils.toTitleCase(name(), "_");
		// GameCore.debug("Initialized TileType "+this);
	}
	
	private static final TileTypeDefs[] values = TileTypeDefs.values();
	public static TileTypeDefs value(int ord) { return values[ord]; }
	
	@Override
	public TileTypeEnum getTypeEnum() { return tileType; }
	@Override
	public boolean isWalkable() { return walkable; }
	@Override
	public boolean isOpaque() { return opaque; }
	@Override
	public float getSpeedRatio() { return speedRatio; }
	@Override
	public Color getColor() { return color; }
	@Override
	public TileTypeEnum getUnderType() { return underType; }
	
	@Override
	public float getLightRadius() { return lightRadius; }
	@Override
	public SwimAnimation getSwimAnimation() { return swimAnimation; }
	@Override
	public TileTypeRenderer getRenderer() { return renderer; }
	
	@Override
	public DestructionManager pDestruct() { return destructionManager; }
	
	@Override
	public float update(@NotNull Tile.TileContext context, float delta) {
		if(updateAction == null)
			return 0;
		
		float delayOriginal = context.getData(TileDataTag.UpdateTimer);
		float delayLeft = delayOriginal - delta;
		
		if(delayLeft <= 0)
			delayLeft = updateAction.update(context);
		
		context.setData(TileDataTag.UpdateTimer, delayLeft);
		return delayLeft;
	}
	
	@Override
	public Object[] createDataArray() {
		return new Object[0];
	}
	
	@Override
	public boolean hasData(TileDataTag<?> dataTag) {
		return false;
	}
	
	@Override
	public <T> T getData(TileDataTag<T> dataTag, Object[] dataArray) {
		return null;
	}
	
	@Override
	public <T> void setData(TileDataTag<T> dataTag, T value, Object[] dataArray) {
		
	}
	
	@Override
	public String getName() { return formattedName; }
}
