package miniventure.game.item;

import miniventure.game.GameCore;

import java.awt.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import org.jetbrains.annotations.Nullable;

public class ItemStackSlot extends ItemSlot {
	
	private int count;
	
	public ItemStackSlot(boolean showName, @Nullable Item item, int count) {
		super(showName, item);
		this.count = count;
	}
	public ItemStackSlot(int index, boolean showName, @Nullable Item item, int count) {
		super(index, showName, item);
		this.count = count;
	}
	public ItemStackSlot(int index, boolean showName, @Nullable Item item, int count, Color backgroundColor) {
		super(index, showName, item, backgroundColor);
		this.count = count;
	}
	
	
	public int getCount() { return count; }
	public ItemStackSlot setCount(int count) {
		this.count = count;
		return this;
	}
	
	protected boolean showCount() { return getItem() != null && getCount() > 0; }
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if(showCount()) {
			BitmapFont font = GameCore.getFont();
			font.setColor(getTextColor());
			font.draw(batch, getCount() + "", getX() + 2, getY() + 2 + font.getLineHeight());
		}
	}
}
