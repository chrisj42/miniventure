package miniventure.game.screen;

import miniventure.game.GameCore;
import miniventure.game.item.Item;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class InventoryScreen extends MenuScreen {
	
	class InventoryItem extends VisTextButton {
		
		private Item item;
		
		public InventoryItem(Item item) {
			super(item.getItemData().getName());
			//setHeight(Tile.SIZE*4/3);
			this.item = item;
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
			player.setActiveItem(table.getCell(InventoryItem.this).getRow());
			GameCore.setScreen(null);
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			//super.draw(batch, parentAlpha);
			item.getItemData().drawItem(item.getStackSize(), batch, GameCore.getFont(), getX(), getY());
			if(getKeyboardFocus() == this)
				batch.draw(GameCore.icons.get("tile-frame"), getX(), getY());
		}
	}
	
	private Player player;
	
	public InventoryScreen(Player player, Array<Item> inventory) {
		// I think scene components can have keyboard controls...
		//System.out.println("inv menu with " + inventory);
		this.player = player;
		
		for(Item item: inventory) {
			table.add(new InventoryItem(item)).left();
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
		
		setKeyboardFocus(inventory.size > 0 ? table.getCells().get(0).getActor() : getRoot());
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
	
	
}
