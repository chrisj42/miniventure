package miniventure.game.world.levelgen;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Objects;

import miniventure.game.world.levelgen.NoiseMapper.NoiseMapRegion;
import miniventure.game.world.levelgen.util.FloatField;
import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class NoiseMapRegionEditor extends MyPanel {
	
	@NotNull private final NoiseMapEditor mapEditor;
	@NotNull private final NoiseMapRegion region;
	private final FloatField sizeField;
	final JButton removeBtn;
	private final ButtonGroup mapTypeButtonGroup = new ButtonGroup();
	private final JRadioButton tileTypeBtn, noiseMapBtn;
	private final JComboBox<TileTypeEnum> tileTypeSelector;
	private final JComboBox<NoiseMapper> noiseMapSelector;
	
	public NoiseMapRegionEditor(@NotNull NoiseMapEditor mapEditor, @NotNull NoiseMapRegion mapRegion) {
		this.mapEditor = mapEditor;
		this.region = mapRegion;
		
		setBackground(null);
		
		removeBtn = new JButton("X");
		removeBtn.setForeground(Color.RED);
		removeBtn.setFont(removeBtn.getFont().deriveFont(Font.BOLD, 14f));
		removeBtn.addActionListener(e -> mapEditor.removeRegion(this, mapRegion));
		removeBtn.setEnabled(mapEditor.getNoiseMap().getRegionCount() > 1);
		add(removeBtn);
		
		sizeField = new FloatField(mapRegion.getSize(), 5);
		sizeField.addValueListener(val -> {
			mapRegion.setSize(val);
			// mapEditor.testPanel.getMapPanel().invalidateMaps();
			refresh();
		});
		add(sizeField);
		
		tileTypeSelector = new JComboBox<>(TileTypeEnum.values());
		tileTypeSelector.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
				mapRegion.setTileType((TileTypeEnum)e.getItem());
			
			// mapEditor.testPanel.getMapPanel().invalidateMaps();
			/*if(source == null) {
				source = tileTypeSelector;
				for(NoiseMapEditor editor : mapEditor.testPanel.getNoiseMapperPanel().getElements())
					if(!NoiseMapRegionEditor.this.mapEditor.equals(editor))
						for(NoiseMapRegionEditor rEditor : editor.getRegionEditors())
							rEditor.resetNoiseMapSelector();
				source = null;
				refresh();
			}*/
			
			refresh();
		});
		tileTypeSelector.setSelectedItem(mapRegion.getTileType());
		
		noiseMapSelector = new JComboBox<>();
		
		tileTypeBtn = new JRadioButton("TileType");
		tileTypeBtn.setOpaque(false);
		tileTypeBtn.setSelected(mapRegion.givesTileType());
		tileTypeBtn.addActionListener(e -> {
			// if(!mapRegion.givesTileType())
			// 	mapEditor.testPanel.getMapPanel().invalidateMaps();
			mapRegion.setGivesTileType(true);
			
			TileTypeEnum type = mapRegion.getTileType();
			tileTypeSelector.setSelectedItem(type);
			
			remove(noiseMapSelector);
			add(tileTypeSelector);
			refresh();
		});
		
		noiseMapBtn = new JRadioButton("Reference Noise Map");
		noiseMapBtn.setOpaque(false);
		noiseMapBtn.setSelected(!mapRegion.givesTileType());
		noiseMapBtn.addActionListener(e -> {
			if(mapRegion.givesTileType())
				mapEditor.testPanel.getMapPanel().invalidateMaps();
			mapRegion.setGivesTileType(false);
			
			resetNoiseMapSelector();
			NoiseMapper mapper = mapRegion.getChainNoiseMapper();
			noiseMapSelector.setSelectedItem(mapper);
			
			remove(tileTypeSelector);
			add(noiseMapSelector);
			refresh();
		});
		
		mapTypeButtonGroup.add(tileTypeBtn);
		mapTypeButtonGroup.add(noiseMapBtn);
		
		resetNoiseMapSelector();
		if(mapRegion.getChainNoiseMapper() != null)
			noiseMapSelector.setSelectedItem(mapRegion.getChainNoiseMapper());
		
		// noiseMapSelector.setName(toString()+" Selector");
		noiseMapSelector.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				NoiseMapper prev = mapRegion.getChainNoiseMapper();
				mapRegion.setChainNoiseMapper((NoiseMapper) e.getItem());
				if(!Objects.equals(prev, mapRegion.getChainNoiseMapper()))
					mapEditor.testPanel.getMapPanel().invalidateMaps();
			}
			// System.out.println(((Component)e.getSource()).getName());
			/*if(source == null) {
				source = noiseMapSelector;
				for(NoiseMapEditor editor : mapEditor.testPanel.getNoiseMapperPanel().getElements())
					for(NoiseMapRegionEditor rEditor : editor.getRegionEditors())
						if(!NoiseMapRegionEditor.this.equals(rEditor))
							rEditor.resetNoiseMapSelector();
				source = null;
				refresh();
			}*/
			
			refresh();
		});
		
		add(tileTypeBtn);
		add(noiseMapBtn);
		
		if(noiseMapBtn.isSelected())
			add(noiseMapSelector);
		else {
			add(tileTypeSelector);
			if(!tileTypeBtn.isSelected())
				tileTypeSelector.setEnabled(false);
		}
	}
	
	// private static Object source = null;
	
	// private static final Color trans = new Color(0, 0, 0, 0);
	private void refresh() {
		// setBackground(trans);
		mapEditor.refresh();
	}
	
	void resetNoiseMapSelector() {
		Object sel = noiseMapSelector.getSelectedItem();
		noiseMapSelector.removeAllItems();
		for(NoiseMapper map : getNoiseMaps())
			noiseMapSelector.addItem(map);
		noiseMapSelector.setSelectedItem(sel/*region.getChainNoiseMapper()*/);
		if(!region.givesTileType()) {
			noiseMapBtn.setSelected(true);
		} else
			tileTypeBtn.doClick();
		noiseMapBtn.setEnabled(mapEditor.testPanel.getNoiseMapperPanel().getElementCount() > 1);
	}
	
	@NotNull
	private NoiseMapper[] getNoiseMaps() {
		NoiseMapEditor[] editors = mapEditor.testPanel.getNoiseMapperPanel().getElements();
		ArrayList<NoiseMapper> maps = new ArrayList<>();
		for(NoiseMapEditor editor: editors) {
			if(editor.equals(mapEditor))
				continue;
			// if(checkMapForLoops(editor.getNoiseMap()))
			// 	continue;
					
			maps.add(editor.getNoiseMap());
		}
		return maps.toArray(new NoiseMapper[maps.size()]);
	}
	
	@Override
	public String toString() { return "RegionEditor["+region+']'; }
}
