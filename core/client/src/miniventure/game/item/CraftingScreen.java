package miniventure.game.item;

import java.util.Arrays;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.CraftRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class CraftingScreen extends MenuScreen {
	
	private static final Color background = new Color(.2f, .4f, 1f, 1);
	
	private final Recipe[] recipes;
	private final Inventory playerInv;
	
	private final CraftableItem[] list;
	private final ItemSelectionTable table;
	
	private final ItemStackSlot invCount = new ItemStackSlot(true, null, 0, null);
	private final VerticalGroup inInv = new OpaqueVerticalGroup(background);
	private final VerticalGroup costs = new OpaqueVerticalGroup(background);
	
	private static class OpaqueVerticalGroup extends VerticalGroup {
		private final Color color;
		
		public OpaqueVerticalGroup(Color color) {
			this.color = color;
			pad(5);
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), color, parentAlpha, batch);
			super.draw(batch, parentAlpha);
		}
	}
	
	private static class CraftableRecipe {
		private Recipe recipe;
		private boolean canCraft;
		CraftableRecipe(Recipe r, Inventory inv) {
			this.recipe = r;
			canCraft = r.canCraft(inv);
		}
	}
	
	public CraftingScreen(Recipe[] recipes, Inventory playerInventory) {
		this.recipes = recipes;
		this.playerInv = playerInventory;
		
		CraftableRecipe[] recipeCache = new CraftableRecipe[recipes.length];
		for(int i = 0; i < recipeCache.length; i++)
			recipeCache[i] = new CraftableRecipe(recipes[i], playerInventory);
		
		Arrays.sort(recipeCache, (r1, r2) -> {
			if(r1.canCraft == r2.canCraft) return 0;
			if(r1.canCraft) return -1;
			return 1;
		});
		
		list = new CraftableItem[recipeCache.length];
		for(int i = 0; i < list.length; i++) {
			CraftableItem craftEntry = new CraftableItem(recipeCache[i].recipe, i);
			list[i] = craftEntry;
			craftEntry.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent e, float x, float y) {
					craft(craftEntry.getSlotIndex());
				}
			});
			craftEntry.addListener(new InputListener() {
				@Override
				public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
					table.setSelection(craftEntry.getSlotIndex());
				}
			});
		}
		
		refreshCanCraft();
		
		table = new ItemSelectionTable(list, getHeight()) {
			@Override
			public void setSelection(int index) {
				super.setSelection(index);
				CraftingScreen.this.setHighlightedRecipe(list[index].recipe);
			}
		};
		table.addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.ENTER) {
					craft(table.getSelection());
					return true;
				}
				return false;
			}
		});
		addActor(table);
		
		inInv.columnAlign(Align.left);
		inInv.space(5);
		inInv.addActor(new Label("In inventory:", GameCore.getSkin()));
		inInv.addActor(invCount);
		addActor(inInv);
		
		costs.columnAlign(Align.left);
		costs.space(5);
		addActor(costs);
		
		
		table.setPosition(0, getHeight(), Align.topLeft);
		table.pack();
		
		getRoot().addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.Z || keycode == Keys.ESCAPE)
					ClientCore.setScreen(null);
				return true;
			}
		});
		
		setKeyboardFocus(table);
		//setKeyboardFocus(list.length == 0 ? getRoot() : list[0]);
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	private void craft(int index) {
		if(list[index].recipe.tryCraft(playerInv))
			refreshCanCraft();
		
		// tell server about the attempt
		ClientCore.getClient().send(new CraftRequest(index));
	}
	
	private void setHighlightedRecipe(Recipe recipe) {
		inInv.removeActor(invCount);
		invCount.setItem(recipe.getResult().item);
		invCount.setCount(playerInv.getCount(invCount.getItem()));
		inInv.addActor(invCount);
		
		costs.clearChildren();
		costs.addActor(new Label("Costs:", GameCore.getSkin()));
		for(ItemStack cost: recipe.getCosts())
			costs.addActor(new ItemStackSlot(true, cost.item, cost.count, null));
		
		inInv.invalidate();
		inInv.pack();
		costs.invalidate();
		costs.pack();
		
		inInv.setPosition(table.getPrefWidth() + 10, getHeight() - inInv.getHeight());
		costs.setPosition(table.getPrefWidth() + 10, getHeight() - inInv.getHeight() - 10 - costs.getHeight());
	}
	
	private void refreshCanCraft() {
		for(CraftableItem item: list)
			item.canCraft = item.recipe.canCraft(playerInv);
		if(invCount.getItem() != null)
			invCount.setCount(playerInv.getCount(invCount.getItem()));
	}
	
	private class CraftableItem extends ItemStackSlot {
		
		private final Recipe recipe;
		private boolean canCraft;
		
		private CraftableItem(Recipe recipe, int idx) {
			super(idx, true, recipe.getResult().item, recipe.getResult().count);
			this.recipe = recipe;
			canCraft = recipe.canCraft(playerInv);
			
			/*addListener(new FocusListener() {
				@Override
				public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
					if(focused)
						setHighlightedRecipe(recipe);
				}
			});*/
		}
		
		@Override
		public Color getTextColor() { return canCraft ? Color.WHITE : Color.RED; }
		
	}
	
	
	/*private class ItemStackDisplay extends Widget {
		private ItemStack item;
		private Color backgroundColor = new Color(.2f, .4f, 1f, 1f);
		private float width, height;
		
		public ItemStackDisplay() {}
		public ItemStackDisplay(@NotNull Item item, int count) {
			setItem(new ItemStack(item, count));
		}
		
		public void setItem(ItemStack item) {
			this.item = item;
			width = ItemRenderStrategy.FULL.getWidth(item.item) + 10;
			height = ItemRenderStrategy.FULL.getWidth(item.item) + 10;
			setSize(width, height);
		}
		
		@Override public float getPrefWidth() { return width; }
		@Override public float getPrefHeight() { return height; }
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			if(backgroundColor != null)
				MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), backgroundColor, batch);
			item.item.drawItem(item.count, batch, getX()+5, getY()+5);
		}
	}*/
}
