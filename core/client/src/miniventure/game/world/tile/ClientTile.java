package miniventure.game.world.tile;

import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.level.ClientLevel;
import miniventure.game.world.level.Level;
import miniventure.game.world.tile.TileStack.TileData;

import org.jetbrains.annotations.NotNull;

public class ClientTile extends RenderTile {
	
	public ClientTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types, @NotNull SerialMap[] data) {
		this((ClientLevel)level, x, y, types, data);
	}
	public ClientTile(@NotNull ClientLevel level, int x, int y, @NotNull TileTypeEnum[] types, @NotNull SerialMap[] data) {
		super(level, x, y, types, data);
	}
	
	@NotNull @Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
	public void apply(TileData tileData, TileTypeEnum updatedType) {
		ClientTileStack newStack = makeStack(tileData.getTypes(), tileData.getDataMaps());
		for(TileTypeEnum type: getTypeStack().getEnumTypes(true)) {
			if(type == updatedType)
				continue; // forget the animation start time, because this is a new animation.
			Float curValue = getDataMap(type).get(TileCacheTag.AnimationStart);
			if(curValue == null)
				continue; // there's no data to transfer.
			SerialMap dest = newStack.getDataMap(type);
			if(dest != null) // can be null if the new stack is missing a type that the current stack has.
				dest.put(TileCacheTag.AnimationStart, curValue);
		}
		setTileStack(newStack);
	}
}
