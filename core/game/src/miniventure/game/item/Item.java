package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Item {
	
	public static final float ICON_SIZE = 32;
	
	// TODO allow items to be animated
	
	// NOTE: all data aspects should be final, because one item instance is used to represent a whole stack. Now, with this in mind, one can set a temp var to determine what sort of item to return from the use() method. It should be reset following that, however.
	
	@NotNull private final TextureRegion texture;
	@NotNull private final String name;
	
	private boolean used = false;
	
	Item(@NotNull String name, TextureRegion texture) {
		this.texture = texture == null ? new TextureRegion() : texture;
		this.name = name;
	}
	
	@NotNull public TextureRegion getTexture() { return texture; }
	@NotNull public String getName() { return name; }
	public int getMaxStackSize() { return 64; } // by default
	public int getStaminaUsage() { return 1; } // default; note that without a successful attack or interaction, no stamina is lost.
	
	// called to reset the item
	@Nullable public final Item resetUsage() {
		Item newItem = getUsedItem();
		used = false;
		return newItem;
	}
	
	/// The item has been used. For most items, this means the item is now depleted, and can no longer be used. Note that there is a contract with this method; it should not modify the state of the current item, however it can return a slightly modified version to be used instead.
	// overridden by subclasses to return a new item instance with any change in state that should happen when the item is used; usually though, using an item results in it disappearing.
	@Nullable
	protected Item getUsedItem() { return null; }
	
	public void use() { used = true; }
	public boolean isUsed() { return used; }
	
	// these three below are in case the item has anything to do with the events.
	
	public boolean attack(WorldObject obj, Player player) { return obj.attackedBy(player, this, 1); }
	
	public boolean interact(WorldObject obj, Player player) { return obj.interactWith(player, this); }
	// this is called after all interaction attempts.
	public void interact(Player player) {} // interact reflexively.
	
	private float renderWidth;
	private float renderHeight;
	private boolean initializedWidth = false;
	private boolean initializedHeight = false;
	
	public void drawItem(int stackSize, Batch batch, BitmapFont font, float x, float y) {
		drawItem(stackSize, batch, font, x, y, Color.WHITE);
	}
	public void drawItem(int stackSize, Batch batch, BitmapFont font, float x, float y, Color textColor) {
		float width = texture.getRegionWidth();
		//float height = texture.getRegionHeight();
		float tx = x + Math.max(0, (ICON_SIZE - texture.getRegionWidth())/2);
		
		Color prev = batch.getColor();
		batch.setColor(Color.BLACK);
		batch.draw(texture, tx-2, y-2);
		batch.setColor(prev);
		batch.draw(texture, tx, y);
		
		float textOff = font.getCapHeight() + font.getAscent();
		MyUtils.writeOutlinedText(font, batch, stackSize+"", x+1, y+textOff-font.getDescent(), textColor);
		MyUtils.writeOutlinedText(font, batch, name, x+width+10, y+(getRenderHeight()+textOff)/2, textColor);
	}
	
	public float getRenderHeight() {
		if(!initializedHeight) {
			renderHeight = Math.max(texture.getRegionHeight(), GameCore.getTextLayout(name).height);
			initializedHeight = true;
		}
		return renderHeight;
	}
	
	public float getRenderWidth() {
		if(!initializedWidth) {
			renderWidth = texture.getRegionWidth() + 10 + GameCore.getTextLayout(name).width;
			initializedWidth = true;
		}
		return renderWidth;
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
	
	public abstract Item copy();
	
	@Override
	public String toString() {
		return name + " Item";
	}
}
