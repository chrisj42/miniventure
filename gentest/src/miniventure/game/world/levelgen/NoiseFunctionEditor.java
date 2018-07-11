package miniventure.game.world.levelgen;

import javax.swing.JLabel;
import javax.swing.Scrollable;

import java.awt.Dimension;
import java.awt.Rectangle;

import miniventure.game.world.levelgen.util.IntegerField;
import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.levelgen.util.StickyValidatedField;
import miniventure.game.world.levelgen.util.StringField;
import miniventure.game.world.levelgen.util.ValidatedField;

import org.jetbrains.annotations.NotNull;

class NoiseFunctionEditor extends MyPanel implements NamedObject, Scrollable {
	
	private final NamedNoiseFunction noiseFunction;
	
	private IntegerField numCurves;
	private IntegerField coordsPerValue;
	private StringField seed;
	
	NoiseFunctionEditor(@NotNull NamedNoiseFunction noiseFunction) {
		this.noiseFunction = noiseFunction;
		seed = new StringField("", 10);
		seed.addValueListener(val -> {
			try {
				long s = Long.parseLong(val);
				noiseFunction.setSeed(s);
			} catch(NumberFormatException ex) {
				noiseFunction.setSeed(val);
			}
		});
		add(new JLabel("Seed (blank for random):"));
		add(seed);
		
		coordsPerValue = new IntegerField(noiseFunction.getCoordsPerValue(), 1);
		coordsPerValue.addValueListener(noiseFunction::setCoordsPerValue);
		add(new JLabel("density (higher = bigger areas):"));
		add(coordsPerValue);
		
		numCurves = new IntegerField(noiseFunction.getCurveCount(), 1);
		numCurves.addValueListener(noiseFunction::setCurveCount);
		add(new JLabel("num curves:"));
		add(numCurves);
	}
	
	public NamedNoiseFunction getNoiseFunction() { return noiseFunction; }
	
	@Override
	public void setObjectName(@NotNull String name) { noiseFunction.setObjectName(name); }
	@Override @NotNull
	public String getObjectName() { return noiseFunction.getObjectName(); }
	
	
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
