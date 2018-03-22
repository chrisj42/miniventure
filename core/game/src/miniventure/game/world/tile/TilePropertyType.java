package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.world.tile.AnimationProperty.AnimationType;

// PT is the class required.
public final class TilePropertyType<PT extends TilePropertyInstance> {
	
	/*
		This specifies all the different top level property types all tile types have.
		Each property type will have:
			- a default value
		
		So this needs to be a bit different... instead of being an enum, it needs to be a list of classes, one for each property type. The class given will be the class that represents instances of that type.
		
	 */
	
	private static int curOrdinal = 0;
	
	/* --- TYPE DEFINITIONS --- */
	
	
	public static final TilePropertyType<AnimationProperty> Render = new TilePropertyType<>(tileType -> new AnimationProperty(tileType, true, AnimationType.SINGLE_FRAME));
	
	public static final TilePropertyType<OverlapProperty> Overlap = new TilePropertyType<>(tileType -> new OverlapProperty(tileType, false));
	
	public static final TilePropertyType<ConnectionProperty> Connect = new TilePropertyType<>(tileType -> new ConnectionProperty(tileType, false));
	
	public static final TilePropertyType<LightProperty> Light = new TilePropertyType<>(tileType -> () -> 0);
	
	public static final TilePropertyType<UpdateProperty> Update = new TilePropertyType<>(tileType -> (delta, tile) -> {});
	
	public static final TilePropertyType<SolidProperty> Solid = new TilePropertyType<>(tileType -> SolidProperty.WALKABLE);
	
	public static final TilePropertyType<TouchListener> Touch = new TilePropertyType<>(tileType -> (entity, tile, initial) -> false);
	
	public static final TilePropertyType<DestructibleProperty> Attack = new TilePropertyType<>(DestructibleProperty::INDESTRUCTIBLE);
	
	public static final TilePropertyType<InteractableProperty> Interact = new TilePropertyType<>(tileType -> (p, i, t) -> false);
	
	public static final TilePropertyType<TransitionProperty> Transition = new TilePropertyType<>(tileType -> new TransitionProperty(tileType));
	
	
	/* --- ENUMERATION SETUP --- */
	
	
	private static final HashMap<String, TilePropertyType<?>> nameToValue = new HashMap<>();
	private static final HashMap<TilePropertyType<?>, String> valueToName = new HashMap<>();
	static {
		nameToValue.put("Render", Render);
		nameToValue.put("Overlap", Overlap);
		nameToValue.put("Connect", Connect);
		nameToValue.put("Light", Light);
		nameToValue.put("Update", Update);
		nameToValue.put("Solid", Solid);
		nameToValue.put("Touch", Touch);
		nameToValue.put("AttackResponse", Attack);
		nameToValue.put("InteractResponse", Interact);
		nameToValue.put("Transition", Transition);
		
		for(String name: nameToValue.keySet())
			valueToName.put(nameToValue.get(name), name);
	}
	
	/** @noinspection unchecked*/
	public static TilePropertyType<? extends TilePropertyInstance>[] values() { return valueToName.keySet().toArray(new TilePropertyType[valueToName.size()]); }
	public static TilePropertyType valueOf(String str) { return nameToValue.get(str); }
	
	public static final TilePropertyType<? extends TilePropertyInstance>[] values = values();
	
	private final int ordinal;
	
	public String name() { return valueToName.get(this); }
	public int ordinal() { return ordinal; }
	
	@Override public int hashCode() { return ordinal(); }
	@Override public boolean equals(Object other) { return other instanceof TilePropertyType && ((TilePropertyType)other).ordinal() == ordinal(); }
	
	
	interface PropertyFetcher<PT extends TilePropertyInstance> {
		PT getProperty(TileType tileType);
	}
	
	/* --- INSTANCE DEFINITIONS --- */
	
	
	private final PropertyFetcher<PT> defaultInstance;
	
	private TilePropertyType(PropertyFetcher<PT> defaultInstance) {
		this.defaultInstance = defaultInstance;
		ordinal = curOrdinal;
		curOrdinal++;
	}
	
	public PT getDefaultInstance(TileType tileType) { return defaultInstance.getProperty(tileType); }
}
