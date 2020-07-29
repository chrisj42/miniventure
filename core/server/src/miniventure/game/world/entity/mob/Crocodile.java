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
	
	protected Crocodile(@NotNull ServerWorld world, EntityDataSet data, Version version, ValueAction<EntityDataSet> modifier) {
		super(world, data, version, d -> {
			modifier.act(d);
			d.setPrefix("ai");
			d.add("type", AiType.Crocodile);
		});
	}
	
	@Override
	public EntityDataSet save() {
		EntityDataSet data = super.save();
		data.setPrefix("ai");
		data.remove("type"); // we don't need this data, we already know it.
		return data;
	}
	
	@Override
	public boolean canPermeate(Tile tile) {
		return tile.getType().getTypeEnum() == TileTypeEnum.WATER;
	}
}
