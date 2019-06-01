package miniventure.game.item;

import java.util.Objects;

import miniventure.game.client.ClientCore;
import miniventure.game.client.FontStyle;
import miniventure.game.item.CraftingScreen.ClientRecipe;
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
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

import org.jetbrains.annotations.Nullable;

public class InventoryScreen extends MenuScreen {
	
	private static final int MAX_ITEMS_PER_ROW = 9;
	
	private ClientInventory inventory;
	
	private Table mainGroup;
	
	private ProgressBar fillBar;
	
	private Table slotTable;
	
	private float lastAmt;
	private String lastItem;
	
	public InventoryScreen(Camera camera, Batch batch) {
		super(false, new DiscreteViewport(camera), batch);
		mainGroup = useTable(Align.left, false);
		mainGroup.defaults().padBottom(2f);
		addMainGroup(mainGroup, RelPos.BOTTOM_LEFT);
		
		// make crafting screen button
		VisTextButton craftBtn = makeButton("Craft", () -> {});
		craftBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(ClientCore.getScreen() instanceof CraftingScreen) {
					ClientCore.setScreen(null);
					craftBtn.focusLost();
				}
				else
					ClientCore.setScreen(new CraftingScreen());
			}
		});
		craftBtn.setBackground(new BaseDrawable(craftBtn.getBackground()) {
			@Override
			public void draw(Batch batch, float x, float y, float width, float height) {
				Color prev = batch.getColor();
				batch.setColor(new Color(1f, 1f, 1f, .4f));
				super.draw(batch, x, y, width, height);
				batch.setColor(prev);
			}
		});
		
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
		
		// create the bar at the bottom with the space and held item info
		
		HorizontalGroup infoBar = new HorizontalGroup();
		
		infoBar.addActor(craftBtn);
		infoBar.addActor(makeLabel("    Inventory Space:   ", false));
		infoBar.addActor(fillBar);
		
		// create the label such that it refreshes automatically when the selected item changes
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
		// reapply some default behavior from makeLabel
		handItemLabel.setAlignment(Align.left, Align.left);
		registerLabel(FontStyle.Default, handItemLabel);
		
		// add some left padding to space it from the fill bar
		Container<VisLabel> box = new Container<>(handItemLabel);
		box.padLeft(20f);
		infoBar.addActor(box);
		
		mainGroup.add(infoBar).pad(5f).align(Align.left); // add the group to the table with some layout settings
		
		// set focus to the table so that scrolling and pressing the number keys actually has an effect
		setKeyboardFocus(slotTable);
		setScrollFocus(slotTable);
	}
	
	// called on creation of a ClientPlayer, since this screen is created with the GameScreen, before the player exists.
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
					() -> {
						ClientRecipe recipe = inventory.getCurrentBlueprint();
						if(recipe == null)
							return inventory.getSelection() == invi;
						return recipe.needsItem(inventory.getItem(invi));
					}
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
}
