package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientItem extends Item {
	
	private final float usability; // displayed as a little bar in the item icon.
	@NotNull private final CursorHighlight highlightMode;
	@Nullable private final EquipmentSlot equipmentType;
	
	ClientItem(@NotNull String name, @NotNull TextureHolder texture, float usability, @NotNull CursorHighlight highlightMode, @Nullable EquipmentSlot equipmentType) {
		super(name, texture);
		
		this.usability = usability;
		this.highlightMode = highlightMode;
		this.equipmentType = equipmentType;
	}
	
	@Override
	public float getUsabilityStatus() { return usability; }
	
	@Override @NotNull
	public CursorHighlight getHighlightMode() { return highlightMode; }
	
	@Override @Nullable
	public EquipmentSlot getEquipmentType() { return equipmentType; }
	
	@Override
	public boolean equals(Object other) {
		if(!super.equals(other)) return false;
		if(usability > 0 && usability < 1)
			return this == other; // anything with partial durability cannot ever stack. 
		if(!getClass().equals(other.getClass())) return false;
		ClientItem o = (ClientItem) other;
		return usability == o.usability;
	}
	
	@Override
	public int hashCode() { return super.hashCode() + Float.hashCode(usability); }
	
	@NotNull
	public static ItemStack deserializeStack(@NotNull String[] data) {
		int count = ItemStack.fetchCount(data);
		Item item = ClientItem.deserialize(ItemStack.fetchItemData(data));
		return new ItemStack(item, count);
	}
	
	public static ClientItem deserialize(String[] data) {
		if(data == null) return null;
		// FIXME surely this can't always work right... it completely disregards what map the item got the sprite from! What if it was using the regular tile atlas? Then again maybe that's a bad example. But there could be name overlap...
		TextureHolder t = GameCore.icons.get(data[1]);
		if(t == null)
			t = GameCore.tileAtlas.getRegion(data[1]);
		if(t == null)
			t = GameCore.entityAtlas.getRegion(data[1]);
		if(t == null)
			throw new SpriteNotFoundException("item texture "+data[1]+" not found in icon, tile, or entity atlas.");
		return new ClientItem(
			data[0],
			t,
			Float.parseFloat(data[2]),
			CursorHighlight.values[Integer.parseInt(data[3])],
			data[4].equals("null") ? null : EquipmentSlot.values[Integer.parseInt(data[4])]
		);
	}
	
	private static class SpriteNotFoundException extends RuntimeException {
		SpriteNotFoundException(String msg) {
			super(msg);
		}
	}
}
