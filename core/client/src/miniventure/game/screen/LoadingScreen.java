package miniventure.game.screen;

import javax.swing.JLabel;

import java.util.Stack;

import miniventure.game.util.ProgressLogger;

public class LoadingScreen extends MenuScreen implements ProgressLogger, BackgroundInheritor {
	
	/*
		I want to have a system where it displays a message, and the message shows a #/total progress format.
		Also, you should be able to have multiple of these, so I can say:
		
		Loading level 1/5...
		
		Loading entity 4/217...
		
		Like that.  
	 */
	
	private final Stack<JLabel> messageLabels = new Stack<>();
	
	private BackgroundProvider gdxBackground;
	
	public LoadingScreen() {
		super(true, true);
		// setOpaque(true);
		// setBackground(Color.WHITE);
	}
	
	@Override
	public void pushMessage(String message) {
		final JLabel label = makeLabel(message);
		// label.setBackground(getBackground());
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
	
	@Override
	public void setBackground(final BackgroundProvider gdxBackground) {
		this.gdxBackground = gdxBackground;
	}
	
	@Override
	public BackgroundProvider getGdxBackground() {
		return gdxBackground;
	}
	
	@Override
	public void glDraw() {
		if(gdxBackground != null)
			gdxBackground.glDraw();
		else
			super.glDraw();
	}
}
