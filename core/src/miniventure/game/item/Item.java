package miniventure.game.item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Item {
	
	/*
		Will have a use() method, to mark that an item has gotten used. Called by tiles and entities. This class will determine whether it can be used again, however.
		Perhaps later I can add a parameter to the use method to specify how *much* to use it.
		
		
	 */
	
	private TextureRegion texture;
	
	public Item(TextureRegion texture) {
		this.texture = texture;
	}
	
	public TextureRegion getTexture() { return texture; }
	
	public abstract boolean isReflexive();
	
	public int getDamage() { return 1; }
	
}
