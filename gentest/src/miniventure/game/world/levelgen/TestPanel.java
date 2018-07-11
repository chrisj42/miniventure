package miniventure.game.world.levelgen;

import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import miniventure.game.util.Action;
import miniventure.game.world.levelgen.ListPanel.ElementContainer;
import miniventure.game.world.levelgen.util.MyPanel;

/**
 * Holds all the testing components in a JTabbedPane: Noise functions, Noise mappers, and the Map.
 */
public class TestPanel extends MyPanel {
	
	private final JTabbedPane tabPane;
	private final GlobalPanel globalPanel;
	private final ListPanel<NoiseFunctionEditor> noiseFunctionPanel;
	private final ListPanel<NoiseMapEditor> noiseMapperPanel;
	private final MapPanel mapPanel;
	
	public TestPanel() {
		/*
			Noise function list and noise mapper list seem similar, so I'll make a super class for them.
			
			- at the top, above tabbed pane: GlobalPanel
		 */
		
		globalPanel = new GlobalPanel(this);
		noiseFunctionPanel = new ListPanel<>(NoiseFunctionEditor.class, name -> new NoiseFunctionEditor(new NamedNoiseFunction(name)));
		noiseMapperPanel = new ListPanel<>(NoiseMapEditor.class, name -> new NoiseMapEditor(this, new NoiseMapper(name, noiseFunctionPanel.getElements()[0].getNoiseFunction())));
		
		Action resetMaps = () -> {
			for(NoiseMapEditor mapEditor: noiseMapperPanel.getElements())
				mapEditor.resetFunctionSelector();
		};
		noiseFunctionPanel.addContainerListener(new ContainerListener() {
			@Override public void componentAdded(ContainerEvent e) { resetMaps.act(); }
			@Override public void componentRemoved(ContainerEvent e) { SwingUtilities.invokeLater(resetMaps::act); }
		});
		noiseMapperPanel.addContainerListener(new ContainerListener() {
			@Override public void componentAdded(ContainerEvent e) {
				if(e.getChild() instanceof ListPanel.ElementContainer)
					((NoiseMapEditor)((ListPanel.ElementContainer)e.getChild()).element).resetFunctionSelector();
			}
			@Override
			public void componentRemoved(ContainerEvent e) {
				SwingUtilities.invokeLater(() -> {
					// System.out.println("removed map");
					for(NoiseMapEditor mapEditor: noiseMapperPanel.getElements())
						for(NoiseMapRegionEditor editor: mapEditor.getRegionEditors())
							editor.resetNoiseMapSelector();
					refresh();
				});
			}
		});
		
		mapPanel = new MapPanel(this);
		
		tabPane = new JTabbedPane();
		tabPane.addTab("Noise Functions", noiseFunctionPanel);
		tabPane.addTab("Noise Mappings", noiseMapperPanel);
		tabPane.addTab("Generated Map", mapPanel);
		
		// setFocus(mapPanel);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		globalPanel.setMaximumSize(new Dimension(globalPanel.getMaximumSize().width, globalPanel.getPreferredSize().height));
		add(globalPanel);
		add(tabPane);
	}
	
	public void setFocus(Component component) {
		// System.out.println("index = "+tabPane.indexOfTabComponent(component));
		// if(tabPane.indexOfTabComponent(component) >= 0) {
			tabPane.setSelectedComponent(component);
			refresh();
		// }
		// else System.out.println("component "+component+" is not a tab");
	}
	
	public void refresh() {
		revalidate();
		repaint();
	}
	
	public ListPanel<NoiseMapEditor> getNoiseMapperPanel() {
		return noiseMapperPanel;
	}
	
	public ListPanel<NoiseFunctionEditor> getNoiseFunctionPanel() {
		return noiseFunctionPanel;
	}
	
	public GlobalPanel getGlobalPanel() {
		return globalPanel;
	}
	
	public MapPanel getMapPanel() {
		return mapPanel;
	}
	
	
	@Override
	public Dimension getPreferredSize() {
		Dimension prefSize = super.getPreferredSize();
		return new Dimension(Math.max(800, prefSize.width), Math.max(600, prefSize.height));
	}
}
