package miniventure.game.world.tile;

import java.util.EnumMap;

import miniventure.game.util.function.MapFunction;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class TileEnumMapper<T> {
	
	private final EnumMap<TileTypeEnum, T> overrides = new EnumMap<>(TileTypeEnum.class);
	
	private final MapFunction<T> mapper;
	
	public TileEnumMapper(@NotNull MapFunction<T> mapper) {
		this.mapper = mapper;
	}
	
	public T mapValue(@NotNull TileTypeEnum enumValue, @NotNull T original) {
		if(!overrides.containsKey(enumValue))
			overrides.put(enumValue, mapper.get(original));
		
		return overrides.get(enumValue);
	}
	
}
