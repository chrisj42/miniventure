package miniventure.game.world.entity.mob;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Player extends Mob {
	
	public Player() {
		super("player");
	}
	
	public void checkInput(float delta) {
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
	public void update(float delta) {
		// pollAnimation things like hunger, stamina, etc.
	}
}
