package miniventure.game.world.entity;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Entity {
	
	private Sprite sprite;
	
	public Entity(Sprite sprite) {
		this.sprite = sprite;
	}
	public Entity(Sprite sprite, int x, int y) {
		this(sprite);
		sprite.setPosition(x, y);
	}
	
	public abstract void update(float delta);
	
	public void render(SpriteBatch batch, float delta) {
		sprite.draw(batch);
	}
	
	protected void setSprite(TextureRegion texture) {
		float x = sprite.getX(), y = sprite.getY();
		sprite = new Sprite(texture);
		sprite.setPosition(x, y);
	}
	
	public void move(int xd, int yd) {
		moveTo((int)sprite.getX()+xd, (int)sprite.getY()+yd);
		// TODO instead of the above, make sure that the entity is not obstructed along the way. Also check for tile and entity touches.
	}
	
	public void moveTo(int x, int y) {
		// this method doesn't care where you end up.
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		x = Math.min(x, GameCore.SCREEN_WIDTH);
		y = Math.min(y, GameCore.SCREEN_HEIGHT);
		sprite.setPosition(x, y);
	}
}
