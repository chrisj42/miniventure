package miniventure.game.item.typenew;

import java.util.Arrays;

import miniventure.game.GameCore;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Item {
	
	public static final float ICON_SIZE = 32;
	
	@NotNull private final ItemType type;
	private String[] data;
	
	@NotNull private final String name;
	
	private boolean used = false;
	
	private float renderWidth;
	private float renderHeight;
	private boolean initializedWidth = false;
	private boolean initializedHeight = false;
	
	public Item(@NotNull ItemType type) { this(type, type.getInitialData()); }
	public Item(@NotNull ItemType type, String[] data) {
		this.type = type;
		this.data = data;
		
		this.name = MyUtils.toTitleCase(type.name());
	}
	
	@NotNull public ItemType getType() { return type; }
	
	@NotNull public String getName() { return name; }
	
	public void use() { used = true; }
	public boolean isUsed() { return used; }
	
	public int getMaxStackSize() { return type.getProp(StackableProperty.class).getMaxStackSize(); }
	
	// note that without a successful attack or interaction, no stamina is lost.
	public int getStaminaUsage() { return type.getProp(StaminaUsageProperty.class).getStaminaUsage(this); }
	
	// called to reset the item
	@Nullable
	public final Item resetUsage() {
		if(!used) return this;
		Item newItem = type.getProp(UsageBehavior.class).getUsedItem(this);
		used = false;
		return newItem;
	}
	
	@NotNull
	public TextureRegion getTexture() { return type.getProp(RenderProperty.class).getSprite(this); }
	
	public void drawItem(int stackSize, Batch batch, float x, float y) {
		drawItem(stackSize, batch, x, y, Color.WHITE);
	}
	public void drawItem(int stackSize, Batch batch, float x, float y, Color textColor) {
		TextureRegion texture = getTexture();
		float width = texture.getRegionWidth();
		float tx = x + Math.max(0, (ICON_SIZE - texture.getRegionWidth())/2);
		
		Color prev = batch.getColor();
		batch.setColor(Color.BLACK);
		batch.draw(texture, tx-2, y-2);
		batch.setColor(prev);
		batch.draw(texture, tx, y);
		
		FreeTypeFontParameter params = GameCore.getDefaultFontConfig();
		params.color = textColor;
		params.borderWidth = 1;
		
		BitmapFont font = GameCore.getFont(params);
		
		float textOff = font.getCapHeight() + font.getAscent();
		font.draw(batch, stackSize+"", x+1, y+textOff-font.getDescent());
		font.draw(batch, getName(), x+width+10, y+(getRenderHeight()+textOff)/2);
	}
	
	public float getRenderHeight() {
		if(!initializedHeight) {
			renderHeight = Math.max(getTexture().getRegionHeight(), GameCore.getTextLayout(name).height);
			initializedHeight = true;
		}
		return renderHeight;
	}
	
	public float getRenderWidth() {
		if(!initializedWidth) {
			renderWidth = getTexture().getRegionWidth() + 10 + GameCore.getTextLayout(name).width;
			initializedWidth = true;
		}
		return renderWidth;
	}
	
	
	String getData(Class<? extends ItemProperty> property, int propDataIndex) {
		type.checkDataAccess(property, propDataIndex);
		return data[type.getPropDataIndex(property)+propDataIndex];
	}
	
	void setData(Class<? extends ItemProperty> property, int propDataIndex, String data) {
		type.checkDataAccess(property, propDataIndex);
		this.data[type.getPropDataIndex(property)+propDataIndex] = data;
	}
	
	
	public Item copy() { return new Item(type, data); }
	
	@Override
	public int hashCode() { return name.hashCode() * 47 + Arrays.hashCode(data); }
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Item)) return false;
		Item o = (Item) other;
		return type.equals(o.type) && Arrays.equals(data, o.data);
	}
	
	@Override public String toString() { return name + " Item"; }
}
