package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @noinspection FrequentlyUsedInheritorInspection*/
public class ServerTileType extends TileType {
	
	private final TileType model;
	
	public ServerTileType(@NotNull TileType model) {
		super(model.getEnumType(), model.walkable, model.propertyMap, new ServerDestructionManager(model.destructionManager), new ServerTileRenderer(model.renderer), model.updateManager);
		this.model = model;
	}
	
	@Override
	public SerialMap getInitialData() {
		return model.getInitialData();
	}
	
	@Override
	public TileTypeEnum getEnumType() {
		return model.getEnumType();
	}
	
	@Override
	public boolean isWalkable() {
		return model.isWalkable();
	}
	
	@Override
	public float update(@NotNull Tile tile) {
		return model.update(tile);
	}
	
	@Override
	public boolean interact(@NotNull Tile tile, Player player, @Nullable Item item) {
		return model.interact(tile, player, item);
	}
	
	@Override
	public boolean touched(@NotNull Tile tile, Entity entity, boolean initial) {
		return model.touched(tile, entity, initial);
	}
	
	@Override
	public String toString() { return model.toString()+"(Server)"; }
}
