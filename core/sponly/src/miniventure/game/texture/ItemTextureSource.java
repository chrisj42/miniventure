package miniventure.game.texture;

import miniventure.game.core.GdxCore;
import miniventure.game.util.function.MapFunction;

public enum ItemTextureSource {
	Icon_Map(GdxCore.icons::get),
	Entity_Atlas(GdxCore.entityAtlas::getRegion),
	Tile_Atlas(GdxCore.tileAtlas::getRegion);
	
	private final MapFunction<String, TextureHolder> textureFetcher;
	
	ItemTextureSource(MapFunction<String, TextureHolder> textureFetcher) {
		this.textureFetcher = textureFetcher;
	}
	
	public TextureHolder get(String textureName) {
		return textureFetcher.get(textureName);
	}
	
	/*public FetchableTextureHolder get(String textureName) {
		return new FetchableTextureHolder(this, textureFetcher.get(textureName));
	}*/
	
	public static final ItemTextureSource[] values = ItemTextureSource.values();
}
