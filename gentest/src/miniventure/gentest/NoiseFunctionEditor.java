package miniventure.gentest;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.Scrollable;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import miniventure.game.util.MyUtils;
import miniventure.game.world.levelgen.NamedNoiseFunction;
import miniventure.gentest.util.IntegerField;
import miniventure.gentest.util.MyPanel;
import miniventure.gentest.util.StringField;

import org.jetbrains.annotations.NotNull;

class NoiseFunctionEditor extends MyPanel implements NamedObject, Scrollable {
	
	private final TestPanel testPanel;
	private final NamedNoiseFunction noiseFunction;
	
	final JCheckBox randomSeed;
	final StringField seed;
	private final IntegerField numCurves;
	private final IntegerField coordsPerValue;
	
	NoiseFunctionEditor(@NotNull TestPanel testPanel, @NotNull NamedNoiseFunction noiseFunction) {
		this.testPanel = testPanel;
		this.noiseFunction = noiseFunction;
		
		randomSeed = new JCheckBox("Random seed", true);
		// add(randomSeed);
		
		seed = new StringField("", 20);
		seed.addValueListener(val -> {
			try {
				long s = Long.parseLong(val);
				noiseFunction.setSeed(s);
			} catch(NumberFormatException ex) {
				noiseFunction.setSeed(val.hashCode());
			}
		});
		
		seed.addKeyListener(new KeyAdapter() {
			@Override public void keyTyped(KeyEvent e) { randomSeed.setSelected(false); }
		});
		
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
			"seed:"+seed.getValue(),
			"random:"+randomSeed.isSelected(),
			"cpv:"+coordsPerValue.getValue(),
			"curves:"+numCurves.getValue()
		);
	}
	
	public NamedNoiseFunction getNoiseFunction() { return noiseFunction; }
	
	void generateSeed() {
		if(seed.getValue().length() == 0)
			randomSeed.setSelected(true);
		
		if(randomSeed.isSelected())
			seed.setValue(String.valueOf(new Random().nextLong()));
		
		noiseFunction.resetFunction();
		
		revalidate();
	}
	
	@Override
	public void setObjectName(@NotNull String name) {
		noiseFunction.setName(name);
		for(NoiseMapEditor editor: testPanel.getNoiseMapperPanel().getElements())
			editor.resetFunctionSelector();
	}
	@Override @NotNull
	public String getObjectName() { return noiseFunction.getName(); }
	
	@Override
	public String toString() { return getObjectName(); }
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getParent().getPreferredSize();
	}
	
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return visibleRect.height/10;
	}
	
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return visibleRect.height/3;
	}
	
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
	
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
