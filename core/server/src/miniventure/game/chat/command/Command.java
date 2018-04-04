package miniventure.game.chat.command;

import miniventure.game.GameProtocol.Message;
import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.chat.MessageBuilder;
import miniventure.game.chat.command.Argument.ArgumentValidator;
import miniventure.game.server.ServerCore;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.entity.mob.ServerPlayer;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public enum Command {
	
	HELP(false, "List and show usage of various commands.",
		new CommandUsageForm("","list all available commands.", executor -> true, Argument.noArgs(), (executor, args, out, err) -> {
			for(Command c : Command.valuesFor(executor))
				c.printHelp(executor, out, true, false, false);
		}),
		
		new CommandUsageForm("<command>", "show detailed help for the given command.", executor -> true, Argument.getSingleArgs(ArgumentValidator.COMMAND), (executor, args, out, err) -> {
			Command c = ArgumentValidator.COMMAND.get(args[0]);
			c.printHelpAdvanced(executor, out);
		}),
		
		new CommandUsageForm("--all", "list all commands in detail", executor -> true, Argument.getSingleArgs(ArgumentValidator.exactString("--all", false)), (executor, args, out, err) -> {
			for(Command c: Command.values()) {
				c.printHelpAdvanced(executor, out);
				out.println();
			}
		})
	),
	
	MSG(false, "Broadcast a message for all players to see.",
		new CommandUsageForm("<message...>", "Everyone on the server will see the message.", executor -> true, new Argument[] {Argument.varArg(arg -> arg)}, (executor, args, out, err) -> {
			String msg = String.join(" ", args);
			ServerCore.getServer().broadcast(new Message(msg));
		})
	),
	
	TP(true, "Teleport a player to a location in the world.",
		new CommandUsageForm("<playername> <playername>", "Teleport the first player to the second player.", executor -> true, Argument.getSingleArgs(ArgumentValidator.PLAYER, ArgumentValidator.PLAYER), ((executor, args, out, err) -> {
			ServerPlayer toMove = ArgumentValidator.PLAYER.get(args[0]);
			ServerPlayer dest = ArgumentValidator.PLAYER.get(args[1]);
			
			ServerLevel level = dest.getLevel();
			if(level != null) {
				toMove.moveTo(level, dest.getPosition());
				ServerCore.getServer().sendToPlayer(toMove, new PositionUpdate(toMove));
				out.println("teleported "+toMove.getName()+" to "+toMove.getPosition(true)+".");
			} else
				err.println("player "+dest.getName()+" is not on a valid level; cannot tp to their location.");
		})),
		
		new CommandUsageForm("<playername>", "Teleports user to the specified player.", executor -> executor != null, Argument.getSingleArgs(ArgumentValidator.PLAYER), ((executor, args, out, err) -> {
			String source = executor.getName();
			Command.valueOf("TP").execute(new String[] {source, args[0]}, executor, out, err);
		})),
		
		new CommandUsageForm("<playername> <x> <y>", "Teleports the specified player to the specified location.", executor -> true, Argument.getSingleArgs(ArgumentValidator.PLAYER, ArgumentValidator.DECIMAL, ArgumentValidator.DECIMAL), ((executor, args, out, err) -> {
			ServerPlayer player = ArgumentValidator.PLAYER.get(args[0]);
			float x = ArgumentValidator.DECIMAL.get(args[1]);
			float y = ArgumentValidator.DECIMAL.get(args[2]);
			ServerLevel level = player.getLevel();
			if(level != null) {
				x += level.getWidth()/2;
				y += level.getHeight()/2;
				player.moveTo(level, x, y);
				ServerCore.getServer().sendToPlayer(player, new PositionUpdate(player));
				out.println("teleported "+player.getName()+" to "+player.getPosition(true)+".");
			} else
				err.println("player "+player.getName()+" is not on a valid level; cannot be teleported.");
		})),
		
		new CommandUsageForm("<x> <y>", "Teleports user to the specified location.", executor -> executor != null, Argument.getSingleArgs(ArgumentValidator.DECIMAL, ArgumentValidator.DECIMAL), ((executor, args, out, err) -> {
			String source = executor.getName();
			Command.valueOf("TP").execute(new String[] {source, args[0], args[1]}, executor, out, err);
		}))
	),
	
	//OP("Give another player access to all commands.")
	
	STOP(true, "Stops the server.",
		new CommandUsageForm("", "Stops the server.", executor -> true, Argument.noArgs(), (executor, args, out, err) -> {
			ServerCore.quit();
		})
	);
	
	static Command getCommand(String commandName, ServerPlayer executor) {
		Command c = null;
		try {
			c = Enum.valueOf(Command.class, commandName.toUpperCase());
		} catch(IllegalArgumentException ignored) {}
		
		if(c != null && c.restricted && !ServerCore.getServer().isAdmin(executor))
			c = null; // not allowed to access.
		
		return c;
	}
	
	private final String name;
	private final String description;
	private final boolean restricted;
	private final CommandUsageForm[] forms;
	
	Command(boolean restricted, String description, CommandUsageForm... forms) {
		this.restricted = restricted;
		this.forms = forms;
		this.name = name();
		this.description = description;
	}
	
	public void execute(String[] args, @Nullable ServerPlayer executor, MessageBuilder out, MessageBuilder err) {
		for(CommandUsageForm form: forms) {
			if(form.execute(executor, args, out, err))
				return;
		}
		
		// no form matched, print help to error stream
		printHelpBasic(executor, err);
	}
	
	private void printHelp(ServerPlayer executor, MessageBuilder out, boolean printDescription, boolean printUsage, boolean printDetails) {
		if(printDescription) out.println(name + " - " + description);
		
		if(printUsage) {
			out.println("Usages:");
			for(CommandUsageForm form : forms) {
				if(!form.executorCheck.get(executor)) continue;
				out.println(name + " " + form.usage);
				if(printDetails) out.println("\t" + form.details);
			}
		}
	}
	
	public void printHelpAdvanced(ServerPlayer executor, MessageBuilder out) { printHelp(executor, out, true, true, true); }
	public void printHelpBasic(ServerPlayer executor, MessageBuilder out) {
		out.print(name+" ");
		printHelp(executor, out, false, true, false);
		out.println("type \"help " + name + "\" for more info.");
	}
	
	private static Command[] valuesFor(@Nullable ServerPlayer player) {
		Array<Command> commands = new Array<>(Command.class);
		boolean op = ServerCore.getServer().isAdmin(player);
		for(Command c: Command.values())
			if(op || !c.restricted)
				commands.add(c);
		
		return commands.shrink();
	}
}
