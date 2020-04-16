package miniventure.game.item;

// materials define durability, and add to sharpness (damage multiplier), and base weight (stamina drain)
public enum MaterialQuality {
	// starter
	Crude(50, 1, "flint"),
	// forest caves
	Basic(120, 2, "stone"),
	// desert caves
	Sturdy(250, 4, "iron"),
	// snowy caves
	Fine(600, 6, "tungsten"),
	// post-game
	Superior(1500, 8, "ruby");
	
	public final int maxDurability; // the number of uses this level of tool gets.
	public final int damageMultiplier; // damage done by this tool is multiplied by this number.
	// maybe make this a stat multiplier..?
	public final String spriteName;
	
	MaterialQuality(int maxDurability, int damageMultiplier, String spriteName) {
		this.maxDurability = maxDurability;
		this.damageMultiplier = damageMultiplier;
		this.spriteName = spriteName;
	}
}
