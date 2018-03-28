package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;

import miniventure.game.world.ClientLevel;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ClientTile extends Tile {
	
	@NotNull private ClientLevel level;
	
	public ClientTile(@NotNull ClientLevel level, int x, int y, @NotNull TileType[] types, String[] data) {
		super(level, x, y, types, data);
		this.level = level;
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		/*
			- Get the surrounding tile types for a tile
			- draw an overlap only after all the centers under it have been drawn
				So, before drawing an overlap, check that the current center is supposed to be drawn under it.
		 */
		
		TileType[][] aroundTypes = new TileType[9][];
		int idx = 0;
		for(int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				Tile oTile = level.getTile(this.x + x, this.y + y);
				if(x == 0 && y == 0) {if(oTile != this) throw new IllegalStateException("Level reference or position of Tile " + this + " is faulty; Level "+level+" returns Tile " + oTile + " at position "+this.x+","+this.y+"."); aroundTypes[idx] = new TileType[0]; }
				else aroundTypes[idx] = oTile != null ? oTile.getTypes() : new TileType[0];
				idx++;
			}
		}
		
		Array<AtlasRegion> sprites = new Array<>();
		
		TileType[] mainTypes = getTypes();
		int firstIdx = 0;
		for(int i = mainTypes.length-1; i >= 0; i--) {
			if(getProp(mainTypes[i], TilePropertyType.Render).isOpaque()) {
				firstIdx = i;
				break;
			}
		}
		
		// To find overlap sprites, it's easier if the tiles are sorted by TileType first, and then position.
		TreeMap<TileType, Boolean[]> overlappingTypes = new TreeMap<>();
		Boolean[] model = new Boolean[9];
		Arrays.fill(model, Boolean.FALSE);
		for(int i = 0; i < aroundTypes.length; i++) {
			for (TileType oType : aroundTypes[i]) { // doesn't matter the order.
				if(!getProp(oType, TilePropertyType.Overlap).canOverlap()) continue; // the type can't even overlap anyway.
				//if(TileType.tileSorter.compare(mainTypes[firstIdx], oType) >= 0) continue; // the type is lower than the lowest *visible* main type.
				overlappingTypes.putIfAbsent(oType, Arrays.copyOf(model, model.length));
				overlappingTypes.get(oType)[i] = true;
			}
		}
		
		Iterator<TileType> overlapTypeIter = overlappingTypes.tailMap(mainTypes[firstIdx], false).keySet().iterator();
		TileType overlapType = overlapTypeIter.hasNext() ? overlapTypeIter.next() : null; // this type will always be just above mainTypes[firstIdx].
		
		for(int i = firstIdx; i < mainTypes.length; i++) {
			// before we use the connection property of the main type, let's check and see if we are transitioning.
			if(i == mainTypes.length - 1 && getProp(mainTypes[i], TilePropertyType.Transition).playingAnimation(this)) // only the top tile can ever be transitioning.
				sprites.add(getProp(mainTypes[i], TilePropertyType.Transition).getAnimationFrame(this, delta));
			else // otherwise, use connection sprite.
				sprites.add(getProp(mainTypes[i], TilePropertyType.Connect).getSprite(this, aroundTypes));
			
			while(overlapType != null && (i >= mainTypes.length-1 || mainTypes[i+1].compareTo(overlapType) > 0)) {
				sprites.addAll(getProp(mainTypes[i], TilePropertyType.Overlap).getSprites(this, overlapType, overlappingTypes.get(overlapType)));
				overlapType = overlapTypeIter.hasNext() ? overlapTypeIter.next() : null;
			}
		}
		
		
		for(AtlasRegion texture: sprites)
			batch.draw(texture, (x-posOffset.x) * SIZE, (y-posOffset.y) * SIZE);
	}
	
}
