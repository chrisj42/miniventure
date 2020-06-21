package miniventure.game.world.tile;

import miniventure.game.texture.TextureHolder;
import miniventure.game.world.management.WorldManager;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class SwimAnimation {
	
	static final TileAnimationSetFrames<TileType> swimAnimations = TileAnimationSetFrames.from(TileType::valueOf);
	
	private final Animation<TextureHolder> swim;
	public final float drawableHeight;
	
	public SwimAnimation(@NotNull TileType tileType) { this(tileType, 0.5f); }
	public SwimAnimation(@NotNull TileType tileType, float drawableHeight) { this(tileType, drawableHeight, new RenderStyle(4)); }
	public SwimAnimation(@NotNull TileType tileType, float drawableHeight, RenderStyle renderStyle) {
		this.drawableHeight = drawableHeight;
		
		this.swim = renderStyle.getAnimation(tileType, swimAnimations);
	}
	
	public void drawSwimAnimation(@NotNull SpriteBatch batch, @NotNull Vector2 center, @NotNull WorldManager world) { drawSwimAnimation(batch, center.x, center.y, world); }
	public void drawSwimAnimation(@NotNull SpriteBatch batch, float x, float y, @NotNull WorldManager world) {
		TextureHolder tex = swim.getKeyFrame(world.getGameTime());
		batch.draw(tex.texture, x-tex.width/2f, y-tex.height/2f);
	}
}
