package miniventure.game.world.levelgen;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.ArrayList;

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
		removeBtn = new JButton("X");
		removeBtn.setForeground(Color.RED);
		removeBtn.setFont(removeBtn.getFont().deriveFont(Font.BOLD, 14f));
		removeBtn.addActionListener(e -> mapEditor.removeRegion(this, mapRegion));
		removeBtn.setEnabled(mapEditor.getNoiseMap().getRegionCount() > 1);
		add(removeBtn);
		
		sizeField = new FloatField(mapRegion.getSize(), 5);
		sizeField.addValueListener(val -> {
			mapRegion.setSize(val);
			refresh();
		});
		add(sizeField);
		
		tileTypeSelector = new JComboBox<>(TileTypeEnum.values());
		tileTypeSelector.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
				mapRegion.setTileType((TileTypeEnum)e.getItem());
			refresh();
		});
		tileTypeSelector.setSelectedItem(mapRegion.getTileType());
		
		noiseMapSelector = new JComboBox<>();
		
		tileTypeBtn = new JRadioButton("TileType");
		tileTypeBtn.setSelected(mapRegion.givesTileType());
		tileTypeBtn.addActionListener(e -> {
			mapRegion.setGivesTileType(true);
			
			TileTypeEnum type = mapRegion.getTileType();
			tileTypeSelector.setSelectedItem(type);
			
			remove(noiseMapSelector);
			add(tileTypeSelector);
			refresh();
		});
		
		noiseMapBtn = new JRadioButton("Reference Noise Map");
		noiseMapBtn.setSelected(!mapRegion.givesTileType());
		noiseMapBtn.addActionListener(e -> {
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
		
		noiseMapSelector.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
				mapRegion.setChainNoiseMapper((NoiseMapper)e.getItem());
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
		
		/*SwingUtilities.invokeLater(() -> {
			MyUtils.sleep(100);
			noiseMapBtn.setEnabled(mapEditor.testPanel.getNoiseMapperPanel().getElementCount() > 1);
		});*/
	}
	
	private void refresh() {
		mapEditor.refresh();
	}
	
	void resetNoiseMapSelector() {
		noiseMapSelector.removeAllItems();
		for(NoiseMapper map : getNoiseMaps())
			noiseMapSelector.addItem(map);
		if(!region.givesTileType()) {
			noiseMapBtn.setSelected(true);
			noiseMapSelector.setSelectedItem(region.getChainNoiseMapper());
		} else
			tileTypeBtn.doClick();
		noiseMapBtn.setEnabled(mapEditor.testPanel.getNoiseMapperPanel().getElementCount() > 1);
	}
	
	@NotNull
	private NoiseMapper[] getNoiseMaps() {
		NoiseMapEditor[] editors = mapEditor.testPanel.getNoiseMapperPanel().getElements();
		// System.out.println("got "+editors.length+" noise maps for region editor "+this);
		ArrayList<NoiseMapper> maps = new ArrayList<>();
		for(NoiseMapEditor editor: editors) {
			if(editor.equals(mapEditor))
				continue;
			maps.add(editor.getNoiseMap());
		}
		return maps.toArray(new NoiseMapper[maps.size()]);
	}
	
	@Override
	public String toString() { return "RegionEditor["+mapEditor.getObjectName()+": "+region+']'; }
}
