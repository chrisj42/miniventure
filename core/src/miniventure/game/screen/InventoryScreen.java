package miniventure.game.screen;

import miniventure.game.GameCore;
import miniventure.game.item.Hands;
import miniventure.game.item.Inventory;
import miniventure.game.item.Item;
import miniventure.game.item.ItemStack;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class InventoryScreen extends MenuScreen {
	
	private final Inventory inventory;
	private final Hands hands;
	
	public InventoryScreen(Inventory inventory, Hands hands) {
		this.inventory = inventory;
		this.hands = hands;
		
		float maxWidth = 0;
		for(int i = 0; i < inventory.getFilledSlots(); i++) {
			Item item = inventory.getItemAt(i);
			InventoryItem invItem = new InventoryItem(item, i);
			Cell c = table.add(invItem).left();
			c.size(item.getRenderWidth()+10, item.getRenderHeight()+10);
			maxWidth = Math.max(maxWidth, c.getPrefWidth());
			table.row().space(10);
		}
		
		for(Cell cell: table.getCells())
			cell.width(maxWidth);
		
		table.setOrigin(Align.center);
		table.setPosition(getWidth()/2, getHeight()/2, Align.center);
		table.pack();
		
		getRoot().addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.E)
					GameCore.setScreen(null);
				return true;
			}
		});
		
		setKeyboardFocus(table.getCells().size > 0 ? table.getCells().get(0).getActor() : getRoot());
	}
	
	@Override
	protected void drawTable(Batch batch, float parentAlpha) {
		MyUtils.fillRect(table.getX(), table.getY(), table.getWidth(), table.getHeight(), .2f, .4f, 1f, parentAlpha, batch);
	}
	
	private void moveFocus(int amt) {
		Actor focused = getKeyboardFocus();
		int row = table.getCell(focused).getRow();
		row += amt;
		while(row < 0) row += table.getRows();
		setKeyboardFocus(table.getCells().get(row % table.getCells().size).getActor());
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	
	private class InventoryItem extends Button {
		
		private final Item item;
		private final int idx;
		
		InventoryItem(Item item, int idx) {
			super(GameCore.getSkin());
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
					return true;
				}
				
				@Override
				public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
					setKeyboardFocus(InventoryItem.this);
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
			ItemStack stack = inventory.removeItemAt(idx);
			hands.clearItem(inventory); // just in case.
			hands.setItem(stack.item, stack.count);
			GameCore.setScreen(null);
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			if(getKeyboardFocus() == this)
				MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), .8f, .8f, .8f, 0.5f*parentAlpha, batch);
			
			item.drawItem(inventory.getStackSizeAt(idx), batch, GameCore.getFont(), getX()+5, getY()+5);
		}
	}
}
