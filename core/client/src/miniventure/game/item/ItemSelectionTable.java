package miniventure.game.item;

import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.kotcrab.vis.ui.layout.VerticalFlowGroup;

public class ItemSelectionTable extends VerticalFlowGroup {
	
	/*
		This will be used both for crafting screens and inventory screens. It should ideally take only half the screen horizontally at most, so that a second inventory screen can be placed next to it, for transfers between chests.
		
		While an inventory screen will additionally display a header section with the total weight and the total space used, a crafting screen will display a couple extra panels for the crafting requirements.
		
		About crafting screens... if we're going to have item crafting and tile crafting, then there's going to have to be some edits here. Or... well actually, maybe not..?
		I think crafting should consist of depositing materials. If you can hold all the materials at once, then that's good, but you don't have to do that. There will be a workbench after all, and you can deposit the materials there until you've got all of them. Then you can craft. Should crafting take some time? Perhaps. probably.
		Tile crafting consists of a menu that once selected, you can choose where to place the new tile, and then deposit materials there for crafting.
		
		So for this class, it will need to know this about its dimensions:
			- 
		
		Actually... should I make a class that is specifically one column? I actually like that idea... it won't have a background, just the background for the individual items, and empty cells.
	 */
	
	private final ItemSlot[] itemSlots;
	private static final Color background = Color.TEAL;
	private static final Color selectionColor = new Color(.8f, .8f, .8f, 0.5f);
	
	private int selectionIndex = 0;
	private final int cellsPerColumn, numColumns;
	
	public ItemSelectionTable(ItemSlot[] itemSlots, float maxHeight) {
		super(5);
		
		// configure the height to be something that equalizes the slots in as few columns as possible.
		float cellHeight = ItemSlot.HEIGHT + getSpacing();
		float maxPerColumn = Math.max(1, (int) (maxHeight / cellHeight));
		numColumns = Math.max(1, MathUtils.ceil(itemSlots.length / maxPerColumn));
		cellsPerColumn = Math.max(1, MathUtils.ceil(itemSlots.length / (float)numColumns));
		
		setHeight(Math.max(getSpacing(), cellHeight * cellsPerColumn - getSpacing()));
		
		if(itemSlots.length > 1) {
			// need to adjust spacing to make sure there isn't a large space at the bottom
			float spaceTaken = Math.min(itemSlots.length, maxPerColumn) * Item.ICON_SIZE;
			float emptySpace = getHeight() - spaceTaken;
			setSpacing(emptySpace / (itemSlots.length-1));
		}
		
		this.itemSlots = itemSlots;
		
		//setDebug(true);
		/*
			Have a text label at the top that displays the name of the currently selected item.
			
			the items are laid out in the table column by column, left to right.
			A slot count may be specified in addition to the renderableitems themselves.
			
			sorting can be manual, or through a couple presets.
			
		 */
		
		for(ItemSlot item: itemSlots)
			addActor(item);
		refresh();
		
		addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				switch(keycode) {
					case Keys.RIGHT: moveFocusX(1); return true;
					case Keys.LEFT: moveFocusX(-1); return true;
					case Keys.UP: moveFocusY(-1); return true;
					case Keys.DOWN: moveFocusY(1); return true;
					
					default: return false;
				}
			}
			
			/*@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				if(fromActor instanceof ItemSlot)
					selectionIndex = ((ItemSlot)fromActor).getSlotIndex();
			}*/
		});
		
		pack();
	}
	
	public void onUpdate() {}
	
	public void setSelection(int index) {
		selectionIndex = index;
	}
	
	public int getSelection() { return selectionIndex; }
	
	void updateSelected(Item newItem) { update(selectionIndex, newItem); }
	void update(int idx, Item newItem) {
		itemSlots[idx].setItem(newItem);
		refresh();
		invalidateHierarchy();
		pack();
		onUpdate();
	}
	
	void refresh() {
		float maxWidth = 0;
		for(ItemSlot item: itemSlots) {
			maxWidth = Math.max(maxWidth, item.getPrefWidth());
		}
		for(ItemSlot item: itemSlots) {
			item.setWidth(maxWidth);
		}
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		float x = getX();
		float y = getY();
		float width = getWidth();
		float height = getHeight();
		if(getParent() instanceof Container) {
			Container c = (Container) getParent();
			x -= c.getPadLeft();
			width += c.getPadX();
			y -= c.getPadBottom();
			height += c.getPadY();
		}
		MyUtils.fillRect(x, y, width, height, background, parentAlpha, batch);
		super.draw(batch, parentAlpha);
	}
	
	// highlight selected item
	@Override
	protected void drawChildren(Batch batch, float parentAlpha) {
		super.drawChildren(batch, parentAlpha);
		if(itemSlots.length > 0) {
			ItemSlot slot = itemSlots[selectionIndex];
			MyUtils.fillRect(slot.getX(), slot.getY(), slot.getWidth(), slot.getHeight(), selectionColor, parentAlpha, batch);
		}
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
		} while(newPos >= itemSlots.length);
		moveFocus(newPos);
	}
	
	public void moveFocus(int index) {
		index %= itemSlots.length;
		setSelection(index);
	}
}
