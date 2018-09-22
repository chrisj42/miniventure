package miniventure.game.screen;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import miniventure.game.util.RelPos;

public final class ClientUtils {
	
	private ClientUtils() {
	}
	
	
	public static void setupTransparentSwingContainer(JPanel panel) {
		com.sun.awt.AWTUtilities.setComponentMixingCutoutShape(panel, new Rectangle());
	}
	
	
	public static void addTempListener(Container parent, Component comp, ComponentListener l) {
		parent.addComponentListener(l);
		parent.addContainerListener(new ContainerListener() {
			@Override
			public void componentAdded(ContainerEvent e) {
			}
			
			@Override
			public void componentRemoved(ContainerEvent e) {
				if(e.getChild() == comp) {
					parent.removeComponentListener(l);
					parent.removeContainerListener(this);
				}
			}
		});
	}
	
	
	// adds a listener to the parent to make sure the given component size always fills the entire space inside the parent. Starts at the given size.
	public static void trackParentSize(Component c, Container parent, Dimension size) {
		addTempListener(parent, c, new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				SwingUtilities.invokeLater(() -> c.setSize(parent.getSize()));
			}
		});
		
		c.setSize(size);
		// parent.add(c);
	}
	
	
	/**
	 * This method takes a component, and adds it to the given container with a set of positioning constraints; there were no layout managers
	 * that positioned components this way, so the container will have a null layout, and this system is like a pseudo-layout.
	 * <p>
	 * What it does is take the two given "anchors" and make sure the positions they represent are on top of one another, before the offset.
	 * The anchor positions are specified by RelPos instances, which refer to positions on a rectangle of the component's preferred size.
	 * <p>
	 * <p>
	 * It works like this. When the screen is resized:
	 * - It determines the coordinates of the container and component anchors.
	 * - It moves the component so the component anchor matches the position of the container anchor.
	 * - It moves the component by (anchorOffsetX, anchorOffsetY).
	 * And it does that for every component added.
	 * <p>
	 * Overlap between multiple components added through this method is not considered.
	 *
	 * @param comp            the component to be added to the parent panel and kept in layout.
	 * @param componentAnchor a RelPos position based on the size of the given component.
	 * @param parent          the container to which the component will be added, and kept in layout.
	 * @param containerAnchor a RelPos position based on the size of the HUD panel.
	 * @param anchorOffsetX   horizontal difference between the target position of the component anchor, and the position of the container anchor.
	 * @param anchorOffsetY   vertical difference between the target position of the component anchor, and the position of the container anchor.
	 */
	public static void addToAnchorLayout(Component comp, RelPos componentAnchor, Container parent, RelPos containerAnchor, int anchorOffsetX, int anchorOffsetY) {
		if(parent.getLayout() != null) {
			System.out.println("NOTE: removing layout " + parent.getLayout() + " from container " + parent + " in order to use anchor layout with component " + comp + ".");
			parent.setLayout(null); // parent cannot have layout for this to work.
		}
		ComponentListener l = new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				SwingUtilities.invokeLater(() -> {
					comp.setSize(comp.getPreferredSize());
					Point compAnchor = componentAnchor.forRectangle(comp.getBounds());
					Point parentAnchor = containerAnchor.forRectangle(parent.getBounds());
					
					Point compPos = comp.getLocation();
					Point locAnchorDifference = new Point(compPos.x - compAnchor.x, compPos.y - compAnchor.y);
					
					parentAnchor.translate(anchorOffsetX, anchorOffsetY);
					comp.setLocation(parentAnchor.x + locAnchorDifference.x, parentAnchor.y + locAnchorDifference.y);
				});
			}
		};
		addTempListener(parent, comp, l);
	}
	public static void addToAnchorLayout(Component comp, RelPos componentAnchor, Container parent, RelPos containerAnchor) {
		addToAnchorLayout(comp, componentAnchor, parent, containerAnchor, 0, 0);
	}
	public static void addToAnchorLayout(Component comp, Container parent, RelPos anchor, int anchorOffsetX, int anchorOffsetY) {
		addToAnchorLayout(comp, anchor, parent, anchor, anchorOffsetX, anchorOffsetY);
	}
	public static void addToAnchorLayout(Component comp, Container parent, RelPos anchor) {
		addToAnchorLayout(comp, anchor, parent, anchor);
	}
}
