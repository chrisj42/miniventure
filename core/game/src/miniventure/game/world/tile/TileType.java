package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.HashMap;

import miniventure.game.item.FoodItem;
import miniventure.game.item.ResourceItem;
import miniventure.game.item.TileItem;
import miniventure.game.item.ToolType;
import miniventure.game.util.MyUtils;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.tile.AnimationProperty.AnimationType;
import miniventure.game.world.tile.DestructibleProperty.PreferredTool;
import miniventure.game.world.tile.DestructibleProperty.RequiredTool;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

import static miniventure.game.world.tile.TilePropertyType.*;

public enum TileType {
	
	HOLE((tileType, map) -> {
		map.put(Solid, SolidProperty.SOLID);
		map.put(Connect, new ConnectionProperty(tileType, true, TileType.valueOf("WATER")));
	}),
	
	DIRT((tileType, map) -> {
		map.put(Solid, SolidProperty.WALKABLE);
		map.put(Attack, new DestructibleProperty(tileType, new RequiredTool(ToolType.Shovel)));
	}),
	
	SAND((tileType, map) -> {
		map.put(Solid, SolidProperty.WALKABLE);
		map.put(Attack, new DestructibleProperty(tileType, new RequiredTool(ToolType.Shovel)));
		map.put(Overlap, new OverlapProperty(tileType, true));
	}),
	
	GRASS((tileType, map) -> {
		map.put(Solid, SolidProperty.WALKABLE);
		map.put(Attack, new DestructibleProperty(tileType, new RequiredTool(ToolType.Shovel)));
		map.put(Overlap, new OverlapProperty(tileType, true));
	}),
	
	WATER((tileType, map) -> {
		map.put(Render, new AnimationProperty(tileType, true, AnimationType.RANDOM, 0.2f, AnimationType.SEQUENCE, 1/24f));
		map.put(Update, new SpreadUpdateProperty(tileType, (type, tile) -> tile.addTile(type), HOLE));
		map.put(Overlap, new OverlapProperty(tileType, true));
	}),
	
	STONE((tileType, map) -> {
		map.put(Solid, SolidProperty.SOLID);
		map.put(Attack, new DestructibleProperty(tileType, 20, new PreferredTool(ToolType.Pickaxe, 5)));
	}),
	
	DOOR_CLOSED((tileType, map) -> {
		map.put(Solid, SolidProperty.SOLID);
		map.put(Attack, new DestructibleProperty(tileType, new RequiredTool(ToolType.Axe)));
		map.put(Render, new AnimationProperty(tileType, true, AnimationType.SINGLE_FRAME));
		map.put(Interact, (InteractableProperty) (p, i, t) -> {
			t.replaceTile(TileType.valueOf("DOOR_OPEN"));
			return true;
		});
	}),
	
	DOOR_OPEN((tileType, map) -> {
		map.put(Solid, SolidProperty.WALKABLE);
		map.put(Render, new AnimationProperty(tileType, false, AnimationType.SINGLE_FRAME));
		map.put(Transition, new TransitionProperty(tileType,
			new TransitionAnimation("open", true, 1/24f, DOOR_CLOSED),
			new TransitionAnimation("close", false, 1/24f, DOOR_CLOSED)
		));
		map.put(Interact, (InteractableProperty) (p, i, t) -> {
			t.replaceTile(DOOR_CLOSED);
			return true;
		});
		map.put(Attack, new DestructibleProperty(tileType, new ItemDrop(TileItem.get(TileType.DOOR_CLOSED)), new RequiredTool(ToolType.Axe)));
	}),
	
	TORCH((tileType, map) -> {
		map.put(Attack, new DestructibleProperty(tileType));
		map.put(Light, (LightProperty) () -> 2);
		map.put(Render, new AnimationProperty(tileType, false, AnimationType.SEQUENCE, 1/12f));
		map.put(Transition, new TransitionProperty(tileType, new TransitionAnimation("enter", true, 1/12f)));
	}),
	
	CACTUS((tileType, map) -> {
		map.put(Solid, SolidProperty.SOLID);
		map.put(Attack, new DestructibleProperty(tileType, 7, null));
		map.put(Render, new AnimationProperty(tileType, false, AnimationType.SINGLE_FRAME));
		map.put(Touch, (TouchListener) (e, t, initial) -> e.attackedBy(t, null, 1));
	}),
	
	TREE((tileType, map) -> {
		map.put(Solid, SolidProperty.SOLID);
		map.put(Attack, new DestructibleProperty(tileType, 10, new PreferredTool(ToolType.Axe, 2), new ItemDrop(ResourceItem.Log.get()), new ItemDrop(FoodItem.Apple.get())));
		map.put(Render, new AnimationProperty(tileType, false, AnimationType.SINGLE_FRAME));
		map.put(Connect, new ConnectionProperty(tileType, true));
	});
	
	/*LAVA(() -> new TilePropertyInstance[] {
		map.put(Touch, (TouchListener) (e, t) -> e.attackedBy(t, null, 5));
		map.put(Render, new AnimationProperty.RandomFrame(0.1f);
	});*/
	
	/*
		Others:
		wheat, farmland, door, floor, wall, stairs?, sapling, torch, ore, ice, cloud?,
		laser source, laser, mirror, laser receiver.
	 */
	
	
	private final HashMap<TilePropertyType, TilePropertyInstance> propertyMap = new HashMap<>();
	
	private final HashMap<TilePropertyType, Integer> propertyDataIndexes = new HashMap<>();
	private final HashMap<TilePropertyType, Integer> propertyDataLengths = new HashMap<>();
	private String[] initialData;
	
	interface PropertyAdder {
		void addProperties(TileType tileType, HashMap<TilePropertyType, TilePropertyInstance> map);
	}
	
	private final PropertyAdder adder;
	
	TileType(@NotNull PropertyAdder adder) {
		this.adder = adder;
	}
	
	public static final TileType[] values = TileType.values();
	
	static {
		for(TileType type: TileType.values)
			type.init();
	}
	
	private void init() {
		// add the default properties
		
		for(TilePropertyType type: TilePropertyType.values)
			propertyMap.put(type, type.getDefaultInstance(this));
		
		// replace the defaults with specified properties
		adder.addProperties(this, propertyMap);
		
		Array<String> initData = new Array<>();
		
		for(TilePropertyType propType: propertyMap.keySet()) {
			TilePropertyInstance prop = propertyMap.get(propType);
			propertyDataIndexes.put(propType, initData.size);
			String[] propData = prop.getInitialData();
			propertyDataLengths.put(propType, propData.length);
			initData.addAll(propData);
		}
		
		initialData = new String[initData.size];
		for(int i = 0; i < initialData.length; i++)
			initialData[i] = initData.get(i);
	}
	
	public <T extends TilePropertyInstance> T getProp(TilePropertyType<T> propertyType) {
		//noinspection unchecked
		return (T) propertyMap.get(propertyType);
	}
	
	public <T extends TilePropertyInstance> T getProp(TilePropertyType<? super T> propertyType, Class<T> asClass) {
		//noinspection unchecked
		return (T) getProp(propertyType);
	}
	
	int getDataLength() { return initialData.length; }
	String[] getInitialData() { return Arrays.copyOf(initialData, initialData.length); }
	
	void checkDataAccess(TilePropertyType propertyType, int propDataIndex) {
		// technically, the below should never happen, unless it's passed the TilePropertyInstance class or a dynamically generated class, or something, because the propertyMap should have an instance of each implementer of the TilePropertyInstance interface.
		if(!propertyDataIndexes.containsKey(propertyType))
			throw new IllegalArgumentException("The specified property type, " + propertyType + ", is not part of the list of property classes for the "+this+" tile type.");
		
		if(propDataIndex >= propertyDataLengths.get(propertyType))
			throw new IllegalArgumentException("Tile property type " + propertyType + ", for the "+this+" tile type, tried to access index past stated length; length="+propertyDataLengths.get(propertyType)+", index="+propDataIndex);
	}
	
	int getPropDataIndex(TilePropertyType type) { return propertyDataIndexes.get(type); }
	int getPropDataLength(TilePropertyType type) { return propertyDataLengths.get(type); }
	
	public String getName() { return MyUtils.toTitleCase(name()); }
	
	public static String[] getJoinedInitialData(TileType... types) {
		int len = 0;
		for(TileType type: types)
			len += type.getDataLength();
		
		String[] data = new String[len];
		
		int offset = 0;
		for(TileType type: types) {
			String[] typeData = type.getInitialData();
			System.arraycopy(typeData, 0, data, offset, typeData.length);
			offset += typeData.length;
		}
		
		return data;
	}
}
