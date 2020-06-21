package miniventure.game.item;

/**
 * This is used to give the result of interactions involving items.
 */
public class Result {
	// the item was used
	public static final Result USED = new Result(true);
	
	// the item was not used, but an interaction did occur
	public static final Result INTERACT = new Result(true);
	
	// no interaction occurred
	public static final Result NONE = new Result(false);
	
	
	public final boolean success;
	
	private Result(boolean success) {
		this.success = success;
	}
}
