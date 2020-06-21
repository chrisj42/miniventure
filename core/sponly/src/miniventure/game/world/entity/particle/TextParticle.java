package miniventure.game.world.entity.particle;

import miniventure.game.core.FontStyle;
import miniventure.game.core.GdxCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.entity.EntitySpawn;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import org.jetbrains.annotations.NotNull;

public class TextParticle extends Particle {
	
	@NotNull private final BounceBehavior bounceBehavior;
	// @NotNull private final LifetimeTracker lifetime;
	private final String text;
	private final Color main;
	private final Color shadow;
	
	public TextParticle(@NotNull EntitySpawn info, String text) { this(info, text, Color.RED); }
	public TextParticle(@NotNull EntitySpawn info, String text, Color main) { this(info, text, main, Color.BLACK); }
	public TextParticle(@NotNull EntitySpawn info, String text, Color main, Color shadow) {
		super(info, 2f, true);
		this.text = text;
		this.main = main;
		this.shadow = shadow;
		// lifetime = new LifetimeTracker(this, 2f);
		
		bounceBehavior = new BounceBehavior(this, (Vector2)null);
		bounceBehavior.scaleVelocity(MathUtils.random(0.5f, 2.5f));
		
		// setRenderer(this);
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		bounceBehavior.update(delta);
		// lifetime.update(delta);
	}
	
	@Override
	protected TextureHolder getSprite() {
		return null;
	}
	
	@Override
	public void render(TextureHolder texture, float x, float y, SpriteBatch batch, float drawableHeight) {
		BitmapFont font = GdxCore.getFont(FontStyle.KeepSizeScaled);
		font.setColor(shadow);
		font.draw(batch, text, x-Tile.SCALE, y+ Tile.SCALE, 0, Align.center, false);
		font.setColor(main);
		try {
			font.draw(batch, text, x, y, 0, Align.center, false);
		} catch(Exception e) {
			System.err.println("error drawing text");
			e.printStackTrace();
		}
	}
	
	
}
