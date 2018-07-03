package miniventure.game.world.tile;

import org.jetbrains.annotations.NotNull;

public class ServerTileType extends TileType {
	
	public ServerTileType(@NotNull TileType model) {
		super(model.getEnumType(), new ServerDestructionManager(model.destructionManager), new ServerTileRenderer(model.renderer), model.updateManager);
	}
}
