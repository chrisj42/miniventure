package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.world.entity.mob.ClientPlayer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class HotbarTable extends Table {
	
	private final ItemSlot[] slots;
	
	public HotbarTable() {
		pad(10);
		// align(Align.bottomRight);
		defaults().pad(5).grow();
		
		slots = new ItemSlot[Hands.HOTBAR_SIZE];
		for(int i = 0; i < slots.length; i++) {
			slots[i] = new ItemSlot(false, null);
			addActor(slots[i]);
		}
		row();
		
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
			slots[i].setItem(item);
			slots[i].setCount(count);
			slots[i].setSelected(i == selection);
			if(diff) invalidateHierarchy();
		}
		
		super.draw(batch, parentAlpha);
		
		
	}
}
