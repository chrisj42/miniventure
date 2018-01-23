package miniventure.game.screen;

import miniventure.game.GameCore;
import miniventure.game.item.Hands;
import miniventure.game.item.Inventory;
import miniventure.game.item.Item;
import miniventure.game.item.ItemStack;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class InventoryScreen extends MenuScreen {
	
	private final Inventory inventory;
	private final Hands hands;
	
	public InventoryScreen(Inventory inventory, Hands hands) {
		this.inventory = inventory;
		this.hands = hands;
		
		for(int i = 0; i < inventory.getFilledSlots(); i++) {
			table.add(new InventoryItem(inventory.getItemAt(i), i)).left();
			table.row();
		}
		
		getRoot().addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.E)
					GameCore.setScreen(null);
				return true;
				//return false;
			}
		});
		
		setKeyboardFocus(table.getCells().size > 0 ? table.getCells().get(0).getActor() : getRoot());
	}
	
	/*@Override
	public void draw() {
		getBatch().begin();
		getBatch().draw(GameCore.icons.get("hotbar"), table.getX(), table.getHeight(), table.getWidth(), table.getHeight());
		getBatch().end();
		super.draw();
	}*/
	
	private void moveFocus(int amt) {
		Actor focused = getKeyboardFocus();
		int row = table.getCell(focused).getRow();
		row += amt;
		while(row < 0) row += table.getRows();
		setKeyboardFocus(table.getCells().get(row % table.getCells().size).getActor());
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	
	private class InventoryItem extends VisTextButton {
		
		private final Item item;
		private final int idx;
		
		InventoryItem(Item item, int idx) {
			super(item.getName());
			//setHeight(Tile.SIZE*4/3);
			this.item = item;
			this.idx = idx;
			addListener(new InputListener() {
				@Override
				public boolean keyDown (InputEvent event, int keycode) {
					if(keycode == Keys.DOWN)
						moveFocus(1);
					else if(keycode == Keys.UP)
						moveFocus(-1);
					else if(keycode == Keys.ENTER)
						select();
					else if(keycode == Keys.E)
						GameCore.setScreen(null);
					//else return false;
					
					return true;
				}
			});
			
			addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent e, float x, float y) {
					select();
				}
			});
		}
		
		private void select() {
			//int idx = table.getCell(InventoryItem.this).getRow();
			ItemStack stack = inventory.removeItemAt(idx);
			hands.clearItem(inventory); // just in case.
			hands.setItem(stack.item, stack.count);
			GameCore.setScreen(null);
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			//super.draw(batch, parentAlpha);
			item.drawItem(inventory.getStackSizeAt(idx), batch, GameCore.getFont(), getX(), getY());
			if(getKeyboardFocus() == this)
				batch.draw(GameCore.icons.get("tile-frame"), getX(), getY());
		}
	}
}
