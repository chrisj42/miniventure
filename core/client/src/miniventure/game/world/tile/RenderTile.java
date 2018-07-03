package miniventure.game.world.tile;

import java.util.*;
import java.util.Map.Entry;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.RelPos;
import miniventure.game.world.Level;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.data.DataMap;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class RenderTile extends Tile {
	
	private ArrayList<Animation<TextureHolder>> spriteStack;
	private ArrayList<String> spriteNames;
	private final HashMap<String, Float> animationDeltas = new HashMap<>(16);
	
	private final Object spriteLock = new Object();
	
	public RenderTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types, DataMap[] data) {
		super(level, x, y, types, data);
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		if(getLevel().getTile(x, y) == null) return; // cannot render if there are no tiles.
		
		synchronized (spriteLock) {
			if(spriteStack == null) return; // cannot render if there are no sprites.
			
			for(int i = 0; i < spriteStack.size(); i++) {
				Animation<TextureHolder> animation = spriteStack.get(i);
				String name = spriteNames.get(i);
				float timeSinceLastUpdate = animationDeltas.get(name) + delta;
				batch.draw(animation.getKeyFrame(timeSinceLastUpdate).texture, (x - posOffset.x) * SIZE, (y - posOffset.y) * SIZE);
				animationDeltas.put(name, timeSinceLastUpdate);
			}
		}
	}
	
	/** @noinspection ObjectAllocationInLoop*/
	@Override
	@SuppressWarnings("unchecked")
	public void updateSprites() {
		TreeMap<TileTypeEnum, TileType> allTypes = new TreeMap<>();
		EnumMap<TileTypeEnum, TileType>[] typeMaps = (EnumMap<TileTypeEnum, TileType>[]) new EnumMap[9];
		EnumSet<TileTypeEnum>[] typeSets = (EnumSet<TileTypeEnum>[]) new EnumSet[9];
		
		int idx = 0;
		for(int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				Tile oTile = getLevel().getTile(this.x + x, this.y + y);
				TileType[] aroundTypes = oTile != null ? oTile.getTypeStack().getTypes() : new TileType[0];
				
				typeMaps[idx] = new EnumMap<>(TileTypeEnum.class);
				for(TileType type: aroundTypes)
					typeMaps[idx].put(type.getEnumType(), type);
				allTypes.putAll(typeMaps[idx]);
				typeSets[idx] = EnumSet.copyOf(typeMaps[idx].keySet());
				
				idx++;
			}
		}
		
		// all tile types have been fetched. Now accumulate the sprites.
		ArrayList<Animation<TextureHolder>> spriteStack = new ArrayList<>(16);
		ArrayList<String> spriteNames = new ArrayList<>(16);
		
		// get overlap data, in case it's needed
		EnumMap<TileTypeEnum, EnumSet<RelPos>> overlapData = OverlapManager.mapTileTypesAround(this);
		
		// iterate through main stack from bottom to top, adding connection and overlap sprites each level.
		Map.Entry<TileTypeEnum, TileType> prev = null, cur;
		Iterator<Entry<TileTypeEnum, TileType>> iter = typeMaps[4].entrySet().iterator();
		while(iter.hasNext() || prev != null) {
			cur = iter.hasNext() ? iter.next() : null;
			if(prev == null) {
				prev = cur;
				continue;
			}
			
			// add connection sprite (or transition) for prev
			spriteStack.add(prev.getValue().getRenderer().getConnectionSprite(this, typeSets));
			spriteNames.add(prev.getKey()+"/"+spriteStack.get(spriteStack.size()-1).getKeyFrames()[0].name);
			
			// check for overlaps that are above prev AND below cur
			NavigableMap<TileTypeEnum, TileType> overlapMap;
			if(cur == null)
				overlapMap = allTypes.subMap(prev.getKey(), false, allTypes.lastKey(), !allTypes.lastKey().equals(prev.getKey()));
			else
				overlapMap = allTypes.subMap(prev.getKey(), false, cur.getKey(), false);
			
			if(overlapMap.size() > 0) { // add found overlaps
				overlapMap.forEach((enumType, tileType) -> {
					ArrayList<Animation<TextureHolder>> sprites = tileType.getRenderer().getOverlapSprites(overlapData.get(enumType));
					for(Animation<TextureHolder> sprite: sprites) {
						spriteStack.add(sprite);
						spriteNames.add(enumType+"/"+sprite.getKeyFrames()[0].name);
					}
				});
			}
			
			prev = cur; // move to next layer
		}
		
		
		// now that we have the new sprites, we need to make sure that the animations remain continuous.
		// to do that, we need to store the run duration of each one, and keep it if the animation is still here.
		// to do *that*, we need a way to identify animations: the TileType name combined with the sprite name.
		// So I'll have to store them together.
		
		// The code below: set the sprites, then check each name in the deltaMap against those in the sprite stack.
		// if in both, don't touch. If in stack only, add to deltaMap with delta 0. If in map only, remove it.
		
		// remember: minimize amount of code in synchronized statements (aka split up synchronized statements where possible), but obviously make sure any breaks in synchronization don't break things.
		
		synchronized (spriteLock) {
			this.spriteStack = spriteStack;
			this.spriteNames = spriteNames;
			
			animationDeltas.keySet().retainAll(spriteNames);
			for(String name: spriteNames)
				animationDeltas.putIfAbsent(name, 0f);
		}
	}
}
