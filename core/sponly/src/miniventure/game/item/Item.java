package miniventure.game.item;

import miniventure.game.core.GdxCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.CursorHighlight;
import miniventure.game.world.entity.mob.player.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Item {
	
	public static final int ICON_SIZE = 16;
	
	// TO-DO allow items to be animated
	
	@NotNull private final ItemType type;
	@NotNull private final String name;
	@NotNull private final TextureHolder texture;
	
	private String saveData = null;
	
	protected Item(@NotNull ItemType type, @NotNull String name, String category) {
		this(type, name, GdxCore.icons.get("items/"+category+'/'+name.toLowerCase()));
	}
	protected Item(@NotNull ItemType type, @NotNull String name, @NotNull TextureHolder texture) {
		this.type = type;
		this.name = name;
		this.texture = texture;
	}
	
	@NotNull public ItemType getType() { return type; }
	
	@NotNull public String getName() { return name; }
	
	@NotNull public TextureHolder getTexture() { return texture; }
	
	@NotNull
	public abstract CursorHighlight getHighlightMode();
	
	@Nullable
	public TextureHolder getCursorTexture() { return null; }
	
	public int getStaminaUsage() { return 1; } // default; note that without a successful attack or interaction, no stamina is lost.
	
	/// The item has been used. For most items, this means the item is now depleted, and can no longer be used. Note that there is a contract with this method; it should not modify the state of the current item, however it can return a slightly modified version to be used instead. (space usage shouldn't change)
	// overridden by subclasses to return a new item instance with any change in state that should happen when the item is used; usually though, using an item results in it disappearing.
	@Nullable
	public Item getUsedItem() { return null; }
	
	// these three below are in case the item has anything to do with the events.
	
	public Result attack(WorldObject obj, Player player) { return obj.attackedBy(player, this, 1); }
	
	public Result interact(WorldObject obj, Player player) { return obj.interactWith(player, this); }
	// this is called after all interaction attempts.
	public Result interact(Player player) { return Result.NONE; } // interact reflexively.
	
	// most items can safely be stacked.
	public boolean isStackable() { return true; }
	
	// generates item save data.
	// since items are immutable, this is not meant to be called multiple times. The Item class calls it once, caches the result, and uses that from then on.
	protected abstract String compileSaveData();
	
	// this is used solely for saving to file, not to-client serialization.
	public String save() {
		if(saveData == null)
			saveData = MyUtils.encodeStringArray(type.name(), compileSaveData());
		return saveData;
	}
	
	public static Item load(@NotNull String data, @NotNull Version version) {
		String[] dataAr = MyUtils.parseLayeredString(data);
		ItemType type = ItemType.valueOf(dataAr[0]);
		return type.loader.load(dataAr[1], version);
	}
	
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
