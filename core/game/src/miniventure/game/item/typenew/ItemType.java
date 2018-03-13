package miniventure.game.item.typenew;

import miniventure.game.api.APIObjectType;
import miniventure.game.api.PropertyFetcher;
import miniventure.game.api.TypeLoader;

import org.jetbrains.annotations.NotNull;

public enum ItemType implements APIObjectType<ItemType, ItemProperty> {
	
	Sword(() -> new ItemProperty[] {
		
	}),
	
	Apple(() -> new ItemProperty[] {
		new EdibleProperty(2)
	}),
	
	// resource items; these don't really have any special properties.
	
	Log;
	
	
	
	
	private final PropertyFetcher<ItemProperty> propertyFetcher;
	ItemType() { this(() -> new ItemProperty[0]); }
	ItemType(@NotNull PropertyFetcher<ItemProperty> fetcher) { this.propertyFetcher = fetcher; }
	
	@Override
	public ItemProperty[] getProperties() { return propertyFetcher.getProperties(); }
	
	@Override public Class<ItemType> getTypeClass() { return ItemType.class; }
	@Override public ItemType getInstance() { return this; }
	
	static {
		TypeLoader.loadType(ItemType.class, ItemProperty.getDefaultPropertyFetcher());
	}
}
