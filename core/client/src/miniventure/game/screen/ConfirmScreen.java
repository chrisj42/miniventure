package miniventure.game.screen;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;
import miniventure.game.util.function.Action;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class ConfirmScreen extends BackgroundInheritor {
	
	public ConfirmScreen(String prompt, Action onConfirm) { this(prompt, onConfirm, ClientCore::backToParentScreen); }
	public ConfirmScreen(String prompt, Action onConfirm, Action onCancel) {
		super(new ScreenViewport());
		
		Table table = useTable();
		
		table.add(makeLabel(prompt)).colspan(2).pad(25);
		
		table.row().pad(10);
		VisTextButton confirmBtn = makeButton("Yes", onConfirm);
		table.add(confirmBtn);
		VisTextButton cancelBtn = makeButton("No", onCancel);
		table.add(cancelBtn);
		
		table.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Keys.ENTER) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchDown);
					confirmBtn.fire(event1);
					setKeyboardFocus(table);
					return true;
				}
				if(keycode == Keys.ESCAPE) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchDown);
					cancelBtn.fire(event1);
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
		
		setKeyboardFocus(table);
	}
	
}