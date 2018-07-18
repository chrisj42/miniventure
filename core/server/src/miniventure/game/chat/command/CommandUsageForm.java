package miniventure.game.chat.command;

import miniventure.game.chat.MessageBuilder;
import miniventure.game.server.ServerCore;
import miniventure.game.util.function.MonoValueFunction;
import miniventure.game.world.entity.mob.ServerPlayer;

public class CommandUsageForm {
	
	interface ExecutionBehavior {
		void execute(ServerPlayer executor, String[] args, MessageBuilder out, MessageBuilder err);
	}
	
	final boolean restricted;
	final String usage;
	final String details;
	final MonoValueFunction<ServerPlayer, Boolean> executorCheck;
	private final Argument[] args;
	private final ExecutionBehavior executionBehavior;
	
	// pass a list of command parameters, and the lambda for the behavior.
	CommandUsageForm(boolean restricted, String usage, String details, ExecutionBehavior executionBehavior, Argument... args) {
		this(restricted, usage, details, executor -> true, executionBehavior, args);
	}
	CommandUsageForm(boolean restricted, String usage, String details, MonoValueFunction<ServerPlayer, Boolean> executorCheck, ExecutionBehavior executionBehavior, Argument... args) {
		this.restricted = restricted;
		this.usage = usage;
		this.details = details;
		this.executorCheck = executorCheck;
		this.args = args;
		this.executionBehavior = executionBehavior;
	}
	
	public boolean execute(ServerPlayer executor, String[] args, MessageBuilder out, MessageBuilder err) {
		if(restricted && !ServerCore.getServer().isAdmin(executor))
			return false;
		if(!executorCheck.get(executor))
			return false;
		
		int off = 0;
		for(Argument arg: this.args) {
			int len = arg.length();
			if(len < 0) len = args.length-off; // takes remaining args
			if(off+len > args.length || !arg.satisfiedBy(args, off))
				return false;
			off += len;
		}
		
		if(off < args.length) return false;
		
		this.executionBehavior.execute(executor, args, out, err);
		return true;
	}
	
}
