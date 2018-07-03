package miniventure.game.world.tileold;

import miniventure.game.util.function.ValueFunction;

import org.jetbrains.annotations.NotNull;

class LightProperty extends TileProperty {
	
	private final ValueFunction<Float> lightRadius;
	
	LightProperty(@NotNull TileType tileType, ValueFunction<Float> lightRadius) {
		super(tileType);
		this.lightRadius = lightRadius;
	}
	LightProperty(@NotNull TileType tileType, float radius) {
		this(tileType, () -> radius);
	}
	
	public float getLightRadius() { return lightRadius.get(); }
	
}
