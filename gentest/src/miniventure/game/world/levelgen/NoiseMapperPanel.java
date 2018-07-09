package miniventure.game.world.levelgen;

import javax.swing.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;

import miniventure.game.world.levelgen.util.StickyValidatedField;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class NoiseMapperPanel extends JPanel {
	
	// note that this class is only one Noise Mapper; The tabbed panel will have a list of them.
	
	// TODO checkbox somewhere in tab for whether space should be distributed evenly when adding/removing map regions. 
	
	private MapDisplayBar bar;
	private final TestPanel testPanel;
	private final JScrollPane regionEditorScrollPane;
	private final JPanel regionEditorContainer;
	
	private final ArrayList<MapRegionEditor> regions = new ArrayList<>();
	
	public NoiseMapperPanel(TestPanel testPanel) {
		this.testPanel = testPanel;
		/*
			- a bar that goes from 0 to 1, colored according to tiletype color of each section
			- underneath the bar is a row consisting of the following, left to right:
				
		 */
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		bar = new MapDisplayBar();
		add(bar);
		
		regionEditorContainer = new JPanel(new GridLayout(0, 1, 0, 5));
		regionEditorScrollPane = new JScrollPane(regionEditorContainer);

		MapRegionEditor rootRegion = new MapRegionEditor(null, null);
		add(rootRegion);
		regions.add(rootRegion);
		
		add(regionEditorScrollPane);
	}
	
	// adds the region in, and uses the size it specifies
	private void addRegion(@NotNull MapRegionEditor region, @NotNull MapRegionEditor ref, boolean addAfter, boolean distributeSize) {
		
		float size;
		if(distributeSize)
			size = 1f / (regions.size()+1);
		else
			size = region.maxVal.getValue() - region.minVal.getValue();
		
		//count++;
		region.maxVal.setValue(region.maxVal.getValue()+size); // double the size, so it doesn't get taken away during distribution.
		
		// set up all the next/prev references
		MapRegionEditor prev = addAfter ? ref : ref.prevRegion;
		MapRegionEditor next = addAfter ? ref.nextRegion : ref;
		insertRegion(prev, region, next);
		
		int refPos = regions.indexOf(ref);
		regionEditorContainer.add(region, refPos >= 0 ? refPos + (addAfter ? 1 : 0) : -1);
		
		// now I just need to make space for the new region, while also making sure it stays in place.
		distributeSize(-size);
		
		revalidate();
		repaint();
	}
	
	private void insertRegion(@Nullable MapRegionEditor left, @NotNull MapRegionEditor mid, @Nullable MapRegionEditor right) {
		if(left != null) left.setNext(mid);
		mid.setPrev(left);
		if(right != null) right.setPrev(mid);
		mid.setNext(right);
		regions.add(right == null ? regions.size() : regions.indexOf(right), mid);
	}
	
	// equally distributes the size of the removed region among the others.
	private void removeRegion(@NotNull MapRegionEditor region) {
		if(region.prevRegion == null && region.nextRegion == null) {
			region.removeBtn.setEnabled(false);
			return;
		}
		if(region.prevRegion != null)
			region.prevRegion.setNext(region.nextRegion);
		if(region.nextRegion != null)
			region.nextRegion.setPrev(region.prevRegion);
		
		regionEditorContainer.remove(region);
		regions.remove(region);
		
		// distribute size
		distributeSize(region.maxVal.getValue() - region.minVal.getValue());
		
		revalidate();
		repaint();
	}
	
	// size can be negative or positive. All this method does is give/take equally from all regions.
	private void distributeSize(float size) {
		if(regions.size() == 0) return;
		
		float[] sizes = new float[regions.size()];
		for(int i = 0; i < regions.size(); i++)
			sizes[i] = regions.get(i).maxVal.getValue() - regions.get(i).minVal.getValue();
		
		final float amt = size / regions.size();
		float min = 0;
		
		for(int i = 0; i < regions.size(); i++) {
			float rSize = sizes[i];
			MapRegionEditor region = regions.get(i);
			rSize += amt;
			rSize = Math.max(rSize, 0);
			region.minVal.setValue(min);
			min += rSize;
			if(region.nextRegion == null)
				min = 1f;
			region.maxVal.setValue(min);
		}
	}
	
	private void moveRegion(@NotNull MapRegionEditor region, int amt) {
		if(amt == 0) return;
		
		// float max = region.maxVal.getValue();
		// float min = region.minVal.getValue();
		// float size = max - min;
		
		MapRegionEditor newRef;
		if(amt < 0) {
			newRef = region.prevRegion;
			if(newRef == null) {
				region.moveUpBtn.setEnabled(false);
				return;
			}
		}
		else {
			newRef = region.nextRegion;
			if(newRef == null) {
				region.moveDownBtn.setEnabled(false);
				return;
			}
		}
		
		for(int i = 0; i < Math.abs(amt)-1; i++) {
			if(amt < 0) {
				if(newRef.prevRegion == null)
					break;
				newRef = newRef.prevRegion;
			}
			else {
				if(newRef.nextRegion == null)
					break;
				newRef = newRef.nextRegion;
			}
		}
		
		// new reference value now attained. Go *past* it.
		
		removeRegion(region);
		addRegion(region, newRef, amt > 0, false);
	}
	
	private static final DecimalFormat format = new DecimalFormat("0.0###");
	
	class MapRegionEditor extends JPanel {
		// name and number (min val) of region, min val editable (unless is first one)
		// radio button for tiletype or other noise function
		// tiletype/noise function selection dropdown
		
		@Nullable
		private MapRegionEditor prevRegion, nextRegion;
		private JButton moveUpBtn, moveDownBtn, addBtn, removeBtn;
		
		private StickyValidatedField<Float> minVal, maxVal;
		
		private ButtonGroup mapTypeButtonGroup = new ButtonGroup();
		private JRadioButton tileTypeBtn, noiseFunctionBtn;
		private JComboBox<String> mappingSelector;
		private Color color;
		
		MapRegionEditor(@Nullable MapRegionEditor prev, @Nullable MapRegionEditor next) {
			
			addBtn = new JButton("Add");
			addBtn.setFont(addBtn.getFont().deriveFont(Font.BOLD));
			addBtn.setForeground(Color.GREEN);
			addBtn.addActionListener(e -> NoiseMapperPanel.this.addRegion(new MapRegionEditor(this, nextRegion), this, true, true));
			
			moveUpBtn = new JButton("UP");
			moveUpBtn.addActionListener(e -> NoiseMapperPanel.this.moveRegion(MapRegionEditor.this, -1));
			
			moveDownBtn = new JButton("DOWN");
			moveDownBtn.addActionListener(e -> NoiseMapperPanel.this.moveRegion(MapRegionEditor.this, 1));
			
			removeBtn = new JButton("X");
			removeBtn.setFont(removeBtn.getFont().deriveFont(Font.BOLD, 16f));
			removeBtn.setForeground(Color.RED);
			removeBtn.addActionListener(e -> NoiseMapperPanel.this.removeRegion(MapRegionEditor.this));
			
			minVal = new StickyValidatedField<>(prev == null ? 0f : prev.maxVal.getValue(), format::format, Float::parseFloat, (text, prevValid) -> {
				try {
					float val = Float.parseFloat(text);
					float min = prevRegion == null ? 0f : prevRegion.minVal.getValue();
					float max = maxVal.getValue();
					if(val < min) return min;
					if(val > max) return max;
					return val;
				} catch(NumberFormatException e) {
					return prevValid;
				}
			}, val -> {
				float min = prevRegion == null ? 0f : prevRegion.minVal.getValue();
				float max = maxVal.getValue();
				return val > min && val < max;
			});
			minVal.setColumns(5);
			minVal.addListener(field -> {
				if(prevRegion != null) {
					prevRegion.maxVal.setText(field.getText());
					prevRegion.maxVal.revalidateText(false);
				}
			});
			
			maxVal = new StickyValidatedField<>(next == null ? 1f : next.minVal.getValue(), format::format, Float::parseFloat, (text, prevValid) -> {
				try {
					float val = Float.parseFloat(text);
					float min = minVal.getValue();
					float max = nextRegion == null ? 1f : nextRegion.maxVal.getValue();
					if(val < min) return min;
					if(val > max) return max;
					return val;
				} catch(NumberFormatException e) {
					return prevValid;
				}
			}, val -> {
				float min = minVal.getValue();
				float max = nextRegion == null ? 1f : nextRegion.maxVal.getValue();
				return val > min && val < max;
			});
			maxVal.setColumns(5);
			maxVal.addListener(field -> {
				if(nextRegion != null) {
					nextRegion.minVal.setText(field.getText());
					nextRegion.minVal.revalidateText(false);
				}
			});
			
			tileTypeBtn = new JRadioButton("TileType");
			tileTypeBtn.setOpaque(false);
			mapTypeButtonGroup.add(tileTypeBtn);
			noiseFunctionBtn = new JRadioButton("Noise Function");
			noiseFunctionBtn.setOpaque(false);
			mapTypeButtonGroup.add(noiseFunctionBtn);
			
			mappingSelector = new JComboBox<>();
			tileTypeBtn.addActionListener(e -> {
				mappingSelector.removeAllItems();
				mappingSelector.setEnabled(true);
				mappingSelector.addItem("Select a TileType...");
				for(TileTypeEnum type: TileTypeEnum.values()) {
					mappingSelector.addItem(type.name());
				}
				NoiseMapperPanel.this.revalidate();
				NoiseMapperPanel.this.repaint();
			});
			noiseFunctionBtn.addActionListener(e -> {
				mappingSelector.removeAllItems();
				mappingSelector.setEnabled(true);
				mappingSelector.addItem("Select a Noise Function...");
				String[] noiseFunctions = testPanel.getNoiseFunctionPanel().getNoiseFunctions();
				for(String n: noiseFunctions) {
					mappingSelector.addItem(n);
				}
				mappingSelector.addItem("Create New");
				mappingSelector.addItemListener(ie -> {
					if(ie.getItem().equals("Create New"))
						testPanel.setFocus(testPanel.getNoiseFunctionPanel());
				});
				NoiseMapperPanel.this.revalidate();
				NoiseMapperPanel.this.repaint();
			});
			mappingSelector.setEditable(false);
			mappingSelector.addItem("<-- Select a button on the left");
			mappingSelector.setEnabled(false);
			
			JButton colorBtn = new JButton("color");
			colorBtn.addActionListener(e -> {
				JColorChooser chooser = new JColorChooser();
				int result = JOptionPane.showConfirmDialog(testPanel, chooser, "Select region color", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if(result == JOptionPane.OK_OPTION) {
					color = chooser.getColor();
					MapRegionEditor.this.setBackground(color);
					NoiseMapperPanel.this.revalidate();
					NoiseMapperPanel.this.repaint();
				}
			});
			
			Color pcolor = prev == null ? null : prev.color;
			Color ncolor = next == null ? null : next.color;
			if(pcolor != null && ncolor != null)
				color = new Color((pcolor.getRed()+ncolor.getRed())/2, (pcolor.getGreen()+ncolor.getGreen())/2, (pcolor.getBlue()+ncolor.getBlue())/2);
			else if(ncolor != null)
				color = ncolor;
			else if(pcolor != null)
				color = pcolor;
			
			if(color != null) {
				// shift it over
				color = new Color((color.getRed()+128)%255, (color.getGreen()+128)%255, (color.getBlue()+128)%255);
				
			}
			else
				color = Color.GRAY; // default first color
			
			setBackground(color);
			
			//JPanel btnPanel = new JPanel();
			add(colorBtn);
			add(addBtn);
			add(moveUpBtn);
			add(moveDownBtn);
			add(removeBtn);
			
			//add(btnPanel);
			
			add(minVal);
			add(new JLabel("to"));
			add(maxVal);
			
			add(new JLabel("map to:"));
			
			add(tileTypeBtn);
			add(noiseFunctionBtn);
			add(mappingSelector);
			
			setNext(next);
			setPrev(prev);
		}
		
		private void setPrev(@Nullable MapRegionEditor prev) {
			this.prevRegion = prev;
			if(prev == null) {
				moveUpBtn.setEnabled(false);
				minVal.setEditable(false);
				minVal.setText("0");
				minVal.revalidateText();
			}
			else {
				moveUpBtn.setEnabled(true);
				minVal.setEditable(true);
			}
			removeBtn.setEnabled(nextRegion != null || prevRegion != null);
		}
		
		private void setNext(@Nullable MapRegionEditor next) {
			this.nextRegion = next;
			if(next == null) {
				moveDownBtn.setEnabled(false);
				maxVal.setEditable(false);
				maxVal.setText("1");
				maxVal.revalidateText();
			}
			else {
				moveDownBtn.setEnabled(true);
				maxVal.setEditable(true);
			}
			removeBtn.setEnabled(nextRegion != null || prevRegion != null);
		}
	}
	
	
	private class MapDisplayBar extends JPanel {
		
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
			for(MapRegionEditor region: regions) {
				g.setColor(region.color);
				int width = (int)(getWidth()*(region.maxVal.getValue()-region.minVal.getValue()));
				g.fillRect(getX()+xOff, getY(), width, getHeight());
				xOff += width;
			}
			
			g.setColor(Color.BLACK);
			g.drawRect(getX(), getY(), getWidth(), getHeight());
		}
	}
}
