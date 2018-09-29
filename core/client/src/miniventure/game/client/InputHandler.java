package miniventure.game.client;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;

import miniventure.game.GameCore;

import com.badlogic.gdx.InputProcessor;

public class InputHandler implements InputProcessor {
	
	
	private static final HashMap<Integer, Integer> awtToGDXKeyCodes = new HashMap<>();
	static {
		// for(KeyEvent.VK_)
	}
	
	private static final float INITIAL_DELAY = 0.5f; // delay between initial press and first simulated press.
	private static final float REPEAT_DELAY = 0.2f; // delay between each simulated press.
	
	private final HashMap<Integer, Float> keyPresses = new HashMap<>(); // keys in here are currently being held down. The value stored is the initial time of press.
	private final HashSet<Integer> pressedKeys = new HashSet<>(); // keys here are treated as being just pressed down.
	private final HashSet<Integer> curKeys = new HashSet<>(); // keys here are treated as being just pressed down.
	private float prevUpdate;
	
	InputHandler() {}
	
	void update(boolean front) {
		if(!front) {
			pressedKeys.clear();
			curKeys.clear();
			return;
		}
		
		final float elapTime = GameCore.getElapsedProgramTime();
		
		for(Integer keycode: keyPresses.keySet()) {
			final float curTime = elapTime - keyPresses.get(keycode);
			final float prevUpdate = this.prevUpdate - keyPresses.get(keycode);
			if(prevUpdate < INITIAL_DELAY && curTime >= INITIAL_DELAY)
				pressedKeys.add(keycode);
			else if(prevUpdate >= INITIAL_DELAY) {
				float prevTime = (prevUpdate - INITIAL_DELAY) % REPEAT_DELAY;
				float delta = curTime - prevUpdate;
				if(prevTime + delta >= REPEAT_DELAY)
					pressedKeys.add(keycode);
			}
		}
		
		prevUpdate = elapTime;
	}
	
	private boolean enabled = true;
	
	public boolean isEnabled() { return enabled; }
	
	public void reset(boolean enable) {
		this.enabled = enable;
		keyPresses.clear();
	}
	
	/**
	 * Used to check if a given key is currently being pressed. This method differs from ClientCore.input.isKeyJustPressed in that it will repeat the press event at regular intervals, after an initial larger delay.
	 * 
	 * @param keycode the keycode of the key to check
	 * @return Whether the given key has just been pressed, either physically, or programmatically for repetition.
	 */
	public boolean pressingKey(int keycode) {
		if(!enabled) return false;
		return ClientCore.input.isKeyJustPressed(keycode) || pressedKeys.contains(keycode);
	}
	
	public boolean isKeyDown(int keycode) { return keyPresses.containsKey(keycode); }
	
	public boolean isKeyJustPressed(int keycode) { return curKeys.contains(keycode); }
	
	@Override
	public boolean keyDown(int keycode) {
		// keycode = KeyEvent.VK_valueOf(KeyEvent.getKeyText(keycode));
		// System.out.println("key down "+ keycode);
		if(keyPresses.containsKey(keycode)) return false;
		keyPresses.put(keycode, GameCore.getElapsedProgramTime());
		curKeys.add(keycode);
		if(isKeyDown(KeyEvent.VK_SHIFT))
			if(isKeyJustPressed(KeyEvent.VK_D) && isKeyDown(KeyEvent.VK_TAB) ||
				isKeyJustPressed(KeyEvent.VK_TAB) && isKeyDown(KeyEvent.VK_D))
				GameCore.debug = !GameCore.debug; // toggle debug mode
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		// keycode = KeyEvent.VK_valueOf(KeyEvent.getKeyText(keycode));
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
