package miniventure.game.world.entity;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Entity {
	
	protected int x, y;
	
	public Entity() { this(0, 0); }
	public Entity(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public abstract void render(SpriteBatch batch, float delta);
	
	public void move(int xd, int yd) {
		x += xd;
		y += yd;
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		x = Math.min(x, GameCore.SCREEN_WIDTH);
		y = Math.min(y, GameCore.SCREEN_HEIGHT);
	}
	
	public void moveTo(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
