package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;

public class InventoryScreen extends MenuScreen {
	
	private final Inventory inventory;
	private final Hands hands;
	
	public InventoryScreen(Inventory inventory, Hands hands) {
		this.inventory = inventory;
		this.hands = hands;
		
		for(int i = 0; i < inventory.getFilledSlots(); i++)
			vGroup.addActor(new InventoryItem(inventory.getItemAt(i), i));
		
		vGroup.setOrigin(Align.right);
		vGroup.columnAlign(Align.left);
		vGroup.setPosition(getWidth()/2, getHeight()/2, Align.center);
		vGroup.pack();
		
		getRoot().addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.E)
					GameCore.setScreen(null);
				return true;
			}
		});
		
		setKeyboardFocus(vGroup.getChildren().size > 0 ? vGroup.getChildren().get(0) : getRoot());
	}
	
	@Override
	protected void drawTable(Batch batch, float parentAlpha) {
		MyUtils.fillRect(vGroup.getX(), vGroup.getY(), vGroup.getWidth(), vGroup.getHeight(), .2f, .4f, 1f, parentAlpha, batch);
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	
	private class InventoryItem extends RenderableListItem {
		InventoryItem(Item item, int idx) {
			super(item, idx, vGroup);
		}
		
		@Override
		void keyDown(InputEvent event, int keycode) {
			if(keycode == Keys.E || keycode == Keys.ESCAPE)
				GameCore.setScreen(null);
		} 
		
		@Override
		void select(int idx) {
			ItemStack stack = inventory.removeItemAt(idx);
			hands.clearItem(inventory); // just in case.
			hands.setItem(stack.item, stack.count);
			GameCore.setScreen(null);
		}
		
		@Override
		protected int getStackSize(int idx) {
			return inventory.getStackSizeAt(idx);
		}
	}
}
