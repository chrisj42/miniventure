package miniventure.game.chat.command;

import miniventure.game.chat.MessageBuilder;
import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.entity.mob.ServerPlayer;

public class CommandUsageForm {
	
	interface ExecutionBehavior {
		void execute(ServerPlayer executor, String[] args, MessageBuilder out, MessageBuilder err);
	}
	
	final String usage;
	final String details;
	final ValueMonoFunction<Boolean, ServerPlayer> executorCheck;
	private final Argument[] args;
	private final ExecutionBehavior executionBehavior;
	
	// pass a list of command parameters, and the lambda for the behavior.
	CommandUsageForm(String usage, String details, ValueMonoFunction<Boolean, ServerPlayer> executorCheck, Argument[] args, ExecutionBehavior executionBehavior) {
		this.usage = usage;
		this.details = details;
		this.executorCheck = executorCheck;
		this.args = args;
		this.executionBehavior = executionBehavior;
	}
	
	public boolean execute(ServerPlayer executor, String[] args, MessageBuilder out, MessageBuilder err) {
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
