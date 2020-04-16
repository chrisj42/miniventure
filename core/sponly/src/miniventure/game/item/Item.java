package miniventure.game.item;

import miniventure.game.network.GameProtocol.SerialItem;
import miniventure.game.texture.FetchableTextureHolder;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Item {
	
	public static final int ICON_SIZE = 16;
	
	// TO-DO allow items to be animated
	
	@NotNull private final String name;
	@NotNull private final FetchableTextureHolder texture;
	
	Item(@NotNull String name, @NotNull FetchableTextureHolder texture) {
		this.name = name;
		this.texture = texture;
	}
	
	@NotNull public TextureHolder getTexture() { return texture.tex; }
	@NotNull public FetchableTextureHolder getFetchableTexture() { return texture; }
	@NotNull public String getName() { return name; }
	
	@NotNull
	public abstract CursorHighlight getHighlightMode();
	
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
}
