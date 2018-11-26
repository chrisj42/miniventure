package miniventure.game.item;

import java.util.Objects;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.ColorBackground;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import org.jetbrains.annotations.Nullable;

public class ItemSlot extends Widget {
	
	private static final Color selectionColor = new Color(.8f, .8f, .8f, 0.5f);
	
	private static final float SPACING = 10, XPADDING = 3, YPADDING = 2;
	static final float HEIGHT = Item.ICON_SIZE+2+YPADDING*2;
	private static final float USABILITY_BAR_HEIGHT = Item.ICON_SIZE/8f; // 4 pixels?
	
	private final boolean showName;
	@Nullable private Item item;
	private int count;
	private float prefWidth;
	private Color textColor;
	private Drawable background;
	
	private boolean selected = false;
	void setSelected(boolean selected) { this.selected = selected; }
	
	public ItemSlot(boolean showName, @Nullable Item item) { this(showName, item, 1); }
	public ItemSlot(boolean showName, @Nullable Item item, int count) { this(showName, item, count, (Drawable)null); }
	public ItemSlot(boolean showName, @Nullable Item item, Color backgroundColor) { this(showName, item, 1, backgroundColor); }
	public ItemSlot(boolean showName, @Nullable Item item, int count, Color backgroundColor) {
		this(showName, item, count, (Drawable)null);
		setBackground(new ColorBackground(this, backgroundColor));
	}
	public ItemSlot(boolean showName, @Nullable Item item, Drawable background) { this(showName, item, 1, background); }
	public ItemSlot(boolean showName, @Nullable Item item, int count, Drawable background) {
		this.showName = showName;
		textColor = Color.WHITE;
		setBackground(background);
		setHeight(HEIGHT);
		if(!showName) {
			prefWidth = Item.ICON_SIZE + 2 + XPADDING * 2;
			setWidth(prefWidth);
		}
		setCount(count);
		setItem(item);
		setTouchable(Touchable.enabled);
	}
	
	@Nullable
	public Item getItem() { return item; }
	
	public ItemSlot setItem(@Nullable Item item) {
		if(Objects.equals(this.item, item)) return this; // no action is needed.
		this.item = item;
		if(!showName) return this; // the layout is always the same.
		else if(item == null) prefWidth = Item.ICON_SIZE * 2;
		else prefWidth = Item.ICON_SIZE + 2 + SPACING + ClientCore.getTextLayout(item.getName()).width;
		prefWidth += XPADDING * 2;
		return this;
	}
	
	public int getCount() { return count; }
	public ItemSlot setCount(int count) {
		this.count = count;
		return this;
	}
	public ItemSlot changeCount(int amt) {
		count += amt;
		return this;
	}
	
	private boolean showCount() { return item != null && count > 0; }
	
	public void setBackground(Drawable background) { this.background = background; }
	
	public ItemSlot setTextColor(Color color) {
		textColor = color;
		return this;
	}
	public Color getTextColor() { return textColor; }
	
	@Override public float getPrefWidth() { return prefWidth+(showName?20:0); }
	@Override public float getPrefHeight() { return HEIGHT; }
	
	@Override public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if(background != null)
			background.draw(batch, getX(), getY(), getWidth(), getHeight());
		Item item = this.item;
		if(item != null) {
			// draw icon
			Color prev = batch.getColor();
			batch.setColor(Color.BLACK);
			int xoff = (Item.ICON_SIZE - item.getTexture().width) / 2;
			int yoff = (Item.ICON_SIZE - item.getTexture().height) / 2;
			batch.draw(item.getTexture().texture, getX()+XPADDING+xoff, getY()+YPADDING+yoff);
			batch.setColor(prev);
			batch.draw(item.getTexture().texture, getX()+2+XPADDING+xoff, getY()+2+YPADDING+yoff);
			
			renderUsability(batch, getX()+2+XPADDING, getY()+2+YPADDING, item.getUsabilityStatus());
			
			if(showName || showCount()) {
				BitmapFont font = ClientCore.getFont();
				font.setColor(getTextColor());
				
				if(showName) {
					float yo = font.getDescent();
					yo = yo + (getHeight() - font.getDescent() + font.getCapHeight() + font.getAscent()) / 2;
					font.draw(batch, item.getName().replace("_", " "), getX() + Item.ICON_SIZE + 2 + SPACING + XPADDING, getY() + yo + YPADDING);
				}
				
				if(showCount())
					font.draw(batch, String.valueOf(getCount()), getX() + 2, getY() + 2 + font.getLineHeight());
			}
		}
		// else
		// 	System.out.println("item null");
		
		if(selected)
			MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), selectionColor, parentAlpha, batch);
	}
	
	private static void renderUsability(Batch batch, float x, float y, float usability) {
		if(usability <= 0 || usability >= 1) return;
		// draw a colored bar for the durability left
		float width = Item.ICON_SIZE * usability;
		Color barColor = usability >= 0.5f ? Color.GREEN : usability >= 0.2f ? Color.YELLOW : Color.RED;
		MyUtils.fillRect(x, y, width, USABILITY_BAR_HEIGHT, barColor, batch);
	}
}
