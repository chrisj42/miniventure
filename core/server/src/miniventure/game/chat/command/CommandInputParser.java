package miniventure.game.chat.command;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import miniventure.game.chat.MessageBuilder;
import miniventure.game.server.ServerCore;
import miniventure.game.world.entity.mob.ServerPlayer;

import org.jetbrains.annotations.Nullable;

public class CommandInputParser extends Thread {
	
	private Scanner in;
	boolean shouldRun;
	
	private ConsoleMessageBuilder out, err;
	
	public CommandInputParser() {
		super("CommandInputParser");
		this.in = new Scanner(System.in).useDelimiter(System.lineSeparator());
		
		out = new ConsoleMessageBuilder(new PrintWriter(new OutputStreamWriter(System.out), true));
		err = new ConsoleMessageBuilder(new PrintWriter(new OutputStreamWriter(System.err), true));
	}
	
	@Override
	public void run() {
		shouldRun = true;
		while(shouldRun) {
			out.print("Enter a command: ");
			out.flush();
			String input = in.next();
			
			executeCommand(input, null, out, err);
			out.println();
		}
		
		ServerCore.quit();
	}
	
	// a null player indicates that it is from the server console.
	public void executeCommand(String input, @Nullable ServerPlayer executor, MessageBuilder out, MessageBuilder err) {
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
		Command command = Command.getCommand(commandName, executor);
		if(command == null)
			err.println("Command not recognized: \""+commandName+"\". Type \"help\" for a list of commands.");
		else
			command.execute(parsed.toArray(new String[parsed.size()]), executor, out, err);
	}
	
}
