package miniventure.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;

public class LoadingScreen extends MenuScreen {
	
	/*
		I want to have a system where it displays a message, and the message shows a #/total progress format.
		Also, you should be able to have multiple of these, so I can say:
		
		Loading level 1/5...
		
		Loading entity 4/217...
		
		Like that.  
	 */
	
	private Array<VisLabel> messageLabels = new Array<>();
	
	public LoadingScreen() {
		//messageLabelnew VisLabel(message);
		/*vGroup.add(messageLabel);
		vGroup.row();
		vGroup.add(labelPercent);*/
	}
	
	/*@Override
	public void draw() {
		for(int i = 0; i < messages.size; i++) {
			messageLabels.get(i).setText(messages.get(i));
		}
		
		super.draw();
	}*/
	
	public int addIncrement(String message) {
		final VisLabel label = new VisLabel(message);
		messageLabels.add(label);
		Gdx.app.postRunnable(() -> {
			vGroup.addActor(label);
		});
		return messageLabels.size-1;
	}
	
	/*public void inc(double amt, String msg) {
		percent += amt;
		labelPercent.setText(percent+"%");
		messageLabel.setText(msg+"...");
	}*/
	
	public void setMessage(final int idx, final String message) {
		Gdx.app.postRunnable(() -> messageLabels.get(idx).setText(message));
	}
	
	public void removeMessage(int idx) {
		VisLabel removed = messageLabels.removeIndex(idx);
		Gdx.app.postRunnable(() -> {
			vGroup.removeActor(removed); // FIXME this won't work right, because I give out the row index as an identifier.
		});
	}
	
}
