package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.ColorBackground;
import miniventure.game.world.entity.mob.player.ClientPlayer;
import miniventure.game.world.entity.mob.player.Player;

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
		
		slots = new ItemSlot[Player.HOTBAR_SIZE];
		for(int i = 0; i < slots.length; i++) {
			slots[i] = new ItemSlot(false, null, InventoryScreen.slotBackground);
			add(slots[i]);
		}
		row();
		background(new ColorBackground(this, InventoryScreen.tableBackground));
		pack();
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		ClientPlayer player = ClientCore.getWorld().getMainPlayer();
		ClientHands hands = player.getHands();
		if(hands.getFillPercent() != fillBar.getValue()) invalidate();
		int selection = hands.getSelection();
		boolean diff = false;
		for(int i = 0; i < slots.length; i++) {
			ItemStack itemStack = hands.getHotbarItem(i);
			slots[i].setItem(itemStack == null ? null : itemStack.item);
			if(itemStack != null)
				slots[i].setCount(itemStack.count);
			slots[i].setSelected(i == selection);
		}
		
		fillBar.setVisible(!(ClientCore.getScreen() instanceof InventoryScreen));
		
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
		fillBar.setValue(player.getHands().getFillPercent());
		filled = true;
	}
}
