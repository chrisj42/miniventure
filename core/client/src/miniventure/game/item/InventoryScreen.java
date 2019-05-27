package miniventure.game.item;

import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.util.DiscreteViewport;
import miniventure.game.util.RelPos;
import miniventure.game.world.entity.mob.player.Player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;

import org.jetbrains.annotations.Nullable;

public class InventoryScreen extends MenuScreen {
	
	static final Color slotBackground = Color.TEAL.cpy().lerp(Color.WHITE, .1f);
	static final Color tableBackground = Color.TEAL;
	private static final Color highlightBackground = Color.TEAL.cpy().lerp(Color.YELLOW, .25f);
	
	private static final int MAX_ITEMS_PER_ROW = 8;
	
	/*
		general system:
		
		- client normally has no reference to inventory at all, only hotbar
			- server sends hotbar updates as necessary
		- when inventory screen opened, client requests inventory data
			- server sends back InventoryUpdate with inventory data and hotbar indices
		
	 */
	
	// private boolean requested = false;
	// private boolean fin = false;
	
	private ClientInventory inventory;
	
	// private SlotData[] slots = null;
	// private final HashMap<Integer, SlotData> slotsById = new HashMap<>();
	// private int[] hotbar = null; // holds slot IDs
	
	private VerticalGroup mainGroup;
	
	// private int spaceUsed = 0;
	private ProgressBar fillBar;
	
	// private ScrollPane scrollPane;
	private Table slotTable;
	
	// private int selection;
	
	public InventoryScreen(Camera camera, Batch batch) {
		super(false, new DiscreteViewport(camera), batch);
		// this.inventory = inv;
		// setDebugAll(true);
		mainGroup = useVGroup(2f, Align.left, false);
		addMainGroup(mainGroup, RelPos.BOTTOM_LEFT);
		
		fillBar = new ProgressBar(0, 1, .01f, false, VisUI.getSkin());
		
		slotTable = new Table(VisUI.getSkin())/* {
			@Override
			protected void drawChildren(Batch batch, float parentAlpha) {
				boolean done = false;
				synchronized (InventoryScreen.this) {
					if(slots != null) {
						done = true;
						if(slots.size() > 0)
							slots.get(selection).slot.setSelected(true);
						super.drawChildren(batch, parentAlpha);
						if(slots.size() > 0) slots.get(selection).slot.setSelected(false);
					}
				}
				if(!done)
					super.drawChildren(batch, parentAlpha);
			}
		}*/;
		slotTable.defaults().fillX().minSize(Item.ICON_SIZE * 1.5f, ItemSlot.HEIGHT * 1.5f);
		// slotTable.pad(10f);
		// slotTable.background(new ColorBackground(slotTable, tableBackground));
		// slotTable.add(makeLabel("Waiting for inventory data...", FontStyle.KeepSize, false));
		
		// int slotsLeft = Player.INV_SIZE;
		
		slotTable.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(inventory == null)
					return false;
				//noinspection SynchronizeOnThis
				synchronized (InventoryScreen.this) {
					for(int i = Keys.NUM_1; i <= Keys.NUM_9; i++) {
						if(keycode == i) {
							inventory.setSelection(i - Keys.NUM_1);
							return true;
						}
					}
				}
				
				return false;
			}
			
			@Override
			public boolean scrolled(InputEvent event, float x, float y, int amount) {
				inventory.setSelection(inventory.getSelection() + amount);
				return true;
			}
		});
		
		/*scrollPane = new ScrollPane(slotTable, VisUI.getSkin()) {
			@Override
			public float getPrefHeight() {
				return InventoryScreen.this.getHeight()*2/3;
			}
		};
		
		// scrollPane.setHeight(getHeight()*2/3);
		scrollPane.setScrollingDisabled(true, false);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setScrollbarsOnTop(false);*/
		
		//mainGroup.addActor(fillBar);
		mainGroup.addActor(slotTable);
		// mainGroup.addActor(scrollPane);
		
		/*mainGroup.setVisible(false);
		Timer t = new Timer(200, e -> mainGroup.setVisible(true));
		t.setRepeats(false);
		t.start();*/
		
		
		
		setKeyboardFocus(slotTable);
		setScrollFocus(slotTable);
	}
	
	public void setInventory(ClientInventory inv) {
		inventory = inv;
		slotTable.clearChildren();
		
		int rows = (int)Math.ceil(Player.INV_SIZE / (float)MAX_ITEMS_PER_ROW);
		int cols = Math.min(MAX_ITEMS_PER_ROW, Player.INV_SIZE);
		ItemSlot[][] allSlots = new ItemSlot[rows][cols];
		
		int idx = 0;
		int fullSlots = inventory.getSlotsTaken();
		for(int r = 0; r < rows && idx < Player.INV_SIZE; r++) {
			for(int c = 0; c < cols && idx < Player.INV_SIZE; c++) {
				ItemStack stack = idx >= fullSlots ? null : inventory.getItemStack(idx);
				Item item = stack == null ? null : stack.item;
				int count = stack == null ? 0 : stack.count;
				
				final int invi = idx;
				allSlots[r][c] = new ItemSlot(false, item, count, new SlotBackground(
					() -> inventory.getSlotsTaken() > invi,
					() -> inventory.getSelection() == invi
				)) {
					@Override @Nullable
					public Item getItem() {
						if(inventory.getSlotsTaken() <= invi)
							return null;
						return inventory.getItem(invi);
					}
					
					@Override
					public int getCount() {
						if(inventory.getSlotsTaken() <= invi)
							return 0;
						return inventory.getItemStack(invi).count;
					}
				};
				
				idx++;
			}
		}
		
		// go backward through the rows of the array and add them to the slot table
		for(int r = allSlots.length - 1; r >= 0; r--) {
			// forward through columns
			for(int c = 0; c < allSlots[r].length; c++) {
				// skip rest of row if one is null
				if(allSlots[r][c] == null)
					break;
				
				slotTable.add(allSlots[r][c]);
			}
			
			slotTable.row();
		}
	}
	
	void setFillPercent(float amt) {
		fillBar.setValue(amt);
	}
}
