package miniventure.game.world.entity.particle;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;

import org.jetbrains.annotations.NotNull;

public class TextParticle extends BounceEntity implements Particle {
	
	@NotNull private final String text;
	@NotNull private final BitmapFont font;
	@NotNull private final Color main, shadow;
	
	@NotNull private TextureRegion dummyTexture = new TextureRegion();
	private final float width, height; 
	
	public TextParticle(@NotNull String text) { this(text, Color.RED); }
	public TextParticle(@NotNull String text, @NotNull Color main) { this(text, main, Color.BLACK); }
	public TextParticle(@NotNull String text, @NotNull Color main, @NotNull Color shadow) {
		super(2f);
		this.text = text;
		this.main = main;
		this.shadow = shadow;
		this.font = GameCore.getFont();
		
		GlyphLayout layout = GameCore.getTextLayout(text);
		this.width = layout.width;
		this.height = layout.height;
	}
	
	@Override
	protected TextureRegion getSprite() { return dummyTexture; }
	
	@Override
	protected Rectangle getUnscaledBounds() {
		Rectangle bounds = super.getUnscaledBounds();
		bounds.width = width;
		bounds.height = height;
		return bounds;
	}
	
	@Override
	protected void drawSprite(SpriteBatch batch, float x, float y) {
		font.setColor(shadow);
		font.draw(batch, text, x-1, y+1, 0, Align.center, false);
		font.setColor(main);
		font.draw(batch, text, x, y, 0, Align.center, false);
	}
}
