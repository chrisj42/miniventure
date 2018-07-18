package miniventure.gentest;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Scrollable;

import java.awt.Dimension;
import java.awt.Rectangle;

import miniventure.gentest.util.MyPanel;

import org.jetbrains.annotations.NotNull;

public abstract class NoisePanel extends MyPanel implements Scrollable {
	
	public abstract void setObjectName(@NotNull String name);
	@NotNull public abstract String getObjectName();
	
	protected void addLabeled(String label, JComponent component) {
		add(new JLabel(label));
		add(component);
	}
	
	@Override
	public String toString() { return getObjectName(); }
	
	@Override
	public Dimension getPreferredScrollableViewportSize() { return getParent().getPreferredSize(); }
	
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return visibleRect.height/10;
	}
	
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return visibleRect.height/3;
	}
	
	@Override public boolean getScrollableTracksViewportWidth() { return true; }
	@Override public boolean getScrollableTracksViewportHeight() { return false; }
}
