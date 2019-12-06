package miniventure.game;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import miniventure.game.util.ProgressLogger;

public class ProgressPrinter implements ProgressLogger {
	
	private Deque<String> messages = new ArrayDeque<>();
	
	private boolean topEphemeral = false;
	
	@Override
	public void pushMessage(String message, boolean ephemeral) {
		if(topEphemeral)
			popMessage();
		messages.push(message);
		topEphemeral = ephemeral;
		printProgress();
	}
	
	@Override
	public void editMessage(String newMessage, boolean ephemeral) {
		popMessage();
		messages.push(newMessage);
		topEphemeral = ephemeral;
		printProgress();
	}
	
	@Override
	public void popMessage() {
		messages.poll();
	}
	
	public void printProgress() {
		StringBuilder str = new StringBuilder();
		Iterator<String> iter = messages.iterator();
		while(iter.hasNext()) {
			str.append(iter.next());
			if(iter.hasNext())
				str.append(" -- ");
			else
				str.append(System.lineSeparator());
		}
		
		System.out.println(str);
	}
	
	/*private void appendNextMessage(StringBuilder str, Iterator<String> iter) {
		if(iter.hasNext()) {
			String msg = iter.next();
			str.append(" (").append(msg);
			appendNextMessage(str, iter);
			str.append(")");
		}
	}*/
}
