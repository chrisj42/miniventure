package miniventure.game.item;

import miniventure.game.util.function.FetchFunction;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.VisUI;

import org.jetbrains.annotations.Nullable;

public class InventoryPanel extends Table {
	
	private final int minSlots;
	private final int maxSlots;
	private final int maxSlotsPerRow;
	
	private ClientInventory inventory;
	
	public InventoryPanel(int minSlots, int maxSlots) { this(minSlots, maxSlots, maxSlots); }
	public InventoryPanel(int minSlots, int maxSlots, int maxSlotsPerRow) {
		super(VisUI.getSkin());
		
		this.minSlots = minSlots;
		this.maxSlots = maxSlots;
		this.maxSlotsPerRow = maxSlotsPerRow;
		
		defaults().fillX().minSize(Item.ICON_SIZE * ItemIcon.UI_SCALE, ItemSlot.HEIGHT * ItemIcon.UI_SCALE);
	}
	
	void setInventory(ClientInventory inventory) {
		clearChildren();
		this.inventory = inventory;
		
		int rows = (int)Math.ceil(maxSlots / (float) maxSlotsPerRow);
		int cols = Math.min(maxSlotsPerRow, maxSlots);
		ItemSlot[][] allSlots = new ItemSlot[rows][cols];
		
		int idx = 0;
		// int fullSlots = inventory.getSlotsTaken();
		for(int r = 0; r < rows && idx < maxSlots; r++) {
			for(int c = 0; c < cols && idx < maxSlots; c++) {
				allSlots[r][c] = makeItemSlot(idx++);
			}
		}
		
		// go backward through the rows of the array and add them to the slot table
		for(int r = allSlots.length - 1; r >= 0; r--) {
			// forward through columns
			for(int c = 0; c < allSlots[r].length; c++) {
				// skip rest of row if one is null
				if(allSlots[r][c] == null)
					break;
				
				add(allSlots[r][c]);
			}
			
			row();
		}
	}
	
	// allows a dynamic offset to be provided
	// int getIndex(int idx) { return idx; }
	
	ItemSlot makeItemSlot(int idx) {
		return new InventorySlot(idx, () -> false);
	}
	
	class InventorySlot extends ItemSlot {
		
		private final int idx;
		private boolean over;
		
		InventorySlot(int idx, FetchFunction<Boolean> drawSelected) {
			super(false, null, 0);
			this.idx = idx;
			
			setBackground(new SlotBackground(
				() -> inventory.getSlotsTaken() > idx || minSlots > idx,
				() -> over,
				drawSelected
			));
			
			addListener(new InputListener() {
				@Override
				public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
					over = true;
				}
				
				@Override
				public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
					over = false;
				}
			});
		}
		
		@Override @Nullable
		public Item getItem() {
			if(idx < 0 || inventory.getSlotsTaken() <= idx)
				return null;
			return inventory.getItem(idx);
		}
		
		@Override
		public int getCount() {
			if(idx < 0 || inventory.getSlotsTaken() <= idx)
				return 0;
			return inventory.getCount(inventory.getItem(idx));
		}
	}
}
