package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.utils.Align;

public class InventoryScreen extends MenuScreen {
	
	private static final Color slotBackgroundColor = Color.TEAL.cpy().lerp(Color.WHITE, .1f);
	
	private final Inventory inventory;
	private final ClientHands hands;
	
	private final HorizontalGroup hGroup;
	private final ProgressBar fillBar;
	private final ItemSelectionTable invGroup;
	
	public InventoryScreen(Inventory inventory, ClientHands hands) {
		super(false);
		this.inventory = inventory;
		this.hands = hands;
		
		fillBar = new ProgressBar(0, 1, .01f, true, GameCore.getSkin());
		
		hGroup = new HorizontalGroup();
		hGroup.align(Align.right);
		setMainGroup(hGroup, RelPos.RIGHT);
		hGroup.rowCenter();
		hGroup.addActor(fillBar);
		
		float heightAvailable = getHeight() * 3 / 4;
		int minSlots = (int) (heightAvailable / Item.ICON_SIZE);
		
		Item[] allItems = inventory.getUniqueItems();
		ItemSlot[] items = new ItemSlot[Math.max(allItems.length, minSlots)];
		for(int i = 0; i < items.length; i++) {
			if(i >= allItems.length)
				items[i] = new ItemSlot(i, true, new HandItem(), slotBackgroundColor);
			else
				items[i] = new ItemStackSlot(i, true, allItems[i], inventory.getCount(allItems[i]), slotBackgroundColor);
		}
		
		invGroup = new ItemSelectionTable(items, getHeight());
		Container<ItemSelectionTable> inventoryContainer = new Container<>(invGroup);
		inventoryContainer.fill().pad(10, 10, 10, 0);
		hGroup.addActor(inventoryContainer);
		
		float percentUsed = inventory.getSpaceLeft() / (float)inventory.getSlots();
		fillBar.setValue(1 - percentUsed);
		
		InputListener l = new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Keys.ESCAPE || keycode == Keys.E) {
					ClientCore.setScreen(null);
					return true;
				}
				return false;
			}
		};
		
		addListener(l);
		invGroup.addListener(l);
		
		/*table = new ItemSelectionTable(items, getHeight()) {
			@Override
			public void onUpdate() {
				table.setPosition(InventoryScreen.this.getWidth(), InventoryScreen.this.getHeight(), Align.topRight);
			}
		};
		table.addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				int hotbarSlots = Hands.HOTBAR_SIZE;
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
						Item[] items = inventory.getUniqueItems();
						for(int i = 0; i < items.length; i++)
							table.update(i, items[i]);
					} else {
						// remove the highlighted item only
						Item removed = new HandItem();
						// Item removed = inventory.removeItemAt(table.getSelection(), new HandItem());
						ClientCore.getClient().send(new ItemDropRequest(new ItemStack(removed, 1)));
						table.updateSelected(getSelectedItem());
					}
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
		}*/
		
		// addActor(table);
		// table.setPosition(getWidth(), getHeight(), Align.topRight);
		
		// setKeyboardFocus(table);
		hGroup.pack();
	}
	
	@Override
	protected void layoutActors() {
		invGroup.refresh();
		super.layoutActors();
	}
	
	/*private void swapSelections() {
		// hands.swapItem(inventory, table.getSelection(), hands.getSelection());
		// table.updateSelected(getSelectedItem());
	}
	
	private Item getSelectedItem() { return new HandItem();*//*inventory.getItemAt(table.getSelection());*//* }*/
	
	/*@Override
	protected void layoutActors() {
		invGroup.setSize(invGroup.getPrefWidth(), invGroup.getPrefHeight());
		hGroup.setSize(fillBar.getPrefWidth()+invGroup.getPrefWidth(), fillBar.getPrefHeight()+invGroup.getPrefHeight());
		System.out.println("inv group pref size: "+invGroup.getPrefWidth()+","+invGroup.getPrefHeight());
		System.out.println("h group pref size: "+hGroup.getPrefWidth()+","+hGroup.getPrefHeight());
		super.layoutActors();
		System.out.println(Gdx.graphics.getWidth()+","+Gdx.graphics.getHeight());
		hGroup.setX(Gdx.graphics.getWidth() - hGroup.getWidth());
		System.out.println("h group position: "+hGroup.getX()+","+hGroup.getY()+","+hGroup.getWidth()+","+hGroup.getHeight());
		System.out.println("inv group position: "+invGroup.getX()+","+invGroup.getY()+","+invGroup.getWidth()+","+invGroup.getHeight());
	}*/
}
