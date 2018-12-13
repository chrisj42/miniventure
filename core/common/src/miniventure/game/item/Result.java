package miniventure.game.item;

/**
 * This is used to give the result of interactions involving items.
 */
public enum Result {
	// the item was used
	USED(true),
	
	// the item was not used, but an interaction did occur
	INTERACT(true),
	
	// no interaction occurred
	NONE(false);
	
	
	public final boolean success;
	
	Result(boolean success) {
		this.success = success;
	}
}
