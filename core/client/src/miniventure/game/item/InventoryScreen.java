package miniventure.game.item;

import java.util.ArrayList;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.InventoryAddition;
import miniventure.game.GameProtocol.InventoryRequest;
import miniventure.game.GameProtocol.InventoryUpdate;
import miniventure.game.GameProtocol.ItemDropRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.util.ColorBackground;
import miniventure.game.util.RelPos;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;

import org.jetbrains.annotations.NotNull;

/** @noinspection SynchronizeOnThis*/
public class InventoryScreen extends MenuScreen {
	
	static final Color slotBackground = Color.TEAL.cpy().lerp(Color.WHITE, .1f);
	static final Color tableBackground = Color.TEAL;
	private static final Color highlightBackground = Color.TEAL.cpy().lerp(Color.YELLOW, .25f);
	
	/*
		general system:
		
		- client normally has no reference to inventory at all, only hotbar
			- server sends hotbar updates as necessary
		- when inventory screen opened, client requests inventory data
			- server sends back InventoryUpdate with inventory data and hotbar indices
		
	 */
	
	private boolean requested = false;
	private boolean fin = false;
	
	private ClientHands hands;
	
	private ArrayList<SlotData> inventory = null;
	private final HashMap<Integer, SlotData> slotsById = new HashMap<>();
	private int[] hotbar = null; // holds slot IDs
	
	private VerticalGroup mainGroup;
	
	private int spaceUsed = 0;
	private ProgressBar fillBar;
	
	private ScrollPane scrollPane;
	private Table slotTable;
	
	private int selection;
	
	public InventoryScreen(ClientHands hands) {
		super(false);
		this.hands = hands;
		// setDebugAll(true);
		mainGroup = useVGroup(2f, Align.right, false);
		addMainGroup(mainGroup, RelPos.RIGHT);
		
		fillBar = new ProgressBar(0, 1, .01f, false, GameCore.getSkin());
		
		slotTable = new Table(GameCore.getSkin()) {
			@Override
			protected void drawChildren(Batch batch, float parentAlpha) {
				boolean done = false;
				synchronized (InventoryScreen.this) {
					if(inventory != null) {
						done = true;
						if(inventory.size() > 0)
							inventory.get(selection).slot.setSelected(true);
						super.drawChildren(batch, parentAlpha);
						if(inventory.size() > 0) inventory.get(selection).slot.setSelected(false);
					}
				}
				if(!done)
					super.drawChildren(batch, parentAlpha);
			}
		};
		slotTable.defaults().fillX().minSize(Item.ICON_SIZE * 3, ItemSlot.HEIGHT/2);
		slotTable.pad(10f);
		slotTable.background(new ColorBackground(slotTable, tableBackground));
		slotTable.add(new Label("Waiting for inventory data...", new LabelStyle(GameCore.getFont(), Color.WHITE)));
		
		slotTable.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Keys.E || keycode == Keys.ESCAPE) {
					ClientCore.setScreen(null);
					return true;
				}
				
				synchronized (InventoryScreen.this) {
					if(inventory.size() > 0) {
						for(int i = 0; i < Player.HOTBAR_SIZE; i++) {
							if(keycode == Keys.NUM_1 + i) {
								setHotbar(i, selection);
								return true;
							}
						}
						
						if(keycode == Keys.ENTER) {
							setHotbar(hands.getSelection(), selection);
							ClientCore.setScreen(null);
							return true;
						}
					}
				}
				
				return false;
			}
		});
		
		scrollPane = new ScrollPane(slotTable, GameCore.getSkin());
		
		scrollPane.setHeight(getHeight()*2/3);
		
		mainGroup.addActor(fillBar);
		mainGroup.addActor(scrollPane);
		
		setKeyboardFocus(slotTable);
	}
	
	@Override
	protected void layoutActors() {
		scrollPane.pack();
		scrollPane.setHeight(getHeight()*2/3);
		scrollPane.setWidth(scrollPane.getPrefWidth());
		super.layoutActors();
	}
	
	// should only be called by the LibGDX Application thread
	@Override
	public synchronized void focus() {
		super.focus();
		if(!requested) {
			requested = true;
			ClientCore.getClient().send(new InventoryRequest(null));
		}
	}
	
	// should only be called by the LibGDX Application thread
	public synchronized void close() {
		System.out.println("closing inventory screen");
		if(requested) {
			if(hotbar == null)
				hotbar = new int[0];
			else {
				for(int i = 0; i < hotbar.length; i++)
					hotbar[i] = inventory.indexOf(slotsById.get(hotbar[i]));
			}
			
			ClientCore.getClient().send(new InventoryRequest(hotbar));
		}
		fin = true;
	}
	
	// should only be called by the GameClient thread
	public void inventoryUpdate(InventoryUpdate update) {
		if(ClientCore.getScreen() != this)
			return; // ignore
		
		synchronized (this) {
			if(fin) return; // exited, ignore
			
			hotbar = update.hotbar;
			inventory = new ArrayList<>(update.itemStacks.length);
			
			slotTable.clearChildren();
			
			for(String[] data: update.itemStacks)
				addItem(data);
			
			for(int i = 0; i < hotbar.length; i++) {
				if(hotbar[i] >= 0) {
					SlotData data = inventory.get(hotbar[i]);
					data.toggleHotbar(i);
				}
			}
			
			refresh();
		}
	}
	
	// should only be called by the GameClient thread
	public void itemAdded(InventoryAddition addition) {
		if(ClientCore.getScreen() != this)
			return; // ignore
		
		synchronized (this) {
			if(fin) return;
			addItem(addition.newItem);
			refresh();
		}
	}
	
	private void addItem(String[] data) {
		ItemStack stack = ItemStack.deserialize(data);
		SlotData slot = new SlotData(stack);
		inventory.add(slot);
		slotsById.put(slot.id, slot);
		spaceUsed += stack.item.getSpaceUsage() * stack.count;
		slot.slot.addListener(new InputListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				synchronized (InventoryScreen.this) {
					selection = inventory.indexOf(slot);
				}
			}
		});
		slotTable.add(slot.slot).row();
	}
	
	private void decrementItem(SlotData data) {
		data.setCount(data.getCount()-1);
		spaceUsed -= data.getItem().getSpaceUsage();
		refresh();
	}
	
	private void removeItem(int index, boolean all) {
		SlotData data = inventory.get(index);
		
		if(!all && data.getCount() > 1) {
			decrementItem(data);
			return;
		}
		
		inventory.remove(index);
		if(inventory.size() > 0) 
			selection %= inventory.size();
		slotsById.remove(data.id);
		spaceUsed -= data.getCount() * data.getItem().getSpaceUsage();
		slotTable.removeActor(data.slot);
		refresh();
	}
	
	// if index is already present in location, set to -1 (remove)
	// if present elsewhere, remove and set here
	// else set here
	private synchronized void setHotbar(int hotbarIdx, int invIdx) {
		SlotData data = inventory.get(invIdx);
		data.toggleHotbar(hotbarIdx);
	}
	
	private void refresh() {
		if(slotTable.getChildren().size == 0)
			slotTable.add().row();
		fillBar.setValue(spaceUsed / (float)Player.INV_SIZE);
		hands.setFillPercent(fillBar.getValue());
		slotTable.invalidateHierarchy();
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		
		synchronized (this) {
			if(ClientCore.input.pressingKey(Keys.Q) && inventory.size() > 0) {
				boolean all = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT);
				ClientCore.getClient().send(new ItemDropRequest(false, selection, all));
				removeItem(selection, all);
			}
		}
		
		if(ClientCore.input.pressingKey(Keys.UP))
			moveSelection(-1);
		if(ClientCore.input.pressingKey(Keys.DOWN))
			moveSelection(1);
	}
	
	private synchronized void moveSelection(int amt) {
		if(inventory.size() == 0) return;
		int newSel = selection + amt;
		while(newSel < 0) newSel += inventory.size();
		selection = newSel % inventory.size();
	}
	
	private static int counter = 0;
	private static synchronized int nextId() { return counter++; }
	
	private class SlotData extends MutableItemStack {
		
		private final int id;
		
		private ItemSlot slot;
		private int hotbarIndex = -1;
		
		public SlotData(@NotNull ItemStack stack) {
			super(stack);
			id = nextId();
			slot = new ItemSlot(true, stack.item, stack.count, slotBackground);
		}
		
		public SlotData(@NotNull Item item, int count) {
			super(item, count);
			id = nextId();
			slot = new ItemSlot(true, item, count);
		}
		
		// hotbarIndex is always >= 0
		public void toggleHotbar(int hotbarIndex) {
			int prev = this.hotbarIndex; // cache original hotbar index of this slot
			
			// clear whatever is currently at the new index to make way for this one
			SlotData existing = slotsById.get(hotbar[hotbarIndex]);
			if(existing != null)
				existing.clearFromHotbar();
			
			// if they are the same, then the above clear already took care of matters.
			// prev is used because if this was the same, then the hotbar index will have been reset.
			if(prev != hotbarIndex) {
				// the new hotbar index is different; this item may or may not have already had a place on the hotbar.
				
				// if a previous place exists, clear it before moving to the new one.
				clearFromHotbar(false);
				
				this.hotbarIndex = hotbarIndex;
				if(prev < 0) // did not used to be in hotbar, so we need to make a new highlight background
					slot.setBackground(new ColorBackground(slot, highlightBackground));
				
				// update the new location
				hands.updateItem(hotbarIndex, new ItemStack(getItem(), getCount()));
				hotbar[hotbarIndex] = id;
			}
		}
		
		private void clearFromHotbar() { clearFromHotbar(true); }
		private void clearFromHotbar(boolean resetBackground) {
			if(hotbarIndex < 0) return;
			hotbar[hotbarIndex] = -1;
			hands.updateItem(hotbarIndex, null);
			hotbarIndex = -1;
			if(resetBackground)
				slot.setBackground(new ColorBackground(slot, slotBackground));
		}
		
		@Override
		public void setItem(@NotNull Item item) {
			super.setItem(item);
			slot.setItem(item);
		}
		
		@Override
		public void setCount(int count) {
			super.setCount(count);
			slot.setCount(count);
		}
		
		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(!(o instanceof SlotData)) return false;
			SlotData that = (SlotData) o;
			return id == that.id;
		}
		
		@Override
		public int hashCode() { return id; }
		
		@Override
		public String toString() {
			return super.toString()+"id("+id+") w/ItemSlot "+slot;
		}
	}
}
