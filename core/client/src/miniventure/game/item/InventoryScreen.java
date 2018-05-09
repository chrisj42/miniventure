package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.MenuScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class InventoryScreen extends MenuScreen {
	
	private static final Color backgroundColor = Color.TEAL;
	
	private final Inventory inventory;
	private final ClientHands hands;
	private ItemSelectionTable table;
	
	public InventoryScreen(Inventory inventory, ClientHands hands) {
		this.inventory = inventory;
		this.hands = hands;
		
		ItemSlot[] items = new ItemSlot[inventory.getSlots()];
		for(int i = 0; i < items.length; i++) {
			items[i] = new ItemSlot(i, true, inventory.getItemAt(i), backgroundColor);
		}
		
		table = new ItemSelectionTable(items, getHeight()) {
			@Override
			public void onUpdate() {
				table.setPosition(InventoryScreen.this.getWidth(), InventoryScreen.this.getHeight(), Align.topRight);
			}
		};
		table.addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
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
					hands.dropInvItems(getSelectedItem(), Gdx.input.isKeyPressed(Keys.SHIFT_LEFT));
				}
				
				return false;
			}
		});
		
		for(ItemSlot slot: items) {
			slot.addListener(new InputListener() {
				@Override
				public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
					table.setSelection(slot.getSlotIndex());
				}
			});
			slot.addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					swapSelections();
				}
			});
		}
		
		addActor(table);
		table.setPosition(getWidth(), getHeight(), Align.topRight);
		
		setKeyboardFocus(table/*items.length == 0 ? getRoot() : items[0]*/);
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	private void swapSelections() {
		hands.swapItem(inventory, table.getSelection(), hands.getSelection());
		table.updateSelected(getSelectedItem());
	}
	
	private Item getSelectedItem() { return inventory.getItemAt(table.getSelection()); }
	/*private class InventoryItem extends RenderableListItem {
		InventoryItem(Item item, int idx) {
			super(item, idx);
		}
		
		@Override
		void keyDown(InputEvent event, int keycode) {
			
		} 
		
		*//*@Override
		void select(int idx) {
			ItemStack stack = inventory.removeItemAt(idx);
			//hands.clearItems(inventory); // just in case.
			hands.setItem(stack.item, stack.count);
			ClientCore.setScreen(null);
		}*//*
	}
	
	private class EmptySlot extends InventoryItem {
		EmptySlot(int idx) {
			super(new HandItem(), idx);
		}
		
		@Override protected boolean showName() { return false; }
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), Color.TEAL, parentAlpha, batch);
			super.draw(batch, parentAlpha);
		}
	}*/
}
