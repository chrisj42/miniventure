package miniventure.game.screen;

import miniventure.game.core.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;
import miniventure.game.util.function.Action;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class ConfirmScreen extends BackgroundInheritor {
	
	public ConfirmScreen(String prompt, Action onConfirm) { this(prompt, onConfirm, ClientCore::backToParentScreen); }
	public ConfirmScreen(String prompt, Action onConfirm, Action onCancel) {
		super(new ScreenViewport());
		
		Table table = useTable();
		
		VisLabel promptLabel = makeLabel(prompt);
		promptLabel.setAlignment(Align.center);
		table.add(promptLabel).colspan(2).pad(25);
		
		table.row().pad(10);
		VisTextButton confirmBtn = makeButton("Yes", onConfirm);
		table.add(confirmBtn);
		VisTextButton cancelBtn = makeButton("No", onCancel);
		table.add(cancelBtn);
		
		mapButtons(table, confirmBtn, cancelBtn);
		
		setKeyboardFocus(table);
	}
	
	
}
