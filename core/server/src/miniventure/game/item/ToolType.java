package miniventure.game.item;

import miniventure.game.util.customenum.SerialEnumMap;
import miniventure.game.world.entity.mob.player.Player;

import org.jetbrains.annotations.NotNull;

public enum ToolType implements ServerItemSource {
	
	Crude_Axe(ToolClass.Axe, 80),
	Crude_Pickaxe(ToolClass.Pickaxe, 80),
	Crude_Shovel(ToolClass.Shovel, 60),
	Club(ToolClass.Club, 120),
	
	Iron_Axe(ToolClass.Axe, 350),
	Iron_Pickaxe(ToolClass.Pickaxe, 350),
	Iron_Shovel(ToolClass.Shovel, 300),
	Sword(ToolClass.Sword, 300)
	;
	
	public static final ToolType[] values = ToolType.values();
	
	@NotNull
	private final ToolClass toolClass;
	private final int maxDurability;
	
	@NotNull
	private final ToolItem item;
	
	ToolType(@NotNull ToolClass toolClass, int maxDurability) {
		this.toolClass = toolClass;
		this.maxDurability = maxDurability;
		item = new ToolItem(this, maxDurability);
	}
	
	@Override @NotNull
	public ServerItem get() {
		return item;
	}
	
	public static class ToolItem extends ServerItem {
		
		// todo determine if I want to have all tools follow the same material/upgrade path, or let them deviate
		// - if deviate, determine how to provide levels; perhaps just a fetcher that is provided the tier?
			// - perhaps I want to give each tool type an enum for the levels, where they all implement a "ToolLevel" interface or something
		
		/* Tool aspects:
			- base damage: ToolType; provide an ineffective and effective damage stat
			- 
			
		 */
		
		@NotNull
		private final ToolType type;
		private final int durability;
		
		private ToolItem(@NotNull ToolType type, int durability) {
			super(ItemType.Tool, type.name(), "tools");
			this.type = type;
			this.durability = durability;
		}
		
		public ToolClass getToolType() { return type.toolClass; }
		
		@Override
		public ToolItem getUsedItem() {
			if(durability > 1)
				return new ToolItem(type, durability-1);
			
			return null;
		}
		
		@Override
		public int getStaminaUsage() { return type.toolClass.staminaUsage; }
		
		// the the Item class will have the wrong value, but it only references it through this method so this override will negate any effects.
		public float getDurability() { return type.maxDurability == 0 ? 0 : durability / (float) type.maxDurability; }
		
		@Override @NotNull
		public Player.CursorHighlight getHighlightMode() {
			return type.toolClass.cursorType;
		}
		
		@Override
		protected void addSerialData(SerialEnumMap<ItemDataTag<?>> map) {
			super.addSerialData(map);
			map.put(ItemDataTag.Usability, getDurability());
		}
		
		@Override
		public boolean equals(Object other) {
			return super.equals(other) && ((ToolItem)other).durability == durability;
		}
		
		@Override
		public int hashCode() { return super.hashCode() + durability; }
		
		@Override
		public String[] save() {
			return new String[] {getType().name(), type.name(), String.valueOf(durability)};
		}
		
		public static ToolItem load(String[] data) {
			ToolType type = ToolType.valueOf(data[0]);
			int dur = Integer.parseInt(data[1]);
			return new ToolItem(type, dur);
		}
		
		@Override public String toString() { return "ToolItem("+type+",dura="+durability+')'; }
	}
}
