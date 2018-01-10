package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;

public abstract class Item {
	
	/*
		Will have a use() method, to mark that an item has gotten used. Called by tiles and entities. This class will determine whether it can be used again, however.
		Perhaps later I can add a parameter to the use method to specify how *much* to use it.
		
		
	 */
	
	private static final TextureRegion missing = GameCore.icons.get("missing");
	
	@NotNull private TextureRegion texture;
	private boolean used = false;
	
	public Item(TextureRegion texture) {
		this.texture = texture == null ? missing : texture;
	}
	
	protected void setUsed() { used = true; }
	public boolean isUsed() { return used; }
	
	@NotNull
	public TextureRegion getTexture() { return texture; }
	
	public abstract String getName();
	
	public abstract boolean isReflexive();
	
	public boolean interact(Player player, Tile tile) { return false; }
	
	public int getDamage(WorldObject target) { return 1; }
	
	public abstract Item clone();
}
