package miniventure.game.world.tile;

import miniventure.game.util.MyUtils;

public abstract class TileType {
	
	private final TileTypeEnum enumType;
	
	TileType(TileTypeEnum enumType) { this.enumType = enumType; }
	
	public TileTypeEnum getTypeEnum() { return enumType; }
	
	public String getName() { return MyUtils.toTitleCase(enumType.name(), "_"); }
	
	public static void init() {
		System.out.println("tiletype init"); // quick test, delete later
	}
}
