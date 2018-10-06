package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;

public class InventoryScreen extends MenuScreen {
	
	private static final Color backgroundColor = Color.TEAL;
	
	private final Inventory inventory;
	private final ClientHands hands;
	private ItemSelectionTable table;
	
	private final HorizontalGroup hGroup;
	private final ProgressBar fillBar;
	private final VerticalGroup invGroup;
	
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
		
		invGroup = new VerticalGroup() {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), backgroundColor, parentAlpha, batch);
				super.draw(batch, parentAlpha);
			}
		};
		hGroup.addActor(invGroup);
		
		Item[] allItems = inventory.getUniqueItems();
		ItemSlot[] items = new ItemSlot[allItems.length];
		for(int i = 0; i < items.length; i++) {
			items[i] = new ItemStackSlot(i, true, allItems[i], inventory.getCount(allItems[i]), backgroundColor);
			invGroup.addActor(items[i]);
		}
		
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
