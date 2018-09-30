package miniventure.game.item;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import miniventure.game.GameProtocol.CraftRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.MenuScreen;

public class CraftingScreen extends MenuScreen {
	
	private static final Color background = new Color(.2f, .4f, 1f, 1);
	
	private final Inventory playerInv;
	
	private final CraftableItem[] list;
	private final ItemSelectionTable table;
	
	// FIXME fix up some things relating to the item selection table
	
	private final ItemStackSlot invCount = new ItemStackSlot(true, null, 0) {
		@Override protected boolean showCount() { return getItem() != null; }
	};
	private final JPanel inInv = new OpaqueVerticalGroup(background);
	private final JPanel costs = new OpaqueVerticalGroup(background);
	
	private static class OpaqueVerticalGroup extends JPanel {
		// private final Color color;
		
		public OpaqueVerticalGroup(Color color) {
			// this.color = color;
			// pad(5);
			setBackground(color);
		}
		
		/*@Override
		public void draw(Batch batch, float parentAlpha) {
			MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), color, parentAlpha, batch);
			super.draw(batch, parentAlpha);
		}*/
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
	
	@Override
	public void focus() {
		ClientCore.setScreen(null);
	}
	
	public CraftingScreen(Recipe[] recipes, Inventory playerInventory) {
		super(false);
		this.playerInv = playerInventory;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
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
			craftEntry.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					craft(craftEntry);
				}
			});
			craftEntry.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER)
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
		table.setFocusable(false);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keycode = e.getKeyCode();
				if(keycode == KeyEvent.VK_ENTER) {
					craft(list[table.getSelection()]);
					// return true;
				}
				// return false;
			}
		});
		add(table);
		
		// inInv.columnAlign(Align.left);
		// inInv.space(5);
		inInv.add(new JLabel("In inventory:"));
		inInv.add(invCount);
		add(inInv);
		
		// costs.columnAlign(Align.left);
		// costs.space(5);
		add(costs);
		
		
		// table.setPosition(0, getHeight(), Align.topLeft);
		// table.pack();
		table.setAlignmentX(LEFT_ALIGNMENT);
		table.setAlignmentY(TOP_ALIGNMENT);
		
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keycode = e.getKeyCode();
				if(keycode == KeyEvent.VK_Z || keycode == KeyEvent.VK_ESCAPE)
					ClientCore.setScreen(null);
				// return true;
			}
		});
		
		table.requestFocus();
	}
	
	// @Override
	// public boolean usesWholeScreen() { return false; }
	
	
	/*@Override
	public Dimension getPreferredSize() {
		Dimension tableSize = table.getPreferredSize();
		return new Dimension(tableSize.width + 10 + costs.getPreferredSize().width, tableSize.height);
	}*/
	
	private void craft(CraftableItem item) {
		if(item.recipe.tryCraft(playerInv) != null)
			refreshCanCraft();
		
		// tell server about the attempt
		ClientCore.getClient().send(new CraftRequest(item.getRecipeIndex()));
	}
	
	private void setHighlightedRecipe(Recipe recipe) {
		inInv.remove(invCount);
		invCount.setItem(recipe.getResult().item);
		invCount.setCount(playerInv.getCount(invCount.getItem()));
		inInv.add(invCount);
		
		costs.removeAll();
		costs.add(new JLabel("Costs:"));
		for(ItemStack cost: recipe.getCosts())
			costs.add(new ItemStackSlot(true, cost.item, cost.count));
		
		inInv.revalidate();
		// inInv.pack();
		costs.revalidate();
		// costs.pack();
		
		inInv.setLocation(table.getPreferredSize().width + 10, getHeight() - inInv.getHeight());
		costs.setLocation(table.getPreferredSize().width + 10, getHeight() - inInv.getHeight() - 10 - costs.getHeight());
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
