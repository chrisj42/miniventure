package miniventure.game.world.entity.mob;

import miniventure.game.util.Version;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.entity.ClassDataList;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class Crocodile extends MobAi {
	
	public Crocodile(@NotNull ServerWorld world) { super(world, AiType.Crocodile); }
	
	protected Crocodile(@NotNull ServerWorld world, ClassDataList allData, Version version, ValueFunction<ClassDataList> modifier) {
		super(world, allData, version, data -> {
			modifier.act(data);
			data.get(2).add(AiType.Crocodile.name());
		});
	}
	
	@Override
	public ClassDataList save() {
		ClassDataList allData = super.save();
		allData.get(allData.size()-1).remove(0); // we don't need this data, we already know it.
		return allData;
	}
	
	@Override
	public boolean canPermeate(Tile tile) {
		return tile.getType().getTypeEnum() == TileTypeEnum.WATER;
	}
}
