package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.item.Hands.HandItem;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;

public class InventoryScreen extends MenuScreen {
	
	private final Inventory inventory;
	private final Hands hands;
	private ItemSelectionTable table;
	
	public InventoryScreen(Inventory inventory, Hands hands) {
		this.inventory = inventory;
		this.hands = hands;
		
		InventoryItem[] items = new InventoryItem[inventory.getSlots()];
		for(int i = 0; i < items.length; i++) {
			if(i < inventory.getFilledSlots())
				items[i] = new InventoryItem(inventory.getItemAt(i), i);
			else
				items[i] = new EmptySlot(i);
			//vGroup.addActor(new InventoryItem(inventory.getItemAt(i), i));
		}
		table = new ItemSelectionTable(items);
		
		addActor(table);
		table.setPosition(getWidth()/2, getHeight()/2, Align.center);
		
		getRoot().addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.E)
					ClientCore.setScreen(null);
				return true;
			}
		});
		
		setKeyboardFocus(items.length == 0 ? getRoot() : items[0]);
		//setKeyboardFocus(vGroup.getChildren().size > 0 ? vGroup.getChildren().get(0) : getRoot());
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	
	private class InventoryItem extends RenderableListItem {
		InventoryItem(Item item, int idx) {
			super(item, idx);
		}
		
		@Override
		void keyDown(InputEvent event, int keycode) {
			if(keycode == Keys.E || keycode == Keys.ESCAPE)
				ClientCore.setScreen(null);
		} 
		
		@Override
		void select(int idx) {
			ItemStack stack = inventory.removeItemAt(idx);
			hands.clearItem(inventory); // just in case.
			hands.setItem(stack.item, stack.count);
			ClientCore.setScreen(null);
		}
		
		@Override
		protected int getStackSize(int idx) {
			return inventory.getStackSizeAt(idx);
		}
	}
	
	private class EmptySlot extends InventoryItem {
		EmptySlot(int idx) {
			super(new HandItem(), idx);
		}
		
		@Override
		public void select(int idx) {
			hands.clearItem(inventory);
			ClientCore.setScreen(null);
		}
		
		@Override
		protected int getStackSize(int idx) { return 1; }
		@Override protected boolean showName() { return false; }
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), Color.TEAL.cpy().mul(1, 1, 1, parentAlpha), batch);
			super.draw(batch, parentAlpha);
		}
	}
}
