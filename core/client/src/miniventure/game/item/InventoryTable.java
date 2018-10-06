package miniventure.game.item;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.utils.Array;

public class InventoryTable extends HorizontalGroup {
	
	private static final Color tableBackground = Color.TEAL;
	private static final Color slotBackground = Color.TEAL.cpy().lerp(Color.WHITE, .1f);
	private static final Color selectionColor = new Color(.8f, .8f, .8f, 0.5f);
	
	
	private final Array<ItemSlot> itemSlots = new Array<>(ItemSlot.class);
	
	private int selectionIndex = 0;
	private int cellsPerColumn, numColumns;
	
	public InventoryTable(Inventory inv, boolean fillBarLeft) {
		/*
			This will be used for the personal inventory screen,
			and when displaying other inventories alongside such as a chest.
			
			besides the inventory and which side the fill bar goes on, other
			differences in the behavior of personal and other inventory screens
			are (small ones that are almost the same):
				- if items can be selected (double)
				- if hotbar setting is enabled (personal)
				- items can be dropped from either inventory
			
			so it looks like the key events on the items will be open to the
			screen classes. this will just allow you to highlight items and
			drop them if desired.
			
			I like the idea of this taking up 3/4 of the screen height, and
			maybe 2/5 of the screen width, at most. Though I suppose width is
			variable.
		 */
		
		
	}
	
}
