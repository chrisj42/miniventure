package miniventure.game.texture;

import java.util.HashMap;
import java.util.LinkedList;

import miniventure.game.util.function.ValueAction;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.utils.Array;

public class TextureAtlasHolder {
	
	private final TextureAtlas atlas;
	
	private final TextureHolder[] holders;
	private final HashMap<String, Array<TextureHolder>> holderMap = new HashMap<>();
	
	public TextureAtlasHolder(TextureAtlas atlas) {
		this.atlas = atlas;
		
		LinkedList<TextureHolder> list = new LinkedList<>();
		for(AtlasRegion r: atlas.getRegions()) {
			TextureHolder t = new TextureHolder(r);
			list.add(t);
			holderMap.computeIfAbsent(t.name, name -> new Array<>(TextureHolder.class)).add(t);
		}
		
		holders = list.toArray(new TextureHolder[0]);
	}
	public TextureAtlasHolder(TextureAtlasData data) {
		atlas = null;
		Array<Region> regions = data.getRegions();
		holders = new TextureHolder[regions.size];
		int i = 0;
		for(Region r: regions) {
			TextureHolder t = new TextureHolder(r);
			holders[i++] = t;
			holderMap.computeIfAbsent(t.name, name -> new Array<>(TextureHolder.class)).add(t);
		}
	}
	
	public int countRegions(String name) {
		Array<TextureHolder> ar = holderMap.get(name);
		return ar == null ? 0 : ar.size;
	}
	
	public TextureHolder getRegion(String name) {
		Array<TextureHolder> ar = holderMap.get(name);
		return ar == null ? null : ar.get(0);
	}
	
	public Array<TextureHolder> getRegions(String name) {
		return holderMap.getOrDefault(name, new Array<>(TextureHolder.class));
	}
	
	public void iterRegions(ValueAction<TextureHolder> action) {
		for(TextureHolder t: holders)
			action.act(t);
	}
	
	public void dispose() {
		if(atlas != null)
			atlas.dispose();
	}
	
}
