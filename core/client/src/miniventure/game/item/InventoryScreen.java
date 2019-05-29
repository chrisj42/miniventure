package miniventure.game.item;

import java.util.Objects;

import miniventure.game.client.ClientCore;
import miniventure.game.client.FontStyle;
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
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;

import org.jetbrains.annotations.Nullable;

public class InventoryScreen extends MenuScreen {
	
	// static final Color slotBackground = Color.TEAL.cpy().lerp(Color.WHITE, .1f);
	// static final Color tableBackground = Color.TEAL;
	// private static final Color highlightBackground = Color.TEAL.cpy().lerp(Color.YELLOW, .25f);
	
	private static final int MAX_ITEMS_PER_ROW = 9;
	
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
	
	private Table mainGroup;
	
	// private int spaceUsed = 0;
	private ProgressBar fillBar;
	
	// private ScrollPane scrollPane;
	private Table slotTable;
	
	private float lastAmt;
	private String lastItem;
	
	public InventoryScreen(Camera camera, Batch batch) {
		super(false, new DiscreteViewport(camera), batch);
		mainGroup = useTable(Align.left, false);
		mainGroup.defaults().padBottom(2f);
		addMainGroup(mainGroup, RelPos.BOTTOM_LEFT);
		
		fillBar = new ProgressBar(0, 1, .01f, false, VisUI.getSkin()) {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				float amt = inventory == null ? 0 : inventory.getPercentFilled();
				if(amt != lastAmt)
					setValue(amt);
				lastAmt = amt;
				super.draw(batch, parentAlpha);
			}
		};
		
		slotTable = new Table(VisUI.getSkin());
		slotTable.defaults().fillX().minSize(Item.ICON_SIZE * ItemIcon.UI_SCALE, ItemSlot.HEIGHT * ItemIcon.UI_SCALE);
		
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
				if(inventory == null)
					return false;
				inventory.setSelection(inventory.getSelection() + amount);
				return true;
			}
		});
		
		
		mainGroup.add(slotTable).row();
		
		HorizontalGroup infoBar = new HorizontalGroup();
		
		infoBar.addActor(makeLabel("Inventory Space:   ", false));
		infoBar.addActor(fillBar);
		
		VisLabel handItemLabel = new VisLabel("Held Item", new LabelStyle(ClientCore.getFont(FontStyle.Default), null)) {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				ItemStack stack = inventory == null ? null : inventory.getSelectedItem();
				Item item = stack == null ? null : stack.item;
				String name = item == null ? null : item.getName();
				if(!Objects.equals(name, lastItem)) {
					setText("Held Item: " + (name == null ? "Hand" : name));
					lastItem = name;
				}
				super.draw(batch, parentAlpha);
			}
		};
		handItemLabel.setAlignment(Align.left, Align.left);
		registerLabel(FontStyle.Default, handItemLabel);
		
		Container<VisLabel> box = new Container<>(handItemLabel);
		box.padLeft(20f);
		infoBar.addActor(box);
		
		mainGroup.add(infoBar).pad(5f).align(Align.left);
		
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
