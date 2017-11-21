package miniventure.game.world.mob;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player extends Mob {
	
	public Player() {
		super("player");
	}
	
	private void checkInput(float delta) {
		// checks for keyboard input to move the player.
		// getDeltaTime() returns the time passed between the last and the current frame in seconds.
		int speed = 200; // this is technically in units/second.
		int xd = 0, yd = 0;
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) xd -= speed * delta;
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) xd += speed * delta;
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) yd += speed * delta;
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) yd -= speed * delta;
		
		move(xd, yd);
	}
	
	@Override
	public void render(SpriteBatch batch, float delta) {
		checkInput(delta);
		
		super.render(batch, delta);
	}
}
