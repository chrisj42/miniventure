package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import org.jetbrains.annotations.NotNull;

public class InventoryScreen extends MenuScreen {
	
	private final ClientHands hands;
	
	private final InventoryDisplayGroup invGroup;
	
	public InventoryScreen(ClientHands hands) {
		super(false);
		this.hands = hands;
		
		invGroup = new InventoryDisplayGroup(hands.getInv(), getHeight() * 2 / 3);
		addMainGroup(invGroup, RelPos.TOP_RIGHT, 0, -10);
		
		invGroup.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				for(int i = 0; i < Hands.HOTBAR_SIZE; i++) {
					if(keycode == Keys.NUM_1 + i) {
						toggleHotbarItem(i, invGroup.getSelectedItem(), true);
						return true;
					}
				}
				
				if(keycode == Keys.ENTER) {
					// set currently selected item to currently selected hotbar slot
					toggleHotbarItem(hands.getSelection(), invGroup.getSelectedItem(), false);
					ClientCore.setScreen(null);
				}
				
				return false;
			}
		});
		
		for(int i = 0; i < Hands.HOTBAR_SIZE; i++)
			invGroup.setHotbarHighlight(hands.getItem(i), true);
	}
	
	@Override
	public void focus() {
		super.focus();
		setKeyboardFocus(invGroup);
	}
	
	// this method deals with setting the special inventory highlight for items that are in the hotbar.
	private void toggleHotbarItem(int hotbarIndex, @NotNull Item item, boolean toggleOff) {
		
		if(item instanceof HandItem) {
			// we're taking an item off the hotbar (by replacing it with a hand)
			Item prevItem = hands.removeItem(hotbarIndex);
			invGroup.setHotbarHighlight(prevItem, false);
		}
		else {
			// we could be adding or removing an item from the hotbar, or just moving it around. Or moving it and removing another one.
			
			if(hands.getItem(hotbarIndex).equals(item)) {
				if(toggleOff) {
					// remove it from the hotbar
					hands.removeItem(hotbarIndex);
					invGroup.setHotbarHighlight(false);
				}
			}
			else {
				// adding an item, or moving it around and possibly replacing another item.
				
				// because the item could move, we will try to remove it from the hotbar now, to be added back later.
				invGroup.setHotbarHighlight(item, false);
				hands.removeItem(item);
				
				Item prevItem = hands.replaceItem(hotbarIndex, item);
				
				// since item was already removed if it existed before, we can just add it back here.
				invGroup.setHotbarHighlight(true);
				// we need to make sure to unset this item as a hotbar item, since it was replaced.
				invGroup.setHotbarHighlight(prevItem, false);
			}
		}
	}
}
