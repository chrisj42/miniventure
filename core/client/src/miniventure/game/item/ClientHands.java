package miniventure.game.item;

import miniventure.game.GameProtocol.ItemDropRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.world.entity.mob.player.Player;

import org.jetbrains.annotations.Nullable;

public class ClientHands {
	
	// the fact that there is items in the hotbar is mostly for rendering. Most of these methods are for rendering.
	
	private ItemStack[] hotbar;
	private int selection;
	private float fillPercent;
	
	public ClientHands() {
		hotbar = new ItemStack[Player.HOTBAR_SIZE];
		fillPercent = 0;
	}
	
	public void dropInvItems(boolean all) {
		ClientCore.getClient().send(new ItemDropRequest(true, selection, all));
	}
	
	public void setSelection(int idx) { selection = idx; }
	public int getSelection() { return selection; }
	
	public ItemStack getHotbarItem(int idx) { return hotbar[idx]; }
	
	public ItemStack getSelectedItem() { return getHotbarItem(getSelection()); }
	
	public float getFillPercent() { return fillPercent; }
	
	// the data isn't null, but may contain null arrays.
	public void updateItems(String[][] data, float fillPercent) {
		this.fillPercent = fillPercent;
		for(int i = 0; i < hotbar.length; i++)
			hotbar[i] = data[i] == null ? null : ItemStack.deserialize(data[i]);
	}
	
	void updateItem(int index, @Nullable ItemStack stack) {
		hotbar[index] = stack;
	}
	
	void setFillPercent(float fillPercent) { this.fillPercent = fillPercent; }
}
