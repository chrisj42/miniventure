package miniventure.game.world.tile;

import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.Tile.TileContext;

public class TileStackData {
	
	// TODO first is stack-wide tile data, then is each layer's data
	
	private String[][] stackData;
	
	TileStackData(String[] data) {
		this.stackData = new String[data.length][];
		for(int i = 0; i < data.length; i++)
			this.stackData[i] = MyUtils.parseLayeredString(data[i]);
	}
	
	void deserializeTypeData(int layer, TileContext context) {
		for (int j = 0; j < stackData[layer].length; j+=2) {
			TileDataTag<?> tag = TileDataTag.valueOf(Integer.parseInt(stackData[layer][j]));
			String data = stackData[layer][j+1];
			deserializeData(tag, data, context);
		}
	}
	
	private static <T> void deserializeData(TileDataTag<T> tag, String data, TileContext context) {
		context.setData(tag, tag.deserialize(data));
	}
}
