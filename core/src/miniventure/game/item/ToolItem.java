package miniventure.game.item;

public class ToolItem extends ItemData {
	
	private final ToolType toolType;
	
	public ToolItem(ToolType type) {
		super(type.name(), type.texture, 1);
		this.toolType = type;
	}
	
	public ToolType getType() { return toolType; }
}
