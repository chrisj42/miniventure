package miniventure.game.screen;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;
import miniventure.game.util.function.Action;
import miniventure.game.util.function.ValueFunction;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class InputScreen extends BackgroundInheritor {
	
	public InputScreen(String prompt, ValueFunction<String> onConfirm) { this(prompt, onConfirm, ClientCore::backToParentScreen); }
	public InputScreen(String prompt, ValueFunction<String> onConfirm, Action onCancel) {
		super(new ScreenViewport());
		
		Table table = useTable();
		table.defaults().pad(10);
		
		table.add(makeLabel(prompt)).colspan(2).row();
		
		TextField field = new TextField("", VisUI.getSkin());
		registerField(field);
		table.add(field).colspan(2).fillX();
		
		table.row().pad(10);
		VisTextButton confirmBtn = makeButton("Confirm", () -> onConfirm.act(field.getText()));
		table.add(confirmBtn);
		VisTextButton cancelBtn = makeButton("Cancel", onCancel);
		table.add(cancelBtn);
		
		field.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Keys.ENTER) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchDown);
					confirmBtn.fire(event1);
					setKeyboardFocus(field);
					return true;
				}
				if(keycode == Keys.ESCAPE) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchDown);
					cancelBtn.fire(event1);
					setKeyboardFocus(field);
					return true;
				}
				return false;
			}
			
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if(keycode == Keys.ENTER) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchUp);
					confirmBtn.fire(event1);
					return true;
				}
				if(keycode == Keys.ESCAPE) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchUp);
					cancelBtn.fire(event1);
					return true;
				}
				return false;
			}
		});
		
		setKeyboardFocus(field);
	}
	
}
