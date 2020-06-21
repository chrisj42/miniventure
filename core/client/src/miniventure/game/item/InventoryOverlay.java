package miniventure.game.item;

import java.util.EnumMap;
import java.util.Objects;

import miniventure.game.core.ClientCore;
import miniventure.game.core.FontStyle;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.util.ColorBackground;
import miniventure.game.screen.util.DiscreteViewport;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
	components:
		- togglable inv panel for main inventory
		- hotbar inv panel
		- drag-drop enabled item slots for each equipment slot
			- custom
		- progress/fill bar
		
 */

public class InventoryOverlay extends MenuScreen {
	
	private static final int MAX_ITEMS_PER_ROW = 9;
	
	private ClientPlayerInventory invManager;
	private ClientInventory inventory;
	
	private Table mainGroup;
	
	private ProgressBar fillBar;
	
	private DragAndDrop dragAndDrop;
	
	private InventoryPanel slotTable;
	// private InventoryPanel hotbar;
	private EnumMap<EquipmentType, ItemSlot> equipmentSlots;
	
	private float lastAmt; // cache for progress bar
	private String lastItem; // cache for held item label
	
	public InventoryOverlay(Camera camera) {
		super(false, new DiscreteViewport(camera));
		mainGroup = useTable(Align.left, false);
		mainGroup.defaults().padBottom(2f);
		addMainGroup(mainGroup, RelPos.BOTTOM_LEFT);
		
		equipmentSlots = new EnumMap<>(EquipmentType.class);
		
		dragAndDrop = new DragAndDrop();
		
		// make crafting screen button
		VisTextButton craftBtn = makeButton("Craft", () -> {});
		// craftBtn.addActorBefore(craftBtn.getLabel(), makeEquipmentSlot(EquipmentSlot.HAMMER));
		// craftBtn.invalidate();
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
		/*craftBtn.setBackground(new BaseDrawable(craftBtn.getBackground()) {
			@Override
			public void draw(Batch batch, float x, float y, float width, float height) {
				Color prev = batch.getColor();
				batch.setColor(new Color(1f, 1f, 1f, .4f));
				super.draw(batch, x, y, width, height);
				batch.setColor(prev);
			}
		});*/
		
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
		
		slotTable = new InventoryPanel(0, PlayerInventory.INV_SIZE, MAX_ITEMS_PER_ROW) {
			// @Override
			// int getIndex(int idx) {
			// 	return idx + invManager.getHotbarItemCount();
			// }
			
			@Override
			ItemSlot makeItemSlot(int idx) {
				final int index = idx - 1; // each will track the index before in the inventory; the first one is special
				
				ItemSlot slot = new InventorySlot(index, () -> invManager.getSelection() == index);
				slot.addListener(new InputListener() {
					@Override
					public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
						// note, DragListener handles events for those slots that have DragAndDrop enabled; so all such slots automatically count the event as handled. So returning false has no effect. However for the first slot that doesn't have that listener, returning true is needed to handle the event.
						// System.out.println("clicked index "+index+" when slots taken is "+invManager.getInv().getSlotsTaken()+" (event handled: "+event.isHandled()+")");
						if(index < invManager.getInv().getSlotsTaken()) {
							invManager.setSelection(index);
							return true;
						}
						return false;
					}
				});
				if(index >= 0) {
					// add drag-and-drop functions
					SlotInfo info = new SlotInfo(slot, index);
					dragAndDrop.addSource(new SlotSource(info));
					dragAndDrop.addTarget(new SlotTarget(info));
				}
				return slot;
			}
		};
		
		mainGroup.add(slotTable).align(Align.left).row();
		
		/*hotbar = new InventoryPanel(PlayerInventory.HOTBAR_SIZE, PlayerInventory.HOTBAR_SIZE) {
			*//*@Override
			int getIndex(int idx) {
				return invManager.getInventoryIndex(idx);
			}*//*
			
			@Override
			ItemSlot makeItemSlot(int idx) {
				ItemSlot slot = new InventorySlot(idx, () -> invManager.getSelection() == idx);
				// add drag-and-drop functions
				SlotInfo info = new SlotInfo(slot, idx, true);
				dragAndDrop.addSource(new SlotSource(info));
				dragAndDrop.addTarget(new SlotTarget(info));
				return slot;
			}
		};
		
		mainGroup.add(hotbar).expandX().align(Align.center).row();
		*/
		// create the bar at the bottom with the space and held item info
		
		HorizontalGroup infoBar = new HorizontalGroup();
		
		infoBar.addActor(craftBtn);
		// infoBar.addActor(makeEquipmentSlot(EquipmentType.HAMMER));
		// infoBar.addActor(makeEquipmentSlot(EquipmentType.ARMOR));
		// infoBar.addActor(makeEquipmentSlot(EquipmentType.ACCESSORY));
		infoBar.addActor(makeLabel("    Inventory Space:   ", false));
		infoBar.addActor(fillBar);
		
		// create the label such that it refreshes automatically when the selected item changes
		VisLabel handItemLabel = new VisLabel("Held Item", new LabelStyle(ClientCore.getFont(FontStyle.Default), null)) {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				ItemStack stack = invManager == null ? null : invManager.getSelectedItem();
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
		
		addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(inventory == null)
					return false;
				//noinspection SynchronizeOnThis
				synchronized (InventoryOverlay.this) {
					for(int i = 0; i <= Math.min(inventory.getSlotsTaken(), 9); i++) {
						if(keycode == Keys.NUM_1 + i) {
							invManager.setSelection(i - 1); // -1 to buffer for hand space
							return true;
						}
					}
				}
				
				return false;
			}
			
			@Override
			public boolean scrolled(InputEvent event, float x, float y, int amount) {
				if(invManager == null)
					return false;
				int idx = invManager.getSelection() + amount;
				invManager.setSelection(idx);
				return true;
			}
		});
		
		// debug
		// mainGroup.setBackground(new ColorBackground(mainGroup, Color.NAVY));
		
		// will this set the stage as the input handler, or prevent any inputs from being read?
		setKeyboardFocus(null);
		setScrollFocus(null);
	}
	
	private ItemSlot makeEquipmentSlot(EquipmentType type) {
		ItemSlot slot = new ItemSlot(false, null) {
			@Override @Nullable
			public Item getItem() {
				return invManager == null ? null : invManager.getEquippedItem(type);
			}
			
			@Override
			public int getCount() {
				return 0;
			}
		};
		slot.addListener(new InputListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				slot.setSelected(true);
			}
			
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				slot.setSelected(false);
			}
		});
		slot.setBackground(new SlotBackground(() -> false, slot::getSelected));
		equipmentSlots.put(type, slot);
		return slot;
	}
	
	// called on creation of a ClientPlayer, since this screen is created with the GameScreen, before the player exists.
	public void setInventory(ClientPlayerInventory inv) {
		invManager = inv;
		inventory = invManager.getInv();
		dragAndDrop.clear();
		slotTable.setInventory(inventory);
		// hotbar.setInventory(inventory);
		// add drag-and-drop to equipment slots
		
		// no slots for now, so disable
		/*for(EquipmentType slotType: EquipmentType.values) {
			SlotInfo info = new SlotInfo(slotType);
			dragAndDrop.addSource(new SlotSource(info));
			dragAndDrop.addTarget(new SlotTarget(info));
		}*/
	}
	
	private static class SlotPayload extends Payload {
		@NotNull private final ClientItem item;
		
		private SlotPayload(Actor actor, @NotNull ClientItem item) {
			this.item = item;
			setDragActor(actor);
		}
	}
	
	private class SlotInfo {
		
		private final ItemSlot slot;
		private final int index;
		// private final boolean isHotbar;
		private final EquipmentType equipmentType;
		
		public SlotInfo(ItemSlot slot, int index) {
			this(slot, index, null);
		}
		
		public SlotInfo(EquipmentType equipmentType) {
			this(equipmentSlots.get(equipmentType), -1, equipmentType);
		}
		
		private SlotInfo(ItemSlot slot, int index, EquipmentType equipmentType) {
			this.slot = slot;
			this.index = index;
			// this.isHotbar = isHotbar;
			this.equipmentType = equipmentType;
		}
		
		/*public int getInvIndex() {
			return isHotbar ? invManager.getInventoryIndex(index) : index;
		}
		public int getHotbarIndex() {
			return isHotbar ? index : -1;
		}*/
	}
	
	private class SlotSource extends Source {
		
		private SlotInfo info;
		
		public SlotSource(SlotInfo info) {
			super(info.slot);
			this.info = info;
		}
		
		@Override
		public Payload dragStart(InputEvent event, float x, float y, int pointer) {
			// only allow dragging if there's an item in the slot
			ClientItem item = (ClientItem) info.slot.getItem();
			if(item == null)
				return null;
			
			return new SlotPayload(new ItemIcon(item, info.slot.getCount()), item);
		}
	}
	
	private class SlotTarget extends Target {
		
		private final SlotInfo info;
		
		public SlotTarget(SlotInfo info) {
			super(info.slot);
			this.info = info;
		}
		
		@Override
		public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
			// SlotInfo other = ((SlotSource)source).info;
			if(source.getActor() == getActor())
				return false;
			
			// inventory slots are only good up to the ones that have an item
			if(info.index > inventory.getSlotsTaken())
				return false;
			
			// the equipment type must match to equip an item
			if(info.equipmentType != null) {
				ClientItem item = ((SlotPayload)payload).item;
				if(item.getEquipmentType() != info.equipmentType)
					return false;
			}
			
			return true;
		}
		
		@Override
		public void drop(Source source, Payload payload, float x, float y, int pointer) {
			SlotInfo other = ((SlotSource)source).info;
			
			if(info.equipmentType != null) // equip item
				invManager.equipItem(info.equipmentType, other.index);
			else if(other.equipmentType != null) // unequip item
				invManager.unequipItem(other.equipmentType, info.index);
			else // inv movement
				inventory.moveItem(other.index, info.index);
		}
	}
}
