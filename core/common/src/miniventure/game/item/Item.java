package miniventure.game.item;

import miniventure.game.texture.TextureHolder;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Item {
	
	public static final int ICON_SIZE = 16;
	
	// TO-DO allow items to be animated
	
	@NotNull private final String name;
	@NotNull private final TextureHolder texture;
	
	Item(@NotNull String name, @NotNull TextureHolder texture) {
		this.name = name;
		this.texture = texture;
	}
	
	@NotNull public TextureHolder getTexture() { return texture; }
	@NotNull public String getName() { return name; }
	
	public abstract float getUsabilityStatus();
	
	@NotNull
	public abstract CursorHighlight getHighlightMode();
	
	@Nullable
	public abstract EquipmentSlot getEquipmentType();
	
	// make sure this continues to reflect all important state in subclass implementations. Usually it will be covered by the name, but otherwise (such as with tools and their durability) the subclass ought to take that state into account.
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(!getClass().equals(other.getClass())) return false;
		Item o = (Item) other;
		return name.equals(o.name);
	}
	
	@Override
	public int hashCode() { return name.hashCode(); }
	
	@Override
	public String toString() { return name + ' '+getClass().getSimpleName(); }
	
	public String[] serialize() {
		EquipmentSlot equipmentType = getEquipmentType();
		return new String[] {
			name,
			texture.name,
			String.valueOf(getUsabilityStatus()),
			String.valueOf(getHighlightMode().ordinal()),
			equipmentType == null ? "null" : String.valueOf(equipmentType.ordinal())
		};
	}
}
