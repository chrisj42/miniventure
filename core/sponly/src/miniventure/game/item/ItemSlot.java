package miniventure.game.item;

import java.util.Objects;

import miniventure.game.core.ClientCore;
import miniventure.game.core.FontStyle;
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
	
	private static final float SPACING = 10, XPADDING = 15, YPADDING = 8;
	private static final float ICON_WIDTH = Item.ICON_SIZE * ItemIcon.UI_SCALE + XPADDING*2;
	static final float HEIGHT = Item.ICON_SIZE * ItemIcon.UI_SCALE + YPADDING*2;
	
	private final boolean showName;
	@Nullable private Item item;
	private int count;
	private float prefWidth;
	private Color textColor;
	private Drawable background;
	
	private boolean selected = false;
	void setSelected(boolean selected) { this.selected = selected; }
	boolean getSelected() { return selected; }
	
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
			prefWidth = ICON_WIDTH;
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
		if(item == null || !showName)
			prefWidth = ICON_WIDTH;
		else
			prefWidth = ICON_WIDTH + SPACING + ClientCore.getTextLayout(ClientCore.getFont(FontStyle.KeepSize), item.getName()).width;
		
		setWidth(prefWidth);
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
	
	private boolean showCount() { return getItem() != null && getCount() > 0; }
	
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
		ClientItem item = (ClientItem) getItem();
		if(item != null) {
			// draw icon
			Color prev = batch.getColor();
			batch.setColor(Color.BLACK);
			float size = Item.ICON_SIZE * ItemIcon.UI_SCALE;
			float ypad = background instanceof SlotBackground ? YPADDING * 2 : YPADDING;
			batch.draw(item.getTexture().texture, getX()-1+XPADDING, getY()-1+ypad, size, size);
			batch.setColor(prev);
			batch.draw(item.getTexture().texture, getX()+1+XPADDING, getY()+1+ypad, size, size);
			
			ItemIcon.renderUsability(batch, getX()+2+XPADDING, getY()+2+YPADDING, item.getUsabilityStatus());
			
			if(showName || showCount()) {
				BitmapFont font = ClientCore.getFont(FontStyle.KeepSize);
				font.setColor(getTextColor());
				
				if(showName) {
					float yo = font.getDescent();
					yo = yo + (getHeight() - font.getDescent() + font.getCapHeight() + font.getAscent()) / 2;
					font.draw(batch, MyUtils.toTitleFormat(item.getName()), getX() + Item.ICON_SIZE * ItemIcon.UI_SCALE + 2 + SPACING + XPADDING, getY() + yo + YPADDING);
				}
				
				if(showCount()) {
					font.draw(batch, String.valueOf(getCount()), getX() + XPADDING, getY() + ypad - YPADDING + font.getLineHeight());
				}
			}
		}
		// else
		// 	System.out.println("item null");
		
		if(selected)
			MyUtils.fillRect(getX(), getY(), getWidth(), getHeight(), selectionColor, parentAlpha, batch);
	}
}
