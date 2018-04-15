package miniventure.game.chat.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import miniventure.game.chat.MessageBuilder;
import miniventure.game.server.ServerCore;
import miniventure.game.world.entity.mob.ServerPlayer;

import org.jetbrains.annotations.Nullable;

public class CommandInputParser implements Runnable {
	
	private BufferedReader in;
	private boolean shouldRun;
	
	private ConsoleMessageBuilder out, err;
	
	public CommandInputParser() {
		this.in = new BufferedReader(new InputStreamReader(System.in));
		
		out = new ConsoleMessageBuilder(new PrintWriter(new OutputStreamWriter(System.out), true));
		err = new ConsoleMessageBuilder(new PrintWriter(new OutputStreamWriter(System.err), true));
	}
	
	@Override
	public void run() {
		shouldRun = true;
		
		while(shouldRun) {
			out.print("Enter a command: ");
			out.flush();
			String input = "";
			try {
				while(shouldRun && !in.ready()) {
					try {
						Thread.sleep(5);
					} catch(InterruptedException ignored) {
					}
				}
				if(!shouldRun) break;
				input = in.readLine();
			} catch(IOException e) {
				e.printStackTrace();
			}
			if(!shouldRun) break;
			
			executeCommand(input, null, out, err);
			out.println();
		}
		
		ServerCore.quit();
	}
	
	// a null player indicates that it is from the server console.
	public void executeCommand(String input, @Nullable ServerPlayer executor, MessageBuilder out, MessageBuilder err) {
		if(input.length() == 0) return;
		
		List<String> args = new ArrayList<>();
		
		StringBuilder arg = new StringBuilder();
		boolean escaped = false;
		boolean quoted = false;
		for(char c: input.toCharArray()) {
			if(escaped) {
				arg.append(c);
				escaped = false;
				continue;
			}
			if(c == '\\') {
				escaped = true;
				continue;
			}
			
			if(c == '\"') {
				quoted = !quoted;
				continue;
			}
			
			if(c == ' ' && !quoted) {
				args.add(arg.toString());
				arg = new StringBuilder();
				continue;
			}
			
			arg.append(c);
		}
		
		if(args.size() == 0 || arg.length() > 0)
			args.add(arg.toString());
		
		/*List<String> parsed = new ArrayList<>();
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
		}*/
		
		//System.out.println("parsed command: "+args);
		
		String commandName = args.remove(0);
		Command command = Command.getCommand(commandName, executor);
		if(command == null && commandName.startsWith("/")) {
			commandName = commandName.substring(1);
			command = Command.getCommand(commandName, executor);
		}
		if(command == null)
			err.println("Command not recognized: \""+commandName+"\". Type \"help\" for a list of commands.");
		else
			command.execute(args.toArray(new String[args.size()]), executor, out, err);
	}
	
	public void end() { shouldRun = false; }
}
