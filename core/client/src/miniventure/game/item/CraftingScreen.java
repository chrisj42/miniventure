package miniventure.game.item;

import java.util.Arrays;

import miniventure.game.GameCore;
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
	
	private final Recipe[] recipes;
	private final Inventory playerInv;
	
	private final CraftableItem[] list;
	
	private final ItemStackDisplay invCount = new ItemStackDisplay();
	private final VerticalGroup inInv = new VerticalGroup();
	private final VerticalGroup costs = new VerticalGroup();
	
	public CraftingScreen(Recipe[] recipes, Inventory playerInventory) {
		this.recipes = recipes;
		this.playerInv = playerInventory;
		
		list = new CraftableItem[recipes.length];
		for(int i = 0; i < list.length; i++) {
			list[i] = new CraftableItem(recipes[i], i);
			vGroup.addActor(list[i]);
		}
		
		inInv.setOrigin(Align.top);
		inInv.columnAlign(Align.left);
		inInv.space(5);
		inInv.addActor(new Label("In inventory:", GameCore.getSkin()));
		inInv.addActor(invCount);
		addActor(inInv);
		
		costs.setOrigin(Align.top);
		costs.columnAlign(Align.left);
		costs.space(5);
		addActor(costs);
		
		refreshCanCraft();
		
		Arrays.sort(list, (r1, r2) -> {
			if(r1.canCraft == r2.canCraft) return 0;
			if(r1.canCraft) return -1;
			return 1;
		});
		
		vGroup.columnAlign(Align.left);
		vGroup.setPosition(0, getHeight()-vGroup.getPrefHeight());
		vGroup.pack();
		
		getRoot().addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.Z)
					ClientCore.setScreen(null);
				return true;
			}
		});
		
		setKeyboardFocus(vGroup.getChildren().size > 0 ? vGroup.getChildren().get(0) : getRoot());
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	@Override
	protected void drawTable(Batch batch, float parentAlpha) {
		MyUtils.fillRect(vGroup.getX(), vGroup.getY(), vGroup.getWidth(), vGroup.getHeight(), .2f, .4f, 1f, parentAlpha, batch);
		MyUtils.fillRect(costs.getX(), costs.getY(), costs.getPrefWidth(), costs.getPrefHeight(), .2f, .4f, 1f, parentAlpha, batch);
		MyUtils.fillRect(inInv.getX(), inInv.getY(), inInv.getPrefWidth(), inInv.getPrefHeight(), .2f, .4f, 1f, parentAlpha, batch);
	}
	
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
		
		inInv.setPosition(vGroup.getPrefWidth() + 10, getHeight() - inInv.getHeight());
		costs.setPosition(vGroup.getPrefWidth() + 10, getHeight() - inInv.getHeight() - 10 - costs.getHeight());
		
		/*System.out.println("menu bounds: " + new Rectangle(vGroup.getX(), vGroup.getY(), vGroup.getPrefWidth(), vGroup.getPrefHeight()));
		System.out.println("count bounds: " + new Rectangle(inInv.getX(), inInv.getY(), inInv.getPrefWidth(), inInv.getPrefHeight()));
		System.out.println("costs bounds: " + new Rectangle(costs.getX(), costs.getY(), costs.getPrefWidth(), costs.getPrefHeight()));*/
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
			super(recipe.getResult().item, idx, vGroup);
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
		
		public ItemStackDisplay() {}
		public ItemStackDisplay(@NotNull Item item, int count) {
			this.item = new ItemStack(item, count);
		}
		
		public void setItem(ItemStack item) {
			this.item = item;
			setSize(item.item.getRenderWidth() + 10, item.item.getRenderHeight() + 10);
		}
		
		@Override public float getPrefWidth() { return item.item.getRenderWidth(); }
		@Override public float getPrefHeight() { return item.item.getRenderHeight(); }
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			if(backgroundColor != null)
				MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), backgroundColor, batch);
			item.item.drawItem(item.count, batch, getX()+5, getY()+5);
		}
	}
}
