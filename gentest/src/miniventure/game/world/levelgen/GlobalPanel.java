package miniventure.game.world.levelgen;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import miniventure.game.world.levelgen.util.FloatField;
import miniventure.game.world.levelgen.util.IntegerField;
import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.levelgen.util.StringField;

/**
 * Contains: global seed selection, world size selection, regen button
 * soon: toolbar? or just "save", "load", and "new" buttons?
 */
public class GlobalPanel extends MyPanel {
	
	private final TestPanel testPanel;
	
	private StringField seedField;
	private JCheckBox customSeedOption;
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
		saveBtn.addActionListener(e -> {
			
		});
		
		topPanel.add(saveBtn);
		
		
		loadBtn = new JButton("Load");
		topPanel.add(loadBtn);
		
		/*seedField = new StringField("This doesn't actually do anything.", 20);
		customSeedOption = new JCheckBox("Custom Seed", false);
		customSeedOption.addActionListener(e -> {
			seedField.setEditable(customSeedOption.isSelected());
			refresh();
		});
		seedField.setEditable(customSeedOption.isSelected());
		*/
		widthField = new IntegerField(0, 4, 0);
		heightField = new IntegerField(0, 4, 0);
		
		regenButton = new JButton("Regen world");
		regenButton.addActionListener(e -> {
			testPanel.setFocus(testPanel.getMapPanel());
			testPanel.getMapPanel().generate(widthField.getValue(), heightField.getValue());
		});
		
		// add(customSeedOption);
		// add(new JLabel("Seed:"));
		// add(seedField);
		topPanel.add(new JLabel("World Width:"));
		topPanel.add(widthField);
		topPanel.add(new JLabel("World Height:"));
		topPanel.add(heightField);
		topPanel.add(regenButton);
		
		speedField = new IntegerField(5, 2, 1);
		bottomPanel.add(new JLabel("scroll speed:"));
		bottomPanel.add(speedField);
		
		zoomField = new IntegerField(2, 2, 1);
		zoomField.addValueListener(val -> testPanel.getMapPanel().repaint());
		bottomPanel.add(new JLabel("zoom:"));
		bottomPanel.add(zoomField);
		
		bufferField = new FloatField(3, 2, 0);
		bufferField.addValueListener(val -> testPanel.getMapPanel().repaint());
		bottomPanel.add(new JLabel("scroll buffer:"));
		bottomPanel.add(bufferField);
		
		queueField = new IntegerField(3, 2, 1);
		bottomPanel.add(new JLabel("max gen queue length:"));
		bottomPanel.add(queueField);
		
		add(topPanel);
		add(bottomPanel);
		mapPanel = bottomPanel;
	}
	
	private void refresh() {
		revalidate();
		repaint();
	}
}
