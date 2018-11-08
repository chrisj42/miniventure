package miniventure.game.texture;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.utils.Array;

public class TextureAtlasHolder {
	
	public final TextureAtlas atlas;
	private final Array<Region> regions;
	
	public TextureAtlasHolder(TextureAtlas atlas) {
		this.atlas = atlas;
		regions = null;
		for(AtlasRegion region: atlas.getRegions())
			region.name = fixPath(region.name);
	}
	public TextureAtlasHolder(TextureAtlasData data) {
		atlas = null;
		regions = data.getRegions();
		for(Region region: regions)
			region.name = fixPath(region.name);
	}
	
	/*
		In the tile atlas, some folders are parented more for organization that isn't meant to show
		in the code and call names. These extra folders end in a "_", and it is meant that all subfolders
		would originally have been top-level folders with the name prefix of the parent folder's name.
		So, this converts the "_/" pattern you would see in the atlas to just a "_" that the code expects.
		
		Note that there should not be any files as direct children of folders ending in "_", only subfolders.
		All files should be contained in said subfolders.
	 */
	private static String fixPath(String name) {
		return name.replaceAll("(\\w+)_/(\\w+)", "$2_$1");
	}
	
	public TextureHolder findRegion(String name) {
		if(atlas != null)
			return new TextureHolder(atlas.findRegion(name));
		
		for(Region region: regions)
			if(region.name.equals(name))
				return new TextureHolder(region);
		
		return null;
	}
	
	public Array<TextureHolder> findRegions(String name) {
		Array<TextureHolder> textures = new Array<>(TextureHolder.class);
		if(atlas != null) {
			for(AtlasRegion region: atlas.findRegions(name))
				textures.add(new TextureHolder(region));
		}
		else {
			for(Region region : regions) {
				if(region.name.equals(name))
					textures.add(new TextureHolder(region));
			}
		}
		
		return textures;
	}
	
	public Array<TextureHolder> getRegions() {
		Array<TextureHolder> textures = new Array<>();
		if(atlas != null) {
			for(AtlasRegion r: atlas.getRegions()) {
				textures.add(new TextureHolder(r));
			}
		}
		else {
			for(Region r: regions)
				textures.add(new TextureHolder(r));
		}
		
		return textures;
	}
	
	public void dispose() {
		if(atlas != null)
			atlas.dispose();
	}
	
}
