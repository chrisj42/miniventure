package miniventure.game.texture;

import miniventure.game.GameCore;
import miniventure.game.util.function.MapFunction;

public enum ItemTextureSource {
	Icon_Map(GameCore.icons::get),
	Entity_Atlas(GameCore.entityAtlas::getRegion),
	Tile_Atlas(GameCore.tileAtlas::getRegion);
	
	private final MapFunction<String, TextureHolder> textureFetcher;
	
	ItemTextureSource(MapFunction<String, TextureHolder> textureFetcher) {
		this.textureFetcher = textureFetcher;
	}
	
	public TextureHolder getTexture(String textureName) {
		return textureFetcher.get(textureName);
	}
	
	public FetchableTextureHolder get(String textureName) {
		return new FetchableTextureHolder(this, textureFetcher.get(textureName));
	}
	
	public static final ItemTextureSource[] values = ItemTextureSource.values();
}
