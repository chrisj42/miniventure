package miniventure.game.world.entity.particle;

import java.util.Arrays;

import miniventure.game.GameCore;
import miniventure.game.util.Version;
import miniventure.game.world.WorldManager;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class TextParticle extends BounceEntity implements Particle {
	
	@NotNull private final String text;
	@NotNull private final Color main, shadow;
	
	@NotNull private TextureRegion dummyTexture = new TextureRegion();
	private final float width, height; 
	
	public TextParticle(@NotNull WorldManager world, @NotNull String text) { this(world, text, Color.RED); }
	public TextParticle(@NotNull WorldManager world, @NotNull String text, @NotNull Color main) { this(world, text, main, Color.BLACK); }
	public TextParticle(@NotNull WorldManager world, @NotNull String text, @NotNull Color main, @NotNull Color shadow) {
		super(world, null, 2f);
		this.text = text;
		this.main = main;
		this.shadow = shadow;
		
		GlyphLayout layout = GameCore.getTextLayout(text);
		this.width = layout.width;
		this.height = layout.height;
	}
	
	protected TextParticle(@NotNull WorldManager world, String[][] allData, Version version) {
		super(world, Arrays.copyOfRange(allData, 0, allData.length-1), version);
		String[] data = allData[allData.length-1];
		this.text = data[0];
		main = Color.valueOf(data[1]);
		shadow = Color.valueOf(data[2]);
		
		GlyphLayout layout = GameCore.getTextLayout(text);
		this.width = layout.width;
		this.height = layout.height;
	}
	
	@Override
	public Array<String[]> save() {
		Array<String[]> data = super.save();
		
		data.add(new String[] {
			text,
			main.toString(),
			shadow.toString()
		});
		
		return data;
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
		BitmapFont font = GameCore.getFont();
		font.setColor(shadow);
		font.draw(batch, text, x-1, y+1, 0, Align.center, false);
		font.setColor(main);
		font.draw(batch, text, x, y, 0, Align.center, false);
	}
}
