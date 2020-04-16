package miniventure.game.item;

import miniventure.game.texture.FetchableTextureHolder;

import org.jetbrains.annotations.NotNull;

public abstract class ServerEquipmentItem extends ServerItem implements EquipmentItem {
	
	@NotNull
	private final EquipmentType equipmentType;
	
	protected ServerEquipmentItem(@NotNull EquipmentType equipmentType, @NotNull ItemType type, @NotNull String name, String category) {
		super(type, name, category);
		this.equipmentType = equipmentType;
	}
	
	protected ServerEquipmentItem(@NotNull EquipmentType equipmentType, @NotNull ItemType type, @NotNull String name, @NotNull FetchableTextureHolder texture) {
		super(type, name, texture);
		this.equipmentType = equipmentType;
	}
	
	@Override @NotNull
	public EquipmentType getEquipmentType() { return equipmentType; }
	
	@Override
	public ServerItem getUsedItem() {
		return this; // using equipment generally means equipping it; this modifies the inventory, so continuing to do the normal thing when an item is used would probably mess things up. If we return this, then it doesn't bother doing any of that, so everything is fine.
	}
}
