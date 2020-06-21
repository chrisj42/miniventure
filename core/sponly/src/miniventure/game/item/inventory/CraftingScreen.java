package miniventure.game.item.inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import miniventure.game.core.FontStyle;
import miniventure.game.core.GdxCore;
import miniventure.game.core.InputHandler.Control;
import miniventure.game.item.Item;
import miniventure.game.item.ItemStack;
import miniventure.game.item.recipe.Recipe;
import miniventure.game.item.recipe.RecipeSet;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.util.ColorBackground;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;
import miniventure.game.world.entity.mob.player.PlayerInventory;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;

public class CraftingScreen extends MenuScreen {
	
	// private static final Color slotBackground = Color.BLUE.cpy().lerp(Color.WHITE, .1f);
	private static final Color tableBackground = Color.SKY.cpy().lerp(Color.BLACK, .2f);
	private static final Color craftableBackground = Color.GREEN.cpy().lerp(tableBackground, .5f);
	private static final Color notCraftableBackground = Color.FIREBRICK.cpy().lerp(tableBackground, .3f);
	
	/*
		general system:
		
		- client normally has no reference to inventory at all, only hotbar
			- server sends hotbar updates as necessary
		- when inventory screen opened, client requests inventory data
			- server sends back InventoryUpdate with inventory data and hotbar indices
		
	 */
	
	private ArrayList<RecipeSlot> recipes = null;
	
	private final Table mainGroup;
	
	private final Table craftableTable;
	private final Table costTable;
	private Label resultStockLabel;
	private final HashMap<Item, Label> costStockLabels = new HashMap<>();
	private final HashMap<Item, ItemSlot> costSlots = new HashMap<>();
	
	private final ScrollPane scrollPane;
	private final Table recipeListTable;
	
	private final PlayerInventory inv;
	private int selection;
	
	public CraftingScreen(RecipeSet<?> recipeSet, PlayerInventory inv) {
		super();
		this.inv = inv;
		
		mainGroup = useTable(Align.topLeft, false);
		addMainGroup(mainGroup, RelPos.TOP_LEFT);
		
		mainGroup.add(makeLabel("personal crafting", FontStyle.CrafterHeader, false)).row();
		
		craftableTable = new Table(VisUI.getSkin());
		craftableTable.defaults().pad(2f);
		craftableTable.pad(10f);
		// craftableTable.add(makeLabel("Waiting for crafting data...", FontStyle.KeepSize, false));
		
		costTable = new Table(VisUI.getSkin());
		costTable.pad(5f);
		costTable.defaults().pad(2f).align(Align.center);
		// costTable.add(makeLabel("Waiting for crafting data...", FontStyle.KeepSize, false));
		
		recipeListTable = new Table(VisUI.getSkin()) {
			@Override
			protected void drawChildren(Batch batch, float parentAlpha) {
				boolean done = false;
				if(recipes != null) {
					done = true;
					if(recipes.size() > 0)
						recipes.get(selection).setSelected(true);
					super.drawChildren(batch, parentAlpha);
					if(recipes.size() > 0) recipes.get(selection).setSelected(false);
				}
				if(!done)
					super.drawChildren(batch, parentAlpha);
			}
		};
		recipeListTable.defaults().pad(1f).fillX().minSize(Item.ICON_SIZE * 3, ItemSlot.HEIGHT/2);
		recipeListTable.pad(10f);
		// recipeListTable.add(makeLabel("Waiting for crafting data...", false));
		
		recipeListTable.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(Control.CANCEL.matches(keycode) || Control.CRAFTING_TOGGLE.matches(keycode)) {
					GdxCore.setScreen(null);
					return true;
				}
				
				if(recipes != null && recipes.size() > 0) {
					if(Control.CONFIRM.matches(keycode)) {
						recipes.get(selection).recipe.onSelect(inv);
						return true;
					}
				}
				
				return false;
			}
		});
		
		scrollPane = new ScrollPane(recipeListTable, VisUI.getSkin()) {
			@Override
			public float getPrefHeight() {
				return CraftingScreen.this.getHeight()/2;
			}
		};
		
		// scrollPane.setHeight(getHeight()*2/3);
		scrollPane.setScrollingDisabled(true, false);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setScrollbarsOnTop(false);
		
		Table leftTable = new Table();
		leftTable.pad(0);
		leftTable.defaults().pad(0);
		
		leftTable.add(craftableTable).row();
		leftTable.add(scrollPane).row();
		
		mainGroup.add(leftTable);
		mainGroup.add(costTable).align(Align.top);
		
		// setup recipes
		recipes = new ArrayList<>(recipeSet.getRecipeCount());
		for(int i = 0; i < recipeSet.getRecipeCount(); i++) {
			final Recipe recipe = recipeSet.getRecipe(i);
			
			// ItemStack[] costs = recipe.getCosts();
			
			// ItemStack result = recipe.getResult();
			// RecipeData recipe = new RecipeData(serialRecipe.setOrdinal, serialRecipe.recipeIndex, (Item) result.item, result.count, costs);
			
			RecipeSlot slot = new RecipeSlot(recipe);
			slot.addListener(new InputListener() {
				@Override
				public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
					selection = recipes.indexOf(slot);
					itemChanged();
				}
			});
			slot.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					slot.recipe.onSelect(inv);
				}
			});
			slot.updateCanCraft(this.inv);
			// if(GameCore.debug && slot.recipe.canCraft)
			// 	System.out.println("recipe "+slot.recipe.id+" is craftable");
			recipes.add(slot);
		}
		
		selection = 0;
		itemChanged();
		refreshCraftability();
		
		// sort recipes by craftability, craftable first
		recipes.sort(Comparator.comparing(slot -> slot.craftableCache,
				Comparator.comparingInt(canCraft -> canCraft ? 0 : 1)
		));
		
		// if(GameCore.debug)
		// 	System.out.println("recipe display order: " + Arrays.toString(ArrayUtils.mapArray(recipes.toArray(), Integer.class, recipe -> ((RecipeSlot)recipe).recipe.id)));
		
		// add sorted recipes to ui
		for(RecipeSlot slot: recipes)
			recipeListTable.add(slot).row();
		
		
		mainGroup.pack();
		mainGroup.background(new ColorBackground(mainGroup, tableBackground));
		
		// mainGroup.setVisible(false);
		// Timer t = new Timer(200, e -> mainGroup.setVisible(true));
		// t.setRepeats(false);
		// t.start();
		
		setKeyboardFocus(recipeListTable);
		setScrollFocus(scrollPane);
	}
	
	// should only be called by GameClient thread, or the libGDX thread.
	/*public void recipeUpdate(RecipeUpdate recipeRequest) {
		if(GdxCore.getScreen() != this)
			return; // don't care
		
		synchronized (this) {
			recipes = new ArrayList<>(recipeRequest.recipes.length);
			recipeListTable.clearChildren();
			
			recipeListTable.pack();
			recipeListTable.invalidateHierarchy();
			
			Gdx.app.postRunnable(() -> mainGroup.setVisible(true)); // if the timer hasn't yet expired by now
		}
	}*/
	
	// called after item selection changes; resets info in InfoTable
	private void itemChanged() {
		Recipe recipe = recipes.get(selection).recipe;
		final ItemStack result = recipe.getResult();
		
		craftableTable.clearChildren();
		craftableTable.add(new ItemIcon(result.item, result.count)).row();
		craftableTable.add(makeLabel(result.item.getName(), FontStyle.KeepSize, false)).row();
		String stockText = recipe.isItemRecipe() ? "Stock: " + inv.getCount(result.item) : "";
		craftableTable.add(resultStockLabel = makeLabel(stockText, FontStyle.KeepSize, false)).row();
		
		costTable.clearChildren();
		costTable.add(makeLabel("Stock", FontStyle.StockHeader, false));
		costTable.add(makeLabel("Required", FontStyle.CostHeader, false));
		costTable.row();
		costSlots.clear();
		for(ItemStack cost : recipe.getCosts()) {
			Label costLabel = makeLabel(String.valueOf(inv.getCount(cost.item)), FontStyle.KeepSize, false);
			costStockLabels.put(cost.item, costLabel);
			costTable.add(costLabel);
			
			Color background = inv.getCount(cost.item) >= cost.count ? craftableBackground : notCraftableBackground;
			ItemSlot slot = new ItemSlot(true, cost.item, cost.count, background);
			costSlots.put(cost.item, slot);
			costTable.add(slot).align(Align.left);
			costTable.row();
		}
		
		layoutActors();
	}
	
	// called after crafting an item
	public void refreshCraftability() {
		// update slot highlights
		for(RecipeSlot slot: recipes)
			slot.updateCanCraft(inv);
		
		// update stock labels
		Recipe recipe = recipes.get(selection).recipe;
		if(resultStockLabel != null)
			resultStockLabel.setText(recipe.isItemRecipe() ? "Stock: "+ inv.getCount(recipe.getResult().item) : "");
		for(ItemStack cost: recipe.getCosts()) {
			Label costStockLabel = costStockLabels.get(cost.item);
			costStockLabel.setText(String.valueOf(inv.getCount(cost.item)));
			
			ItemSlot slot = costSlots.get(cost.item);
			Color background = inv.getCount(cost.item) >= cost.count ? craftableBackground : notCraftableBackground; 
			slot.setBackground(new ColorBackground(slot, background));
		}
	}
	
	/*@Override
	public synchronized void draw() {
		super.draw();
	}*/
	
	@Override
	public void act(float delta) {
		super.act(delta);
		
		if(GdxCore.input.pressingKey(Keys.UP))
			moveSelection(-1);
		if(GdxCore.input.pressingKey(Keys.DOWN))
			moveSelection(1);
	}
	
	private void moveSelection(int amt) {
		if(recipes.size() == 0) return;
		selection = MyUtils.wrapIndex(selection + amt, recipes.size());
		scrollPane.setSmoothScrolling(false);
		scrollPane.scrollTo(0, recipes.get(selection).getY(), 0, ItemSlot.HEIGHT);
		scrollPane.updateVisualScroll();
		scrollPane.setSmoothScrolling(true);
		itemChanged();
	}
	
	private static final class RecipeSlot extends ItemSlot {
		
		private final Recipe recipe;
		private boolean craftableCache;
		
		RecipeSlot(Recipe recipe) {
			super(true, recipe.getResult().item, recipe.getResult().count);
			this.recipe = recipe;
		}
		
		public void updateCanCraft(Inventory inventory) {
			craftableCache = recipe.canCraft(inventory);
			setBackground(new ColorBackground(this, craftableCache ? craftableBackground : notCraftableBackground));
		}
	}
}
