package miniventure.game.world.tile;

import java.util.EnumMap;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.management.WorldManager;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class SwimAnimation {
	
	private static final EnumMap<TileTypeEnum, Animation<TextureHolder>> swimAnimations = new EnumMap<>(TileTypeEnum.class);
	
	private final Animation<TextureHolder> swim;
	public final TileTypeEnum tileType;
	public final float drawableHeight;
	
	public SwimAnimation(@NotNull TileTypeEnum enumType) { this(enumType, 0.5f); }
	public SwimAnimation(@NotNull TileTypeEnum enumType, float drawableHeight) {
		this.tileType = enumType;
		this.drawableHeight = drawableHeight;
		
		swim = swimAnimations.computeIfAbsent(enumType, k ->
			new Animation<>(16, 
				GameCore.tileAtlas.getRegions(enumType.name().toLowerCase()+"/swim")
			)
		);
	}
	
	public void drawSwimAnimation(@NotNull Batch batch, @NotNull Vector2 center, @NotNull WorldManager world) {
		TextureHolder tex = swim.getKeyFrame(world.getGameTime());
		batch.draw(tex.texture, center.x-tex.width/2f, center.y-tex.height/2f);
	}
	
	/*public String serialize() {
		return tileType.name()+','+drawableHeight;
	}
	
	public static SwimAnimation deserialize(String data) {
		String[] parts = data.split(",");
		return new SwimAnimation(TileTypeEnum.valueOf(parts[0]), Float.parseFloat(parts[1]));
	}*/
}
