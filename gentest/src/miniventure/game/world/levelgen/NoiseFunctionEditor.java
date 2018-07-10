package miniventure.game.world.levelgen;

import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.levelgen.util.ValidatedField;

class NoiseFunctionEditor extends MyPanel implements NamedObject {
	
	private ValidatedField<String> name;
	private ValidatedField<Integer> numCurves;
	
	NoiseFunctionEditor() {
		name = new ValidatedField<>(String::valueOf, str -> {
			if(str.length() == 0) return false;
			return true; // TODO check for duplicates
		});
		
		numCurves = new ValidatedField<>(Integer::parseInt, ValidatedField.POSITIVE);
	}
	
	@Override
	public void setObjectName(String name) {
		this.name.setText(name);
	}
	
	@Override
	public String getObjectName() {
		return name.getValue();
	}
}
