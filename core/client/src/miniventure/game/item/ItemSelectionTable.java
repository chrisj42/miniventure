package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;

public class ItemSelectionTable<T extends RenderableListItem> extends Table {
	
	private static final float SLOT_WIDTH = Item.ICON_SIZE*2, SLOT_HEIGHT = RenderableListItem.MAX_HEIGHT;
	
	@FunctionalInterface
	interface ItemSelectAction<T extends RenderableListItem> {
		void onSelect(T item);
	}
	
	private final T[] listItems;
	private final ItemSelectAction<T> selectAction;
	private final VisLabel selectedItemLabel;
	
	//private final Actor[][] tableEntries;
	
	private int selectionIndex = 0;
	
	public ItemSelectionTable(T[] listItems, ItemSelectAction<T> selectAction) {
		this(listItems, listItems.length, selectAction);
	}
	public ItemSelectionTable(T[] listItems, int slots, ItemSelectAction<T> selectAction) {
		this(listItems, slots, Gdx.graphics.getWidth(), selectAction);
	}
	public ItemSelectionTable(T[] listItems, float maxWidth, ItemSelectAction<T> selectAction) {
		this(listItems, listItems.length, maxWidth, selectAction);
	}
	public ItemSelectionTable(T[] listItems, int slots, float maxWidth, ItemSelectAction<T> selectAction) {
		super(GameCore.getSkin());
		
		this.listItems = listItems;
		this.selectAction = selectAction;
		
		setDebug(true);
		/*
			Have a text label at the top that displays the name of the currently selected item.
			
			the items are laid out in the table column by column, left to right.
			A slot count may be specified in addition to the renderableitems themselves.
			
			sorting can be manual, or through a couple presets.
			
		 */
		int numColumns = (int) (maxWidth / SLOT_WIDTH);
		int numRows = MathUtils.ceilPositive(listItems.length*1f / numColumns);
		//tableEntries = new Actor[numRows][];
		
		selectedItemLabel = new VisLabel("(no item)");
		
		int rowCount = 0, total = 0;
		
		// first row is different
		add(selectedItemLabel);
		row().colspan(numColumns);
		
		while(total < slots) {
			//if(rowCount == 0)
			//	tableEntries[getRows()] = new Actor[Math.min(listItems.length - total, numColumns)];
			
			add(total < listItems.length ? listItems[total] : new EmptySlot());
			//tableEntries[getRows()][rowCount] = c.getActor();
			rowCount++; total++;
			if((rowCount+1) * SLOT_WIDTH > maxWidth)
				row().size(SLOT_WIDTH, SLOT_HEIGHT).space(10).uniform();
		}
	}
	
	public T getSelected() { return listItems.length == 0 ? null : listItems[selectionIndex]; }
	
	public void moveFocus(int xd, int yd) {
		int newSelection = selectionIndex + xd + yd * getColumns();
		if(newSelection < 0)
			newSelection = listItems.length - ((-newSelection) % listItems.length);
		newSelection = newSelection % listItems.length;
		moveFocus(newSelection);
	}
	
	public void moveFocus(int index) {
		index %= listItems.length;
		getStage().setKeyboardFocus(listItems[index]);
		selectionIndex = index;
	}
	
	private static class EmptySlot extends Widget {
		public EmptySlot() {
			
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			super.draw(batch, parentAlpha);
			MyUtils.fillRect(getX(), getY(), SLOT_WIDTH, SLOT_HEIGHT, Color.TEAL, batch);
		}
	}
}
