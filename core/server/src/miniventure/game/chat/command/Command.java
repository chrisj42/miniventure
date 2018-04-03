package miniventure.game.chat.command;

import miniventure.game.GameProtocol.Message;
import miniventure.game.chat.MessageBuilder;
import miniventure.game.chat.command.Argument.ArgumentValidator;
import miniventure.game.server.ServerCore;
import miniventure.game.world.entity.mob.ServerPlayer;

import org.jetbrains.annotations.Nullable;

public enum Command {
	
	HELP("List and show usage of various commands.",
		new CommandUsageForm("","list all available commands.", Argument.noArgs(), (args, out, err) -> {
			for(Command c : Command.values())
				c.printHelp(out, true, false, false);
		}),
		
		new CommandUsageForm("<command>", "show detailed help for the given command.", Argument.singleArg(ArgumentValidator.COMMAND), (args, out, err) -> {
			Command c = ArgumentValidator.COMMAND.get(args[0]);
			c.printHelpAdvanced(out);
		}),
		
		new CommandUsageForm("--all", "list all commands in detail", Argument.singleArg(ArgumentValidator.exactString("--all", false)), (args, out, err) -> {
			for(Command c: Command.values()) {
				c.printHelpAdvanced(out);
				out.println();
			}
		})
	),
	
	MSG("Broadcast a message for all players to see.",
		new CommandUsageForm("<message...>", "Everyone on the server will see the message.", new Argument[] {Argument.varArg(arg -> arg)}, (args, out, err) -> {
			String msg = String.join(" ", args);
			ServerCore.getServer().broadcast(new Message(msg));
		})
	),
	
	/*TP("<playername> <playername> OR <playername> <x> <y> [level]", "Teleport a player to a location.", "In the first form, teleports the first player to the second player. In the second form, teleports the player to the given location. If level is not specified, it defaults to the player's current level.", (args, out, err) -> {
		
	}),*/
	
	STOP("Stops the server.",
		new CommandUsageForm("", "Stops the server.", Argument.noArgs(), (args, out, err) -> {
			ServerCore.quit();
		})
	);
	
	static Command getCommand(String commandName) {
		Command c = null;
		try {
			c = Enum.valueOf(Command.class, commandName.toUpperCase());
		} catch(IllegalArgumentException ignored) {}
		return c;
	}
	
	private final String name;
	private final String description;
	private final CommandUsageForm[] forms;
	
	Command(String description, CommandUsageForm... forms) {
		this.forms = forms;
		this.name = name();
		this.description = description;
	}
	
	public void execute(String[] args, @Nullable ServerPlayer executor, MessageBuilder out, MessageBuilder err) {
		for(CommandUsageForm form: forms) {
			// TODO take executor into account for TP command form; specifying only one player name is only valid if the executor is not null.
			if(form.execute(args, out, err))
				return;
		}
		
		// no form matched, print help to error stream
		printHelpBasic(err);
	}
	
	private void printHelp(MessageBuilder out, boolean printDescription, boolean printUsage, boolean printDetails) {
		if(printDescription) out.println(name + " - " + description);
		
		if(printUsage) {
			out.println("Usages:");
			for(CommandUsageForm form : forms) {
				out.println(name + " " + form.usage);
				if(printDetails) out.println("\t" + form.details);
			}
		}
	}
	
	public void printHelpAdvanced(MessageBuilder out) { printHelp(out, true, true, true); }
	public void printHelpBasic(MessageBuilder out) {
		out.print(name+" ");
		printHelp(out, false, true, false);
		out.println("type \"help " + name + "\" for more info.");
	}
}
