package miniventure.game.item;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import miniventure.game.GameProtocol.ItemDropRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.MenuScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

public class InventoryScreen extends MenuScreen {
	
	private static final Color backgroundColor = Color.TEAL;
	
	private final Inventory inventory;
	private final ClientHands hands;
	private ItemSelectionTable table;
	
	public InventoryScreen(Inventory inventory, ClientHands hands) {
		super(false);
		this.inventory = inventory;
		this.hands = hands;
		
		// TODO redo the item selection table entirely.
		
		ItemSlot[] items = new ItemSlot[inventory.getSlots()];
		for(int i = 0; i < items.length; i++) {
			items[i] = new ItemSlot(i, true, inventory.getItemAt(i), backgroundColor);
		}
		
		table = new ItemSelectionTable(items, getHeight());
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keycode = e.getKeyCode();
				int hotbarSlots = hands.getSlots();
				if(keycode >= Keys.NUM_1 && keycode <= Keys.NUM_1+hotbarSlots-1) {
					int slot = keycode - Keys.NUM_1;
					hands.setSelection(slot);
					return true;
				}
				
				if(keycode == Keys.ENTER) {
					swapSelections();
					return true;
				}
				
				if(keycode == Keys.E || keycode == Keys.ESCAPE) {
					ClientCore.setScreen(null);
					return true;
				}
				
				if(keycode == Keys.Q) {
					if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
						Item item = getSelectedItem();
						hands.dropInvItems(item, true);
						Item[] items = inventory.getItems();
						for(int i = 0; i < items.length; i++)
							table.update(i, items[i]);
					} else {
						// remove the highlighted item only
						Item removed = inventory.replaceItemAt(table.getSelection(), new HandItem());
						ClientCore.getClient().send(new ItemDropRequest(new ItemStack(removed, 1)));
						table.updateSelected(getSelectedItem());
					}
				}
				
				return false;
			}
		});
		
		for(ItemSlot slot: items) {
			slot.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() != KeyEvent.VK_ENTER)
						return;
					table.setSelection(slot.getSlotIndex());
				}
			});
			slot.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					swapSelections();
				}
			});
		}
		
		add(table);
		table.setPosition(getWidth(), getHeight(), Align.topRight);
		
		table.requestFocus();
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	private void swapSelections() {
		hands.swapItem(inventory, table.getSelection(), hands.getSelection());
		table.updateSelected(getSelectedItem());
	}
	
	private Item getSelectedItem() { return inventory.getItemAt(table.getSelection()); }
}
