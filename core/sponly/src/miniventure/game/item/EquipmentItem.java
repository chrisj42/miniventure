package miniventure.game.item;

import org.jetbrains.annotations.NotNull;

public interface EquipmentItem {
	
	@NotNull
	EquipmentType getEquipmentType();
	
	enum EquipmentType {
		ARMOR, ACCESSORY;
		
		EquipmentType() {}
		
		public static final EquipmentType[] values = EquipmentType.values();
	}
}
