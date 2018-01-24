package miniventure.game.item;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public class Inventory {
	
	/*
		items only really exist inside inventories. That is, items can only really be manipulated inside an inventory.
		Stacks exist only here.
	 */
	
	private final Array<Integer> stackSizes;
	private final Array<Item> items;
	private final int slots;
	
	@Nullable private final Hands mustFit; // the idea of this is that at any point, we must insure that the contents of the hand can fit into the inventory.
	
	public Inventory(int slots) { this(slots, null); }
	public Inventory(int slots, @Nullable Hands mustFit) {
		this.slots = slots;
		stackSizes = new Array<>(true, slots, Integer.class);
		items = new Array<>(true, slots, Item.class);
		this.mustFit = mustFit;
	}
	
	public int getSlots() { return slots; }
	public int getFilledSlots() { return items.size; }
	
	/// Returns how many items could successfully be added.
	public int addItem(Item item) { return addItem(item, 1); }
	public int addItem(Item item, int count) { return addItem(item, count, true); }
	public int addItem(Item item, int count, boolean addToTop) { return addItem(item, count, addToTop, true); }
	private int addItem(Item item, int count, boolean addToTop, boolean checkMustFit) {
		if(item.getName().equalsIgnoreCase("hand")) {
			System.out.println("attempted addition of hand item");
			Thread.dumpStack();
			return 0;
		}
		if(checkMustFit && mustFit != null && mustFit.getEffectiveItem() != null) 
			addItem(mustFit.getEffectiveItem(), mustFit.getCount(), false, false);
		else
			checkMustFit = false; // so that it doesn't attempt to remove the "mustFit" item afterward
		
		int left = count;
		int idx = 0;
		while(left > 0 && (idx = getFirstMatch(idx, item, false)) >= 0) {
			int space = item.getMaxStackSize() - stackSizes.get(idx);
			int added = Math.min(space, left);
			left -= added;
			stackSizes.set(idx, stackSizes.get(idx) + added);
		}
		
		while(left > 0 && items.size < slots) {
			int added = Math.min(left, item.getMaxStackSize());
			items.add(item.copy());
			stackSizes.add(added);
			left -= added;
		}
		
		if(addToTop) {
			idx = getFirstMatch(item, true);
			if(idx >= 0) {
				items.insert(0, items.removeIndex(idx));
				stackSizes.insert(0, stackSizes.removeIndex(idx));
			}
		}
		
		if(checkMustFit && mustFit != null)
			removeItem(mustFit.getEffectiveItem(), mustFit.getCount());
		
		return count - left;
	}
	
	/// Returns how many items could successfully be removed.
	public int removeItem(Item item) { return removeItem(item, 1); }
	public int removeItem(Item item, int count) {
		int left = count;
		int idx = 0;
		while(left > 0 && (idx = getFirstMatch(idx, item, true)) >= 0) {
			int removed = Math.min(left, stackSizes.get(idx));
			if(removed == stackSizes.get(idx)) {
				items.removeIndex(idx);
				stackSizes.removeIndex(idx);
			}
			else
				stackSizes.set(idx, stackSizes.get(idx) - removed);
			
			left -= removed;
		}
		
		if(left > 0 && mustFit != null && mustFit.getUsableItem().equals(item)) {
			int removed = Math.min(left, mustFit.getCount());
			mustFit.setItem(mustFit.getUsableItem(), mustFit.getCount() - removed);
			mustFit.resetItemUsage(); // clears empty stacks
			left -= removed;
		}
		
		return count - left;
	}
	
	public boolean hasItem(Item item) { return hasItem(item, 1); }
	public boolean hasItem(Item item, int count) { return countItem(item) >= count; }
	
	public int countItem(Item item) {
		int count = 0;
		for(int i = 0; i < items.size; i++)
			if(items.get(i).equals(item))
				count += stackSizes.get(i);
		
		if(mustFit != null && mustFit.getUsableItem().equals(item))
			count += mustFit.getCount();
		
		return count;
	}
	
	
	private int getFirstMatch(Item item, boolean matchFull) { return getFirstMatch(0, item, matchFull); }
	private int getFirstMatch(int startIdx, Item item, boolean matchFull) {
		for(int i = startIdx; i < items.size; i++)
			if(items.get(i).equals(item) && (matchFull || stackSizes.get(i) < item.getMaxStackSize()))
				return i;
		
		return -1;
	}
	
	/*public ItemStack[] getItems() {
		ItemStack[] stacks = new ItemStack[items.size];
		for(int i = 0; i < items.size; i++)
			stacks[i] = new ItemStack(items.get(i), stackSizes.get(i));
		
		return stacks;
	}*/
	
	Item getItemAt(int idx) {
		checkIndex(idx);
		return items.get(idx);
	}
	
	int getStackSizeAt(int idx) {
		checkIndex(idx);
		return stackSizes.get(idx);
	}
	
	ItemStack removeItemAt(int idx) {
		checkIndex(idx);
		
		int count = stackSizes.removeIndex(idx);
		Item item = items.removeIndex(idx);
		return new ItemStack(item, count);
	}
	
	private void checkIndex(int idx) {
		if(idx >= slots) throw new IndexOutOfBoundsException("cannot access index " + idx + " of "+slots+"-slot inventory.");
		if(idx >= items.size) throw new IndexOutOfBoundsException("cannot access inventory index " + idx + "; inventory only contains " + items.size + " items.");
	}
	
}
