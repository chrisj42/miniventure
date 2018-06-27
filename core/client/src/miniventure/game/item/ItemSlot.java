package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.screen.ColorBackground;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import org.jetbrains.annotations.Nullable;

public class ItemSlot extends Actor {
	
	private static final float SPACING = 10, XPADDING = 3, YPADDING = 2;
	public static final float HEIGHT = Item.ICON_SIZE+2+YPADDING*2;
	
	private final boolean showName;
	@Nullable private Item item;
	private float prefWidth;
	private Color textColor;
	private Drawable background;
	
	private final int slotIndex;
	
	public ItemSlot(boolean showName, @Nullable Item item) { this(0, showName, item); }
	public ItemSlot(int index, boolean showName, @Nullable Item item) { this(index, showName, item, (Drawable)null); }
	public ItemSlot(int index, boolean showName, @Nullable Item item, Color backgroundColor) {
		this(index, showName, item, new ColorBackground(null, backgroundColor));
		((ColorBackground)background).setActor(this);
	}
	public ItemSlot(boolean showName, @Nullable Item item, Drawable background) { this(0, showName, item, background); }
	public ItemSlot(int index, boolean showName, @Nullable Item item, Drawable background) {
		this.slotIndex = index;
		this.showName = showName;
		this.background = background;
		textColor = Color.WHITE;
		setItem(item);
		setHeight(HEIGHT);
	}
	
	public int getSlotIndex() { return slotIndex; }
	
	@Nullable
	public Item getItem() { return item; }
	
	public ItemSlot setItem(@Nullable Item item) {
		this.item = item instanceof HandItem ? null : item;
		if(!showName) prefWidth = Item.ICON_SIZE + 2;
		else if(item == null) prefWidth = Item.ICON_SIZE * 2;
		else prefWidth = Item.ICON_SIZE + 2 + SPACING + GameCore.getTextLayout(item.getName()).width;
		prefWidth += XPADDING * 2;
		setWidth(prefWidth);
		return this;
	}
	
	public void setBackground(Drawable background) { this.background = background; }
	
	public ItemSlot setTextColor(Color color) {
		textColor = color;
		return this;
	}
	public Color getTextColor() { return textColor; }
	
	public float getPrefWidth() { return prefWidth; }
	
	@Override public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if(background != null)
			background.draw(batch, getX(), getY(), getWidth(), getHeight());
		Item item = getItem();
		if(item != null) {
			// draw icon
			Color prev = batch.getColor();
			batch.setColor(Color.BLACK);
			batch.draw(item.getTexture().texture, getX()+ XPADDING, getY()+YPADDING);
			batch.setColor(prev);
			batch.draw(item.getTexture().texture, getX()+2+ XPADDING, getY()+2+YPADDING);
			item.renderIconExtras(batch, getX()+2+ XPADDING, getY()+2+YPADDING);
			
			if(showName) {
				BitmapFont font = GameCore.getFont();
				font.setColor(getTextColor());
				float yo = font.getDescent();
				yo = yo + (getHeight() - font.getDescent() + font.getCapHeight() + font.getAscent()) / 2;
				font.draw(batch, item.getName().replace("_", " "), getX()+Item.ICON_SIZE+2+SPACING+ XPADDING, getY()+yo+YPADDING);
			}
		}
	}
}
