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
	}
	public TextureAtlasHolder(TextureAtlasData data) {
		atlas = null;
		regions = data.getRegions();
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
