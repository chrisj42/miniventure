package miniventure.game.world;

import miniventure.game.item.Item;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemDrop {
	
	/*
		// in the end, a random number will be generated, 0 to 1, and an array of floats, one for each diff, will determine how many items are dropped.
		// the floats specify the upper bound in the 0-1 range
		
		First, just generate an integer between minCount and maxCount.
		Then, apply the bias. This is done by mapping the range from minCount to maxCount to the range of the specific difficulty.
		
		1/8 chance if 0 bias, for all diffs
		
		chance for those in the range, goes from 1/(total/numdiffs)% chance to 0% chance at the edges of the range.
		
		at 0.5 bias, it can go halfway up to the max at lowest difficulty.
		
		for lowest difficulty:
			- for bias of 0:
				chance of min = 1/total%
				chance of max = 1/total%
				min percent = 1/total% at all
				max percent = 1/total% at all
			- for bias of 0.5:
				chance of min = 
				chance of max = 
				min percent = 
				max percent = 
			- for bias of 1:
				chance of min = 
				chance of max = 
				min percent = 0% at half to total
				mid percent = 1/total% at quarter to total
				max percent = 1/(total/2)% at first
			
			ex. total = 8, bias = 1, diff = 1 of 4
			chance of values:
				1: 
				2: 
				3: 
				4: 
				5: 0
				6: 0
				7: 0
				8: 0
		
		There are two biases here: range bias and chance bias.
			Range bias literally removes options from the probability space.
				- at 0, all options are possible.
				- at 1, the options are only those that fit within the difficulty's fraction. So, the 1/numdiffs number of options (rounded up) in the order of the difficulty levels.
				- at 0.5, the possible options are half the number of difficulties. If there are 4 diffs and 8 options, then diff 1 will have options 1-4, diff 2 has 2-5, diff 3 has 4-7, diff 4 has 5-8.
				
				if 4 diffs and 3 options, and bias 1:
					d-1 has 
		
		At 0, all are possible with equal chance.
		At 0.5, all are possible, but 
		At 1, the min/max have shifted to only contain.
		
		
		
		
		peaceful = easy, but no evil mobs. It makes up, because you can't get the drops evil mobs have.
		difficulty is going to be a final choice; you choose when you create the world, like gamemode. But like gamemode, there will be a command to change the difficulty (as a "cheats" function, of course).
		
		so in essence, there are three difficulties.
		
		cow hide: 1, occ 2 in easy; 1 or 2 (eq. chance) in normal; 2, occ 1 in hard.
		iron: 
	*/
	
	/* NOTE: for tiles that drop something, they will drop them progressively; the last hit will drop the last one. Though, you can bias it so that the last drops all the items, or the last drops half the items, etc.
		lastDropBias:
			1 = all items are dropped when the tile is destroyed; none before.
			0 = items are dropped at equal intervals so that the last hit drops the last item.
			0.5 = half the items are dropped when the tile is destroyed; the other half is equally distributed.
			i.e. lastDropBias = part of items that are dropped when the tile is destroyed. The rest are equally distributed.
	 */
	
	private Item item;
	private int count;
	
	public ItemDrop(Item item, int minCount, int maxCount, float biasAmt, boolean roundUp) {
		/*
			this varies between the min count and max count depending on the difficulty. Well, it will depending on the biasAmt argument, a value from 0 to 1.
				- 0 means there is equal chance for any amount to be dropped on any difficulty.
				
				- 1 means that it is biased so that each difficulty gets a quarter of the numbers from min to max.
					So, if min is 1, and max is 8, and there are 4 difficulties, then lowest diff gets 1-2, 2nd diff gets 3-4, 3rd gets 5-6, and highest diff gets 7-8.
				
				- 0.5 means there is a possibility that a low difficulty gets a high number.
				0, min=1, max=8: 
		 */
		
		this(item, (maxCount+minCount)/2);
	}
	
	public ItemDrop(Item item) {
		this(item, 1);
	}
	
	public ItemDrop(Item item, int count) {
		this.item = item;
		this.count = count;
	}
	
	public ItemDrop(Item item, int peaceCount, int easyCount, int mediumCount, int hardCount) {
		this(item, mediumCount);
	}
	
	
	
	
	public int getItemsDropped() {
		// TODO implement this for different counts
		return count;
	}
	
	public Item[] getDroppedItems() {
		Item[] items = new Item[getItemsDropped()];
		for(int i = 0; i < items.length; i++)
			items[i] = item.copy();
		
		return items;
	}
}
