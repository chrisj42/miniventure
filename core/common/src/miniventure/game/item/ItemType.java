package miniventure.game.item;

import miniventure.game.texture.TextureHolder;

import org.jetbrains.annotations.NotNull;

public enum ItemType {
	
	Tool,
	Food,
	Tile,
	Resource,
	Entity,
	Misc;
	
	static abstract class SimpleEnumItem extends Item {
		
		SimpleEnumItem(@NotNull ItemType type, @NotNull String name) {
			super(type, name);
		}
		
		SimpleEnumItem(@NotNull ItemType type, @NotNull String name, @NotNull TextureHolder texture) {
			super(type, name, texture);
		}
		
		@Override
		public String[] save() {
			return new String[] {getType().name(), getName()};
		}
		
		@Override
		public Item copy() { return this; }
	}
}
