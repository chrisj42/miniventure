package miniventure.game.item.type;

import miniventure.game.item.Item;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ToolItem extends Item {
	
	private static final float DURABILITY_BAR_HEIGHT = 4; // 8 pixels.
	
	public enum Material {
		Wood(30, 1, 5),
		
		Stone(80, 2, 5),
		
		Iron(250, 4, 4),
		
		Gem(800, 8, 3);
		
		public final int maxDurability; // the number of uses this level of tool gets.
		public final int damageMultiplier; // damage done by this tool is multiplied by this number.
		public final int staminaUsage; // the stamina points that are used up each use.
		
		Material(int maxDurability, int damageMultiplier, int staminaUsage) {
			this.maxDurability = maxDurability;
			this.damageMultiplier = damageMultiplier;
			this.staminaUsage = staminaUsage;
		}
	}
	
	
	
	private final ToolType toolType;
	private final Material material;
	private final int durability;
	
	public ToolItem(ToolType type, Material material) { this(type, material, material.maxDurability); }
	public ToolItem(ToolType type, Material material, int durability) {
		super(material.name() + " " + type.name(), type.texture==null?new TextureRegion():type.texture);
		this.toolType = type;
		this.material = material;
		this.durability = durability;
	}
	
	@Override public int getMaxStackSize() { return 1; }
	
	public ToolType getType() { return toolType; }
	public Material getMaterial() { return material; }
	
	@Override
	public ToolItem getUsedItem() {
		if(durability > 1)
			return new ToolItem(toolType, material, durability-1);
		
		return null;
	}
	
	@Override
	public int getStaminaUsage() { return material.staminaUsage; }
	
	@Override public boolean attack(WorldObject obj, Player player) {
		boolean success = obj.attackedBy(player, this, material.damageMultiplier);
		if(success) use();
		return success;
	}
	
	@Override
	public void drawItem(int stackSize, Batch batch, BitmapFont font, float x, float y, Color textColor) {
		super.drawItem(stackSize, batch, font, x, y, textColor);
		
		if(durability == material.maxDurability) return; // no bar
		
		// draw a colored bar for the durability left
		float durPerc = durability*1f / material.maxDurability;
		float width = Item.ICON_SIZE * durPerc;
		Color barColor = durPerc >= 0.5f ? Color.GREEN : durPerc >= 0.2f ? Color.YELLOW : Color.RED;
		MyUtils.fillRect(x, y, width, DURABILITY_BAR_HEIGHT, barColor, batch);
	}
	
	@Override
	public boolean equals(Object other) {
		return super.equals(other) && ((ToolItem)other).durability == durability;
	}
	
	@Override
	public int hashCode() { return super.hashCode() + durability; }
	
	@Override
	public ToolItem copy() { return new ToolItem(toolType, material, durability); }
}
