package miniventure.game.world.entity.mob;

import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.entity.EntityDataSet;
import miniventure.game.world.entity.EntitySpawn;
import miniventure.game.world.management.Level;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import org.jetbrains.annotations.NotNull;

public class Crocodile extends MobAi {
	
	protected Crocodile(@NotNull EntitySpawn info) { super(info, AiType.Crocodile); }
	
	protected Crocodile(@NotNull Level level, EntityDataSet allData, Version version, ValueAction<EntityDataSet> modifier) {
		super(level, allData, version, data -> {
			modifier.act(data);
			data.get("ai").add("type", AiType.Crocodile);
		});
	}
	
	@Override
	public EntityDataSet save() {
		EntityDataSet allData = super.save();
		allData.get("ai").remove("type"); // we don't need this data, we already know it.
		return allData;
	}
	
	@Override
	public boolean canPermeate(Tile tile) {
		return tile.getType() == TileType.WATER;
	}
}
