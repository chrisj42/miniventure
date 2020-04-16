package miniventure.game.world.tile;

import miniventure.game.world.level.ClientLevel;
import miniventure.game.world.level.Level;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class ClientTile extends RenderTile {
	
	/*public ClientTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types, @NotNull TileDataMap[] data) {
		this((ClientLevel)level, x, y, types, data);
	}
	public ClientTile(@NotNull ClientLevel level, int x, int y, @NotNull TileTypeEnum[] types, @NotNull TileDataMap[] data) {
		super(level, x, y, types, data);
	}*/
	public ClientTile(@NotNull Level level, int x, int y) {
		this((ClientLevel)level, x, y);
	}
	public ClientTile(@NotNull ClientLevel level, int x, int y) {
		super(level, x, y, new TileTypeEnum[] {TileTypeEnum.HOLE}, null);
	}
	
	@NotNull @Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
	public void apply(TileData tileData, TileTypeEnum updatedType) {
		// ClientTileStack newStack = makeStack(tileData.getTypes(), tileData.getDataMaps());
		Float[] animStarts = new Float[getStackSize()];
		TileTypeEnum[] newStack = tileData.getTypes();
		for (int i = 0; i < getStackSize(); i++) {
			animStarts[i] =
				newStack[i] == getLayer(i).getTypeEnum() && getLayer(i).hasData(TileDataTag.AnimationStart)
				? setContext(i).getData(TileDataTag.AnimationStart)
				: null;
		}
		setStack(newStack, tileData.getDataMaps());
		for (int i = 0; i < getStackSize(); i++) {
			TileContext context = setContext(i);
			// ClientTileType type = (ClientTileType) getLayer(i);
			if(context.getType().getTypeEnum() == updatedType)
				continue; // forget the animation start time, because this is a new animation.
			Float curValue = animStarts[i];
			if(curValue == null)
				continue; // there's no data to transfer.
			// TileTypeDataMap dest = newStack.getDataMap(type);
			// if(dest != null) // can be null if the new stack is missing a type that the current stack has.
			// 	dest.add(TileDataTag.AnimationStart, curValue);
			context.setData(TileDataTag.AnimationStart, curValue);
		}
	}
}
