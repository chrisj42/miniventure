package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;

import org.jetbrains.annotations.NotNull;

public class Item {
	
	public static final int ICON_SIZE = 32;
	
	// TO-DO allow items to be animated
	
	@NotNull private final String name;
	@NotNull private final TextureHolder texture;
	private final int spaceUsage;
	private final float usability; // displayed as a little bar in the item icon.
	
	Item(@NotNull String name, @NotNull TextureHolder texture) {
		this.name = name;
		this.texture = texture;
		this.spaceUsage = getSpaceUsage(); // since this is a server constructor and the server overrides this method, it will give the right number.
		this.usability = getUsabilityStatus(); // same as with space usage.
	}
	
	private Item(@NotNull String name, @NotNull TextureHolder texture, int spaceUsage, float usability) {
		this.name = name;
		this.texture = texture;
		this.spaceUsage = spaceUsage;
		this.usability = usability;
	}
	
	@NotNull public TextureHolder getTexture() { return texture; }
	@NotNull public String getName() { return name; }
	public int getSpaceUsage() { return spaceUsage; }
	// This returns a value used 
	public float getUsabilityStatus() { return usability; }
	
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
	public int hashCode() { return name.hashCode(); }
	
	@Override
	public String toString() { return name + " Item"; }
	
	public String[] serialize() {
		return new String[] {
			name,
			texture.name,
			String.valueOf(spaceUsage),
			String.valueOf(getUsabilityStatus())
		};
	}
	
	@NotNull
	public static Item deserialize(@NotNull String[] info) {
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
			Integer.parseInt(info[2]),
			Float.parseFloat(info[3])
		);
	}
	
	private static class SpriteNotFoundException extends RuntimeException {
		SpriteNotFoundException(String msg) {
			super(msg);
		}
	}
}
