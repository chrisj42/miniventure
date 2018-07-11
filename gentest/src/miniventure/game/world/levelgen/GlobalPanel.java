package miniventure.game.world.levelgen;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import miniventure.game.world.levelgen.util.IntegerField;
import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.levelgen.util.ValidatedField;

/**
 * Contains: global seed selection, world size selection, regen button
 * soon: toolbar? or just "save", "load", and "new" buttons?
 */
public class GlobalPanel extends MyPanel {
	
	private final TestPanel testPanel;
	
	private ValidatedField<String> seedField;
	private JCheckBox customSeedOption;
	final IntegerField widthField;
	final IntegerField heightField;
	
	private JButton regenButton;
	
	public GlobalPanel(TestPanel testPanel) {
		this.testPanel = testPanel;
		/*
			- global world seed selector field
				- checkbox to left for "custom world seed"; text field is only editable when this is checked (if you want to keep same seed, check this)
			- world size (width, height)
			- "regen level"	button, which will only be enabled if the settings are valid
		 */
		
		seedField = new ValidatedField<>(String::valueOf, val -> !customSeedOption.isSelected() || val.length() > 0);
		seedField.setColumns(20);
		customSeedOption = new JCheckBox("Custom Seed", false);
		customSeedOption.addActionListener(e -> {
			seedField.setEditable(customSeedOption.isSelected());
			seedField.revalidateText(false);
			refresh();
		});
		seedField.setEditable(customSeedOption.isSelected());
		
		widthField = new IntegerField(0, 4, 0);
		heightField = new IntegerField(0, 4, 0);
		
		regenButton = new JButton("Regen world");
		regenButton.addActionListener(e -> {
			testPanel.setFocus(testPanel.getMapPanel());
			testPanel.getMapPanel().generate(widthField.getValue(), heightField.getValue());
		});
		
		seedField.addListener(field -> refresh());
		
		add(customSeedOption);
		add(new JLabel("Seed:"));
		add(seedField);
		add(new JLabel("World Width:"));
		add(widthField);
		add(new JLabel("World Height:"));
		add(heightField);
		add(regenButton);
	}
	
	private void refresh() {
		regenButton.setEnabled(seedField.getValid());
		revalidate();
		repaint();
	}
}
