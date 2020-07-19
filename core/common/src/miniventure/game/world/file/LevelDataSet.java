package miniventure.game.world.file;

import java.util.List;

import miniventure.game.util.Version;
import miniventure.game.world.level.LevelId;
import miniventure.game.world.tile.TileStack.TileData;

import org.jetbrains.annotations.NotNull;

public class LevelDataSet {
	
	// used to interface between loaded level data and saved-to-file level data
	// only used for levels that have already been generated
	
	public final LevelId levelId;
	public final int width, height;
	public final Version dataVersion;
	public final TileData[][] tileData;
	public final List<String> entityData;
	
	public LevelDataSet(@NotNull Version dataVersion, LevelId levelId, int width, int height, TileData[][] tileData, List<String> entityData) {
		this.dataVersion = dataVersion;
		this.levelId = levelId;
		this.width = width;
		this.height = height;
		this.tileData = tileData;
		this.entityData = entityData;
	}
}
