package miniventure.game.texture;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;

public class TextureHolder {
	
	public final AtlasRegion texture;
	public final int width, height;
	public final String name;
	
	public TextureHolder(AtlasRegion texture) {
		this(texture, texture.getRegionWidth(), texture.getRegionHeight());
	}
	
	public TextureHolder(Region region) { this(null, region.name, region.width, region.height); }
	
	public TextureHolder(AtlasRegion texture, int width, int height) { this(texture, texture.name, width, height); }
	public TextureHolder(AtlasRegion texture, String name, int width, int height) {
		this.texture = texture;
		this.name = name;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String toString() { return "TextureHolder["+name+','+width+'x'+height+", AtlasRegion:"+texture+']'; }
}
