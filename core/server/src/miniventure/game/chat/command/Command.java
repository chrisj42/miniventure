package miniventure.game.chat.command;

import java.util.Arrays;

import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.chat.MessageBuilder;
import miniventure.game.chat.command.Argument.ArgumentValidator;
import miniventure.game.server.ServerCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.TimeOfDay;
import miniventure.game.world.entity.mob.ServerPlayer;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public enum Command {
	
	HELP("List and show usage of various commands.",
		new CommandUsageForm(false, "","list all available commands.", (executor, args, out, err) -> {
			for(Command c : Command.valuesFor(executor))
				c.printHelp(executor, out, true, false, false);
		}),
		
		new CommandUsageForm(false, "<command>", "show detailed help for the given command.", (executor, args, out, err) -> {
			Command c = ArgumentValidator.COMMAND.get(args[0]);
			c.printHelpAdvanced(executor, out);
		}, Argument.get(ArgumentValidator.COMMAND)),
		
		new CommandUsageForm(false, "--all", "list all commands in detail", (executor, args, out, err) -> {
			for(Command c: Command.values()) {
				c.printHelpAdvanced(executor, out);
				out.println();
			}
		}, Argument.get(ArgumentValidator.exactString(false, "--all")))
	),
	
	MSG("Broadcast a message for all players to see.",
		new CommandUsageForm(false, "<message...>", "Everyone on the server will see the message.", (executor, args, out, err) -> {
			String msg = String.join(" ", args);
			ServerCore.getServer().broadcastMessage(executor, msg);
		}, Argument.varArg(ArgumentValidator.ANY))
	),
	
	PMSG("Send a private message to another player.",
		new CommandUsageForm(false, "<playername> <message...>", "Send a message to the specified player, that only they can see.", MyUtils::notNull, ((executor, args, out, err) -> {
			ServerPlayer player = ArgumentValidator.PLAYER.get(args[0]);
			String msg = String.join("", Arrays.copyOfRange(args, 1, args.length));
			
			ServerCore.getServer().sendMessage(executor, player, msg);
			
		}), Argument.get(ArgumentValidator.PLAYER), Argument.varArg(ArgumentValidator.ANY))
	),
	
	TP("Teleport a player to a location in the world.",
		new CommandUsageForm(true, "<playername> <playername>", "Teleport the first player to the second player.", executor -> true, ((executor, args, out, err) -> {
			ServerPlayer toMove = ArgumentValidator.PLAYER.get(args[0]);
			ServerPlayer dest = ArgumentValidator.PLAYER.get(args[1]);
			
			ServerLevel level = dest.getLevel();
			if(level != null) {
				toMove.moveTo(level, dest.getPosition());
				ServerCore.getServer().sendToPlayer(toMove, new PositionUpdate(toMove));
				out.println("teleported "+toMove.getName()+" to "+toMove.getPosition(true)+".");
			} else
				err.println("player "+dest.getName()+" is not on a valid level; cannot tp to their location.");
		}), Argument.get(ArgumentValidator.PLAYER, ArgumentValidator.PLAYER)),
		
		new CommandUsageForm(true, "<playername>", "Teleports user to the specified player.", executor -> executor != null, ((executor, args, out, err) -> {
			String source = executor.getName();
			Command.valueOf("TP").execute(new String[] {source, args[0]}, executor, out, err);
		}), Argument.get(ArgumentValidator.PLAYER)),
		
		new CommandUsageForm(true, "<playername> <x> <y>", "Teleports the specified player to the specified location.", executor -> true, ((executor, args, out, err) -> {
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
		}), Argument.get(ArgumentValidator.PLAYER, ArgumentValidator.DECIMAL, ArgumentValidator.DECIMAL)),
		
		new CommandUsageForm(true, "<x> <y>", "Teleports user to the specified location.", executor -> executor != null, ((executor, args, out, err) -> {
			String source = executor.getName();
			Command.valueOf("TP").execute(new String[] {source, args[0], args[1]}, executor, out, err);
		}), Argument.get(ArgumentValidator.DECIMAL, ArgumentValidator.DECIMAL))
	),
	
	TIME("Get or set the time of day.",
		new CommandUsageForm(false, "", "Print the time of day, in 24 hour clock format, and the time Dawn, Day, Dusk, or Night.", (executor, args, out, err) -> {
			float daylightOffset = ServerCore.getWorld().getDaylightOffset();
			out.println(TimeOfDay.getClockString(daylightOffset)+" - " + TimeOfDay.getTimeString(daylightOffset));
		}),
		
		new CommandUsageForm(
			true, MyUtils.arrayToString(TimeOfDay.names, "<", ">", "|"), "Set time to start of specified range. timeOfDay can be one of: "+MyUtils.arrayToString(TimeOfDay.names, "", "", ", "), (executor, args, out, err) -> {
			TimeOfDay timeOfDay = TimeOfDay.valueOf(MyUtils.toTitleCase(args[0]));
			ServerCore.getWorld().setTimeOfDay(timeOfDay.getStartOffsetSeconds());
			float dayTime = ServerCore.getWorld().getDaylightOffset();
			out.println("Set time of day to "+TimeOfDay.getClockString(dayTime)+" ("+TimeOfDay.getTimeOfDay(dayTime)+")");
		}, Argument.get(ArgumentValidator.exactString(false, TimeOfDay.names))),
		
		new CommandUsageForm(true, "<HH:MM>", "Set time to the specified 24-hour clock format time (HH:MM)", (executor, args, out, err) -> {
			float dayTime = ArgumentValidator.CLOCK_TIME.get(args[0]);
			ServerCore.getWorld().setTimeOfDay(dayTime);
			out.println("Set time of day to "+TimeOfDay.getClockString(dayTime)+" ("+TimeOfDay.getTimeOfDay(dayTime)+")");
			
		}, Argument.get(ArgumentValidator.CLOCK_TIME))
	),
	
	OP("Give another player access to all commands, or take it away.",
		new CommandUsageForm(true, "<playername> <isAdmin>", "Give or take admin permissions for the specified player. (isAdmin = true OR false)", executor -> true, ((executor, args, out, err) -> {
			ServerPlayer player = ArgumentValidator.PLAYER.get(args[0]);
			boolean give = ArgumentValidator.BOOLEAN.get(args[1]);
			boolean success = ServerCore.getServer().setAdmin(player, give);
			if(success)
				out.println("Admin permissions "+(give?"granted":"removed")+" for player "+player.getName()+".");
			else
				err.println("failed to change admin permissions of player "+player.getName()+".");
		}), Argument.get(ArgumentValidator.PLAYER, ArgumentValidator.BOOLEAN))
	),
	
	STATUS("Print the server's status on various things.",
		new CommandUsageForm(true, "", "Print various pieces of info about the server.", (executor, args, out, err) -> ServerCore.getServer().printStatus(out))
	),
	
	STOP("Stops the server.",
		new CommandUsageForm(true, "", "Stops the server.", executor -> true, (executor, args, out, err) -> ServerCore.quit())
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
	
	Command(String description, CommandUsageForm... forms) {
		boolean restricted = true;
		for(CommandUsageForm form: forms)
			restricted &= form.restricted;
		this.restricted = restricted;
		
		this.forms = forms;
		this.name = MyUtils.toTitleCase(name());
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
		boolean admin = ServerCore.getServer().isAdmin(executor);
		
		if(printDescription) out.println(name + " - " + description);
		
		if(printUsage) {
			out.println("Usages:");
			for(CommandUsageForm form : forms) {
				if(!form.executorCheck.get(executor)) continue;
				if(form.restricted && !admin) continue;
				out.println(name + " " + form.usage);
				if(printDetails) out.println("    " + form.details);
			}
		}
	}
	
	public void printHelpAdvanced(ServerPlayer executor, MessageBuilder out) { printHelp(executor, out, true, true, true); }
	public void printHelpBasic(ServerPlayer executor, MessageBuilder out) {
		out.print(name+" ");
		printHelp(executor, out, false, true, false);
		out.println("type /\"help " + name + "\" for more info.");
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
