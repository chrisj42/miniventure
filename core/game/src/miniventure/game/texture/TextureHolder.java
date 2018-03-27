package miniventure.game.texture;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureHolder {
	
	public final TextureRegion texture;
	public final int width, height;
	
	public TextureHolder(TextureRegion texture) {
		this(texture, texture.getRegionWidth(), texture.getRegionHeight());
	}
	
	public TextureHolder(Region region) { this(null, region.width, region.height); }
	
	public TextureHolder(TextureRegion texture, int width, int height) {
		this.texture = texture;
		this.width = width;
		this.height = height;
	}
	
}
