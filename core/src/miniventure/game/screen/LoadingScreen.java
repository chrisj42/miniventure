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
		/*table.add(messageLabel);
		table.row();
		table.add(labelPercent);*/
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
			table.row();
			table.add(label);
			table.row();
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
			int row = table.getCell(removed).getRow();
			table.removeActor(removed); // FIXME this won't work right, as this will not remove the extra spacing rows. And even if it did, I would have to be careful because I give out the row index as an identifier.
			if(row+2 == table.getCells().size)
				table.getCells().removeRange(row-1, row+1);
		});
	}
	
}
