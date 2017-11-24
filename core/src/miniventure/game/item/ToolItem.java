package miniventure.game.item;

public class ToolItem extends Item {
	
	private final ToolType toolType;
	
	public ToolItem(ToolType type) {
		this.toolType = type;
	}
	
	public ToolType getType() { return toolType; }
	
	
	@Override
	public boolean isRelfexive() {
		return false;
	}
}
