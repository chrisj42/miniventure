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
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;

import org.jetbrains.annotations.NotNull;

public class CraftingScreen extends MenuScreen {
	
	private static final Color background = new Color(.2f, .4f, 1f, 1);
	
	private final Recipe[] recipes;
	private final Inventory playerInv;
	
	private final CraftableItem[] list;
	private final ItemSelectionTable table;
	
	private final ItemStackDisplay invCount = new ItemStackDisplay();
	private final VerticalGroup inInv = new OpaqueVerticalGroup(background);
	private final VerticalGroup costs = new OpaqueVerticalGroup(background);
	
	private static class OpaqueVerticalGroup extends VerticalGroup {
		private final Color color;
		
		public OpaqueVerticalGroup(Color color) {
			this.color = color;
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), color.cpy().mul(1, 1, 1, parentAlpha), batch);
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
			list[i] = new CraftableItem(recipeCache[i].recipe, i);
		}
		
		refreshCanCraft();
		
		table = new ItemSelectionTable(list);
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
				if(keycode == Keys.Z)
					ClientCore.setScreen(null);
				return true;
			}
		});
		
		setKeyboardFocus(list.length == 0 ? getRoot() : list[0]);
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	private void setHighlightedRecipe(Recipe recipe) {
		inInv.removeActor(invCount);
		invCount.setItem(new ItemStack(recipe.getResult().item, playerInv.countItem(recipe.getResult().item)));
		inInv.addActor(invCount);
		
		costs.clearChildren();
		costs.addActor(new Label("Costs:", GameCore.getSkin()));
		for(ItemStack cost: recipe.getCosts())
			costs.addActor(new ItemStackDisplay(cost.item, cost.count));
		
		inInv.pack();
		costs.pack();
		
		inInv.setPosition(table.getPrefWidth() + 10, getHeight() - inInv.getHeight());
		costs.setPosition(table.getPrefWidth() + 10, getHeight() - inInv.getHeight() - 10 - costs.getHeight());
	}
	
	private void refreshCanCraft() {
		for(CraftableItem item: list)
			item.canCraft = item.recipe.canCraft(playerInv);
		if(invCount.item != null)
			invCount.setItem(new ItemStack(invCount.item.item, playerInv.countItem(invCount.item.item)));
	}
	
	private class CraftableItem extends RenderableListItem {
		
		private final Recipe recipe;
		private boolean canCraft;
		
		private CraftableItem(Recipe recipe, int idx) {
			super(recipe.getResult().item, idx);
			this.recipe = recipe;
			canCraft = recipe.canCraft(playerInv);
			
			addListener(new FocusListener() {
				@Override
				public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
					if(focused)
						setHighlightedRecipe(recipe);
				}
			});
		}
		
		@Override
		void keyDown(InputEvent event, int keycode) {
			if(keycode == Keys.ESCAPE || keycode == Keys.Z)
				ClientCore.setScreen(null);
		}
		
		@Override
		void select(int idx) {
			if(recipe.tryCraft(playerInv))
				refreshCanCraft();
			
			// tell server about the attempt
			ClientCore.getClient().send(new CraftRequest(idx));
		}
		
		@Override
		protected int getStackSize(int idx) {
			return recipe.getResult().count;
		}
		
		@Override
		protected Color getItemTextColor() {
			return canCraft ? Color.WHITE : Color.RED;
		}
		
		/*@Override
		public void draw(Batch batch, float parentAlpha) {
			System.out.println("rendering crafting item, width = " + getWidth());
			super.draw(batch, parentAlpha);
		}*/
	}
	
	
	private class ItemStackDisplay extends Widget {
		private ItemStack item;
		private Color backgroundColor = new Color(.2f, .4f, 1f, 1f);
		private float width, height;
		
		public ItemStackDisplay() {}
		public ItemStackDisplay(@NotNull Item item, int count) {
			setItem(new ItemStack(item, count));
		}
		
		public void setItem(ItemStack item) {
			this.item = item;
			width = item.item.getRenderWidth() + 10;
			height = item.item.getRenderHeight() + 10;
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
	}
}
