package miniventure.game.world.levelgen;

import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;

import java.awt.Component;
import java.awt.Dimension;

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
		noiseFunctionPanel = new ListPanel<>(() -> new NoiseFunctionEditor());
		noiseMapperPanel = new ListPanel<>(() -> new NoiseMapEditor(this, new NoiseMapper("master", new Coherent2DNoiseFunction(100, 1))));
		mapPanel = new MapPanel(this);
		
		tabPane = new JTabbedPane();
		tabPane.addTab("Noise Functions", noiseFunctionPanel);
		tabPane.addTab("Noise Mappings", noiseMapperPanel);
		tabPane.addTab("Generated Map", mapPanel);
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		globalPanel.setMaximumSize(new Dimension(globalPanel.getMaximumSize().width, globalPanel.getPreferredSize().height));
		add(globalPanel);
		add(tabPane);
		
		//add(noiseMapperPanel); // TODO this is actually only one noise map... I should be adding a ListPanel *of* these, instead.
		ready();
	}
	
	public void setFocus(Component component) {
		if(tabPane.indexOfTabComponent(component) >= 0) {
			tabPane.setSelectedComponent(component);
			revalidate();
			repaint();
		}
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
