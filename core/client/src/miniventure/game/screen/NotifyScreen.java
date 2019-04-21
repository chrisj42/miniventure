package miniventure.game.screen;

import miniventure.game.util.function.Action;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class NotifyScreen extends MenuScreen {
	
	public NotifyScreen(boolean clearGdxBackground, Action onConfirm, String buttonText, String... message) {
		super(clearGdxBackground);
		
		Table table = useTable();
		table.defaults().pad(5);
		
		for(String s: message)
			table.add(makeLabel(s, true)).row();
		
		VisTextButton button = makeButton(buttonText, onConfirm);
		table.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Keys.ENTER) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchDown);
					button.fire(event1);
					setKeyboardFocus(table);
					return true;
				}
				return false;
			}
			
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if(keycode == Keys.ENTER) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchUp);
					button.fire(event1);
					return true;
				}
				return false;
			}
		});
		
		table.add(button).row();
		
		setKeyboardFocus(table);
	}
	
}
