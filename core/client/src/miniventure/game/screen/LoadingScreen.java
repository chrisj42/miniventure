package miniventure.game.screen;

import javax.swing.JLabel;

import java.util.Stack;

import miniventure.game.util.ProgressLogger;

public class LoadingScreen extends MenuScreen implements ProgressLogger {
	
	/*
		I want to have a system where it displays a message, and the message shows a #/total progress format.
		Also, you should be able to have multiple of these, so I can say:
		
		Loading level 1/5...
		
		Loading entity 4/217...
		
		Like that.  
	 */
	
	private Stack<JLabel> messageLabels = new Stack<>();
	
	public LoadingScreen() {
		super(true, false);
	}
	
	@Override
	public void pushMessage(String message) {
		final JLabel label = new JLabel(message);
		messageLabels.push(label);
		add(label);
	}
	
	@Override
	public void editMessage(final String message) {
		final JLabel label = messageLabels.peek();
		label.setText(message);
	}
	
	@Override
	public void popMessage() {
		JLabel removed = messageLabels.pop();
		remove(removed);
	}
	
}
