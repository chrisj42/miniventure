package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.client.Style;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import org.jetbrains.annotations.Nullable;

public class ItemIcon extends Widget {
	
	private static final float USABILITY_BAR_HEIGHT = Item.ICON_SIZE/8f; // 4 pixels?
	
	private final Item item;
	private final int count;
	
	public ItemIcon(Item item, int count) {
		this.item = item;
		this.count = count;
	}
	
	@Nullable
	public Item getItem() { return item; }
	
	private boolean showCount() { return item != null && count > 0; }
	
	@Override public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		
		if(item != null) {
			// draw icon
			Color prev = batch.getColor();
			batch.setColor(Color.BLACK);
			float xoff = (Item.ICON_SIZE - item.getTexture().width) / 2f;
			float yoff = (Item.ICON_SIZE - item.getTexture().height) / 2f;
			batch.draw(item.getTexture().texture, getX()-1+xoff, getY()-1+yoff);
			batch.setColor(prev);
			batch.draw(item.getTexture().texture, getX()+1+xoff, getY()+1+yoff);
			
			renderUsability(batch, getX(), getY()+2, item.getUsabilityStatus());
			
			if(showCount()) {
				BitmapFont font = ClientCore.getFont(Style.KeepSize);
				
				font.draw(batch, String.valueOf(count), getX() + 2, getY() + 2 + font.getLineHeight());
			}
		}
	}
	
	private static void renderUsability(Batch batch, float x, float y, float usability) {
		if(usability <= 0 || usability >= 1) return;
		// draw a colored bar for the durability left
		float width = Item.ICON_SIZE * usability;
		Color barColor = usability >= 0.5f ? Color.GREEN : usability >= 0.2f ? Color.YELLOW : Color.RED;
		MyUtils.fillRect(x, y, width, USABILITY_BAR_HEIGHT, barColor, batch);
	}
	
	@Override
	public float getMinWidth() {
		return getPrefWidth();
	}
	
	@Override
	public float getMinHeight() {
		return getPrefHeight();
	}
	
	@Override
	public float getPrefWidth() {
		return Item.ICON_SIZE;
	}
	
	@Override
	public float getPrefHeight() {
		return Item.ICON_SIZE;
	}
	
	@Override
	public float getMaxWidth() {
		return getPrefWidth();
	}
	
	@Override
	public float getMaxHeight() {
		return getPrefHeight();
	}
}
