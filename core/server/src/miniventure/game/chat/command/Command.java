package miniventure.game.chat.command;

import java.util.Arrays;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.chat.MessageBuilder;
import miniventure.game.chat.command.Argument.ArgValidator;
import miniventure.game.server.ServerCore;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Config;
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
			Command c = ArgValidator.COMMAND.get(args[0]);
			c.printHelpAdvanced(executor, out);
		}, Argument.get(ArgValidator.COMMAND)),
		
		new CommandUsageForm(false, "--all", "list all commands in detail", (executor, args, out, err) -> {
			for(Command c: Command.values()) {
				c.printHelpAdvanced(executor, out);
				out.println();
			}
		}, Argument.get(ArgValidator.exactString(false, "--all")))
	),
	
	MSG("Broadcast a message for all players to see.",
		new CommandUsageForm(false, "<message...>", "Everyone on the server will see the message.", (executor, args, out, err) -> {
			String msg = String.join(" ", args);
			ServerCore.getServer().broadcastMessage(executor, msg);
		}, Argument.varArg(ArgValidator.ANY))
	),
	
	PMSG("Send a private message to another player.",
		new CommandUsageForm(false, "<playername> <message...>", "Send a message to the specified player, that only they can see.", MyUtils::notNull, ((executor, args, out, err) -> {
			ServerPlayer player = ArgValidator.PLAYER.get(args[0]);
			String msg = String.join("", Arrays.copyOfRange(args, 1, args.length));
			
			ServerCore.getServer().sendMessage(executor, player, msg);
			
		}), Argument.get(ArgValidator.PLAYER), Argument.varArg(ArgValidator.ANY))
	),
	
	CONFIG("Edit configuration values for how the world works.",
		new CommandUsageForm(true, "list", "Display the current value of all config entries.", (executor, args, out, err) -> {
			out.println("Config values:");
			for(Config value: Config.values)
				out.println("    "+value+" = "+value.get());
		}, Argument.get(ArgValidator.exactString(false, "list"))),
		
		new CommandUsageForm(true, "get <configvalue>", "Display the current value of a config entry.", (executor, args, out, err) -> {
			Config value = ArgValidator.CONFIG_VALUE.get(args[1]);
			out.println(value+": "+value.get());
		}, Argument.get(ArgValidator.exactString(false, "get"), ArgValidator.CONFIG_VALUE)),
		
		new CommandUsageForm(true, "set <configname> <value>", "Set the value of a config entry.", (executor, args, out, err) -> {
			Config value = ArgValidator.CONFIG_VALUE.get(args[1]);
			if(value.set(args[2])) {
				out.println("Successfully set " + value + " to " + value.get());
				ServerCore.getWorld().broadcastWorldUpdate();
			} else
				err.println("Could not set config value.");
		}, Argument.get(ArgValidator.exactString(false, "set"), ArgValidator.CONFIG_VALUE, ArgValidator.ANY))
	),
	
	TP("Teleport a player to a location in the world.",
		new CommandUsageForm(true, "<playername> <playername>", "Teleport the first player to the second player.", executor -> true, ((executor, args, out, err) -> {
			ServerPlayer toMove = ArgValidator.PLAYER.get(args[0]);
			ServerPlayer dest = ArgValidator.PLAYER.get(args[1]);
			
			ServerLevel level = dest.getLevel();
			if(level != null) {
				toMove.moveTo(level, dest.getPosition());
				ServerCore.getServer().sendToPlayer(toMove, new PositionUpdate(toMove));
				out.println("teleported "+toMove.getName()+" to "+toMove.getPosition(true)+".");
			} else
				err.println("player "+dest.getName()+" is not on a valid level; cannot tp to their location.");
		}), Argument.get(ArgValidator.PLAYER, ArgValidator.PLAYER)),
		
		new CommandUsageForm(true, "<playername>", "Teleports user to the specified player.", executor -> executor != null, ((executor, args, out, err) -> {
			String source = executor.getName();
			Command.valueOf("TP").execute(new String[] {source, args[0]}, executor, out, err);
		}), Argument.get(ArgValidator.PLAYER)),
		
		new CommandUsageForm(true, "<playername> <x> <y>", "Teleports the specified player to the specified location.", executor -> true, ((executor, args, out, err) -> {
			ServerPlayer player = ArgValidator.PLAYER.get(args[0]);
			float x = ArgValidator.DECIMAL.get(args[1]);
			float y = ArgValidator.DECIMAL.get(args[2]);
			// TODO later, add command forms without a level that redirect to the same form, but using the player's current level.
			ServerLevel level = player.getLevel();
			if(level != null) {
				x += level.getWidth()/2;
				y += level.getHeight()/2;
				player.moveTo(level, x, y);
				ServerCore.getServer().sendToPlayer(player, new PositionUpdate(player));
				out.println("teleported "+player.getName()+" to "+player.getPosition(true)+".");
			} else
				err.println("player "+player.getName()+" is not on a valid level; cannot be teleported.");
		}), Argument.get(ArgValidator.PLAYER, ArgValidator.DECIMAL, ArgValidator.DECIMAL)),
		
		new CommandUsageForm(true, "<x> <y>", "Teleports user to the specified location.", executor -> executor != null, ((executor, args, out, err) -> {
			String source = executor.getName();
			Command.valueOf("TP").execute(new String[] {source, args[0], args[1]}, executor, out, err);
		}), Argument.get(ArgValidator.DECIMAL, ArgValidator.DECIMAL))
	),
	
	TIME("Get or set the time of day.", "Note, the \"clocktime\" parameter used below refers to the time format \"HH:MM\", with the hour(HH) in the range 0-23, and the minute(MM) in the range 0-59, as one expects from a normal 24-hour clock.",
		new CommandUsageForm(false, "get", "Print the time of day, in 24 hour clock format, along with the current time \"range\" (day, night, etc).", (executor, args, out, err) -> {
			float daylightOffset = ServerCore.getWorld().getDaylightOffset();
			out.println(TimeOfDay.getTimeString(daylightOffset));
		}, Argument.get(ArgValidator.exactString(false, "get"))),
		
		new CommandUsageForm(true, "set <clocktime | "+ ArrayUtils.arrayToString(TimeOfDay.names, "", ">", " | "), "Set time given clocktime, or to start of specified range.", (executor, args, out, err) -> {
			float dayTime = ArgValidator.TIME.get(args[1]);
			ServerCore.getWorld().setTimeOfDay(dayTime);
			dayTime = ServerCore.getWorld().getDaylightOffset();
			out.println("Set time of day to "+TimeOfDay.getClockString(dayTime)+" ("+TimeOfDay.getTimeOfDay(dayTime)+")");
		}, Argument.get(ArgValidator.exactString(false, "set"), ArgValidator.TIME)
		),
		
		new CommandUsageForm(true, "add <HH:MM>", "Add the specified number of hours and minutes to the current time.", (executor, args, out, err) -> {
			float diffTime = ArgValidator.CLOCK_DURATION.get(args[1]);
			float dayTime = ServerCore.getWorld().changeTimeOfDay(diffTime);
			out.println("Added "+TimeOfDay.getClockString(diffTime-TimeOfDay.SECONDS_START_TIME_OFFSET)+" to the current time; new time: "+TimeOfDay.getClockString(dayTime)+" ("+TimeOfDay.getTimeOfDay(dayTime)+")");
			
		}, Argument.get(ArgValidator.exactString(false, "add"), ArgValidator.CLOCK_DURATION)),
		
		new CommandUsageForm(true, "sub <HH:MM>", "Subtract the specified number of hours and minutes from the current time.", (executor, args, out, err) -> {
			float diffTime = ArgValidator.CLOCK_DURATION.get(args[1]);
			float dayTime = ServerCore.getWorld().changeTimeOfDay(-diffTime);
			out.println("Subtracted "+TimeOfDay.getClockString(diffTime-TimeOfDay.SECONDS_START_TIME_OFFSET)+" from the current time; new time: "+TimeOfDay.getClockString(dayTime)+" ("+TimeOfDay.getTimeOfDay(dayTime)+")");
			
		}, Argument.get(ArgValidator.exactString(false, "sub"), ArgValidator.CLOCK_DURATION))
	),
	
	OP("Give another player access to all commands, or take it away.",
		new CommandUsageForm(true, "<playername> <isAdmin>", "Give or take admin permissions for the specified player. (isAdmin = true OR false)", executor -> true, ((executor, args, out, err) -> {
			ServerPlayer player = ArgValidator.PLAYER.get(args[0]);
			boolean give = ArgValidator.BOOLEAN.get(args[1]);
			boolean success = ServerCore.getServer().setAdmin(player, give);
			if(success)
				out.println("Admin permissions "+(give?"granted":"removed")+" for player "+player.getName()+".");
			else
				err.println("failed to change admin permissions of player "+player.getName()+".");
		}), Argument.get(ArgValidator.PLAYER, ArgValidator.BOOLEAN))
	),
	
	STATUS("Print the server's status on various things.",
		new CommandUsageForm(true, "", "Print various pieces of info about the server.", (executor, args, out, err) -> ServerCore.getServer().printStatus(out))
	),
	
	DEBUG("Toggles debug mode.", "This will show some extra things, and also allow certain cheats executed from a client to affect the server.",
		new CommandUsageForm(true, "on", "Enable debug mode.",
			((executor, args, out, err) -> GameCore.debug = true), Argument.get(ArgValidator.exactString(false, "on", "true", "1", "yes"))),
		new CommandUsageForm(true, "off", "Disable debug mode.",
			((executor, args, out, err) -> GameCore.debug = false), Argument.get(ArgValidator.exactString(false, "off", "false", "0", "no")))
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
	private final String description, details;
	private final boolean restricted;
	private final CommandUsageForm[] forms;
	
	Command(String description, CommandUsageForm... forms) { this(description, null, forms); }
	Command(String description, String details, CommandUsageForm... forms) {
		boolean restricted = true;
		for(CommandUsageForm form: forms)
			restricted &= form.restricted;
		this.restricted = restricted;
		
		this.forms = forms;
		this.name = MyUtils.toTitleCase(name());
		this.description = description;
		this.details = details;
	}
	
	public void execute(String[] args, @Nullable ServerPlayer executor, MessageBuilder out, MessageBuilder err) {
		for(CommandUsageForm form: forms) {
			if(form.execute(executor, args, out, err))
				return;
		}
		
		// no form matched, print help to error stream
		printHelpBasic(executor, err);
	}
	
	private static final String tab = "    ";
	
	private void printHelp(ServerPlayer executor, MessageBuilder out, boolean printDescription, boolean printUsage, boolean printDetails) {
		boolean admin = ServerCore.getServer().isAdmin(executor);
		
		if(printDescription) out.println(name + " - " + description);
		
		if(printDetails && details != null) out.println(tab+details);
		
		if(printUsage) {
			out.println("Usages:");
			for(CommandUsageForm form : forms) {
				if(!form.executorCheck.get(executor)) continue;
				if(form.restricted && !admin) continue;
				out.println(tab+name + " " + form.usage);
				if(printDetails) out.println(tab+tab + form.details);
			}
		}
	}
	
	public void printHelpAdvanced(ServerPlayer executor, MessageBuilder out) { printHelp(executor, out, true, true, true); }
	public void printHelpBasic(ServerPlayer executor, MessageBuilder out) {
		out.print(name+" ");
		printHelp(executor, out, false, true, false);
		out.println("type \"/help " + name + "\" for more info.");
	}
	
	private static Command[] valuesFor(@Nullable ServerPlayer player) {
		Array<Command> commands = new Array<>(Command.class);
		boolean op = ServerCore.getServer().isAdmin(player);
		for(Command c: Command.values())
			if((op || !c.restricted) && (c != DEBUG || player == null))
				commands.add(c);
		
		return commands.shrink();
	}
}
