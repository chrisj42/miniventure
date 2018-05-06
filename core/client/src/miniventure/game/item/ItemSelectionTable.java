package miniventure.game.item;

import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
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
	
	private static final float SLOT_WIDTH = Item.ICON_SIZE*2, SLOT_HEIGHT = RenderableListItem.MAX_HEIGHT;
	
	private final RenderableListItem[] listItems;
	//private final VisLabel selectedItemLabel;
	
	//private final Actor[][] tableEntries;
	
	private int selectionIndex = 0;
	
	public ItemSelectionTable(RenderableListItem[] listItems) {
		this(listItems, listItems.length);
	}
	public ItemSelectionTable(RenderableListItem[] listItems, int slots) {
		this(listItems, slots, Gdx.graphics.getHeight());
	}
	public ItemSelectionTable(RenderableListItem[] listItems, float maxHeight) {
		this(listItems, listItems.length, maxHeight);
	}
	public ItemSelectionTable(RenderableListItem[] listItems, int slots, float maxHeight) {
		super(5);
		//super(GameCore.getSkin());
		
		// configure the height to be something that equalizes the slots in as few columns as possible.
		float cellHeight = SLOT_HEIGHT + getSpacing();
		float maxPerColumn = (int) (maxHeight / cellHeight);
		float numColumns = MathUtils.ceil(slots / maxPerColumn);
		int cellsPerColumn = MathUtils.ceil(slots / numColumns);
		
		setHeight(cellHeight * cellsPerColumn);
		
		this.listItems = listItems;
		
		setDebug(true);
		/*
			Have a text label at the top that displays the name of the currently selected item.
			
			the items are laid out in the table column by column, left to right.
			A slot count may be specified in addition to the renderableitems themselves.
			
			sorting can be manual, or through a couple presets.
			
		 */
		//int numColumns = (int) (maxWidth / SLOT_WIDTH);
		//int numRows = MathUtils.ceilPositive(listItems.length*1f / numColumns);
		//tableEntries = new Actor[numRows][];
		
		//selectedItemLabel = new VisLabel("(no item)");
		
		//int rowCount = 0, total = 0;
		
		// first row is different
		//addActor(selectedItemLabel);
		//row().colspan(numColumns);
		
		/*while(total < slots) {
			//if(rowCount == 0)
			//	tableEntries[getRows()] = new Actor[Math.min(listItems.length - total, numColumns)];
			
			add(total < listItems.length ? listItems[total] : new EmptySlot());
			//tableEntries[getRows()][rowCount] = c.getActor();
			rowCount++; total++;
			if((rowCount+1) * SLOT_WIDTH > maxWidth)
				row().size(SLOT_WIDTH, SLOT_HEIGHT).space(10).uniform();
		}*/
		for(int i = 0; i < slots; i++) {
			if(i >= listItems.length)
				addActor(new EmptySlot());
			else
				addActor(listItems[i]);
		}
		
		for(RenderableListItem item: listItems)
			item.setTable(this);
		
		pack();
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), .2f, .4f, 1f, parentAlpha, batch);
		super.draw(batch, parentAlpha);
	}
	
	public RenderableListItem getSelected() { return listItems.length == 0 ? null : listItems[selectionIndex]; }
	
	public void moveFocus(int xd, int yd) {
		int newSelection = selectionIndex - yd;
		if(newSelection < 0)
			newSelection = listItems.length - ((-newSelection) % listItems.length);
		newSelection = newSelection % listItems.length;
		moveFocus(newSelection);
	}
	
	public void moveFocus(int index) {
		index %= listItems.length;
		Stage stage = getStage();
		if(stage != null)
			stage.setKeyboardFocus(listItems[index]);
		selectionIndex = index;
	}
	
	private static class EmptySlot extends Widget {
		public EmptySlot() {
			
		}
		
		@Override public float getPrefWidth() { return SLOT_WIDTH; }
		@Override public float getPrefHeight() { return SLOT_HEIGHT; }
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			super.draw(batch, parentAlpha);
			MyUtils.fillRect(getX(), getY(), SLOT_WIDTH, SLOT_HEIGHT, Color.TEAL, batch);
		}
	}
}
