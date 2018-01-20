package miniventure.game.world.entity;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

import org.jetbrains.annotations.NotNull;

public class TextParticle extends BounceEntity {
	
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
	protected void drawSprite(SpriteBatch batch, float x, float y) {
		BitmapFont font = GameCore.getFont();
		font.setColor(shadow);
		font.draw(batch, text, x-1, y+1, 0, Align.center, false);
		font.setColor(main);
		font.draw(batch, text, x, y, 0, Align.center, false);
	}
}
