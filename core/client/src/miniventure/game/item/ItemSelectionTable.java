package miniventure.game.item;

import miniventure.game.util.MyUtils;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
	
	//private static final float SLOT_WIDTH = Item.ICON_SIZE*2, SLOT_HEIGHT = RenderableListItem.MAX_HEIGHT;
	
	private final RenderableListItem[] listItems;
	//private final VisLabel selectedItemLabel;
	
	//private final Actor[][] tableEntries;
	
	private int selectionIndex = 0;
	private final int cellsPerColumn, numColumns;
	
	public ItemSelectionTable(RenderableListItem[] listItems, float maxHeight) {
		super(5);
		//super(GameCore.getSkin());
		//System.out.println("item selection table of "+ Arrays.toString(listItems));
		
		// configure the height to be something that equalizes the slots in as few columns as possible.
		float cellHeight = RenderableListItem.MAX_HEIGHT + getSpacing();
		float maxPerColumn = (int) (maxHeight / cellHeight);
		numColumns = Math.max(1, MathUtils.ceil(listItems.length / maxPerColumn));
		cellsPerColumn = Math.max(1, MathUtils.ceil(listItems.length / (float)numColumns));
		
		setHeight(cellHeight * cellsPerColumn);
		
		this.listItems = listItems;
		
		//setDebug(true);
		/*
			Have a text label at the top that displays the name of the currently selected item.
			
			the items are laid out in the table column by column, left to right.
			A slot count may be specified in addition to the renderableitems themselves.
			
			sorting can be manual, or through a couple presets.
			
		 */
		
		for(RenderableListItem listItem: listItems) {
			addActor(listItem);
			listItem.setTable(this);
		}
		
		pack();
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), .2f, .4f, 1f, parentAlpha, batch);
		super.draw(batch, parentAlpha);
	}
	
	//public RenderableListItem getSelected() { return listItems.length == 0 ? null : listItems[selectionIndex]; }
	
	public void moveFocusX(int amt) {
		int xPos = selectionIndex / cellsPerColumn;
		int yPos = selectionIndex % cellsPerColumn;
		int newPos;
		do {
			xPos = MyUtils.mod(xPos+amt, numColumns);
			newPos = xPos * cellsPerColumn + yPos;
		} while(newPos >= listItems.length);
		moveFocus(newPos);
	}
	public void moveFocusY(int amt) {
		int xPos = selectionIndex / cellsPerColumn;
		int yPos = selectionIndex % cellsPerColumn;
		int newPos;
		do {
			yPos = MyUtils.mod(yPos+amt, cellsPerColumn);
			newPos = xPos * cellsPerColumn + yPos;
		} while(newPos >= listItems.length);
		moveFocus(newPos);
	}
	
	public void moveFocus(int index) {
		index %= listItems.length;
		Stage stage = getStage();
		if(stage != null)
			stage.setKeyboardFocus(listItems[index]);
		selectionIndex = index;
	}
}
