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
		
		
		NamedNoiseFunction continentNoise = new NamedNoiseFunction("Continent Noise", 500, 4);
		NamedNoiseFunction landNoise = new NamedNoiseFunction("Land Noise", 300, 3);
		NamedNoiseFunction biomeNoise = new NamedNoiseFunction("Biome Noise", 120, 2);
		NamedNoiseFunction detailNoise = new NamedNoiseFunction("Detail Noise", 12, 2);
		NamedNoiseFunction islandNoise = new NamedNoiseFunction("Island Noise", 24, 2);
		NamedNoiseFunction cactusNoise = new NamedNoiseFunction("Cactus Noise", 4, 2);
		
		
		NoiseMapper plainsBiome = new NoiseMapper("Plains", detailNoise)
			.addRegion(GRASS, 8)
			.addRegion(TREE_CARTOON, 1);
		
		NoiseMapper desertBiome = new NoiseMapper("Desert", cactusNoise)
			.addRegion(CACTUS, 1)
			.addRegion(SAND, 10);
		
		NoiseMapper canyonBiome = new NoiseMapper("Canyons", detailNoise)
			.addRegion(STONE, 20)
			.addRegion(GRASS, 10)
			.addRegion(TREE_PINE, 1)
			.addRegion(GRASS, 10)
			.addRegion(STONE, 20);
		
		
		NoiseMapper mountainBiome = new NoiseMapper("Mountains", biomeNoise)
			.addRegion(STONE, 1)
			.addRegion(canyonBiome, 4.5f)
			.addRegion(STONE, 1);
		
		
		NoiseMapper oceanBiome = new NoiseMapper("Ocean", biomeNoise)
			.addRegion(WATER, 1);
		
		NoiseMapper deepOceanBiome = new NoiseMapper("Deep Ocean", islandNoise)
			.addRegion(WATER, 10)
			.addRegion(SAND, 1);
		
		
		
		NoiseMapper highTerrain = new NoiseMapper("High Terrain", landNoise)
			.addRegion(desertBiome, 4)
			.addRegion(plainsBiome, 8)
			.addRegion(mountainBiome, 6);
		
		
		
		NoiseMapper landTerrain = new NoiseMapper("Land", continentNoise)
			.addRegion(highTerrain, 0.9f)
			.addRegion(SAND, 2);
		
		
		NoiseMapper rivers = new NoiseMapper("Rivers", biomeNoise)
			.addRegion(landTerrain, 1)
			.addRegion(WATER, .15f)
			.addRegion(landTerrain, 1);
		
		
		NoiseMapper biomeCategories = new NoiseMapper("Surface Type", continentNoise)
			.addRegion(rivers, 1)
			.addRegion(oceanBiome, 1)
			.addRegion(deepOceanBiome, 1);
		
		
		
		globalPanel = new GlobalPanel(this);
		noiseFunctionPanel = new ListPanel<>(this, NoiseFunctionEditor.class, "<html>the value in the seed field will be retained <em>only</em> if \"random seed\" is unchecked.", name -> new NoiseFunctionEditor(this, new NamedNoiseFunction(name)));
		noiseMapperPanel = new ListPanel<>(this, NoiseMapEditor.class, null, name -> new NoiseMapEditor(this, new NoiseMapper(name, noiseFunctionPanel.getElements()[0].getNoiseFunction())));
		
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
			if(Objects.equals(tabPane.getSelectedComponent(), mapPanel)) {
				globalPanel.mapPanel.setVisible(true);
				mapPanel.focus(true);
				refresh();
			} else {
				mapPanel.unfocus(true);
				globalPanel.mapPanel.setVisible(false);
				refresh();
			}
		});
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		globalPanel.setMaximumSize(new Dimension(globalPanel.getMaximumSize().width, globalPanel.getPreferredSize().height));
		add(globalPanel);
		add(tabPane);
		
		SwingUtilities.invokeLater(() -> {
			// add all the editors for the biomes and noise functions from above
			
			noiseFunctionPanel.addElement(new NoiseFunctionEditor(this, continentNoise));
			noiseFunctionPanel.addElement(new NoiseFunctionEditor(this, landNoise));
			noiseFunctionPanel.addElement(new NoiseFunctionEditor(this, biomeNoise));
			noiseFunctionPanel.addElement(new NoiseFunctionEditor(this, detailNoise));
			noiseFunctionPanel.addElement(new NoiseFunctionEditor(this, islandNoise));
			noiseFunctionPanel.addElement(new NoiseFunctionEditor(this, cactusNoise));
			
			NoiseMapEditor[] mapEditors = NoiseMapEditor.getEditorsForAll(this,
				biomeCategories, rivers, landTerrain, highTerrain, deepOceanBiome,
				oceanBiome, mountainBiome, canyonBiome, desertBiome, plainsBiome
			);
			
			for(NoiseMapEditor editor: mapEditors)
				noiseMapperPanel.addElement(editor);
			
			for(NoiseMapEditor editor: mapEditors)
				for(NoiseMapRegionEditor rEditor: editor.getRegionEditors())
					rEditor.resetNoiseMapSelector();
			
			setFocus(mapPanel);
		});
		
		setFocus(mapPanel);
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
		return new Dimension(Math.max(700, prefSize.width), Math.max(500, prefSize.height));
	}
}
