package miniventure.game.screen;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.HashMap;

import miniventure.game.util.Action;
import miniventure.game.util.RelPos;

public class AnchorPanel extends JPanel {
	
	private final JFrame frame;
	private final HashMap<Component, Action> layoutActions = new HashMap<>(); 
	
	public AnchorPanel(JFrame frame) {
		super(null);
		this.frame = frame;
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				SwingUtilities.invokeLater(() -> setSize(frame.getContentPane().getSize()));
			}
		});
		
		addContainerListener(new ContainerListener() {
			@Override
			public void componentAdded(final ContainerEvent e) {
				repaint();
			}
			
			@Override
			public void componentRemoved(final ContainerEvent e) {
				layoutActions.remove(e.getChild());
				repaint();
			}
		});
		
		setFocusable(false);
		setOpaque(false);
		
		frame.add(this);
	}
	
	@Override
	public void setLayout(final LayoutManager mgr) {
		if(mgr != null)
			System.err.println("tried to set layout of anchor panel "+this+" to "+mgr+"; Anchor Panels cannot have layouts.");
		else
			super.setLayout(null);
	}
	
	@Override
	public void doLayout() {
		// System.out.println("laying out anchor panel "+this);
		for(Action a: layoutActions.values())
			a.act();
		super.doLayout();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return frame.getContentPane().getPreferredSize();
	}
	
	@Override
	public Dimension getMinimumSize() {
		return frame.getContentPane().getMinimumSize();
	}
	
	@Override
	public Dimension getMaximumSize() {
		return frame.getContentPane().getMaximumSize();
	}
	
	@Override
	protected void addImpl(final Component comp, final Object constraints, final int index) {
		super.addImpl(comp, constraints, index);
		if(comp instanceof MenuScreen)
			((MenuScreen)comp).setupAnchorLayout(this);
	}
	
	public void addToAnchorLayout(Component comp, RelPos componentAnchor, RelPos containerAnchor, int anchorOffsetX, int anchorOffsetY) {
		layoutActions.put(comp, () -> {
			comp.invalidate();
			comp.setSize(comp.getPreferredSize());
			Point compAnchor = componentAnchor.forRectangle(comp.getBounds());
			Point parentAnchor = containerAnchor.forRectangle(getBounds());
			
			Point compPos = comp.getLocation();
			Point locAnchorDifference = new Point(compPos.x - compAnchor.x, compPos.y - compAnchor.y);
			
			parentAnchor.translate(anchorOffsetX, anchorOffsetY);
			comp.setLocation(parentAnchor.x + locAnchorDifference.x, parentAnchor.y + locAnchorDifference.y);
		});
	}
	public void addToAnchorLayout(Component comp, RelPos componentAnchor, RelPos containerAnchor) {
		addToAnchorLayout(comp, componentAnchor, containerAnchor, 0, 0);
	}
	public void addToAnchorLayout(Component comp, RelPos anchor, int anchorOffsetX, int anchorOffsetY) {
		addToAnchorLayout(comp, anchor, anchor, anchorOffsetX, anchorOffsetY);
	}
	public void addToAnchorLayout(Component comp, RelPos anchor) {
		addToAnchorLayout(comp, anchor, anchor);
	}
}
