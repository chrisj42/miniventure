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
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.management.ServerWorld;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandInputParser implements Runnable {
	
	static final MapFunction<ServerPlayer, Boolean> SERVER_ONLY = player ->
		player == null || player.getWorld().getServer().isMultiplayer();
	
	private final ServerWorld world;
	
	// this isn't a scanner because it needs to not block for input.
	private BufferedReader in;
	private boolean shouldRun;
	
	private ConsoleMessageBuilder out, err;
	
	private final Object executionLock = new Object();
	private boolean executing = false;
	
	public CommandInputParser(@NotNull ServerWorld world) {
		this.world = world;
		
		out = new ConsoleMessageBuilder(new PrintWriter(new OutputStreamWriter(System.out), true));
		err = new ConsoleMessageBuilder(new PrintWriter(new OutputStreamWriter(System.err), true));
	}
	
	@Override
	public void run() {
		shouldRun = true;
		MyUtils.debug("Starting server command parser");
		
		this.in = new BufferedReader(new InputStreamReader(System.in));
		while(shouldRun) {
			out.print("Enter a command: ");
			out.flush();
			String input;
			try {
				while(shouldRun && !in.ready()) {
					MyUtils.sleep(5);
				}
				if(!shouldRun) break;
				input = in.readLine();
			} catch(IOException e) {
				e.printStackTrace();
				break;
			}
			
			MyUtils.waitUntilFinished(world::postRunnable, () -> executeCommand(world, input, null, out, err));
			
			out.println();
		}
		
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MyUtils.debug("Ending server command parser");
	}
	
	// a null player indicates that it is from the server console.
	public static void executeCommand(@NotNull ServerWorld world, String input, @Nullable ServerPlayer executor, MessageBuilder out, MessageBuilder err) {
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
		Command command = Command.getCommand(commandName);
		/*if(command == null && commandName.startsWith("/")) {
			commandName = commandName.substring(1);
			command = Command.getCommand(commandName);
		}*/
		if(command == null)
			err.println("Command not recognized: \""+commandName+"\". Type \"/help\" for a list of commands.");
		else
			command.execute(world, executor, args.toArray(new String[0]), out, err);
	}
	
	public void end() { shouldRun = false; }
}
