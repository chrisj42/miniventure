package miniventure.game.world.entity;

import miniventure.game.item.Item;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ItemEntity extends Entity {
	
	public ItemEntity(Item item, int xvel, int yvel) {
		super(new Sprite(item.getTexture(), 0, 0, 0, 0));
	}
	
	@Override
	public void update(float delta) {
		
	}
	
	@Override
	public void render(SpriteBatch batch, float delta) {
		
	}
}
