package miniventure.gentest;

import javax.swing.BoxLayout;
import javax.swing.JLabel;

import miniventure.game.world.levelgen.NamedNoiseFunction;

import org.jetbrains.annotations.NotNull;

public class NoiseFunctionViewer extends NoisePanel {
	
	@NotNull private final NamedNoiseFunction noiseFunction;
	
	public NoiseFunctionViewer(@NotNull NamedNoiseFunction function) {
		this.noiseFunction = function;
		
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(new JLabel("Size: "+noiseFunction.getCoordsPerValue()));
		add(new JLabel("Curves: "+noiseFunction.getCurveCount()));
	}
	
	@Override
	public void setObjectName(@NotNull String name) {
		noiseFunction.setName(name);
	}
	
	@NotNull
	@Override
	public String getObjectName() {
		return noiseFunction.getName();
	}
}
