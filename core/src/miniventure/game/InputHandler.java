package miniventure.game;

import java.util.HashMap;
import java.util.HashSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

public class InputHandler implements InputProcessor {
	
	private static final float INITIAL_DELAY = 0.5f; // delay between initial press and first simulated press.
	private static final float REPEAT_DELAY = 0.1f; // delay between each simulated press.
	
	private final HashMap<Integer, Float> keyPresses = new HashMap<>(); // keys in here are currently being held down. The value stored is the initial time of press.
	private final HashSet<Integer> pressedKeys = new HashSet<>(); // keys here are treated as being just pressed down.
	private float prevUpdate;
	
	InputHandler() {}
	
	void update() {
		pressedKeys.clear();
		
		final float curTime = GameCore.getElapsedProgramTime();
		
		for(Integer keycode: keyPresses.keySet()) {
			if(prevUpdate < INITIAL_DELAY && curTime >= INITIAL_DELAY)
				pressedKeys.add(keycode);
			else if(prevUpdate >= INITIAL_DELAY) {
				float prevTime = (prevUpdate - INITIAL_DELAY) % REPEAT_DELAY;
				float delta = curTime - prevUpdate;
				if(prevTime + delta >= REPEAT_DELAY)
					pressedKeys.add(keycode);
			}
		}
		
		prevUpdate = curTime;
	}
	
	public boolean pressingKey(int keycode) {
		return Gdx.input.isKeyJustPressed(keycode) || pressedKeys.contains(keycode);
	}
	
	
	@Override
	public boolean keyDown(int keycode) {
		keyPresses.put(keycode, GameCore.getElapsedProgramTime());
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		keyPresses.remove(keycode);
		return false;
	}
	
	@Override public boolean keyTyped(char character) { return false; }
	
	@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
	@Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
	@Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
	@Override public boolean mouseMoved(int screenX, int screenY) { return false; }
	@Override public boolean scrolled(int amount) { return false; }
	
}
