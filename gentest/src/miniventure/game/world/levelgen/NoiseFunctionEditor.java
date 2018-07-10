package miniventure.game.world.levelgen;

import javax.swing.Scrollable;

import java.awt.Dimension;
import java.awt.Rectangle;

import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.levelgen.util.StickyValidatedField;
import miniventure.game.world.levelgen.util.ValidatedField;

import org.jetbrains.annotations.NotNull;

class NoiseFunctionEditor extends MyPanel implements NamedObject, Scrollable {
	
	private final NamedNoiseFunction noiseFunction;
	
	private StickyValidatedField<String> name;
	private ValidatedField<Integer> numCurves;
	
	NoiseFunctionEditor(@NotNull NamedNoiseFunction noiseFunction) {
		this.noiseFunction = noiseFunction;
		
		name = new StickyValidatedField<>(noiseFunction.getObjectName(), String::valueOf, String::toString, str -> {
			if(str.length() == 0) return false;
			return true; // TODO check for duplicates
		});
		
		numCurves = new ValidatedField<>(Integer::parseInt, ValidatedField.POSITIVE);
	}
	
	public NamedNoiseFunction getNoiseFunction() { return noiseFunction; }
	
	@Override
	public void setObjectName(@NotNull String name) {
		this.name.setText(name);
	}
	
	@NotNull
	@Override
	public String getObjectName() {
		return name.getValue();
	}
	
	
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
