package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Item {
	
	// TODO allow items to be animated
	
	// NOTE: all data aspects should be final, because one item instance is used to represent a whole stack. Now, with this in mind, one can set a temp var to determine what sort of item to return from the use() method. It should be reset following that, however.
	
	@NotNull private final TextureRegion texture;
	private final String name;
	
	Item(String name, @NotNull TextureRegion texture) {
		this.texture = texture;
		this.name = name;
	}
	
	@NotNull public TextureRegion getTexture() { return texture; }
	public String getName() { return name; }
	public int getMaxStackSize() { return 64; } // by default
	public int getStaminaUsage() { return 1; } // default; note that without a successful attack or interaction, no stamina is lost.
	
	/// The item has been used. For most items, this means the item is now depleted, and can no longer be used. Note that there is a contract with this method; it should not modify the state of the current item, however it can return a slightly modified version to be used instead.
	@Nullable public Item use() { return null; }
	
	// these three below are in case the item has anything to do with the events.
	
	boolean interact(Player player) { return false; } // interact reflexively.
	boolean interact(WorldObject obj, Player player) { return false; }
	boolean attack(WorldObject obj, Player player) { return false; }
	
	public int getDamage(WorldObject target) { return 1; } // by default
	
	public void drawItem(int stackSize, Batch batch, BitmapFont font, float x, float y) {
		batch.draw(GameCore.icons.get("hotbar"), x-2, y-2);
		batch.draw(texture, x, y);
		MyUtils.writeOutlinedText(font, batch, stackSize+"", x+1, y+font.getCapHeight()+1);
		
		float width = texture.getRegionWidth();
		float height = texture.getRegionHeight();
		MyUtils.writeOutlinedText(font, batch, name, x+width+10, y-5+height*2/3);
	}
	
	@Override
	public boolean equals(Object other) {
		if(!getClass().equals(other.getClass())) return false;
		Item o = (Item) other;
		return name.equals(o.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public Item copy() {
		return new Item(name, texture);
	}
	
	@Override
	public String toString() {
		return name + " Item";
	}
}
