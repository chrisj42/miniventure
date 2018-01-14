package miniventure.game.world.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import org.jetbrains.annotations.NotNull;

public class TextParticle extends BounceEntity {
	
	private static final BitmapFont font = new BitmapFont();
	
	@NotNull private final String text;
	@NotNull private final Color main, shadow;
	
	public TextParticle(@NotNull String text) { this(text, Color.RED); }
	public TextParticle(@NotNull String text, @NotNull Color main) { this(text, main, Color.BLACK); }
	public TextParticle(@NotNull String text, @NotNull Color main, @NotNull Color shadow) {
		super(new Sprite(), 2f);
		this.text = text;
		this.main = main;
		this.shadow = shadow;
	}
	
	@Override
	public void render(SpriteBatch batch, float delta) {
		Vector2 pos = getBounds().getCenter(new Vector2());
		pos.y += getZ();
		font.setColor(shadow);
		font.draw(batch, text, pos.x-1, pos.y+1, 0, Align.center, false);
		font.setColor(main);
		font.draw(batch, text, pos.x, pos.y, 0, Align.center, false);
	}
}
