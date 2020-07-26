package miniventure.game.item;

import miniventure.game.network.GameProtocol.SerialItem;
import miniventure.game.network.GameProtocol.SerialItemStack;
import miniventure.game.texture.FetchableTextureHolder;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.customenum.SerialEnumMap;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientItem extends Item implements EquipmentItem {
	
	@NotNull private final CursorHighlight highlightMode;
	private final float usability; // displayed as a little bar in the item icon.
	@Nullable private final EquipmentType equipmentType;
	@Nullable private final FetchableTextureHolder cursorTexture;
	
	public ClientItem(SerialItem item) {
		super(item.name, item.texture.getTexture());
		
		//noinspection rawtypes
		SerialEnumMap<ItemDataTag> map = new SerialEnumMap<>(item.data, null, ItemDataTag.class);
		
		this.highlightMode = item.highlightMode;
		this.usability = map.getOrDefault(ItemDataTag.Usability, 0f);
		this.equipmentType = map.get(ItemDataTag.EquipmentType);
		this.cursorTexture = map.get(ItemDataTag.CursorSprite);
	}
	
	public float getUsabilityStatus() { return usability; }
	
	@Override @NotNull
	public CursorHighlight getHighlightMode() { return highlightMode; }
	
	@Nullable @Override
	public EquipmentType getEquipmentType() {
		return equipmentType;
	}
	
	@Nullable
	public TextureHolder getCursorTexture() {
		return cursorTexture == null ? null : cursorTexture.tex;
	}
	
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
	public static ItemStack deserializeStack(@NotNull SerialItemStack stack) {
		Item item = new ClientItem(stack.item);
		return new ItemStack(item, stack.count);
	}
	
	/*public static TextureHolder getItemTexture(String data) {
		// surely this can't always work right... it completely disregards what map the item got the sprite from! What if it was using the regular tile atlas? Then again maybe that's a bad example. But there could be name overlap...
		TextureHolder t = GameCore.icons.get(data);
		if(t == null)
			t = GameCore.tileAtlas.getRegion(data);
		if(t == null)
			t = GameCore.entityAtlas.getRegion(data);
		if(t == null)
			throw new SpriteNotFoundException("item texture "+data+" not found in icon, tile, or entity atlas.");
		return t;
	}
	
	private static class SpriteNotFoundException extends RuntimeException {
		SpriteNotFoundException(String msg) {
			super(msg);
		}
	}*/
}
