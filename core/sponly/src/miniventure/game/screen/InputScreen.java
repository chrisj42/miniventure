package miniventure.game.screen;

import miniventure.game.core.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;
import miniventure.game.util.function.Action;
import miniventure.game.util.function.ValueAction;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;

/*
	TODO in order to make this class more useful, it should allow for a variable number of inputs, and buttons
		- for each input, have a prefix and initial text
		- for each button, have text and onclick action
		- also allow for a title/header
		- menu should also be contained in a ui frame, i.e. have some solid background
 */
public class InputScreen extends BackgroundInheritor {
	
	public InputScreen(String prompt, ValueAction<String> onConfirm) { this(prompt, onConfirm, ClientCore::backToParentScreen); }
	public InputScreen(String[] prompt, ValueAction<String> onConfirm) { this(prompt, onConfirm, ClientCore::backToParentScreen); }
	public InputScreen(String prompt, ValueAction<String> onConfirm, Action onCancel) {
		this(new String[] {prompt}, onConfirm, onCancel);
	}
	public InputScreen(String[] prompt, ValueAction<String> onConfirm, Action onCancel) {
		super(new ScreenViewport());
		
		Table table = useTable();
		table.defaults().pad(10);
		
		for(String line: prompt) {
			table.add(makeLabel(line)).colspan(2).row();
		}
		
		TextField field = new TextField("", VisUI.getSkin());
		registerField(field);
		table.add(field).colspan(2).fillX();
		
		table.row().pad(10);
		VisTextButton confirmBtn = makeButton("Confirm", () -> onConfirm.act(field.getText()));
		table.add(confirmBtn);
		VisTextButton cancelBtn = makeButton("Cancel", onCancel);
		table.add(cancelBtn);
		
		mapFieldButtons(field, confirmBtn, cancelBtn);
		setKeyboardFocus(field);
	}
	
	public static class CircularFunction<T> implements ValueAction<T> {
		
		@FunctionalInterface
		public interface CircularAction<T> {
			void act(T obj, CircularFunction<T> self);
		}
		
		private final CircularAction<T> action;
		
		public CircularFunction(CircularAction<T> action) {
			this.action = action;
		}
		
		@Override
		public void act(T obj) {
			action.act(obj, this);
		}
		
		
	}
}
