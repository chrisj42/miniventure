package miniventure.game.world.entity.particle;

import java.util.ArrayList;
import java.util.Arrays;

import miniventure.game.world.entity.ClassDataList;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.entity.EntityRenderer.TextRenderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.NotNull;

public class TextParticle extends BounceEntity implements Particle {
	
	@NotNull private final String text;
	@NotNull private final Color main, shadow;
	
	public TextParticle(@NotNull String text) { this(text, Color.RED); }
	public TextParticle(@NotNull String text, @NotNull Color main) { this(text, main, Color.BLACK); }
	public TextParticle(@NotNull String text, @NotNull Color main, @NotNull Color shadow) {
		super(null, 2f);
		scaleVelocity(MathUtils.random(0.5f, 2.5f));
		this.text = text;
		this.main = main;
		this.shadow = shadow;
		setRenderer(new TextRenderer(text, main, shadow));
	}
	
	protected TextParticle(ClassDataList allData, final Version version, ValueFunction<ClassDataList> modifier) {
		super(allData, version, modifier);
		ArrayList<String> data = allData.get(2);
		this.text = data.get(0);
		main = Color.valueOf(data.get(1));
		shadow = Color.valueOf(data.get(2));
		
		setRenderer(new TextRenderer(text, main, shadow));
	}
	
	@Override
	public ClassDataList save() {
		ClassDataList allData = super.save();
		ArrayList<String> data = new ArrayList<>(Arrays.asList(
			text,
			main.toString(),
			shadow.toString()
		));
		
		allData.add(data);
		return allData;
	}
}
