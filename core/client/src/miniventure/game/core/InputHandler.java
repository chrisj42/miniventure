package miniventure.game.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;

public class InputHandler implements InputProcessor {
	
	private static final float INITIAL_DELAY = 0.5f; // delay between initial press and first simulated press.
	private static final float REPEAT_DELAY = 0.2f; // delay between each simulated press.
	
	private enum PressType {
		Down, Pressed, JustPressed
	}
	
	public enum Control {
		// gameplay
		
		MOVE_UP(Keys.UP, Keys.W),
		MOVE_DOWN(Keys.DOWN, Keys.S),
		MOVE_LEFT(Keys.LEFT, Keys.A),
		MOVE_RIGHT(Keys.RIGHT, Keys.D),
		
		ATTACK(Mapping.mouse(Buttons.LEFT)),
		INTERACT(Mapping.mouse(Buttons.RIGHT)),
		
		DROP_ITEM(Keys.Q),
		// DROP_STACK(new Mapping(Keys.Q, Modifier.SHIFT)),
		
		INVENTORY_TOGGLE(Keys.E),
		CRAFTING_TOGGLE(Keys.TAB),
		
		// these two I'm not sure I need.
		TOGGLE_FACE_CURSOR(Keys.R), // toggles between facing the cursor, and facing the last direction moved.
		FOLLOW_CURSOR(Mapping.mouse(Buttons.MIDDLE)), // when pressed, the player will follow the cursor.
		// QUICK_ACTION(), // place + deposit, deposit facing, build facing
		
		// ui
		
		// CURSOR_UP(false, Keys.UP),
		// CURSOR_DOWN(false, Keys.DOWN),
		// CURSOR_LEFT(false, Keys.LEFT),
		// CURSOR_RIGHT(false, Keys.RIGHT),
		CHAT(Keys.T),
		CONFIRM(Keys.ENTER),
		CANCEL(Keys.ESCAPE),
		PAUSE(Keys.ESCAPE);
		
		// private final boolean editable; // non-editable ones don't show up in the key binding menu.
		private final List<Mapping> defaultMappings;
		private final List<Mapping> mappings = new LinkedList<>();
		
		Control(Integer... defaultKeys) {
			this(ArrayUtils.mapArray(defaultKeys, Mapping.class, Mapping::key));
		}
		Control(Mapping... defaultMappings) {
			this.defaultMappings = Arrays.asList(defaultMappings);
			resetMappings();
		}
		
		public boolean isPressed(InputHandler input, PressType pressType) {
			for(Mapping mapping: mappings)
				if(mapping.isPressed(input, pressType))
					return true;
			
			return false;
		}
		
		public boolean matches(int code) { return matches(code, false); }
		public boolean matches(int code, boolean mouse) {
			for(Mapping mapping: mappings)
				if(mapping.mouse == mouse && mapping.code == code)
					return true;
			
			return false;
		}
		
		public void resetMappings() {
			mappings.clear();
			mappings.addAll(defaultMappings);
		}
		
		public static void resetAllMappings() {
			for(Control control: Control.values())
				control.resetMappings();
		}
	}
	
	private final HashMap<Integer, Long> keyPresses = new HashMap<>(); // keys in here are currently being held down. The value stored is the initial time of press.
	private final HashSet<Integer> pressedKeys = new HashSet<>(); // keys here are treated as being just pressed down.
	// mouse
	private final HashMap<Integer, Long> buttonPresses = new HashMap<>(); // keys in here are currently being held down. The value stored is the initial time of press.
	private final HashSet<Integer> pressedButtons = new HashSet<>(); // keys here are treated as being just pressed down.
	private final HashSet<Integer> justPressedButtons = new HashSet<>(); // just pressed buttons
	private float repressDelay = REPEAT_DELAY;
	private long prevUpdate;
	
	InputHandler() {}
	
	void onFrameStart() {
		final long curTime = System.nanoTime();
		
		updateSet(curTime, keyPresses, pressedKeys);
		updateSet(curTime, buttonPresses, pressedButtons);
		
		prevUpdate = curTime;
	}
	
	// TODO not entirely sure if I need this to be separate; I mean, doing so should technically mean that nothing will be considered just pressed if executed during a Gdx.app runnable, since those are executed right before the render method. But until I figure out what my reasoning for doing this in the single-player version was, I'll leave it be.
	void onFrameEnd() {
		pressedKeys.clear();
		pressedButtons.clear();
		justPressedButtons.clear();
	}
	
	private void updateSet(final long curTime, HashMap<Integer, Long> keyPresses, HashSet<Integer> pressedKeys) {
		for(Entry<Integer, Long> pressTime : keyPresses.entrySet()) {
			final float elapTime = MyUtils.getDelta(pressTime.getValue(), curTime);
			final float prevElapTime = MyUtils.getDelta(pressTime.getValue(), this.prevUpdate);
			// press when first crossing the initial delay threshold
			if(prevElapTime < INITIAL_DELAY && elapTime >= INITIAL_DELAY)
				pressedKeys.add(pressTime.getKey());
				// press again if past at least first reg delay (only triggers when prev elap time is past initial delay)
			else if(elapTime >= INITIAL_DELAY + repressDelay) {
				float prevTime = (prevElapTime - INITIAL_DELAY) % repressDelay;
				float delta = elapTime - prevElapTime;
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
	
	public boolean pressingControl(Control control) { return pressingControl(control, false); }
	public boolean pressingControl(Control control, boolean just) {
		return control.isPressed(this, just ? PressType.JustPressed : PressType.Pressed);
	}
	public boolean holdingControl(Control control) {
		return control.isPressed(this, PressType.Down);
	}
	
	/**
	 * Used to check if a given key is currently being pressed. This method differs from Gdx.input.isKeyJustPressed in that it will repeat the press event at regular intervals, after an initial larger delay.
	 *
	 * @param keycode the keycode of the key to check
	 * @return Whether the given key has just been pressed, either physically, or programmatically for repetition.
	 */
	public boolean pressingKey(int keycode) {
		return pressedKeys.contains(keycode);//Gdx.input.isKeyJustPressed(keycode) || repressingKey(keycode);
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
	// public boolean repressingKey(int keycode) { return pressedKeys.contains(keycode); }
	
	@Override
	public boolean keyDown(int keycode) {
		keyPresses.put(keycode, System.nanoTime());
		pressedKeys.add(keycode);
		if(Modifier.SHIFT.isPressed())
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
		buttonPresses.put(button, System.nanoTime());
		pressedButtons.add(button);
		justPressedButtons.add(button);
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
	
	public static class Mapping {
		public final int code;
		public final boolean mouse;
		
		private static Mapping mouse(int code) { return new Mapping(code, true); }
		private static Mapping key(int code) { return new Mapping(code, false); }
		
		private Mapping(int code, boolean mouse) {
			this.code = code;
			this.mouse = mouse;
		}
		
		public boolean isPressed(InputHandler input, PressType pressType) {
			if(pressType == PressType.JustPressed) {
				return mouse ? input.justPressedButtons.contains(code) : Gdx.input.isKeyJustPressed(code);
			}
			HashMap<Integer, Long> presses = mouse ? input.buttonPresses : input.keyPresses;
			HashSet<Integer> pressed = mouse ? input.pressedButtons : input.pressedKeys;
			
			if(pressType == PressType.Down)
				return presses.containsKey(code);
			else
				return pressed.contains(code);
			// return mouse ? held ? input.buttonPresses.containsKey(code) : input.pressingButton(code) : held ? input.keyPresses.containsKey(code) : input.pressingKey(code);
		}
	}
	
	public enum Modifier {
		SHIFT(Keys.SHIFT_LEFT, Keys.SHIFT_RIGHT),
		ALT(Keys.ALT_LEFT, Keys.ALT_RIGHT),
		CONTROL(Keys.CONTROL_LEFT, Keys.CONTROL_RIGHT);
		
		private final int leftKey, rightKey;
		
		Modifier(int left, int right) {
			leftKey = left;
			rightKey = right;
		}
		
		public boolean isPressed() {
			return Gdx.input.isKeyPressed(leftKey)
					|| Gdx.input.isKeyPressed(rightKey);
		}
	}
}
