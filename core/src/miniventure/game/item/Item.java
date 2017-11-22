package miniventure.game.item;

public abstract class Item {
	
	/*
		Will have a use() method, to mark that an item has gotten used. Called by tiles and entities. This class will determine whether it can be used again, however.
		Perhaps later I can add a parameter to the use method to specify how *much* to use it.
		
		
	 */
	
	public Item() {
		
	}
	
	public abstract boolean isRelfexive();
	
}
