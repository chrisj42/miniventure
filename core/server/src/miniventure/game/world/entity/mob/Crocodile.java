package miniventure.game.world.entity.mob;

import miniventure.game.util.Version;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.entity.ClassDataList;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType.TileTypeEnum;

public class Crocodile extends MobAi {
	
	public Crocodile() { super(AiType.Crocodile); }
	
	protected Crocodile(ClassDataList allData, Version version, ValueFunction<ClassDataList> modifier) {
		super(allData, version, data -> {
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
		return tile.getType().getEnumType() == TileTypeEnum.WATER;
	}
}
