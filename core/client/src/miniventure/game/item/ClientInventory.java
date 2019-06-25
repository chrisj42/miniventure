package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.item.CraftingScreen.ClientRecipe;
import miniventure.game.network.GameProtocol.ItemDropRequest;
import miniventure.game.util.Version;
import miniventure.game.world.tile.ClientTileType;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.tile.TileTypeRenderer;

import org.jetbrains.annotations.NotNull;

public class ClientInventory extends Inventory<Item, ItemStack> {
	
	// the fact that there is items in the hotbar is mostly for rendering. Most of these methods are for rendering.
	
	private ClientRecipe currentBlueprint = null;
	private TileTypeRenderer blueprintRenderer = null;
	private int selection;
	// private float fillPercent;
	
	public ClientInventory(int size) {
		super(size, Item.class, ItemStack.class);
	}
	
	void setBlueprint(ClientRecipe recipe) {
		TileTypeEnum target = recipe.getBlueprintTarget();
		if(target == null) {
			System.err.println("attempted to set current blueprint using a non-blueprint recipe: "+recipe.getName());
			return;
		}
		blueprintRenderer = ClientTileType.get(target).getRenderer();
		currentBlueprint = recipe;
	}
	
	public ClientRecipe getCurrentBlueprint() { return currentBlueprint; }
	public TileTypeRenderer getBlueprintRenderer() { return blueprintRenderer; }
	
	public void removeBlueprint() { blueprintRenderer = null; currentBlueprint = null; }
	
	public void dropInvItems(boolean all) {
		if(blueprintRenderer != null) {
			removeBlueprint();
			return;
		}
		
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
		removeBlueprint();
	}
	public int getSelection() { return selection; }
	
	// public ItemStack getHotbarItem(int idx) { return hotbar[idx]; }
	
	public ItemStack getSelectedItem() {
		if(currentBlueprint != null)
			return new ItemStack(currentBlueprint, 1);
		return getItemStack(getSelection());
	}
	
	private void checkSelection() {
		int size = getSlotsTaken();
		if(selection >= size)
			selection = Math.max(0, size - 1);
	}
	
	@Override
	public synchronized boolean removeItem(Item item) {
		boolean res = super.removeItem(item);
		if(res) checkSelection();
		return res;
	}
	
	@Override
	public synchronized int removeItemStack(Item item) {
		int res = super.removeItemStack(item);
		if(res > 0) checkSelection();
		return res;
	}
	
	@Override
	public synchronized void updateItems(String[][] data) {
		super.updateItems(data);
		checkSelection();
		if(currentBlueprint != null) {
			boolean canCraft = true;
			for(ItemStack cost: currentBlueprint.costs) {
				if(getCount(cost.item) < cost.count) {
					canCraft = false;
					break;
				}
			}
			if(!canCraft)
				removeBlueprint();
		}
	}
	
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
	ItemStack parseStack(String[] data, @NotNull Version version) {
		return ItemStack.deserialize(data);
	}
}
