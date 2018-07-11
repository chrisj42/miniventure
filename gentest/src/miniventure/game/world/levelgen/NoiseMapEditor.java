package miniventure.game.world.levelgen;

import javax.swing.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import miniventure.game.world.levelgen.NoiseMapper.NoiseMapRegion;
import miniventure.game.world.levelgen.util.MyPanel;

import org.jetbrains.annotations.NotNull;

public class NoiseMapEditor extends MyPanel implements NamedObject, Scrollable {
	
	final TestPanel testPanel;
	@NotNull private final NoiseMapper noiseMap;
	private final MapDisplayBar bar;
	
	private JComboBox<NamedNoiseFunction> functionSelector;
	private ArrayList<NoiseMapRegionEditor> regionEditors = new ArrayList<>();
	
	public NoiseMapEditor(TestPanel testPanel, @NotNull NoiseMapper noiseMap) {
		this.testPanel = testPanel;
		this.noiseMap = noiseMap;
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		bar = new MapDisplayBar();
		
		add(bar);
		
		JPanel midPanel = new JPanel();
		
		functionSelector = new JComboBox<>(new NamedNoiseFunction[] {noiseMap.getSource()});
		midPanel.add(new JLabel("Source Noise Function:"));
		midPanel.add(functionSelector);
		
		JButton addBtn = new JButton("Add Region");
		addBtn.addActionListener(e -> {
			NoiseMapRegion region = noiseMap.addRegion();
			if(regionEditors.size() == 1)
				regionEditors.get(0).removeBtn.setEnabled(true);
			regionEditors.add(new NoiseMapRegionEditor(NoiseMapEditor.this, region));
			add(regionEditors.get(regionEditors.size()-1), getComponentCount()-1);
			refresh();
		});
		midPanel.add(addBtn);
		
		add(midPanel);
		
		NoiseMapRegion[] regions = noiseMap.getRegions();
		for(NoiseMapRegion region: regions) {
			regionEditors.add(new NoiseMapRegionEditor(this, region));
			add(regionEditors.get(regionEditors.size()-1));
		}
		/*regionSelector.addActionListener(e -> {
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
		*/
		/*if(testPanel.getNoiseMapperPanel() != null) {
			SwingUtilities.invokeLater(() -> {
				NoiseMapEditor[] editors = testPanel.getNoiseMapperPanel().getElements(NoiseMapEditor.class);
				for(NoiseMapEditor e : editors)
					e.noiseMapCreated();
			});
		}*/
		add(Box.createVerticalGlue());
	}
	
	void removeRegion(NoiseMapRegionEditor regionEditor, NoiseMapRegion region) {
		regionEditors.remove(regionEditor);
		remove(regionEditor);
		noiseMap.removeRegion(region);
		if(regionEditors.size() == 1)
			regionEditors.get(0).removeBtn.setEnabled(false);
		refresh();
	}
	
	@NotNull
	public NoiseMapper getNoiseMap() { return noiseMap; }
	
	public NoiseMapRegionEditor[] getRegionEditors() { return regionEditors.toArray(new NoiseMapRegionEditor[regionEditors.size()]); }
	
	void resetFunctionSelector() {
		NoiseFunctionEditor[] noiseFunctionEditors = testPanel.getNoiseFunctionPanel().getElements();
		NamedNoiseFunction[] noiseFunctions = new NamedNoiseFunction[noiseFunctionEditors.length];
		
		for(int i = 0; i < noiseFunctionEditors.length; i++)
			noiseFunctions[i] = noiseFunctionEditors[i].getNoiseFunction();
		
		Object sel = functionSelector.getSelectedItem();
		functionSelector.removeAllItems();
		for(NamedNoiseFunction f: noiseFunctions)
			functionSelector.addItem(f);
		functionSelector.revalidate();
		functionSelector.setSelectedItem(sel);
	}
	
	void refresh() {
		testPanel.refresh();
	}
	
	/*void noiseMapCreated() {
		if(curRegionEditor != null)
			curRegionEditor.resetNoiseMapSelector();
	}*/
	
	@Override
	public void setObjectName(@NotNull String name) {
		noiseMap.setObjectName(name);
	}
	
	@NotNull
	@Override
	public String getObjectName() {
		return noiseMap.getObjectName();
	}
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getParent().getPreferredSize();
	}
	
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return visibleRect.height/10;
	}
	
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return visibleRect.height/3;
	}
	
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
	
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	
	@Override
	public Dimension getMaximumSize() { return new Dimension(super.getMaximumSize().width, getPreferredSize().height); }
	
	private class MapDisplayBar extends MyPanel {
		
		MapDisplayBar() {
			
		}
		
		@Override
		public Dimension getMinimumSize() { return new Dimension(noiseMap.getRegionCount(), 30); }
		@Override
		public Dimension getMaximumSize() { return new Dimension(super.getMaximumSize().width, getMinimumSize().height); }
		@Override
		public Dimension getPreferredSize() {
			Dimension prefSize = super.getPreferredSize();
			return new Dimension(Math.max(noiseMap.getRegionCount()*2, prefSize.width), prefSize.height);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			NoiseMapRegion[] regions = noiseMap.getRegions();
			Color[] colors = new Color[regions.length];
			for(int i = 0; i < regions.length; i++) {
				Color c;
				if(regions[i].givesTileType())
					c = regions[i].getTileType().color;
				else
					c = Color.LIGHT_GRAY;
				colors[i] = c;
			}
			
			float fxOff = 0;
			int lastOff = 0;
			for(int i = 0; i < regions.length; i++) {
				Color c = colors[i];
				g.setColor(c);
				float fwidth = getWidth()*regions[i].getSize()/noiseMap.getTotal();
				int width = Math.max(1, (int)fwidth);
				int xOff = (int)fxOff;
				g.fillRect(xOff, 0, width, getHeight());
				if(width > 1) {
					g.setColor(Testing.invertColor(Testing.blendColors(i == 0 ? null : colors[i - 1], colors[i], i == colors.length - 1 ? null : colors[i + 1])));
					g.drawRect(lastOff, 0, width+(xOff-lastOff), getHeight());
				}
				fxOff += fwidth;
				lastOff = xOff+width;
			}
			
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		}
	}
}
