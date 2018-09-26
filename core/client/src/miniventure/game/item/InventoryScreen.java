package miniventure.game.item;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.MenuScreen;

public class InventoryScreen extends MenuScreen {
	
	private static final Color backgroundColor = new Color(0, 128, 128);
	
	private final Inventory inventory;
	private final ClientHands hands;
	private ItemSelectionTable table;
	
	public InventoryScreen(Inventory inventory, ClientHands hands) {
		super(true, false);
		this.inventory = inventory;
		this.hands = hands;
		
		// FIXME redo the item selection table entirely.
		
		ItemSlot[] items = new ItemSlot[inventory.getSlots()];
		for(int i = 0; i < items.length; i++) {
			items[i] = new ItemSlot(i, true, inventory.getUniqueItemAt(i), backgroundColor);
		}
		
		table = new ItemSelectionTable(items, getHeight());
		/*table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				
				
				// return false;
			}
		});*/
		
		for(ItemSlot slot: items) {
			slot.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					/*if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						
						return;
					}*/
					int keycode = e.getKeyCode();
					int hotbarSlots = Hands.HOTBAR_SIZE;
					if(keycode >= KeyEvent.VK_1 && keycode <= KeyEvent.VK_1+hotbarSlots-1) {
						int slot = keycode - KeyEvent.VK_1;
						hands.setSelection(slot);
						return;// true;
					}
					
					if(keycode == KeyEvent.VK_ENTER) {
						table.setSelection(slot.getSlotIndex());
						// swapSelections();
						return;// true;
					}
					
					if(keycode == KeyEvent.VK_E || keycode == KeyEvent.VK_ESCAPE) {
						ClientCore.setScreen(null);
						return;// true;
					}
					
					if(keycode == KeyEvent.VK_Q) {
						if(e.isShiftDown()) {
							Item item = getSelectedItem();
							hands.dropInvItems(item, true);
							Item[] items = inventory.getUniqueItems();
							for(int i = 0; i < items.length; i++)
								table.update(i, items[i]);
						} else {
							// remove the highlighted item only
							// fixme hotbar table / inventory panel
							// Item toRemove = 
							// Item removed = inventory.removeItem(table.getSelection(), new HandItem());
							// ClientCore.getClient().send(new ItemDropRequest(new ItemStack(removed, 1)));
							table.updateSelected(getSelectedItem());
						}
					}
				}
			});
			/*slot.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					swapSelections();
				}
			});*/
		}
		
		add(table);
		// table.setLocation(getWidth(), getHeight());
		
		table.setFocusable(false);
		// table.requestFocus();
		setFocusable(true);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_E || e.getKeyCode() == KeyEvent.VK_ESCAPE)
					ClientCore.setScreen(null);
			}
		});
		requestFocus();
	}
	
	// @Override
	// public boolean usesWholeScreen() { return false; }
	
	/*private void swapSelections() {
		hands.swapItem(inventory, table.getSelection(), hands.getSelection());
		table.updateSelected(getSelectedItem());
	}*/
	
	private Item getSelectedItem() { return inventory.getUniqueItemAt(table.getSelection()); }
}
