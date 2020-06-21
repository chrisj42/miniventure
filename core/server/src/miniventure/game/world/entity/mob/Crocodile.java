package miniventure.game.world.entity.mob;

import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.entity.EntityDataSet;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class Crocodile extends MobAi {
	
	public Crocodile(@NotNull ServerWorld world) { super(world, AiType.Crocodile); }
	
	protected Crocodile(@NotNull ServerWorld world, EntityDataSet allData, Version version, ValueAction<EntityDataSet> modifier) {
		super(world, allData, version, data -> {
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
		return tile.getType().getTypeEnum() == TileTypeEnum.WATER;
	}
}
