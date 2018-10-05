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
	
	private final Inventory playerInv;
	
	private final CraftableItem[] list;
	private final ItemSelectionTable table;
	
	private final ItemStackSlot invCount = new ItemStackSlot(true, null, 0, null) {
		@Override protected boolean showCount() { return getItem() != null; }
	};
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
		private int index;
		CraftableRecipe(int arrayIndex, Recipe r, Inventory inv) {
			this.index = arrayIndex;
			this.recipe = r;
			canCraft = r.canCraft(inv);
		}
	}
	
	public CraftingScreen(Recipe[] recipes, Inventory playerInventory) {
		super(false);
		this.playerInv = playerInventory;
		
		CraftableRecipe[] recipeCache = new CraftableRecipe[recipes.length];
		for(int i = 0; i < recipeCache.length; i++)
			recipeCache[i] = new CraftableRecipe(i, recipes[i], playerInventory);
		
		Arrays.sort(recipeCache, (r1, r2) -> {
			if(r1.canCraft == r2.canCraft) return 0;
			if(r1.canCraft) return -1;
			return 1;
		});
		
		list = new CraftableItem[recipeCache.length];
		for(int i = 0; i < list.length; i++) {
			CraftableItem craftEntry = new CraftableItem(recipeCache[i].recipe, i, recipeCache[i].index);
			list[i] = craftEntry;
			craftEntry.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent e, float x, float y) {
					craft(craftEntry);
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
					craft(list[table.getSelection()]);
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
	}
	
	private void craft(CraftableItem item) {
		if(item.recipe.tryCraft(playerInv) != null)
			refreshCanCraft();
		
		// tell server about the attempt
		ClientCore.getClient().send(new CraftRequest(item.getRecipeIndex()));
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
		private final int recipeIndex;
		
		private CraftableItem(Recipe recipe, int idx, int recipeIndex) {
			super(idx, true, recipe.getResult().item, recipe.getResult().count);
			this.recipe = recipe;
			this.recipeIndex = recipeIndex;
			canCraft = recipe.canCraft(playerInv);
		}
		
		@Override
		public Color getTextColor() { return canCraft ? Color.WHITE : Color.RED; }
		
		public int getRecipeIndex() { return recipeIndex; }
	}
}
