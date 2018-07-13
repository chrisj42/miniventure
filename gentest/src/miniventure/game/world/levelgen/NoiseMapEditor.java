package miniventure.game.world.levelgen;

import javax.swing.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.LinkedList;

import miniventure.game.util.MyUtils;
import miniventure.game.world.levelgen.NoiseMapper.NoiseMapRegion;
import miniventure.game.world.levelgen.util.MyPanel;

import org.jetbrains.annotations.NotNull;

public class NoiseMapEditor extends MyPanel implements NamedObject, Scrollable {
	
	@NotNull final TestPanel testPanel;
	@NotNull private final NoiseMapper noiseMap;
	private final MapDisplayBar bar;
	
	private JComboBox<NamedNoiseFunction> functionSelector;
	private ArrayList<NoiseMapRegionEditor> regionEditors = new ArrayList<>();
	
	public NoiseMapEditor(@NotNull TestPanel testPanel, @NotNull NoiseMapper noiseMap) {
		this.testPanel = testPanel;
		
		if(noiseMap.getRegionCount() == 0)
			noiseMap.addRegion();
		
		this.noiseMap = noiseMap;
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		bar = new MapDisplayBar();
		
		add(bar);
		
		JPanel midPanel = new JPanel();
		midPanel.setBackground(null);
		
		functionSelector = new JComboBox<>();
		resetFunctionSelector();
		functionSelector.setSelectedItem(noiseMap.getSource());
		functionSelector.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
				noiseMap.setSource((NamedNoiseFunction)e.getItem());
			// testPanel.getMapPanel().invalidateMaps();
		});
		midPanel.add(new JLabel("Source Noise Function:"));
		midPanel.add(functionSelector);
		
		JButton addBtn = new JButton("Add Region");
		addBtn.addActionListener(e -> {
			addRegion(noiseMap.addRegion(), true);
			// testPanel.getMapPanel().invalidateMaps();
		});
		midPanel.add(addBtn);
		
		add(midPanel);
		
		SwingUtilities.invokeLater(() -> {
			NoiseMapRegion[] regions = noiseMap.getRegions();
			for(NoiseMapRegion region: regions)
				addRegion(region, false);
		});
		
		add(Box.createVerticalGlue());
	}
	
	public String getData() {
		String[] regionData = new String[regionEditors.size()];
		for(int i = 0; i < regionEditors.size(); i++)
			regionData[i] = regionEditors.get(i).getData();
	
		return MyUtils.encodeStringArray(
			"name:"+getObjectName(),
			"function:"+functionSelector.getSelectedIndex(),
			"regions:"+MyUtils.encodeStringArray(regionData)
		);
	}
	
	private void addRegion(@NotNull NoiseMapRegion region, boolean refresh) {
		if(regionEditors.size() == 1)
			regionEditors.get(0).removeBtn.setEnabled(true);
		NoiseMapRegionEditor rEditor = new NoiseMapRegionEditor(this, region);
		regionEditors.add(rEditor);
		add(rEditor, getComponentCount()-1);
		if(refresh)
			refresh();
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
	
	boolean checkMapForLoops() {
		for(NoiseMapRegion region: noiseMap.getRegions())
			if(!region.givesTileType())
				if(checkMapForLoops(region.getChainNoiseMapper()))
					return true;
		
		return false;
	}
	private boolean checkMapForLoops(NoiseMapper map) {
		if(noiseMap.equals(map))
			return true;
		
		for(NoiseMapRegion region: map.getRegions()) {
			if(region.givesTileType()) continue;
			if(checkMapForLoops(region.getChainNoiseMapper()))
				return true;
		}
		
		return false;
	}
	
	void refresh() {
		NoiseMapEditor[] editors = testPanel.getNoiseMapperPanel().getElements();
		if(this.equals(editors[0])) {
			// update the background of the other maps by checking for access
			ArrayList<NoiseMapper> accessed = new ArrayList<>(editors.length);
			addAccessedNoiseMaps(noiseMap, accessed);
			for(NoiseMapEditor editor: editors)
				editor.setBackground(accessed.contains(editor.noiseMap) ? null : Color.LIGHT_GRAY);
			
			// setBackground(Color.LIGHT_GRAY);
		}
		testPanel.refresh();
	}
	
	private static void addAccessedNoiseMaps(NoiseMapper map, ArrayList<NoiseMapper> accessed) {
		accessed.add(map);
		for(NoiseMapRegion region: map.getRegions())
			if(!region.givesTileType())
				addAccessedNoiseMaps(region.getChainNoiseMapper(), accessed);
	}
	
	@Override
	public String toString() { return getObjectName()+"-Editor"; }
	
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
			drawBarRegions(g, noiseMap, getWidth(), getHeight(), true, 0, new LinkedList<>());
		}
	}
	
	
	private void drawBarRegions(Graphics g, NoiseMapper noiseMap, final float barWidth, final int barHeight, boolean drawSeparators, final float inherXOff, LinkedList<NoiseMapRegion> visitedRegions) {
		NoiseMapRegion[] regions = noiseMap.getRegions();
		
		float fxOff = inherXOff;
		int lastOff = (int)fxOff;
		for(int i = 0; i < regions.length; i++) {
			float fwidth = barWidth*regions[i].getSize()/noiseMap.getTotal();
			
			int width = Math.max(1, (int)fwidth);
			int xOff = (int)fxOff;
			
			if(!regions[i].givesTileType()) {
				if(visitedRegions.contains(regions[i])) {
					// LOOP DETECTED
					g.setColor(Color.RED);
					g.fillRect(xOff, 0, width, barHeight);
				}
				else {
					visitedRegions.add(regions[i]);
					
					drawBarRegions(g, regions[i].getChainNoiseMapper(), fwidth, barHeight, false, fxOff, visitedRegions);
					visitedRegions.remove(regions[i]);
				}
			}
			else {
				g.setColor(regions[i].getTileType().color);
				g.fillRect(xOff, 0, width, barHeight);
				if(drawSeparators && width > 1) {
					g.drawRect(lastOff, 0, width+(xOff-lastOff), barHeight);
				}
			}
			fxOff += fwidth;
			lastOff = xOff+width;
		}
		
		g.setColor(Color.BLACK);
		g.drawRect((int)inherXOff, 0, (int)barWidth-1, barHeight-1);
	}
	
	static NoiseMapEditor[] getEditorsForAll(TestPanel testPanel, NoiseMapper... maps) {
		NoiseMapEditor[] editors = new NoiseMapEditor[maps.length];
		for(int i = 0; i < maps.length; i++)
			editors[i] = new NoiseMapEditor(testPanel, maps[i]);
		return editors;
	}
}
