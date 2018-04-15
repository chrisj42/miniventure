package miniventure.game.world.entity.particle;

import java.util.Arrays;

import miniventure.game.util.Version;
import miniventure.game.world.entity.EntityRenderer.TextRenderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class TextParticle extends BounceEntity implements Particle {
	
	@NotNull private final String text;
	@NotNull private final Color main, shadow;
	
	public TextParticle(@NotNull String text) { this(text, Color.RED); }
	public TextParticle(@NotNull String text, @NotNull Color main) { this(text, main, Color.BLACK); }
	public TextParticle(@NotNull String text, @NotNull Color main, @NotNull Color shadow) {
		super(null, 2f);
		this.text = text;
		this.main = main;
		this.shadow = shadow;
		setRenderer(new TextRenderer(text, main, shadow));
	}
	
	protected TextParticle(String[][] allData, Version version) {
		super(Arrays.copyOfRange(allData, 0, allData.length-1), version);
		String[] data = allData[allData.length-1];
		this.text = data[0];
		main = Color.valueOf(data[1]);
		shadow = Color.valueOf(data[2]);
		
		setRenderer(new TextRenderer(text, main, shadow));
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
}
