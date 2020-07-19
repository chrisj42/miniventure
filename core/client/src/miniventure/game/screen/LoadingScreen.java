package miniventure.game.screen;

import java.util.EmptyStackException;
import java.util.Stack;

import miniventure.game.util.ProgressLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;

public class LoadingScreen extends MenuScreen implements ProgressLogger {
	
	/*
		I want to have a system where it displays a message, and the message shows a #/total progress format.
		Also, you should be able to have multiple of these, so I can say:
		
		Loading level 1/5...
		
		Loading entity 4/217...
		
		Like that.  
	 */
	
	private final VerticalGroup vGroup;
	private Stack<VisLabel> messageLabels = new Stack<>();
	
	// this is necessary so that a LoadingScreen can be created during initialization before most assets are fully ready; that way the user gets more than a blank screen while everything is loading up.
	private final boolean initLoader;
	
	private boolean topEphemeral = false; // should the top message be overwritten on push?
	
	public LoadingScreen() { this(false); }
	public LoadingScreen(boolean initLoader) {
		super(new ScreenViewport());
		this.initLoader = initLoader;
		vGroup = new VerticalGroup();
		setCenterGroup(vGroup);
		vGroup.space(15);
	}
	
	@Override
	public void pushMessage(String message, boolean ephemeral) {
		if(topEphemeral) {
			topEphemeral = false; // tell editMessage we don't want to pop the top message
			editMessage(message, ephemeral);
			return;
		}
		
		message += "...";
		final VisLabel label = initLoader ? new VisLabel(message) : makeLabel(message);
		messageLabels.push(label);
		topEphemeral = ephemeral;
		Gdx.app.postRunnable(() -> vGroup.addActor(label));
	}
	
	@Override
	public void editMessage(String message, boolean ephemeral) {
		if(topEphemeral) {
			topEphemeral = false; // make popMessage() only pop 1 message
			popMessage();
		}
		
		VisLabel label = messageLabels.empty() ? null : messageLabels.peek();
		if(label == null) {
			// topEphemeral = false; // ensures this method is not called again during pushMessage
			pushMessage(message, ephemeral);
		}
		else {
			Gdx.app.postRunnable(() -> label.setText(message + "..."));
			topEphemeral = ephemeral;
		}
	}
	
	@Override
	public void popMessage() {
		if(topEphemeral) {
			topEphemeral = false;
			popMessage();
		}
		if(!messageLabels.empty()) {
			VisLabel removed = messageLabels.pop();
			Gdx.app.postRunnable(() -> vGroup.removeActor(removed));
		}
	}
	
}
