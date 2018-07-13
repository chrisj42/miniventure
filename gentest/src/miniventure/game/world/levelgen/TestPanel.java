package miniventure.game.world.levelgen;

import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Objects;

import miniventure.game.util.Action;
import miniventure.game.world.levelgen.util.MyPanel;

import static miniventure.game.world.tile.TileType.TileTypeEnum.*;

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
		
		
		NamedNoiseFunction categoryNoise = new NamedNoiseFunction("Category Noise", 96);
		NamedNoiseFunction biomeNoise = new NamedNoiseFunction("Biome Noise", 64);
		NamedNoiseFunction detailNoise = new NamedNoiseFunction("Detail Noise", 8);
		
		
		NoiseMapper oceanBiome = new NoiseMapper("Ocean", detailNoise)
			.addRegion(WATER, 1);
		
		NoiseMapper desertBiome = new NoiseMapper("Desert", detailNoise)
			.addRegion(SAND, 15)
			.addRegion(CACTUS, 1)
			.addRegion(SAND, 5);
		
		NoiseMapper mountainBiome = new NoiseMapper("Mountains", detailNoise)
			.addRegion(STONE, 30);
		
		NoiseMapper canyonBiome = new NoiseMapper("Canyons", detailNoise)
			.addRegion(STONE, 20)
			.addRegion(GRASS, 10)
			.addRegion(TREE_PINE, 1)
			.addRegion(GRASS, 10)
			.addRegion(STONE, 20);
		
		NoiseMapper plainsBiome = new NoiseMapper("Plains", detailNoise)
			.addRegion(TREE_CARTOON, 2)
			.addRegion(GRASS, 18)
			.addRegion(STONE, 1);
		
		NoiseMapper forestBiome = new NoiseMapper("Forest", detailNoise)
			.addRegion(GRASS, 3)
			.addRegion(TREE_POOF, 6);
		
		
		
		NoiseMapper wetBiomes = new NoiseMapper("Wet Biomes", biomeNoise)
			.addRegion(oceanBiome, 1);
		
		NoiseMapper midBiomes = new NoiseMapper("Temperate Biomes", biomeNoise)
			.addRegion(plainsBiome, 7)
			.addRegion(oceanBiome, 1)
			.addRegion(forestBiome, 3);
		
		NoiseMapper dryBiomes = new NoiseMapper("Dry Biomes", biomeNoise)
			.addRegion(desertBiome, 2)
			.addRegion(plainsBiome, 4)
			.addRegion(forestBiome, 1);
		
		NoiseMapper rockyBiomes = new NoiseMapper("Rocky Biomes", biomeNoise)
			.addRegion(mountainBiome, 1)
			.addRegion(canyonBiome, 2)
			.addRegion(mountainBiome, 1);
		
		
		
		NoiseMapper biomeCategories = new NoiseMapper("Biome Categories", categoryNoise)
			.addRegion(wetBiomes, 2)
			.addRegion(midBiomes, 3)
			.addRegion(dryBiomes, 5)
			.addRegion(rockyBiomes, 3);
		
		
		
		globalPanel = new GlobalPanel(this);
		noiseFunctionPanel = new ListPanel<>(NoiseFunctionEditor.class, "<html>the value in the seed field will be retained <em>only</em> if \"random seed\" is unchecked.", name -> new NoiseFunctionEditor(new NamedNoiseFunction(name)));
		noiseMapperPanel = new ListPanel<>(NoiseMapEditor.class, null, name -> new NoiseMapEditor(this, new NoiseMapper(name, noiseFunctionPanel.getElements()[0].getNoiseFunction())));
		
		Action resetMaps = () -> {
			for(NoiseMapEditor mapEditor: noiseMapperPanel.getElements())
				mapEditor.resetFunctionSelector();
		};
		noiseFunctionPanel.addContainerListener(new ContainerListener() {
			@Override public void componentAdded(ContainerEvent e) { resetMaps.act(); }
			@Override public void componentRemoved(ContainerEvent e) { SwingUtilities.invokeLater(resetMaps::act); }
		});
		noiseFunctionPanel.setName("Noise Function Panel");
		noiseMapperPanel.addContainerListener(new ContainerListener() {
			@Override public void componentAdded(ContainerEvent e) {
				if(e.getChild() instanceof ListPanel.ElementContainer)
					((NoiseMapEditor)((ListPanel.ElementContainer)e.getChild()).element).resetFunctionSelector();
			}
			@Override
			public void componentRemoved(ContainerEvent e) {
				SwingUtilities.invokeLater(() -> {
					for(NoiseMapEditor mapEditor: noiseMapperPanel.getElements())
						for(NoiseMapRegionEditor editor: mapEditor.getRegionEditors())
							editor.resetNoiseMapSelector();
					refresh();
				});
			}
		});
		noiseMapperPanel.setName("Noise Mapper Panel");
		
		mapPanel = new MapPanel(this);
		mapPanel.setName("Map Panel");
		
		tabPane = new JTabbedPane();
		tabPane.addTab("Noise Functions", noiseFunctionPanel);
		tabPane.addTab("Noise Mappings", noiseMapperPanel);
		tabPane.addTab("Generated Map", mapPanel);
		
		tabPane.addChangeListener(e -> {
			if(Objects.equals(tabPane.getSelectedComponent(), mapPanel))
				mapPanel.focus(true);
			else
				mapPanel.unfocus(true);
		});
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		globalPanel.setMaximumSize(new Dimension(globalPanel.getMaximumSize().width, globalPanel.getPreferredSize().height));
		add(globalPanel);
		add(tabPane);
		
		SwingUtilities.invokeLater(() -> {
			// add all the editors for the biomes and noise functions from above
			
			noiseFunctionPanel.addElement(new NoiseFunctionEditor(categoryNoise));
			noiseFunctionPanel.addElement(new NoiseFunctionEditor(biomeNoise));
			noiseFunctionPanel.addElement(new NoiseFunctionEditor(detailNoise));
			
			NoiseMapEditor[] mapEditors = NoiseMapEditor.getEditorsForAll(this,
				biomeCategories,
				wetBiomes, midBiomes, dryBiomes, rockyBiomes,
				oceanBiome, desertBiome, mountainBiome, canyonBiome, plainsBiome, forestBiome
			);
			
			for(NoiseMapEditor editor: mapEditors)
				noiseMapperPanel.addElement(editor);
			
			for(NoiseMapEditor editor: mapEditors)
				for(NoiseMapRegionEditor rEditor: editor.getRegionEditors())
					rEditor.resetNoiseMapSelector();
		});
	}
	
	Component getFocus() { return tabPane.getSelectedComponent(); }
	
	void setFocus(Component component) {
		tabPane.setSelectedComponent(component);
		refresh();
	}
	
	void refresh() {
		revalidate();
		repaint();
	}
	
	ListPanel<NoiseMapEditor> getNoiseMapperPanel() {
		return noiseMapperPanel;
	}
	
	ListPanel<NoiseFunctionEditor> getNoiseFunctionPanel() {
		return noiseFunctionPanel;
	}
	
	GlobalPanel getGlobalPanel() {
		return globalPanel;
	}
	
	MapPanel getMapPanel() {
		return mapPanel;
	}
	
	
	@Override
	public Dimension getPreferredSize() {
		Dimension prefSize = super.getPreferredSize();
		return new Dimension(Math.max(600, prefSize.width), Math.max(500, prefSize.height));
	}
}
