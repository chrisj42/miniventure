package miniventure.game.item;

import java.util.Arrays;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ServerItem extends Item {
	
	// NOTE: all data aspects should be final, because one item instance is used to represent a whole stack. Now, with this in mind, one can set a temp var to determine what sort of item to return from the use() method. It should be reset following that, however.
	
	/*
		as soon as interactions are implemented in the new way, and the "hands" situation is worked out, and everything is good, *then* we will move interactions to be server-only, which will include removing interact and attack from the WorldObject class.
		
		The render method will have to stay because the server still manages some of the rendering; it manages entity renderers. Normally I would want to fix this, but the renderer is meshed with the collision box which the server *does* need to know about, so it's not really that simple to remove it. I may just leave it that way to the end of the project, because it's really not worth it.
		
		Also remember to check for uses of blank.png and white.png since I made them only one pixel; that's what started this whole thing lol
	 */
	
	@NotNull private final ItemType type;
	
	protected ServerItem(@NotNull ItemType type, @NotNull String name) {
		this(type, name, true);
	}
	protected ServerItem(@NotNull ItemType type, @NotNull String name, boolean formatName) {
		this(type, name, formatName, GameCore.icons.get("items/"+name.toLowerCase()));
	}
	protected ServerItem(@NotNull ItemType type, @NotNull String name, boolean formatName, @NotNull TextureHolder texture) {
		this(type, formatName ? MyUtils.toTitleCase(name).replaceAll("_", " ") : name, texture);
	}
	protected ServerItem(@NotNull ItemType type, @NotNull String name, @NotNull TextureHolder texture) {
		super(name, texture);
		this.type = type;
	}
	
	@NotNull public ItemType getType() { return type; }
	public int getStaminaUsage() { return 1; } // default; note that without a successful attack or interaction, no stamina is lost.
	@Override
	public float getUsabilityStatus() { return 0; }
	
	@Override @NotNull
	public abstract Player.CursorHighlight getHighlightMode();
	
	/// The item has been used. For most items, this means the item is now depleted, and can no longer be used. Note that there is a contract with this method; it should not modify the state of the current item, however it can return a slightly modified version to be used instead. (space usage shouldn't change)
	// overridden by subclasses to return a new item instance with any change in state that should happen when the item is used; usually though, using an item results in it disappearing.
	@Nullable
	public ServerItem getUsedItem() { return null; }
	
	// these three below are in case the item has anything to do with the events.
	
	public Result attack(WorldObject obj, Player player) { return obj.attackedBy(player, this, 1); }
	
	public Result interact(WorldObject obj, Player player) { return obj.interactWith(player, this); }
	// this is called after all interaction attempts.
	public Result interact(Player player) { return Result.NONE; } // interact reflexively.
	
	// this is used solely for saving to file, not to-client serialization.
	public abstract String[] save();
	
	
	public static String[] save(@Nullable ServerItem item) {
		if(item == null) return null;
		return item.save();
	}
	
	/** @noinspection ConstantConditions*/
	@Nullable
	public static ServerItem load(@Nullable String[] data, @NotNull Version version) {
		if(data == null) return null;
		if(version.atOrBefore("2.2.1.1")) {
			if(data[0].equals("Enum"))
				// cut out the "enum" value because they got moved up
				data = Arrays.copyOfRange(data, 1, data.length);
			else if(data[0].equals("Misc"))
				data[0] = "Hand";
		}
		ItemType type = ItemType.valueOf(data[0]);
		return type.load(Arrays.copyOfRange(data, 1, data.length), version);
	}
	
}
