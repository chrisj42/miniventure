package miniventure.game.world.tile;

import java.util.EnumMap;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.WorldManager;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class SwimAnimation {
	
	private static final EnumMap<TileTypeEnum, TileAnimation<TextureHolder>> swimAnimations = new EnumMap<>(TileTypeEnum.class);
	
	private final TileAnimation<TextureHolder> swim;
	public final TileTypeEnum tileType;
	
	public SwimAnimation(@NotNull TileTypeEnum enumType) {
		this.tileType = enumType;
		
		swimAnimations.computeIfAbsent(enumType, k ->
			new TileAnimation<>(false, 1/16f, 
				GameCore.tileAtlas.findRegions(enumType.name().toLowerCase()+"/swim")
			)
		);
		
		swim = swimAnimations.get(enumType);
	}
	
	public void drawSwimAnimation(@NotNull Batch batch, @NotNull Vector2 center, @NotNull WorldManager world) {
		TextureHolder tex = swim.getKeyFrame(world);
		batch.draw(tex.texture, center.x-tex.width/2, center.y-tex.height/2);
	}
}
