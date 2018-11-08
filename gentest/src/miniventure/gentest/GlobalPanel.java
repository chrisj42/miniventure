package miniventure.gentest;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import miniventure.game.util.MyUtils;
import miniventure.game.world.levelgen.GroupNoiseMapper;
import miniventure.game.world.levelgen.NamedNoiseFunction;
import miniventure.game.world.levelgen.NoiseMapper;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.gentest.util.FloatField;
import miniventure.gentest.util.IntegerField;
import miniventure.gentest.util.MyPanel;

/**
 * Contains: global seed selection, world size selection, regen button
 * soon: toolbar? or just "save", "load", and "new" buttons?
 */
public class GlobalPanel extends MyPanel {
	
	private static final int VERSION = 2;
	
	private final TestPanel testPanel;
	
	final IntegerField widthField;
	final IntegerField heightField;
	
	final MyPanel mapPanel;
	final IntegerField speedField;
	final FloatField bufferField;
	final IntegerField zoomField;
	final IntegerField queueField;
	
	private final JButton regenButton;
	private final JButton saveBtn;
	private final JButton loadBtn;
	
	private static final JFileChooser fileChooser = new JFileChooser();
	
	public GlobalPanel(TestPanel testPanel) {
		this.testPanel = testPanel;
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		MyPanel topPanel = new MyPanel();
		MyPanel bottomPanel = new MyPanel();
		
		/*
			- global world seed selector field
				- checkbox to left for "custom world seed"; text field is only editable when this is checked (if you want to keep same seed, check this)
			- world size (width, height)
			- "regen level"	button, which will only be enabled if the settings are valid
		 */
		
		saveBtn = new JButton("Save");
		loadBtn = new JButton("Load");
		
		widthField = new IntegerField(0, 4, 0);
		heightField = new IntegerField(0, 4, 0);
		
		regenButton = new JButton("Regen world");
		regenButton.addActionListener(e -> {
			if(testPanel.getNoiseMapperPanel().getElements()[0].checkMapForLoops()) {
				JOptionPane.showMessageDialog(testPanel, "Loop(s) detected in noise maps. You must remove all loops connected to the "+testPanel.getNoiseMapperPanel().getElements()[0].getObjectName()+" noise map before being able to generate the world.", "Infinite Loop Warning", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			testPanel.setFocus(testPanel.getMapPanel());
			testPanel.getMapPanel().generate(widthField.getValue(), heightField.getValue());
		});
		
		ActionListener mapFocus = e -> {
			if(Objects.equals(testPanel.getFocus(), testPanel.getMapPanel()))
				testPanel.getMapPanel().requestFocus();
		};
		
		speedField = new IntegerField(5, 2, 1);
		speedField.addActionListener(mapFocus);
		zoomField = new IntegerField(2);
		zoomField.addActionListener(mapFocus);
		bufferField = new FloatField(3, 2, 0);
		bufferField.addActionListener(mapFocus);
		queueField = new IntegerField(3, 2, 1);
		queueField.addActionListener(mapFocus);
		
		
		saveBtn.addActionListener(e -> {
			
			int res = fileChooser.showSaveDialog(testPanel);
			if(res != JFileChooser.APPROVE_OPTION)
				return;
			
			/*
				These things must be saved:
					- values of globalpanel fields
					- all noise functions
					- all noise maps
			 */
			LinkedList<String> data = new LinkedList<>();
			// global data
			data.add(MyUtils.encodeStringArray("global",
				"version:"+VERSION,
				"width:"+widthField.getValue(),
				"height:"+heightField.getValue(),
				"buffer:"+bufferField.getValue(),
				"zoom:"+zoomField.getValue(),
				"queue:"+queueField.getValue()
			));
			
			NoiseFunctionEditor[] functions = testPanel.getNoiseFunctionPanel().getElements();
			String[] functionData = new String[functions.length+1];
			functionData[0] = "functions";
			for(int i = 0; i < functions.length; i++)
				functionData[i+1] = functions[i].getData();
			data.add(MyUtils.encodeStringArray(functionData));
			
			NoiseMapEditor[] maps = testPanel.getNoiseMapperPanel().getElements();
			String[] mapData = new String[maps.length+1];
			mapData[0] = "maps";
			for(int i = 0; i < maps.length; i++)
				mapData[i+1] = maps[i].getData();
			data.add(MyUtils.encodeStringArray(mapData));
			
			try {
				Files.write(fileChooser.getSelectedFile().toPath(), data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			} catch(IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(testPanel, "Failed to save data.", "I/O Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		
		loadBtn.addActionListener(e -> {
			int res = fileChooser.showOpenDialog(testPanel);
			if(res != JFileChooser.APPROVE_OPTION)
				return;
			
			regenButton.setEnabled(false);
			testPanel.getMapPanel().invalidateMaps();
			
			List<String> data;
			try {
				data = Files.readAllLines(fileChooser.getSelectedFile().toPath());
			} catch(IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(testPanel, "Failed to read file.", "I/O Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			HashMap<String, String[]> dataMap = new HashMap<>();
			for(String line: data) {
				String[] lineData = MyUtils.parseLayeredString(line);
				String type = lineData[0];
				dataMap.put(type, Arrays.copyOfRange(lineData, 1, lineData.length));
			}
			
			HashMap<String, String> globalData = parseDataList(dataMap.get("global"));
			int version = new Integer(globalData.get("version"));
			widthField.setValue(new Integer(globalData.get("width")));
			heightField.setValue(new Integer(globalData.get("height")));
			bufferField.setValue(new Float(globalData.get("buffer")));
			zoomField.setValue(new Integer(globalData.get("zoom")));
			queueField.setValue(new Integer(globalData.get("queue")));
			
			String[] functionData = dataMap.get("functions");
			String[] seeds = new String[functionData.length];
			boolean[] randSeeds = new boolean[functionData.length];
			NamedNoiseFunction[] functions = new NamedNoiseFunction[functionData.length];
			for(int i = 0; i < functionData.length; i++) {
				HashMap<String, String> info = parseDataList(MyUtils.parseLayeredString(functionData[i]));
				String name = info.get("name");
				seeds[i] = info.get("seed");
				randSeeds[i] = Boolean.parseBoolean(info.get("random"));
				int curves = Integer.parseInt(info.get("curves"));
				int cpv = Integer.parseInt(info.get("cpv"));
				
				functions[i] = new NamedNoiseFunction(name, cpv, curves);
			}
			
			String[] mapData = dataMap.get("maps");
			NoiseMapper[] maps = new NoiseMapper[mapData.length];
			String[][] regionData = new String[maps.length][];
			for(int i = 0; i < mapData.length; i++) {
				HashMap<String, String> info = parseDataList(MyUtils.parseLayeredString(mapData[i]));
				String name = info.get("name");
				NamedNoiseFunction function = functions[Integer.parseInt(info.get("function"))];
				regionData[i] = MyUtils.parseLayeredString(info.get("regions"));
				boolean isgroup = Boolean.parseBoolean(info.getOrDefault("isgroup", "false"));
				
				NoiseMapper map = isgroup ? new GroupNoiseMapper(name, function) : new NoiseMapper(name, function);
				maps[i] = map;
			}
			
			for(int i = 0; i < mapData.length; i++) {
				for(int j = 0; j < regionData[i].length; j++) {
					HashMap<String, String> info = parseDataList(MyUtils.parseLayeredString(regionData[i][j]));
					float size = Float.parseFloat(info.get("size"));
					boolean givestile = Boolean.valueOf(info.get("givestile"));
					int chainMapIdx = Integer.parseInt(info.get("chainmap"));
					int off = version < 2 && chainMapIdx >= i ? 1 : 0;
					TileTypeEnum tileType = TileTypeEnum.valueOf(info.get("tiletype"));
					NoiseMapper chainmap = chainMapIdx>=0?maps[chainMapIdx+off]:null;
					maps[i].addRegion(tileType, chainmap, givestile, size);
				}
			}
			
			NoiseFunctionEditor[] functionEditors = new NoiseFunctionEditor[functions.length];
			for(int i = 0; i < functions.length; i++) {
				functionEditors[i] = new NoiseFunctionEditor(testPanel, functions[i]);
				functionEditors[i].seed.setValue(seeds[i]);
				functionEditors[i].randomSeed.setSelected(randSeeds[i]);
			}
			testPanel.getNoiseFunctionPanel().replaceElements(functionEditors);
			
			NoiseMapEditor[] mapEditors = new NoiseMapEditor[maps.length];
			for(int i = 0; i < maps.length; i++)
				mapEditors[i] = new NoiseMapEditor(testPanel, maps[i]);
			testPanel.getNoiseMapperPanel().replaceElements(mapEditors);
			
			// and we're done!
			regenButton.setEnabled(true);
			testPanel.refresh();
		});
		
		topPanel.add(saveBtn);
		topPanel.add(loadBtn);
		
		topPanel.add(new JLabel("World Width:"));
		topPanel.add(widthField);
		topPanel.add(new JLabel("World Height:"));
		topPanel.add(heightField);
		topPanel.add(regenButton);
		
		
		bottomPanel.add(new JLabel("scroll speed:"));
		bottomPanel.add(speedField);
		
		
		zoomField.addValueListener(val -> testPanel.getMapPanel().repaint());
		bottomPanel.add(new JLabel("zoom:"));
		bottomPanel.add(zoomField);
		
		
		bufferField.addValueListener(val -> testPanel.getMapPanel().repaint());
		bottomPanel.add(new JLabel("scroll buffer:"));
		bottomPanel.add(bufferField);
		
		
		bottomPanel.add(new JLabel("max gen queue length:"));
		bottomPanel.add(queueField);
		
		add(topPanel);
		add(bottomPanel);
		mapPanel = bottomPanel;
		mapPanel.setVisible(false);
	}
	
	private static HashMap<String, String> parseDataList(String[] data) {
		HashMap<String, String> dataMap = new HashMap<>(data.length);
		for(String s: data)
			dataMap.put(s.substring(0, s.indexOf(':')), s.substring(s.indexOf(':') + 1));
		return dataMap;
	}
	
	private void refresh() {
		revalidate();
		repaint();
	}
}
