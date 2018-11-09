package miniventure.game.item;

import java.util.Arrays;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ServerItem extends Item {
	
	// NOTE: all data aspects should be final, because one item instance is used to represent a whole stack. Now, with this in mind, one can set a temp var to determine what sort of item to return from the use() method. It should be reset following that, however.
	
	@NotNull private final ItemType type;
	
	private boolean used = false;
	
	ServerItem(@NotNull ItemType type, @NotNull String name) {
		this(type, name.replace("_", " "), GameCore.icons.get("items/"+name.toLowerCase()));
	}
	ServerItem(@NotNull ItemType type, @NotNull String name, @NotNull TextureHolder texture) {
		super(name, texture);
		this.type = type;
	}
	
	@NotNull public ItemType getType() { return type; }
	public int getStaminaUsage() { return 1; } // default; note that without a successful attack or interaction, no stamina is lost.
	@Override
	public int getSpaceUsage() { return 1; } // default
	@Override
	public float getUsabilityStatus() { return 0; }
	
	// called to reset the item
	@Nullable
	final ServerItem resetUsage() {
		ServerItem newItem = getUsedItem();
		used = false;
		return newItem;
	}
	
	/// The item has been used. For most items, this means the item is now depleted, and can no longer be used. Note that there is a contract with this method; it should not modify the state of the current item, however it can return a slightly modified version to be used instead. (space usage shouldn't change)
	// overridden by subclasses to return a new item instance with any change in state that should happen when the item is used; usually though, using an item results in it disappearing.
	@Nullable
	protected ServerItem getUsedItem() { return null; }
	
	public void use() { used = true; }
	public boolean isUsed() { return used; }
	
	// these three below are in case the item has anything to do with the events.
	
	public boolean attack(WorldObject obj, Player player) { return obj.attackedBy(player, this, 1); }
	
	public boolean interact(WorldObject obj, Player player) { return obj.interactWith(player, this); }
	// this is called after all interaction attempts.
	public void interact(Player player) {} // interact reflexively.
	
	// this is used solely for saving to file, not to-client serialization.
	public abstract String[] save();
	
	public abstract ServerItem copy();
	
	
	public static String[] save(@Nullable ServerItem item) {
		if(item == null) return null;
		return item.save();
	}
	
	@Nullable
	public static ServerItem load(@Nullable String[] data) {
		if(data == null) return null;
		ItemType type = ItemType.valueOf(data[0]);
		return type.load(Arrays.copyOfRange(data, 1, data.length));
	}
	
	
}
