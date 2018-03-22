package miniventure.game.world.entity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

import miniventure.game.world.entity.EntityRenderer.AnimationRenderer;
import miniventure.game.world.entity.EntityRenderer.SpriteRenderer;
import miniventure.game.world.entity.EntityRenderer.TextRenderer;

public final class RenderStrategy<T extends EntityRenderer> {
	
	
	/* --- TYPE DEFINITIONS --- */
	
	
	public static final RenderStrategy<SpriteRenderer> Single = new RenderStrategy<>(SpriteRenderer::new);
	
	public static final RenderStrategy<AnimationRenderer> Animation = new RenderStrategy<>(AnimationRenderer::new);
	
	public static final RenderStrategy<TextRenderer> Text = new RenderStrategy<>(TextRenderer::new);
	
	
	
	/* --- ENUMERATION SETUP --- */
	
	
	private static final HashMap<String, RenderStrategy<?>> nameToValue = new HashMap<>();
	private static final HashMap<RenderStrategy<?>, String> valueToName = new HashMap<>();
	static {
		try {
			for(Field field: RenderStrategy.class.getDeclaredFields()) {
				if(RenderStrategy.class.isAssignableFrom(field.getType()))
					nameToValue.put(field.getName(), (RenderStrategy<?>)field.get(null));
			}
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
		
		for(String name: nameToValue.keySet())
			valueToName.put(nameToValue.get(name), name);
	}
	
	public static RenderStrategy<? extends EntityRenderer>[] values() { return Arrays.copyOf(values, values.length); }
	public static RenderStrategy valueOf(String str) { return nameToValue.get(str); }
	
	/** @noinspection unchecked*/
	public static final RenderStrategy<? extends EntityRenderer>[] values = values();
	
	private static final HashMap<RenderStrategy<?>, Integer> valueToOrdinal = new HashMap<>();
	static {
		for(int i = 0; i < values.length; i++)
			valueToOrdinal.put(values[i], i);
	}
	
	public String name() { return valueToName.get(this); }
	public int ordinal() { return valueToOrdinal.get(this); }
	
	@Override public int hashCode() { return ordinal(); }
	@Override public boolean equals(Object other) { return other instanceof RenderStrategy && ((RenderStrategy)other).ordinal() == ordinal(); }
	
	
	
	@FunctionalInterface
	interface RendererLoader<T extends EntityRenderer> {
		T getRenderer(String[] data);
	}
	
	
	/* --- INSTANCE DEFINITIONS --- */
	
	
	private final RendererLoader<T> loader;
	
	RenderStrategy(RendererLoader<T> loader) {
		this.loader = loader;
	}
	
	public EntityRenderer getRenderer(String[] data) { return loader.getRenderer(data); }
	
	public String[] save(T renderer) {
		String[] data = renderer.save();
		String[] allData = new String[data.length+1];
		allData[0] = name();
		System.arraycopy(data, 0, allData, 1, data.length);
		return allData;
	}
	
	public T load(String[] data) { return loader.getRenderer(data); }
	
}
