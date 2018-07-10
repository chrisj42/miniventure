package miniventure.game.world.levelgen;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import miniventure.game.world.levelgen.NoiseMapper.NoiseMapRegion;
import miniventure.game.world.levelgen.util.MyPanel;

import org.jetbrains.annotations.NotNull;

public class NoiseMapEditor extends MyPanel implements NamedObject {
	
	final TestPanel testPanel;
	@NotNull private final NoiseMapper noiseMap;
	private final MapDisplayBar bar;
	
	private JPanel regionSelectionPanel;
	private NoiseMapRegionEditor curRegionEditor = null;
	
	public NoiseMapEditor(TestPanel testPanel, @NotNull NoiseMapper noiseMap) {
		this.testPanel = testPanel;
		this.noiseMap = noiseMap;
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		bar = new MapDisplayBar();
		
		add(bar);
		
		NoiseMapRegion[] regions = noiseMap.getRegions();
		regionSelectionPanel = new JPanel();
		JComboBox<NoiseMapRegion> regionSelector = new JComboBox<>(regions);
		
		JButton addBtn = new JButton("Add Region");
		addBtn.addActionListener(e -> {
			NoiseMapRegion region = noiseMap.addRegion();
			regionSelector.removeAllItems();
			for(NoiseMapRegion r: noiseMap.getRegions())
				regionSelector.addItem(r);
			regionSelector.setSelectedItem(region);
			refresh();
		});
		add(addBtn);
		
		regionSelector.addActionListener(e -> {
			// I assume this triggers whenever an item is selected
			if(regionSelector.getSelectedItem() != null) {
				if(curRegionEditor != null) {
					regionSelectionPanel.remove(curRegionEditor);
					curRegionEditor = null;
				}
				curRegionEditor = new NoiseMapRegionEditor(this, (NoiseMapRegion) regionSelector.getSelectedItem());
				regionSelectionPanel.add(curRegionEditor);
				refresh();
			}
		});
		
		regionSelectionPanel.add(regionSelector);
		if(regions.length > 0)
			regionSelector.setSelectedItem(regions[0]);
		add(regionSelectionPanel);
		
		/*if(testPanel.getNoiseMapperPanel() != null) {
			SwingUtilities.invokeLater(() -> {
				NoiseMapEditor[] editors = testPanel.getNoiseMapperPanel().getElements(NoiseMapEditor.class);
				for(NoiseMapEditor e : editors)
					e.noiseMapCreated();
			});
		}*/
	}
	
	@NotNull
	public NoiseMapper getNoiseMap() {
		return noiseMap;
	}
	
	void refresh() {
		testPanel.refresh();
	}
	
	void noiseMapCreated() {
		if(curRegionEditor != null)
			curRegionEditor.resetNoiseMapSelector();
	}
	
	@Override
	public void setObjectName(String name) {
		noiseMap.setObjectName(name);
	}
	
	@Override
	public String getObjectName() {
		return noiseMap.getObjectName();
	}
	
	
	private class MapDisplayBar extends MyPanel {
		
		MapDisplayBar() {
			
		}
		
		@Override
		public Dimension getMinimumSize() { return new Dimension(100, 30); }
		@Override
		public Dimension getMaximumSize() { return new Dimension(super.getMaximumSize().width, getMinimumSize().height); }
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			int xOff = 0;
			for(NoiseMapRegion region: noiseMap.getRegions()) {
				Color color;
				if(region.givesTileType())
					color = region.getTileType().color;
				else
					color = Color.LIGHT_GRAY;
				g.setColor(color);
				int width = (int)(getWidth()*region.getSize());
				g.fillRect(getX()+xOff, getY(), width, getHeight());
				xOff += width;
			}
			
			g.setColor(Color.BLACK);
			g.drawRect(getX(), getY(), getWidth(), getHeight());
		}
	}
}
