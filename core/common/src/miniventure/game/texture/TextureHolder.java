package miniventure.game.texture;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;

public class TextureHolder {
	
	public final AtlasRegion texture;
	public final int width, height;
	public final String name;
	
	public TextureHolder(AtlasRegion texture) {
		this(texture, texture.name, texture.getRegionWidth(), texture.getRegionHeight());
	}
	
	public TextureHolder(Region region) { this(null, region.name, region.width, region.height); }
	
	private TextureHolder(AtlasRegion texture, String name, int width, int height) {
		this.texture = texture;
		this.name = fixPath(name);
		this.width = width;
		this.height = height;
	}
	
	/*
		In the tile atlas, some folders are parented more for organization that isn't meant to show
		in the code and call names. These extra folders end in a "_", and it is meant that all subfolders
		would originally have been top-level folders with the name prefix of the parent folder's name.
		So, this converts the "_/" pattern you would see in the atlas to just a "_" that the code expects.
		
		Note that there should not be any files as direct children of folders ending in "_", only subfolders.
		All files should be contained in said subfolders.
	 */
	static String fixPath(String name) {
		return name.replaceAll("(\\w+)_/(\\w+)", "$2_$1");
	}
	
	@Override
	public String toString() { return "TextureHolder["+name+','+width+'x'+height+", AtlasRegion:"+texture+']'; }
}
