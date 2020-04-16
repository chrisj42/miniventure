package miniventure.game.world.tile;

import java.awt.Color;

import miniventure.game.util.function.MapFunction;
import miniventure.game.util.param.Param;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.UpdateManager.UpdateAction;

interface TP {
	
	Param<Color> COLOR = new Param<>(Color.BLACK);
	Param<TileTypeEnum> UNDER = new Param<>(TileTypeEnum.HOLE);
	Param<Float> SPEED = new Param<>(1f);
	
	Param<DestructionManager> DESTRUCT = new Param<>(DestructionManager.INDESTRUCTIBLE);
	Param<UpdateAction> UPDATE = new Param<>(null);
	
	Param<Float> LIGHT = new Param<>(0f);
	
	class SpriteParam<T> extends Param<MapFunction<TileTypeEnum, T>> {
		public SpriteParam(MapFunction<TileTypeEnum, T> defaultValue) {
			super(defaultValue);
		}
	}
	
	SpriteParam<SwimAnimation> SWIM = new SpriteParam<>(type -> null);
	SpriteParam<TileTypeRenderer> RENDER = new SpriteParam<>(type -> TileTypeRenderer.buildRenderer(type).build());
	
}
