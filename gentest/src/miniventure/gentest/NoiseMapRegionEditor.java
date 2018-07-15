package miniventure.gentest;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Objects;

import miniventure.game.util.MyUtils;
import miniventure.gentest.NoiseMapper.NoiseMapRegion;
import miniventure.gentest.util.ButtonMaker;
import miniventure.gentest.util.FloatField;
import miniventure.gentest.util.MyPanel;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class NoiseMapRegionEditor extends MyPanel {
	
	@NotNull private final NoiseMapEditor mapEditor;
	@NotNull final NoiseMapRegion region;
	private final FloatField sizeField;
	final JButton removeBtn;
	private final JButton upBtn, downBtn;
	private final ButtonGroup mapTypeButtonGroup = new ButtonGroup();
	private final JRadioButton tileTypeBtn, noiseMapBtn;
	private final JComboBox<TileTypeEnum> tileTypeSelector;
	private final JComboBox<NoiseMapper> noiseMapSelector;
	
	public NoiseMapRegionEditor(@NotNull NoiseMapEditor mapEditor, @NotNull NoiseMapRegion mapRegion) {
		this.mapEditor = mapEditor;
		this.region = mapRegion;
		
		setBackground(null);
		
		removeBtn = ButtonMaker.removeButton(e -> mapEditor.removeRegion(this, mapRegion));
		removeBtn.setEnabled(mapEditor.getNoiseMap().getRegionCount() > 1);
		add(removeBtn);
		
		add(Box.createHorizontalStrut(10));
		
		upBtn = ButtonMaker.upButton(e -> {
			region.move(-1);
			mapEditor.updateRegion(this);
			refresh();
		});
		upBtn.setEnabled(region.getIndex() > 0);
		add(upBtn);
		
		downBtn = ButtonMaker.downButton(e -> {
			region.move(1);
			mapEditor.updateRegion(this);
			refresh();
		});
		downBtn.setEnabled(region.getIndex() < mapEditor.getNoiseMap().getRegionCount()-1);
		add(downBtn);
		
		sizeField = new FloatField(mapRegion.getSize(), 3, 5);
		sizeField.addValueListener(val -> {
			mapRegion.setSize(val);
			// mapEditor.testPanel.getMapPanel().invalidateMaps();
			refresh();
		});
		add(sizeField);
		
		tileTypeSelector = new JComboBox<>(TileTypeEnum.values());
		tileTypeSelector.setSelectedItem(mapRegion.getTileType());
		tileTypeSelector.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
				mapRegion.setTileType((TileTypeEnum)e.getItem());
			
			// mapEditor.testPanel.getMapPanel().invalidateMaps();
			refresh();
		});
		
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
		
		noiseMapSelector.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				NoiseMapper prev = mapRegion.getChainNoiseMapper();
				mapRegion.setChainNoiseMapper((NoiseMapper) e.getItem());
				if(!Objects.equals(prev, mapRegion.getChainNoiseMapper()))
					mapEditor.testPanel.getMapPanel().invalidateMaps();
			}
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
	
	public String getData() {
		return MyUtils.encodeStringArray(
			"size:"+region.getSize(),
			"givestile:"+region.givesTileType(),
			"tiletype:"+region.getTileType(),
			"chainmap:"+noiseMapSelector.getSelectedIndex()
		);
	}
	
	void updateButtons() {
		int idx = region.getIndex();
		upBtn.setEnabled(idx > 0);
		downBtn.setEnabled(idx < mapEditor.getNoiseMap().getRegionCount()-1);
		// refresh();
	}
	
	private void refresh() { mapEditor.refresh(); }
	
	// simply removes and re-adds the given map, if it was there to begin with.
	void resetNoiseMapSelector(NoiseMapper map) {
		Object sel = noiseMapSelector.getSelectedItem();
		for(int i = 0; i < noiseMapSelector.getItemCount(); i++) {
			if(noiseMapSelector.getItemAt(i).equals(map)) {
				noiseMapSelector.removeItemAt(i);
				noiseMapSelector.insertItemAt(map, i);
				noiseMapSelector.setSelectedItem(sel);
				refresh();
				return;
			}
		}
	}
	void resetNoiseMapSelector() {
		Object sel = noiseMapSelector.getSelectedItem();
		noiseMapSelector.removeAllItems();
		for(NoiseMapper map : getNoiseMaps())
			noiseMapSelector.addItem(map);
		noiseMapSelector.setSelectedItem(sel);
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
			// if(editor.equals(mapEditor))
			// 	continue;
			maps.add(editor.getNoiseMap());
		}
		return maps.toArray(new NoiseMapper[maps.size()]);
	}
	
	@Override
	public String toString() { return "RegionEditor["+region+']'; }
	
	@Override
	public Dimension getMaximumSize() { return getPreferredSize(); }
}
