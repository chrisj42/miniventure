package miniventure.game.item;

import java.util.Arrays;

import miniventure.game.texture.TextureHolder;

import org.jetbrains.annotations.NotNull;

public abstract class ServerItem extends Item {
	
	@NotNull private final ItemType type;
	
	ServerItem(@NotNull ItemType type, @NotNull String name) {
		super(name);
		this.type = type;
	}
	
	ServerItem(@NotNull ItemType type, @NotNull String name, @NotNull TextureHolder texture) {
		super(name, texture);
		this.type = type;
	}
	
	@NotNull public ItemType getType() { return type; }
	
	public abstract String[] save();
	
	public static String[] save(ServerItem item) {
		if(item == null) return null;
		return item.save();
	}
	
	public static ServerItem load(String[] data) {
		if(data == null) return null;
		ItemType type = ItemType.valueOf(data[0]);
		return type.load(Arrays.copyOfRange(data, 1, data.length));
	}
}
