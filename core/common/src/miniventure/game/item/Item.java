package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;

import org.jetbrains.annotations.NotNull;

public class Item {
	
	public static final int ICON_SIZE = 16;
	
	// TO-DO allow items to be animated
	
	@NotNull private final String name;
	@NotNull private final TextureHolder texture;
	private final float usability; // displayed as a little bar in the item icon.
	private final CursorHighlight highlightMode;
	
	Item(@NotNull String name, @NotNull TextureHolder texture) {
		this.name = name;
		this.texture = texture;
		this.usability = getUsabilityStatus(); // same as with space usage.
		this.highlightMode = null; // expected that the accessor method will be overridden
	}
	
	private Item(@NotNull String name, @NotNull TextureHolder texture, float usability, @NotNull CursorHighlight highlightMode) {
		this.name = name;
		this.texture = texture;
		this.usability = usability;
		this.highlightMode = highlightMode;
	}
	
	@NotNull public TextureHolder getTexture() { return texture; }
	@NotNull public String getName() { return name; }
	// This returns a value used 
	public float getUsabilityStatus() { return usability; }
	
	@NotNull public CursorHighlight getHighlightMode() { return highlightMode; }
	
	// make sure this continues to reflect all important state in subclass implementations. Usually it will be covered by the name, but otherwise (such as with tools and their durability) the subclass ought to take that state into account.
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(usability > 0 && usability < 1)
			return this == other; // anything with partial durability cannot ever stack. 
		if(!getClass().equals(other.getClass())) return false;
		Item o = (Item) other;
		return name.equals(o.name) && usability == o.usability;
	}
	
	@Override
	public int hashCode() { return name.hashCode() + Float.hashCode(usability); }
	
	@Override
	public String toString() { return name + ' '+getClass().getSimpleName(); }
	
	public String[] serialize() {
		return new String[] {
			name,
			texture.name,
			String.valueOf(getUsabilityStatus()),
			String.valueOf(getHighlightMode().ordinal())
		};
	}
	
	@NotNull
	public static Item deserialize(@NotNull String[] info) {
		// FIXME surely this can't always work right... it completely disregards what map the item got the sprite from! What if it was using the regular tile atlas? Then again maybe that's a bad example. But there could be name overlap...
		TextureHolder t = GameCore.icons.get(info[1]);
		if(t == null)
			t = GameCore.descaledTileAtlas.getRegion(info[1]);
		if(t == null)
			t = GameCore.entityAtlas.getRegion(info[1]);
		if(t == null)
			throw new SpriteNotFoundException("item texture "+info[1]+" not found in icon, tile, or entity atlas.");
		return new Item(
			info[0],
			t,
			Float.parseFloat(info[2]),
			CursorHighlight.values[Integer.parseInt(info[3])]
		);
	}
	
	private static class SpriteNotFoundException extends RuntimeException {
		SpriteNotFoundException(String msg) {
			super(msg);
		}
	}
}
