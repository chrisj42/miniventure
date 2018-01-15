package miniventure.game.item;

import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;

public class ItemData {
	
	// TODO allow items to be animated
	
	@NotNull private final TextureRegion texture;
	private final boolean reflexive;
	private final String name;
	private final int maxStackSize;
	
	ItemData(String name, TextureRegion texture) {
		this(name, texture, 64);
	}
	ItemData(String name, TextureRegion texture, int maxStackSize) {
		this(name, texture, maxStackSize, false);
	}
	ItemData(String name, @NotNull TextureRegion texture, int maxStackSize, boolean reflexive) {
		this.texture = texture;
		this.name = name;
		this.reflexive = reflexive;
		this.maxStackSize = maxStackSize;
	}
	
	@NotNull public TextureRegion getTexture() { return texture; }
	public String getName() { return name; }
	public int getMaxStackSize() { return maxStackSize; }
	boolean isReflexive() { return reflexive; }
	
	// these two below are in case the item has anything to do with the events.
	
	boolean attack(Item item, WorldObject obj, Player player) { return false; }
	boolean interact(Item item, WorldObject obj, Player player) { return false; }
	
	public int getDamage(WorldObject target) { return 1; } // by default
	
}
