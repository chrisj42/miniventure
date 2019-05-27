package miniventure.game.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import miniventure.game.GameCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;

public class InputHandler implements InputProcessor {
	
	private static final float INITIAL_DELAY = 0.5f; // delay between initial press and first simulated press.
	private static final float REPEAT_DELAY = 0.2f; // delay between each simulated press.
	
	private float repressDelay = REPEAT_DELAY;
	
	private final HashMap<Integer, Float> keyPresses = new HashMap<>(); // keys in here are currently being held down. The value stored is the initial time of press.
	private final HashSet<Integer> pressedKeys = new HashSet<>(); // keys here are treated as being just pressed down.
	// mouse
	private final HashMap<Integer, Float> buttonPresses = new HashMap<>(); // keys in here are currently being held down. The value stored is the initial time of press.
	private final HashSet<Integer> pressedButtons = new HashSet<>(); // keys here are treated as being just pressed down.
	private float prevUpdate;
	
	InputHandler() {}
	
	void update(/*boolean late*/) {
		// if(late)
			
		// else {
			final float elapTime = GameCore.getElapsedProgramTime();
			
			updateSet(elapTime, keyPresses, pressedKeys);
			updateSet(elapTime, buttonPresses, pressedButtons);
			
			prevUpdate = elapTime;
		// }
	}
	
	private void updateSet(final float elapTime, HashMap<Integer, Float> keyPresses, HashSet<Integer> pressedKeys) {
		pressedKeys.clear();
		
		for(Entry<Integer, Float> pressTime : keyPresses.entrySet()) {
			final float curTime = elapTime - pressTime.getValue();
			final float prevUpdate = this.prevUpdate - pressTime.getValue();
			if(prevUpdate < INITIAL_DELAY && curTime >= INITIAL_DELAY)
				pressedKeys.add(pressTime.getKey());
			else if(prevUpdate >= INITIAL_DELAY) {
				float prevTime = (prevUpdate - INITIAL_DELAY) % repressDelay;
				float delta = curTime - prevUpdate;
				if(prevTime + delta >= repressDelay)
					pressedKeys.add(pressTime.getKey());
			}
		}
	}
	
	void reset() {
		keyPresses.clear();
		buttonPresses.clear();
	}
	
	InputHandler resetDelay() { return repressDelay(REPEAT_DELAY); }
	InputHandler repressDelay(float delay) {
		this.repressDelay = delay;
		reset();
		return this;
	}
	
	/**
	 * Used to check if a given key is currently being pressed. This method differs from Gdx.input.isKeyJustPressed in that it will repeat the press event at regular intervals, after an initial larger delay.
	 * 
	 * @param keycode the keycode of the key to check
	 * @return Whether the given key has just been pressed, either physically, or programmatically for repetition.
	 */
	public boolean pressingKey(int keycode) {
		return Gdx.input.isKeyJustPressed(keycode) || repressingKey(keycode);
	}
	public boolean pressingButton(int button) {
		return pressedButtons.contains(button);
	}
	
	/**
	 * Used to detect when a key is being programmatically re-pressed. Good if you want to avoid collisions with libGDX input listeners.
	 * 
	 * @param keycode the keycode of the key to check
	 * @return Whether the given key is currently being re-pressed programmatically for repetition.
	 */
	public boolean repressingKey(int keycode) { return pressedKeys.contains(keycode); }
	
	@Override
	public boolean keyDown(int keycode) {
		keyPresses.put(keycode, GameCore.getElapsedProgramTime());
		if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))
			if(Gdx.input.isKeyJustPressed(Keys.D) && Gdx.input.isKeyPressed(Keys.TAB) ||
				Gdx.input.isKeyJustPressed(Keys.TAB) && Gdx.input.isKeyPressed(Keys.D))
				GameCore.debug = !GameCore.debug; // toggle debug mode
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		keyPresses.remove(keycode);
		return false;
	}
	
	@Override public boolean keyTyped(char character) { return false; }
	
	@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// if(GameCore.debug) System.out.println("button press: "+button);
		buttonPresses.put(button, GameCore.getElapsedProgramTime());
		pressedButtons.add(button);
		return false;
	}
	@Override public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// if(GameCore.debug) System.out.println("button release: "+button);
		buttonPresses.remove(button);
		return false;
	}
	@Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
	@Override public boolean mouseMoved(int screenX, int screenY) { return false; }
	@Override public boolean scrolled(int amount) { return false; }
	
	public static boolean anyKeyJustPressed(int... keycodes) { return anyKeyPressed(true, keycodes); }
	public static boolean anyKeyPressed(int... keycodes) { return anyKeyPressed(false, keycodes); }
	public static boolean anyKeyPressed(boolean just, int... keycodes) {
		for(int code: keycodes)
			if(just ? Gdx.input.isKeyJustPressed(code) : Gdx.input.isKeyPressed(code))
				return true;
		return false;
	}
}
