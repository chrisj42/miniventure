package miniventure.game.screen;

import miniventure.game.client.ClientCore;
import miniventure.game.util.function.Action;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class NotifyScreen extends MenuScreen {
	
	public NotifyScreen(boolean clearGdxBackground, Action onConfirm, String buttonText, String... message) {
		super(clearGdxBackground, new ScreenViewport(), ClientCore.getBatch());
		
		Table table = useTable();
		table.defaults().pad(5);
		
		for(String s: message)
			table.add(makeLabel(s, true)).row();
		
		VisTextButton button = makeButton(buttonText, onConfirm);
		mapButtons(table, button, null);
		
		table.add(button).row();
		
		setKeyboardFocus(table);
	}
	
}
