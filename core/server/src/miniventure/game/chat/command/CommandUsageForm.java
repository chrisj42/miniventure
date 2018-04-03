package miniventure.game.chat.command;

import miniventure.game.chat.MessageBuilder;

public class CommandUsageForm {
	
	interface CommandExecutor {
		void execute(String[] args, MessageBuilder out, MessageBuilder err);
	}
	
	final String usage;
	final String details;
	private final Argument[] args;
	private final CommandExecutor executor;
	
	// pass a list of command parameters, and the lambda for the behavior.
	CommandUsageForm(String usage, String details, Argument[] args, CommandExecutor executor) {
		this.usage = usage;
		this.details = details;
		this.args = args;
		this.executor = executor;
	}
	
	public boolean execute(String[] args, MessageBuilder out, MessageBuilder err) {
		int off = 0;
		for(Argument arg: this.args) {
			int len = arg.length();
			if(len < 0) len = args.length-off; // takes remaining args
			if(off+len > args.length || !arg.satisfiedBy(args, off))
				return false;
			off += len;
		}
		
		if(off < args.length) return false;
		
		executor.execute(args, out, err);
		return true;
	}
	
}
