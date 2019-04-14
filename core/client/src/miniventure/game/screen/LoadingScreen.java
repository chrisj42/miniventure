package miniventure.game.screen;

import java.util.Stack;

import miniventure.game.screen.util.BackgroundInheritor;
import miniventure.game.util.ProgressLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;

public class LoadingScreen extends BackgroundInheritor implements ProgressLogger {
	
	/*
		I want to have a system where it displays a message, and the message shows a #/total progress format.
		Also, you should be able to have multiple of these, so I can say:
		
		Loading level 1/5...
		
		Loading entity 4/217...
		
		Like that.  
	 */
	
	private final VerticalGroup vGroup;
	private Stack<VisLabel> messageLabels = new Stack<>();
	
	public LoadingScreen() {
		super(new ScreenViewport());
		vGroup = new VerticalGroup();
		setCenterGroup(vGroup);
		vGroup.space(15);
	}
	
	@Override
	public void pushMessage(String message) { pushMessage(message, false); }
	public void pushMessage(String message, boolean simple) {
		message += "...";
		final VisLabel label = simple ? new VisLabel(message) : makeLabel(message);
		messageLabels.push(label);
		Gdx.app.postRunnable(() -> vGroup.addActor(label));
	}
	
	@Override
	public void editMessage(String message) {
		VisLabel label = messageLabels.empty() ? null : messageLabels.peek();
		if(label == null)
			pushMessage(message);
		else
			Gdx.app.postRunnable(() -> label.setText(message+"..."));
	}
	
	@Override
	public void popMessage() {
		VisLabel removed = messageLabels.pop();
		Gdx.app.postRunnable(() -> vGroup.removeActor(removed));
	}
	
}
