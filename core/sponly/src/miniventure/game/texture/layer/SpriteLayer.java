package miniventure.game.texture.layer;

import miniventure.game.texture.TextureHolder;

// single sprite
public class SpriteLayer implements RenderLayer {
	
	private final TextureHolder texture;
	
	public SpriteLayer(TextureHolder texture) {
		this.texture = texture;
	}
	
	@Override
	public TextureHolder getSprite() {
		return texture;
	}
}
