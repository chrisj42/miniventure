package miniventure.game.chat.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import miniventure.game.chat.ConsoleMessageBuilder;
import miniventure.game.chat.MessageBuilder;
import miniventure.game.util.MyUtils;
import miniventure.game.world.entity.mob.player.ServerPlayer;

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
					MyUtils.sleep(5);
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
	}
	
	// a null player indicates that it is from the server console.
	public static void executeCommand(String input, @Nullable ServerPlayer executor, MessageBuilder out, MessageBuilder err) {
		if(input.length() == 0) return;
		
		List<String> args = new ArrayList<>(input.split(" ").length);
		
		StringBuilder arg = new StringBuilder(input.length());
		boolean escaped = false;
		boolean quoted = false;
		char[] chars = input.toCharArray();
		for(int i = 0; i < chars.length; i++) {
			char c = chars[i];
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
				arg = new StringBuilder(input.length()-i);
				continue;
			}
			
			arg.append(c);
		}
		
		if(args.size() == 0 || arg.length() > 0)
			args.add(arg.toString());
		
		
		String commandName = args.remove(0);
		Command command = Command.getCommand(commandName, executor);
		if(command == null && commandName.startsWith("/")) {
			commandName = commandName.substring(1);
			command = Command.getCommand(commandName, executor);
		}
		if(command == null)
			err.println("Command not recognized: \""+commandName+"\". Type \"/help\" for a list of commands.");
		else
			command.execute(args.toArray(new String[0]), executor, out, err);
	}
	
	public void end() { shouldRun = false; }
}
