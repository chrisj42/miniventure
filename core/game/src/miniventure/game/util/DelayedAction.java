package miniventure.game.util;

import javax.swing.Timer;

public class DelayedAction {
	
	private Timer timer;
	
	public DelayedAction(int milliDelay, Action action) {
		timer = new Timer(milliDelay, e -> action.act());
		timer.setRepeats(false);
	}
	
	public void start() { timer.restart(); }
	
}
