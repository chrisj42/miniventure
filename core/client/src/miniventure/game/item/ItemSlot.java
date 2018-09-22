package miniventure.game.item;

import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import miniventure.game.GameCore;
import miniventure.game.screen.ColorBackground;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import org.jetbrains.annotations.Nullable;

public class ItemSlot extends JLabel {
	
	private static final float SPACING = 10, XPADDING = 3, YPADDING = 2;
	public static final float HEIGHT = Item.ICON_SIZE+2+YPADDING*2;
	
	private final boolean showName;
	@Nullable private Item item;
	// private float prefWidth;
	private Color textColor;
	
	private final int slotIndex;
	
	public ItemSlot(boolean showName, @Nullable Item item) { this(0, showName, item); }
	public ItemSlot(int index, boolean showName, @Nullable Item item) { this(index, showName, item, null); }
	public ItemSlot(int index, boolean showName, @Nullable Item item, @Nullable Color backgroundColor) {
		this.slotIndex = index;
		this.showName = showName;
		if(backgroundColor != null)
			setBackground(backgroundColor);
		// textColor = Color.WHITE;
		setForeground(Color.WHITE);
		setItem(item);
		// setPreferredSize(new Dimension(getPreferredSize().width, (int)HEIGHT));
	}
	
	public int getSlotIndex() { return slotIndex; }
	
	@Nullable
	public Item getItem() { return item; }
	
	public ItemSlot setItem(@Nullable Item item) {
		// this.item = item instanceof HandItem ? null : item;
		// if(!showName) prefWidth = Item.ICON_SIZE + 2;
		// else if(item == null) prefWidth = Item.ICON_SIZE * 2;
		// else prefWidth = Item.ICON_SIZE + 2 + SPACING + GameCore.getTextLayout(item.getName()).width;
		// prefWidth += XPADDING * 2;
		// setPreferredSize(new Dimension((int)prefWidth, getPreferredSize().height));
		
		return this;
	}
	
	public ItemSlot setTextColor(Color color) {
		textColor = color;
		return this;
	}
	public Color getTextColor() { return textColor; }
	
	// public float getPrefWidth() { return prefWidth; }
	
	@Override
	protected void paintComponent(Graphics g) {
		
	}
	
	/*public void glDraw(Batch batch, float parentAlpha) {
		// super.draw(batch, parentAlpha);
		// if(background != null)
		// 	background.draw(batch, getX(), getY(), getWidth(), getHeight());
		Item item = getItem();
		if(item != null) {
			// draw icon
			Color prev = batch.getColor();
			batch.setColor(Color.BLACK);
			int xoff = (Item.ICON_SIZE - item.getTexture().width) / 2;
			int yoff = (Item.ICON_SIZE - item.getTexture().height) / 2;
			batch.draw(item.getTexture().texture, getX()+XPADDING+xoff, getY()+YPADDING+yoff);
			batch.setColor(prev);
			batch.draw(item.getTexture().texture, getX()+2+XPADDING+xoff, getY()+2+YPADDING+yoff);
			item.renderIconExtras(batch, getX()+2+XPADDING, getY()+2+YPADDING);
			
			if(showName) {
				BitmapFont font = GameCore.getFont();
				font.setColor(getTextColor());
				float yo = font.getDescent();
				yo = yo + (getHeight() - font.getDescent() + font.getCapHeight() + font.getAscent()) / 2;
				font.draw(batch, item.getName().replace("_", " "), getX()+Item.ICON_SIZE+2+SPACING+ XPADDING, getY()+yo+YPADDING);
			}
		}
	}*/
}
