package miniventure.game.item;

import miniventure.game.GameProtocol.ItemDropRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.world.entity.mob.player.Player;

import org.jetbrains.annotations.Nullable;

public class ClientInventory extends Inventory<Item, ItemStack> {
	
	// the fact that there is items in the hotbar is mostly for rendering. Most of these methods are for rendering.
	
	private int selection;
	// private float fillPercent;
	
	public ClientInventory(int size) {
		super(size, Item.class, ItemStack.class);
	}
	
	public void dropInvItems(boolean all) {
		ClientCore.getClient().send(new ItemDropRequest(selection, all));
		if(all)
			removeItemStack(getItem(selection));
		else
			removeItem(getItem(selection));
	}
	
	public void setSelection(int idx) {
		if(idx < 0)
			idx = getSlotsTaken() + (idx % getSlotsTaken());
		
		selection = idx % getSlotsTaken();
	}
	public int getSelection() { return selection; }
	
	// public ItemStack getHotbarItem(int idx) { return hotbar[idx]; }
	
	public ItemStack getSelectedItem() { return getItemStack(getSelection()); }
	
	// public float getFillPercent() { return fillPercent; }
	
	// the data isn't null, but may contain null arrays.
	/*public void updateItems(String[][] data) {
		// this.fillPercent = fillPercent;
		reset();
		for(int i = 0; i < data.length; i++)
			
			// hotbar[i] = data[i] == null ? null : ItemStack.deserialize(data[i]);
	}*/
	
	/*void updateItem(int index, ItemStack stack) {
		hotbar[index] = stack;
	}*/
	
	// void setFillPercent(float fillPercent) { this.fillPercent = fillPercent; }
	
	
	@Override
	ItemStack parseStack(String[] data) {
		return ItemStack.deserialize(data);
	}
}
