package miniventure.game.world.tile;

import java.awt.Color;

import miniventure.game.world.tile.TileType.TileTypeEnum;

public interface TileTypeInterface {
	
	TileTypeEnum getTypeEnum();
	String getName();
	boolean isWalkable();
	boolean isOpaque();
	float getSpeedRatio();
	Color getColor();
	TileTypeEnum getUnderType();
	
	// will vary from client to server
	Object[] createDataArray();
	// Object[] createTopArray();
	
	boolean hasData(TileDataTag<?> dataTag);
	<T> T getData(TileDataTag<T> dataTag, Object[] dataArray);
	<T> void setData(TileDataTag<T> dataTag, T value, Object[] dataArray);
	
}
