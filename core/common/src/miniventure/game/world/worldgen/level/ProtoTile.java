package miniventure.game.world.worldgen.level;

import miniventure.game.world.Point;
import miniventure.game.world.level.Level;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileLayer;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProtoTile {
	
	// experimental attempt to reduce number of (empty) arrays made during stack.toArray calls.
	// private static final TileTypeEnum[] EMPTY = new TileTypeEnum[0];
	
	/*public interface TileMaker {
		Tile makeTile(@NotNull Level level, )
	}*/
	
	public final Point pos;
	public final int id;
	// private final LinkedList<TileTypeEnum> stack;
	private final TileTypeEnum[] types;
	
	ProtoTile(int x, int y, int id) {
		pos = new Point(x, y);
		this.id = id;
		// stack = new LinkedList<>();
		types = new TileTypeEnum[TileLayer.values.length];
		types[0] = TileTypeEnum.HOLE;
	}
	
	public void addLayer(@NotNull TileTypeEnum type) {
		types[type.layer.ordinal()] = type;
	}
	
	public boolean hasType(@NotNull TileTypeEnum type) {
		return types[type.layer.ordinal()] == type;
	}
	
	@NotNull
	public TileTypeEnum getTopLayer() {
		for(int i = types.length-1; i >= 0; i--) {
			if(types[i] != null) return types[i];
		}
		return TileTypeEnum.HOLE;
	}
	
	// @NotNull
	// public TileTypeEnum[] getStack() { return stack.toArray(EMPTY); }
	
	public TileTypeEnum[] getTypes() { return types; }
	
	public float getVal(float[][] noise) { return noise[pos.x][pos.y]; }
}
