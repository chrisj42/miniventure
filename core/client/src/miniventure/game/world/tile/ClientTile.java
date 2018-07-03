package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;

import miniventure.game.texture.TextureHolder;
import miniventure.game.world.ClientLevel;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.data.DataMap;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ClientTile extends Tile {
	
	@NotNull private ClientLevel level;
	
	public ClientTile(@NotNull ClientLevel level, int x, int y, @NotNull TileTypeEnum[] types, DataMap[] data) {
		super(level, x, y, types, data);
		this.level = level;
	}
	
	@NotNull @Override
	public ClientLevel getLevel() { return level; }
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		render(this, batch, delta, posOffset);
	}
	public static void render(Tile t, SpriteBatch batch, float delta, Vector2 posOffset) {
		/*
			- Get the surrounding tile types for a tile
			- draw an overlap only after all the centers under it have been drawn
				So, before drawing an overlap, check that the current center is supposed to be drawn under it.
		 */
		
		// TODO Redo this method. Getting close to finishing!
		
		if(t.getLevel().getTile(t.x, t.y) == null) return; // cannot render if there are no tiles.
		
		TileType[][] aroundTypes = new TileType[9][];
		int idx = 0;
		for(int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				Tile oTile = t.getLevel().getTile(t.x + x, t.y + y);
				if(x == 0 && y == 0)
					aroundTypes[idx] = new TileType[0];
				else
					aroundTypes[idx] = oTile != null ? oTile.getTypeStack().getTypes() : new TileType[0];
				idx++;
			}
		}
		
		Array<TextureHolder> sprites = new Array<>();
		
		TileType[] mainTypes = t.getTypeStack().getTypes();
		int firstIdx = 0;
		for(int i = mainTypes.length-1; i >= 0; i--) {
			if(mainTypes[i].getRenderer().isOpaque()) {
				firstIdx = i;
				break;
			}
		}
		
		// To find overlap sprites, it's easier if the tiles are sorted by TileType first, and then position.
		TreeMap<TileType, Boolean[]> overlappingTypes = new TreeMap<>();
		Boolean[] model = new Boolean[9];
		Arrays.fill(model, Boolean.FALSE);
		for(int i = 0; i < aroundTypes.length; i++) {
			if(i == 4) continue; // skip the center
			for (TileType oType : aroundTypes[i]) { // doesn't matter the order.
				//if(!oType.getRenderer().canOverlap()) continue; // the type can't even overlap anyway.
				//if(TileType.tileSorter.compare(mainTypes[firstIdx], oType) >= 0) continue; // the type is lower than the lowest *visible* main type.
				overlappingTypes.putIfAbsent(oType, Arrays.copyOf(model, model.length));
				overlappingTypes.get(oType)[i] = true;
			}
		}
		
		Iterator<TileType> overlapTypeIter = overlappingTypes.tailMap(mainTypes[firstIdx], false).keySet().iterator();
		TileType overlapType = overlapTypeIter.hasNext() ? overlapTypeIter.next() : null; // this type will always be just above mainTypes[firstIdx].
		
		for(int i = firstIdx; i < mainTypes.length; i++) {
			// before we use the connection property of the main type, let's check and see if we are transitioning.
			if(i == mainTypes.length - 1 && t.getProp(mainTypes[i], TilePropertyType.Transition).playingAnimation(t)) // only the top tile can ever be transitioning.
				sprites.add(t.getProp(mainTypes[i], TilePropertyType.Transition).getAnimationFrame(t, delta));
			else // otherwise, use connection sprite.
				sprites.add(t.getProp(mainTypes[i], TilePropertyType.Connect).getSprite(t, aroundTypes));
			
			while(overlapType != null && (i >= mainTypes.length-1 || mainTypes[i+1].compareTo(overlapType) > 0)) {
				sprites.addAll(t.getProp(mainTypes[i], TilePropertyType.Overlap).getSprites(t, overlapType, overlappingTypes.get(overlapType)));
				overlapType = overlapTypeIter.hasNext() ? overlapTypeIter.next() : null;
			}
		}
		
		
		for(TextureHolder texture: sprites)
			batch.draw(texture.texture, (t.x-posOffset.x) * SIZE, (t.y-posOffset.y) * SIZE);
	}
	
	@Override
	public void updateSprites() {
		
	}
	
	@Override
	public String toString() { return getType().getEnumType()+" ClientTile"; }
	
}
