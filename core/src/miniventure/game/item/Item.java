package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;

public abstract class Item {
	
	/*
		Will have a use() method, to mark that an item has gotten used. Called by tiles and entities. This class will determine whether it can be used again, however.
		Perhaps later I can add a parameter to the use method to specify how *much* to use it.
		
		
	 */
	
	private static final TextureRegion missing = GameCore.icons.get("missing");
	
	@NotNull private TextureRegion texture;
	private final boolean reflexive;
	private boolean used = false;
	
	public Item(TextureRegion texture) { this(false, texture); }
	public Item(boolean reflexive, TextureRegion texture) {
		this.texture = texture == null ? missing : texture;
		this.reflexive = reflexive;
	}
	
	protected void setUsed() { used = true; }
	public boolean isUsed() { return used; }
	
	
	public Item consume() {
		if(!used) return this;
		
		return null; // generally, used up items disappear. But, tool items will only lose durability, and stacks will decrease by one. 
	}
	
	@NotNull
	public TextureRegion getTexture() { return texture; }
	
	public abstract String getName();
	
	public boolean interact(Player player) {
		if(reflexive) {
			player.interactWith(player, this);
			return true; // do not interact with other things
		}
		return false; // continue
	}
	
	// these two below are in case the item has anything to do with the events.
	
	public boolean attack(WorldObject obj, Player player) { return false; }
	public boolean interact(WorldObject obj, Player player) { return false; }
	
	public int getDamage(WorldObject target) { return 1; } // by default
	
	public abstract Item clone();
}
