package miniventure.game.world.tile;

import miniventure.game.world.level.ClientLevel;
import miniventure.game.world.tile.TileStack.TileData;

import org.jetbrains.annotations.NotNull;

public class ClientTile extends RenderTile {
	
	public ClientTile(@NotNull ClientLevel level, int x, int y) {
		super(level, x, y);
	}
	
	@NotNull @Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
	@Override
	public void setStack(TileTypeInfo[] stackInfo) {
		getLevel().animStartTimes.clear(this); // clear start time; this is probably good enough for now..?
		super.setStack(stackInfo);
		queueSpriteUpdate();
	}
	
}
