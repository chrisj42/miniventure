package miniventure.game.chat.command;

import java.util.Arrays;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.DatalessRequest;
import miniventure.game.GameProtocol.Message;
import miniventure.game.GameProtocol.Ping;
import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.chat.MessageBuilder;
import miniventure.game.chat.command.Argument.ArgValidator;
import miniventure.game.chat.command.CommandUsageForm.ExecutionBehavior;
import miniventure.game.server.GameServer;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.management.Config;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.management.TimeOfDay;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static miniventure.game.chat.command.Argument.SERVER_ONLY;

public enum Command {
	
	CLEAR("Clear the chat console.",
		new CommandUsageForm(false, "", "Clears all messages from the chat screen scroll area.", MyUtils::notNull, (world, executor, args, out, err) -> world.getServer().sendToPlayer(executor, DatalessRequest.Clear_Console))
	),
	
	CONFIG("Edit configuration values for how the world works.",
		new CommandUsageForm(true, "list", "Display the current value of all config entries.", (world, executor, args, out, err) -> {
			out.println("Config values:");
			for(Config value: Config.values)
				out.println("    "+value+" = "+value.get());
		}, ArgValidator.exactString(false, "list")),
		
		new CommandUsageForm(true, "get <configvalue>", "Display the current value of a config entry.", (world, executor, args, out, err) -> {
			Config value = ArgValidator.CONFIG_VALUE.get(args[1]);
			out.println(value+": "+value.get());
		}, ArgValidator.exactString(false, "get"), ArgValidator.CONFIG_VALUE),
		
		new CommandUsageForm(true, "set <configname> <value>", "Set the value of a config entry.", (world, executor, args, out, err) -> {
			Config value = ArgValidator.CONFIG_VALUE.get(args[1]);
			if(value.set(world, args[2], err)) {
				out.println("Successfully set " + value + " to " + value.get());
				world.broadcastWorldUpdate();
			} else
				err.println("Could not set config value.");
	}, ArgValidator.exactString(false, "set"), ArgValidator.CONFIG_VALUE, ArgValidator.ANY)
	),
	
	DEBUG("Toggles debug mode.", "This will show some extra things, and also allow certain cheats executed from a client to affect the server.",
		new CommandUsageForm(true, "on", "Enable debug mode.",
			((world, executor, args, out, err) -> GameCore.debug = true), ArgValidator.exactString(false, "on", "true", "1", "yes")),
		new CommandUsageForm(true, "off", "Disable debug mode.",
			((world, executor, args, out, err) -> GameCore.debug = false), ArgValidator.exactString(false, "off", "false", "0", "no"))
	),
	
	HELP("List and show usage of various commands.",
		new CommandUsageForm(false, "","list all available commands.", (world, executor, args, out, err) -> {
			for(Command c: Command.valuesFor(executor))
				c.printHelp(executor, out, true, false, false);
		}),
		
		new CommandUsageForm(false, "<command>", "show detailed help for the given command.", (world, executor, args, out, err) -> {
			Command c = ArgValidator.COMMAND.get(args[0]);
			if(c.canExecute(executor))
				c.printHelpAdvanced(executor, out);
			else
				err.println("You do not have access to that command.");
		}, ArgValidator.COMMAND),
		
		new CommandUsageForm(false, "--all", "list all commands in detail", (world, executor, args, out, err) -> {
			for(Command c: Command.valuesFor(executor)) {
				c.printHelpAdvanced(executor, out);
				out.println();
			}
		}, ArgValidator.exactString(false, "--all"))
	),
	
	MSG("Broadcast a message for all players to see.",
		new CommandUsageForm(false, "<message...>", "Everyone on the server will see the message.", (world, executor, args, out, err) -> {
			String msg = String.join(" ", args);
			world.getServer().broadcastMessage(executor, msg);
		}, Argument.varArg(ArgValidator.ANY))
	),
	
	OP("Give another player access to all commands, or take it away.",
		new CommandUsageForm(true, "<player> <isAdmin>", "Give or take admin permissions for the specified player. (isAdmin = true OR false)", SERVER_ONLY,
			((world, executor, args, out, err) -> {
				ServerPlayer player = ArgValidator.PLAYER.get(world, args[0]);
				boolean give = ArgValidator.BOOLEAN.get(args[1]);
					
				if(world.getServer().isAdmin(player) == give) {
					out.println("player '"+player.getName()+"' is already "+(give?"":"not ")+" an admin.");
					return;
				}
				
				if(player == executor) {
					// this command can only be used by admins, so this implies that this only runs if an admin attempts to remove their own permissions.
					err.println("You are not allowed to remove permissions from yourself.");
					return;
				}
				
				if(world.getServer().isHost(player)) {
					err.println("Host permissions cannot be modified.");
					return;
				}
				
				boolean success = world.getServer().setAdmin(player, give);
				if(success)
					out.println("Admin permissions "+(give?"granted":"removed")+" for player "+player.getName()+'.');
				else
					err.println("failed to change admin permissions of player "+player.getName()+'.');
		}), ArgValidator.PLAYER, ArgValidator.BOOLEAN)
	),
	
	PING("Test client-server connection speed.",
		new CommandUsageForm(false, "", "From the server console, tests connection speed of all clients; from the chat console, tests only the player who requested the ping.", SERVER_ONLY,
			(world, executor, args, out, err) -> {
			// pings always come from the server.
			Ping ping = new Ping(executor == null ? null : executor.getName());
			if(executor == null)
				world.getServer().broadcast(ping);
			else
				world.getServer().sendToPlayer(executor, new Ping(executor.getName()));
		}),
		new CommandUsageForm(true, "all", "Tests connection speed of all clients.", SERVER_ONLY,
			(world, executor, args, out, err) ->
				world.getServer().broadcast(new Ping(executor == null ? null : executor.getName())),
			ArgValidator.exactString(false, "all")
		),
		new CommandUsageForm(true, "<players...>", "Tests connection speed of all specified players in the given order.", SERVER_ONLY,
			(world, executor, args, out, err) -> {
				for(String arg: args)
					world.getServer().sendToPlayer(ArgValidator.PLAYER.get(world, arg), new Ping(executor.getName()));
			},
			Argument.varArg(ArgValidator.PLAYER)
		)
	),
	
	PMSG("Send a private message to another player.",
		new CommandUsageForm(false, "<player> <message...>", "Send a message to the specified player, that only they can see.", SERVER_ONLY,
			((world, executor, args, out, err) -> {
			ServerPlayer player = ArgValidator.PLAYER.get(world, args[0]);
			String msg = String.join("", Arrays.copyOfRange(args, 1, args.length));
			
			world.getServer().sendMessage(executor, player, msg);
			
		}), ArgValidator.PLAYER, Argument.varArg(ArgValidator.ANY))
	),
	
	SAVE("Save the world to file.",
		new CommandUsageForm(true, "", "Save the current state of the game to file, so it can be loaded later.", (world, executor, args, out, err) -> {
			world.postRunnable(() -> {
				world.saveWorld();
				System.out.println("World Saved."); // for server console, and debug in general
				world.getServer().broadcast(new Message("World Saved.", GameServer.STATUS_MSG_COLOR));
			});
		})
	),
	
	STATUS("Print the server's status on various things.",
		new CommandUsageForm(true, "", "Print various pieces of info about the server.", (world, executor, args, out, err) -> world.getServer().printStatus(out))
	),
	
	STOP("Stops the server.",
		new CommandUsageForm(true, "", "Stops the server.", (world, executor, args, out, err) -> world.exitWorld())
	),
	
	TIME("Get or set the time of day.", "Note, the \"clocktime\" parameter used below refers to the time format \"HH:MM\", with the hour(HH) in the range 0-23, and the minute(MM) in the range 0-59, as one expects from a normal 24-hour clock.",
		new CommandUsageForm(false, "get", "Print the time of day, in 24 hour clock format, along with the current time \"range\" (day, night, etc).", (world, executor, args, out, err) -> {
			float daylightOffset = world.getDaylightOffset();
			out.println(TimeOfDay.getTimeString(daylightOffset));
		}, ArgValidator.exactString(false, "get")),
		
		new CommandUsageForm(true, "set <clocktime | "+ ArrayUtils.arrayToString(TimeOfDay.names, " | "), "Set time given clocktime, or to start of specified range.", (world, executor, args, out, err) -> {
			float dayTime = ArgValidator.TIME.get(args[1]);
			world.setTimeOfDay(dayTime);
			dayTime = world.getDaylightOffset();
			out.println("Set time of day to "+TimeOfDay.getClockString(dayTime)+" ("+TimeOfDay.getTimeOfDay(dayTime)+")");
		}, ArgValidator.exactString(false, "set"), ArgValidator.TIME),
		
		new CommandUsageForm(true, "add <HH:MM>", "Add the specified number of hours and minutes to the current time.", (world, executor, args, out, err) -> {
			float diffTime = ArgValidator.CLOCK_DURATION.get(args[1]);
			float dayTime = world.changeTimeOfDay(diffTime);
			out.println("Added "+TimeOfDay.getClockString(diffTime-TimeOfDay.SECONDS_START_TIME_OFFSET)+" to the current time; new time: "+TimeOfDay.getClockString(dayTime)+" ("+TimeOfDay.getTimeOfDay(dayTime)+")");
			
		}, ArgValidator.exactString(false, "add"), ArgValidator.CLOCK_DURATION),
		
		new CommandUsageForm(true, "sub <HH:MM>", "Subtract the specified number of hours and minutes from the current time.", (world, executor, args, out, err) -> {
			float diffTime = ArgValidator.CLOCK_DURATION.get(args[1]);
			float dayTime = world.changeTimeOfDay(-diffTime);
			out.println("Subtracted "+TimeOfDay.getClockString(diffTime-TimeOfDay.SECONDS_START_TIME_OFFSET)+" from the current time; new time: "+TimeOfDay.getClockString(dayTime)+" ("+TimeOfDay.getTimeOfDay(dayTime)+")");
			
		}, ArgValidator.exactString(false, "sub"), ArgValidator.CLOCK_DURATION)
	),
	
	// all TP forms in one: [player] <player | <x> <y> [level]>
	// args are fairly variable; maybe usage forms split at '|'?
	TP("Teleport a player to a location in the world.",
		new CommandUsageForm(true, "<player> <player>", "Teleport the first player to the second player.", (world, executor, args, out, err) -> {
			ServerPlayer toMove = ArgValidator.PLAYER.get(world, args[0]);
			ServerPlayer dest = ArgValidator.PLAYER.get(world, args[1]);
			
			ServerLevel level = dest.getLevel();
			if(level != null) {
				toMove.moveTo(dest.getPosition());
				world.getServer().sendToPlayer(toMove, new PositionUpdate(toMove));
				out.println("teleported "+toMove.getName()+" to "+toMove.getPosition(true)+".");
			} else
				err.println("player "+dest.getName()+" is not on a valid level; cannot tp to their location.");
		}, ArgValidator.PLAYER, ArgValidator.PLAYER),
		
		new CommandUsageForm(true, "<player>", "Teleports user to the specified player.", MyUtils::notNull, ((world, executor, args, out, err) -> {
			String source = executor.getName();
			Command.valueOf("TP").execute(world, executor, new String[] {source, args[0]}, out, err);
		}), ArgValidator.PLAYER),
		
		new CommandUsageForm(true, "<player> <x> <y>", "Teleports the specified player to the specified location.", ((world, executor, args, out, err) -> {
			ServerPlayer player = ArgValidator.PLAYER.get(world, args[0]);
			float x = ArgValidator.DECIMAL.get(args[1]);
			float y = ArgValidator.DECIMAL.get(args[2]);
			ServerLevel level = player.getLevel();
			if(level != null) {
				x += level.getWidth()/2f;
				y += level.getHeight()/2f;
				player.moveTo(x, y);
				world.getServer().sendToPlayer(player, new PositionUpdate(player));
				out.println("teleported "+player.getName()+" to "+player.getPosition(true)+'.');
			} else
				err.println("player "+player.getName()+" is not on a valid level; cannot be teleported.");
		}), ArgValidator.PLAYER, ArgValidator.DECIMAL, ArgValidator.DECIMAL),
		
		new CommandUsageForm(true, "<x> <y>", "Teleports user to the specified location.", MyUtils::notNull, (world, executor, args, out, err) -> {
			String source = executor.getName();
			Command.valueOf("TP").execute(world, executor, new String[] {source, args[0], args[1]}, out, err);
		}, ArgValidator.DECIMAL, ArgValidator.DECIMAL)
	);
	
	
	static Command getCommand(String commandName, @Nullable ServerPlayer executor) {
		Command c = null;
		try {
			c = Enum.valueOf(Command.class, commandName.toUpperCase());
		} catch(IllegalArgumentException ignored) {}
		
		if(c != null && !c.canExecute(executor))
			c = null; // not allowed to access.
		
		return c;
	}
	
	private static ExecutionBehavior replaceArgs(String command, MapFunction<String[], String[]> argMapper) {
		return (world, executor, args, out, err) ->
			getCommand(command, executor).execute(world, executor, argMapper.get(args), out, err);
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
	
	public void execute(@NotNull ServerWorld world, @Nullable ServerPlayer executor, String[] args, MessageBuilder out, MessageBuilder err) {
		// this method is called after previous canExecute checks, at least by proxy; so this technically should never print unless it's a race condition or something.
		if(!canExecute(executor)) {
			err.println("You do not have access to that command.");
			return;
		}
		
		for(CommandUsageForm form: forms)
			if(form.execute(world, executor, args, out, err))
				return;
		
		// no form matched, print help to error stream
		printHelpBasic(executor, err);
	}
	
	private static final String tab = "    ";
	
	private void printHelp(ServerPlayer executor, MessageBuilder out, boolean printDescription, boolean printUsage, boolean printDetails) {
		if(printDescription) out.println(name + " - " + description);
		
		if(printDetails && details != null) out.println(tab+details);
		
		if(printUsage) {
			out.println("Usages:");
			for(CommandUsageForm form: forms) {
				if(!form.executorCheck.get(executor)) continue;
				out.println(tab+name + ' ' + form.usage);
				if(printDetails) out.println(tab+tab + form.details);
			}
		}
	}
	
	public void printHelpAdvanced(ServerPlayer executor, MessageBuilder out) { printHelp(executor, out, true, true, true); }
	public void printHelpBasic(ServerPlayer executor, MessageBuilder out) {
		out.print(name+' ');
		printHelp(executor, out, false, true, false);
		out.println("type \"/help " + name + "\" for more info.");
	}
	
	private boolean canExecute(@Nullable ServerPlayer executor) {
		if(restricted && executor != null && !executor.getWorld().getServer().isAdmin(executor))
			return false;
		
		// run through each form's checks and see if any of them are viable options
		for(CommandUsageForm form: forms)
			if(form.executorCheck.get(executor))
				return true;

		return false;
	}
	
	public static Command[] valuesFor(@Nullable ServerPlayer player) {
		Array<Command> commands = new Array<>(true, Command.values().length, Command.class);
		for(Command c: Command.values())
			if(c.canExecute(player) && (c != DEBUG || player == null)) // only list debug command on server console; but all admins are still capable of *using* the debug command.
				commands.add(c);
		
		return commands.shrink();
	}
}
