package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.ItemDropRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.ColorBackground;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryDisplayGroup extends Table {
	
	// FIXME BORKED
	
	/*
		This will be used both for container screens and inventory screens. It should ideally take only half the screen horizontally at most, so that a second inventory screen can be placed next to it, for transfers between chests.
		
		While an inventory screen will additionally display a header section with the total weight and the total space used, a crafting screen will display a couple extra panels for the crafting requirements.
		
		About crafting screens... if we're going to have item crafting and tile crafting, then there's going to have to be some edits here. Or... well actually, maybe not..?
		I think crafting should consist of depositing materials. If you can hold all the materials at once, then that's good, but you don't have to do that. There will be a workbench after all, and you can deposit the materials there until you've got all of them. Then you can craft. Should crafting take some time? Perhaps. probably.
		Tile crafting consists of a menu that once selected, you can choose where to place the new tile, and then deposit materials there for crafting.
		
		So for this class, it will need to know this about its dimensions:
			- 
		
		Actually... should I make a class that is specifically one column? I actually like that idea... it won't have a background, just the background for the individual items, and empty cells.
	 */
	
	public static final Color background = Color.TEAL;
	private static final Color hotbarBackground = Color.TEAL.cpy().lerp(Color.YELLOW, .25f);
	public static final Color slotBackgroundColor = Color.TEAL.cpy().lerp(Color.WHITE, .1f);
	
	private static final float MIN_SPACING = 5;
	
	private final ClientHands inventory;
	private Array<ItemSlot> itemSlots;
	private final ProgressBar fillBar;
	private final Table invGroup;
	private final PageCounter pageCounter;
	
	private int selectionIndex = 0;
	private int cellsPerColumn, numColumns;
	
	public InventoryDisplayGroup(final ClientHands inventory, final float maxHeight) {
		this.inventory = inventory;
		
		defaults().space(5f).center();
		background(new ColorBackground(this, background));
		
		// fill bar
		fillBar = new ProgressBar(0, 1, .01f, false, GameCore.getSkin());
		fillBar.setValue(inventory.getFillPercent());
		add(fillBar).growX().row();
		
		invGroup = new Table() {
			// highlight selected item
			@Override
			protected void drawChildren(Batch batch, float parentAlpha) {
				itemSlots.get(selectionIndex).setSelected(true);
				super.drawChildren(batch, parentAlpha);
				itemSlots.get(selectionIndex).setSelected(false);
			}
		};
		invGroup.defaults().growX();
		add(invGroup).row();
		
		pageCounter = new PageCounter();
		add(pageCounter).row();
		
		float heightAvailable = maxHeight - getRowHeight(0) - getRowHeight(2);
		
		float cellHeight = ItemSlot.HEIGHT + MIN_SPACING;
		int invSpaces = Math.max(1, (int) (heightAvailable / cellHeight));
		
		/*Item[] allItems = inventory.getUniqueItems();
		
		itemSlots = new Array<>(ItemSlot.class);
		for(int i = 0; i < invSpaces; i++) {
			ItemSlot slot;
			if(i >= allItems.length)
				slot = new ItemSlot(true, null, slotBackgroundColor);
			else
				slot = new ItemSlot(true, allItems[i], inventory.getCount(allItems[i]), slotBackgroundColor);
			
			slot.addListener(new InputListener() {
				@Override
				public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
					moveFocus(itemSlots.indexOf(slot, true));
				}
			});
			
			itemSlots.add(slot);
			invGroup.add(slot).row();
		}*/
		
		// configure the height to be something that equalizes the slots in as few columns as possible.
		numColumns = Math.max(1, MathUtils.ceil(this.itemSlots.size / (float)invSpaces));
		cellsPerColumn = Math.max(1, MathUtils.ceil(this.itemSlots.size / (float)numColumns));
		
		float height = Math.max(MIN_SPACING, cellHeight * cellsPerColumn - MIN_SPACING);
		
		if(this.itemSlots.size > 1) {
			// need to adjust spacing to make sure there isn't a large space at the bottom
			float spaceTaken = Math.min(this.itemSlots.size, invSpaces) * ItemSlot.HEIGHT;
			float emptySpace = height - spaceTaken;
			for(int i = 1; i < invGroup.getRows(); i++)
				invGroup.getCells().get(i).spaceTop(emptySpace / (this.itemSlots.size - 1));
		}
		
		addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.ESCAPE || keycode == Keys.E) {
					ClientCore.setScreen(null);
					return true;
				}
				
				return false;
			}
		});
		pack();
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		
		if(ClientCore.input.pressingKey(Keys.Q) && getSelectedItem() != null) {
			ClientCore.getClient().send(new ItemDropRequest(false, selectionIndex, Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)));
		}
		
		
		if(ClientCore.input.pressingKey(Keys.RIGHT))
			moveFocusX(1);
		if(ClientCore.input.pressingKey(Keys.LEFT))
			moveFocusX(-1);
		if(ClientCore.input.pressingKey(Keys.UP))
			moveFocusY(-1);
		if(ClientCore.input.pressingKey(Keys.DOWN))
			moveFocusY(1);
	}
	
	private void resetSlot(int idx) {
		ItemSlot slot = itemSlots.get(idx);
		slot.setItem(null)
			.setCount(1)
			.setBackground(new ColorBackground(slot, slotBackgroundColor));
		invGroup.invalidateHierarchy();
	}
	
	@Nullable
	Item getSelectedItem() { return itemSlots.get(selectionIndex).getItem(); }
	
	void setHotbarHighlight(Item item, boolean set) {
		for(ItemSlot slot: itemSlots) {
			if(slot.getItem().equals(item)) {
				setHotbarHighlight(slot, set);
				return;
			}
		}
	}
	void setHotbarHighlight(boolean set) { setHotbarHighlight(itemSlots.get(selectionIndex), set); }
	private void setHotbarHighlight(ItemSlot slot, boolean set) {
		if(set && slot.getItem() == null) return; // don't do it
		slot.setBackground(new ColorBackground(slot, set ? hotbarBackground : slotBackgroundColor));
	}
	
	private void moveFocusX(int amt) { moveFocus(true, amt); }
	private void moveFocusY(int amt) { moveFocus(false, amt); }
	private void moveFocus(boolean x, int amt) {
		int xPos = selectionIndex / cellsPerColumn;
		int yPos = selectionIndex % cellsPerColumn;
		int newPos;
		do {
			if(x)
				xPos = MyUtils.mod(xPos+amt, numColumns);
			else
				yPos = MyUtils.mod(yPos+amt, cellsPerColumn);
			newPos = xPos * cellsPerColumn + yPos;
		} while(newPos >= itemSlots.size);
		moveFocus(newPos);
	}
	
	public void moveFocus(int index) {
		index %= itemSlots.size;
		selectionIndex = index;
	}
	
	private class PageCounter extends Table {
		
		public PageCounter() {
			VisLabel page = new VisLabel("1 / 2");
			add(page).center();
		}
		
	}
}
