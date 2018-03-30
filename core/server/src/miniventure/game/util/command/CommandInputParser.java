package miniventure.game.util.command;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CommandInputParser extends Thread {
	
	private Scanner in;
	private PrintStream out;
	private PrintStream err;
	
	public CommandInputParser() { this(System.in, System.out, System.err); }
	public CommandInputParser(InputStream in, PrintStream out, PrintStream err) {
		super("CommandInputParser");
		this.in = new Scanner(in).useDelimiter(System.lineSeparator());
		this.out = out;
		this.err = err;
	}
	
	@Override
	public void run() {
		boolean shouldRun = true;
		
		while(shouldRun) {
			// TODO when adapting this for use on the client, the out prints will be removed.
			out.print("Enter a command: ");
			String input = in.next();
			
			executeCommand(input);
			out.println();
		}
	}
	
	private void executeCommand(String input) {
		if(input.length() == 0) return;
		List<String> parsed = new ArrayList<>();
		parsed.addAll(Arrays.asList(input.split(" ")));
		int lastIdx = -1;
		for(int i = 0; i < parsed.size(); i++) {
			if(parsed.get(i).contains("\"")) {
				if(lastIdx >= 0) { // closing a quoted String
					while(i > lastIdx) { // join the words together
						parsed.set(lastIdx, parsed.get(lastIdx) + " " + parsed.remove(lastIdx+1));
						i--;
					}
					lastIdx = -1; // reset the "last quote" variable.
				} else // start the quoted String
					lastIdx = i; // set the "last quote" variable.
				
				parsed.set(i, parsed.get(i).replaceFirst("\"", "")); // remove the parsed quote character from the string.
				i--; // so that this string can be parsed again, in case there is another quote.
			}
		}
		
		String commandName = parsed.remove(0);
		Command command = Command.getCommand(commandName);
		if(command == null)
			err.println("Command not recognized: \""+commandName+"\".");
		else
			command.execute(parsed.toArray(new String[parsed.size()]), out, err);
	}
	
}
