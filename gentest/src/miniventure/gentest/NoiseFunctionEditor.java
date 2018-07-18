package miniventure.gentest;

import javax.swing.JLabel;

import miniventure.game.util.MyUtils;
import miniventure.game.world.levelgen.NamedNoiseFunction;
import miniventure.gentest.util.IntegerField;

import org.jetbrains.annotations.NotNull;

class NoiseFunctionEditor extends NoisePanel {
	
	private final TestPanel testPanel;
	private final NamedNoiseFunction noiseFunction;
	
	// final JCheckBox randomSeed;
	// final StringField seed;
	private final IntegerField numCurves;
	private final IntegerField coordsPerValue;
	
	NoiseFunctionEditor(@NotNull TestPanel testPanel, @NotNull NamedNoiseFunction noiseFunction) {
		this.testPanel = testPanel;
		this.noiseFunction = noiseFunction;
		
		// add(new JLabel("Seed:"));
		// add(seed);
		
		coordsPerValue = new IntegerField(noiseFunction.getCoordsPerValue(), 3, 1);
		coordsPerValue.addValueListener(noiseFunction::setCoordsPerValue);
		add(new JLabel("Size:"));
		add(coordsPerValue);
		
		numCurves = new IntegerField(noiseFunction.getCurveCount(), 2, 0);
		numCurves.addValueListener(noiseFunction::setCurveCount);
		add(new JLabel("Curves:"));
		add(numCurves);
	}
	
	public String getData() {
		return MyUtils.encodeStringArray(
			"name:"+getObjectName(),
			// "seed:"+seed.getValue(),
			// "random:"+randomSeed.isSelected(),
			"cpv:"+coordsPerValue.getValue(),
			"curves:"+numCurves.getValue()
		);
	}
	
	public NamedNoiseFunction getNoiseFunction() { return noiseFunction; }
	
	/*void generateSeed() {
		if(seed.getValue().length() == 0)
			randomSeed.setSelected(true);
		
		if(randomSeed.isSelected())
			seed.setValue(String.valueOf(new Random().nextLong()));
		
		noiseFunction.resetFunction();
		
		revalidate();
	}*/
	
	@Override
	public void setObjectName(@NotNull String name) { noiseFunction.setName(name); }
	@Override @NotNull
	public String getObjectName() { return noiseFunction.getName(); }
	
}
