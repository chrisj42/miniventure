package miniventure.game.item.type;

import miniventure.game.GameCore;

public class ResourceItem extends Item {
	
	private String name;
	
	ResourceItem(String name) {
		super(name, GameCore.icons.get(name));
		this.name = name;
	}
	
	@Override
	public Item copy() {
		return new ResourceItem(name);
	}
}
