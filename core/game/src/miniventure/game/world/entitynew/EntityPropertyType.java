package miniventure.game.world.entitynew;

import java.util.Arrays;
import java.util.HashMap;

public final class EntityPropertyType<T extends EntityProperty> {
	/*
		attack, attack listeners, removal listener, renderer, updatables, movement listeners, interactions, light property
	 */
	
	
	/* --- TYPE DEFINITIONS --- */
	
	
	public static final EntityPropertyType<AttackProperty> AttackResult = new EntityPropertyType<>(AttackProperty.class, e -> (obj, item, dmg, entity) -> false);
	
	public static final EntityPropertyType<AttackListener> AttackListener = new EntityPropertyType<>(AttackListener.class, e -> (obj, item, dmg, entity) -> {});
	
	public static final EntityPropertyType<RemovalListener> RemovalListener = new EntityPropertyType<>(RemovalListener.class, e -> (level, entity) -> {});
	
	public static final EntityPropertyType<RenderProperty> Renderer = new EntityPropertyType<>(RenderProperty.class, e -> RenderProperty.DEFAULT);
	
	public static final EntityPropertyType<UpdateListener> Updatable = new EntityPropertyType<>(UpdateListener.class, e -> (delta, entity) -> {});
	
	public static final EntityPropertyType<MoveListener> MoveListener = new EntityPropertyType<>(MoveListener.class, e -> (deltaPos, entity) -> {});
	
	public static final EntityPropertyType<InteractionProperty> InteractAction = new EntityPropertyType<>(InteractionProperty.class, e -> (player, item, entity) -> false);
	
	public static final EntityPropertyType<LightProperty> LightEmitter = new EntityPropertyType<>(LightProperty.class, e -> (entity) -> 0);
	
	public static final EntityPropertyType<SolidProperty> Tangibility = new EntityPropertyType<>(SolidProperty.class, e -> (other, entity) -> false);
	
	public static final EntityPropertyType<TouchListener> TouchListener = new EntityPropertyType<>(TouchListener.class, e -> (other, initial, entity) -> false);
	
	
	/* --- ENUMERATION SETUP --- */
	
	
	/** @noinspection unchecked*/
	public static final EntityPropertyType<? extends EntityProperty>[] values = new EntityPropertyType[] {
		AttackResult, AttackListener, RemovalListener, Renderer, Updatable, MoveListener, InteractAction, LightEmitter, Tangibility, TouchListener
	};
	
	private static final HashMap<EntityPropertyType<?>, Integer> ordinalToValue = new HashMap<>();
	static {
		for(int i = 0; i < values.length; i++)
			ordinalToValue.put(values[i], i);
	}
	
	private static final HashMap<String, EntityPropertyType<?>> nameToValue = new HashMap<>();
	private static final HashMap<EntityPropertyType<?>, String> valueToName = new HashMap<>();
	static {
		nameToValue.put("AttackResult", AttackResult);
		nameToValue.put("AttackListener", AttackListener);
		nameToValue.put("RemovalListener", RemovalListener);
		nameToValue.put("Renderer", Renderer);
		nameToValue.put("Updatable", Updatable);
		nameToValue.put("MoveListener", MoveListener);
		nameToValue.put("InteractAction", InteractAction);
		nameToValue.put("LightEmitter", LightEmitter);
		nameToValue.put("Tangibility", Tangibility);
		nameToValue.put("TouchListener", TouchListener);
	
		for(String name: nameToValue.keySet())
			valueToName.put(nameToValue.get(name), name);
	}
	
	public static EntityPropertyType<? extends EntityProperty>[] values() { return Arrays.copyOf(values, values.length); }
	public static EntityPropertyType valueOf(String str) { return nameToValue.get(str); }
	
	public String name() { return valueToName.get(this); }
	public int ordinal() { return ordinalToValue.get(this); }
	
	@Override public int hashCode() { return ordinal(); }
	@Override public boolean equals(Object other) { return other instanceof EntityPropertyType && ((EntityPropertyType)other).ordinal() == ordinal(); }
	
	
	interface PropertyFetcher<PT extends EntityProperty> {
		PT getProperty(Entity e);
	}
	
	
	/* --- INSTANCE DEFINITIONS --- */
	
	
	private final Class<T> baseClass;
	private final PropertyFetcher<T> defaultPropertyFetcher;
	
	EntityPropertyType(Class<T> baseClass, PropertyFetcher<T> defaultPropertyFetcher) {
		this.baseClass = baseClass;
		this.defaultPropertyFetcher = defaultPropertyFetcher;
	}
	
	public Class<T> getPropertyClass() { return baseClass; }
	
	public T getDefaultInstance(Entity entity) { return defaultPropertyFetcher.getProperty(entity); }
}
