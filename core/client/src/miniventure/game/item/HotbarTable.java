package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.ColorBackground;
import miniventure.game.world.entity.mob.ClientPlayer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class HotbarTable extends Table {
	
	private final ItemSlot[] slots;
	private final ProgressBar fillBar;
	
	public HotbarTable(ProgressBar fillBar) {
		this.fillBar = fillBar;
		pad(5);
		defaults().pad(5);
		
		slots = new ItemSlot[Hands.HOTBAR_SIZE];
		for(int i = 0; i < slots.length; i++) {
			slots[i] = new ItemSlot(false, null, InventoryDisplayGroup.slotBackgroundColor);
			add(slots[i]);
		}
		row();
		background(new ColorBackground(this, InventoryDisplayGroup.background));
		pack();
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		ClientPlayer player = ClientCore.getWorld().getMainPlayer();
		ClientHands hands = player.getHands();
		
		int selection = hands.getSelection();
		boolean diff = false;
		for(int i = 0; i < slots.length; i++) {
			Item item = hands.getItem(i);
			int count = item instanceof HandItem ? 1 : player.getInventory().getCount(item);
			if(count == 0) {
				hands.removeItem(i);
				item = hands.getItem(i);
				count = 1;
				diff = true;
			}
			if(!item.equals(slots[i].getItem())) diff = true;
			boolean diffCount = count != slots[i].getCount();
			slots[i].setItem(item);
			slots[i].setCount(count);
			slots[i].setSelected(i == selection);
			if(diff) invalidateHierarchy();
			else if(diffCount) invalidate();
		}
		
		super.draw(batch, parentAlpha);
		
		if(!filled) invalidate();
	}
	
	private boolean filled = false;
	@Override
	public void layout() {
		super.layout();
		filled = false;
		if(ClientCore.getWorld() == null) return;
		ClientPlayer player = ClientCore.getWorld().getMainPlayer();
		if(player == null) return;
		fillBar.setValue(player.getInventory().getPercentFilled());
		filled = true;
	}
}
