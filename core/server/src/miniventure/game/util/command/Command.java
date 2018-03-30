package miniventure.game.util.command;

import java.io.PrintStream;

import miniventure.game.GameProtocol.Message;
import miniventure.game.server.ServerCore;

public enum Command {
	
	// TODO make methods to parse possibly multiple arguments into an object, given an offset into the arg array; for ex. a list of entities from an arg, or a single entity, or player, or a position update object basically.
	
	HELP("[command]", "List and show usage of various commands.", "<-describe help usage syntax here->", (args, out, err) -> {
		if(args.length == 0) {
			for(Command c : Command.values())
				c.printDescription(out);
		}
		else {
			Command c = getCommand(args[0]);
			if(c == null)
				err.println("Command \""+args[0]+"\" is not recognized.");
			else {
				c.printDescription(out);
				c.printUsage(out);
				c.printDetails(out);
			}
		}
	}),
	
	MSG("<message>", "Broadcast a message for all players to see.", "Everyone on the server will see the message.", (args, out, err) -> {
		if(args.length == 0)
			err.println("a message is required.");
		else {
			String msg = String.join(" ", args);
			ServerCore.getServer().broadcast(new Message(msg));
		}
	}),
	
	/*TP("<playername> <playername> OR <playername> <x> <y> [level]", "Teleport a player to a location.", "In the first form, teleports the first player to the second player. In the second form, teleports the player to the given location. If level is not specified, it defaults to the player's current level.", (args, out, err) -> {
		
	}),*/
	
	NAME("", "", "", (args, out, err) -> {
		
	});
	
	static Command getCommand(String commandName) {
		Command c = null;
		try {
			c = Enum.valueOf(Command.class, commandName.toUpperCase());
		} catch(IllegalArgumentException ignored) {}
		return c;
	}
	
	@FunctionalInterface
	public interface ExecuteAction {
		void execute(String[] args, PrintStream out, PrintStream err); 
	}
	
	private final String name;
	private final String usage;
	private final String description;
	private final String details;
	private final ExecuteAction executeAction;
	
	Command(String usage, String description, String details, ExecuteAction executeAction) {
		this.usage = usage;
		this.details = details;
		this.name = name();
		this.description = description;
		this.executeAction = executeAction;
	}
	
	public void execute(String[] args, PrintStream out, PrintStream err) { executeAction.execute(args, out, err); }
	
	private void printDescription(PrintStream out) {
		out.println(name + " - " + description);
	}
	
	private void printUsage(PrintStream out) {
		out.println("Usage: "+name+" " + usage);
	}
	
	private void printDetails(PrintStream out) {
		out.println(details);
	}
	
	public void printHelp() {
		System.out.println("Usage: "+name+" " + usage);
		//System.out.println("\t"+description);
		System.out.println("type \"help " + name + "\" for more info.");
	}
}
