package miniventure.game.world.tile;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class LiquidTileType extends GroundTileType {
	
	private final TileAnimation<TextureHolder> swim;
	
	LiquidTileType(@NotNull TileTypeEnum enumType, boolean walkable, DestructionManager destructionManager, TileTypeRenderer renderer) {
		this(enumType, walkable, destructionManager, renderer, new UpdateManager(enumType));
	}
	
	LiquidTileType(@NotNull TileTypeEnum enumType, boolean walkable, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		this(enumType, walkable, 0, destructionManager, renderer, updateManager);
	}
	
	LiquidTileType(@NotNull TileTypeEnum enumType, boolean walkable, float lightRadius, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		super(enumType, walkable, lightRadius, destructionManager, renderer, updateManager);
		
		swim = new TileAnimation<>(false, 1/16f, GameCore.tileAtlas.findRegions(enumType.name().toLowerCase()+"/swim"));
	}
	
	public void drawSwimAnimation(@NotNull Batch batch, @NotNull Vector2 center, @NotNull WorldManager world) {
		TextureHolder tex = swim.getKeyFrame(world);
		batch.draw(tex.texture, center.x-tex.width/2, center.y-tex.height/2);
	}
}
