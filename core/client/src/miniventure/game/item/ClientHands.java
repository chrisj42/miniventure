package miniventure.game.item;

import miniventure.game.GameProtocol.ItemDropRequest;
import miniventure.game.client.ClientCore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ClientHands extends Hands {
	
	private ItemSelectionTable hotbarTable;
	
	public ClientHands(Inventory inventory) {
		super(inventory);
		
		ItemSlot[] slots = new ItemSlot[getSlots()];
		for(int i = 0; i < slots.length; i++)
			slots[i] = new HotbarSlot(i, inventory);
		
		hotbarTable = new ItemSelectionTable(slots, ItemSlot.HEIGHT);
		hotbarTable.pack();
	}
	
	public ItemSelectionTable getHotbarTable() { return hotbarTable; }
	
	@Override
	public void setSelection(int idx) {
		super.setSelection(idx);
		hotbarTable.setSelection(idx);
	}
	
	@Override
	Item replaceItemAt(int idx, Item item) {
		Item prev = super.replaceItemAt(idx, item);
		hotbarTable.update(idx, item);
		return prev;
	}
	
	@Override
	public void loadItems(String[] allData) {
		super.loadItems(allData);
		for(int i = 0; i < getSlots(); i++)
			hotbarTable.update(i, getItemAt(i));
	}
	
	public void dropInvItems(Item item, boolean all) {
		if(!(item instanceof HandItem)) {
			int count = all ? getCount(item) : 1;
			for(int i = 0; i < count; i++)
				removeItem(item);
			ClientCore.getClient().send(new ItemDropRequest(new ItemStack(item, count)));
		}
	}
	
	/*public void drawGui(Batch batch, float x, float y) {
		hotbarTable.setPosition(x, y);
		//hotbarTable.draw(batch, 1);
		*//*for(int i = 0; i < getSlots(); i++) {
			Item item = getItemAt(i);
			MyUtils.fillRect(x, y, item.getRenderWidth(), RenderableListItem.MAX_HEIGHT, Color.DARK_GRAY, batch);
			item.drawItem(getItemCount(i), batch, x, y, Color.WHITE, !(item instanceof HandItem));
			if(i == getSelection())
				MyUtils.fillRect(x, y, item.getRenderWidth(), RenderableListItem.MAX_HEIGHT, RenderableListItem.selectionColor, batch);
			y += RenderableListItem.MAX_HEIGHT;
		}*//*
	}*/
	
	private class HotbarSlot extends ItemStackSlot {
		private final Inventory inventory;
		
		public HotbarSlot(int idx, Inventory inventory) {
			super(idx, false, getItemAt(idx), 1, Color.DARK_GRAY);
			this.inventory = inventory;
			/*addListener(new InputListener() {
				@Override
				public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
					//System.out.println("entering hotbar slot "+idx);
				}
				
				
			});*/
			// FIXME I can't seem to prevent clicks on the hotbar from propagating to the game screen and moving the player...
			addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					ClientHands.this.setSelection(idx);
				}
				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					super.touchDown(event, x, y, pointer, button);
					return true; // consume this event, to prevent it from being used to move the player.
				}
			});
		}
		
		@Override
		// the count displayed shows how many of that item are in the inventory.
		public int getCount() {
			//if(ClientCore.getScreen() instanceof InventoryScreen)
			//	return 0; // don't show counts when inventory screen is open..?
			if(inventory.getCount(this.getItem()) == 0)
				return 0;
			
			return ClientHands.this.getCount(getItem()); // includes both inventory and hotbar
		}
	}
}
