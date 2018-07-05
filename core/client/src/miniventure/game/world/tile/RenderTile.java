package miniventure.game.world.tile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.NavigableMap;
import java.util.TreeMap;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.RelPos;
import miniventure.game.world.Level;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.data.DataMap;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class RenderTile extends Tile {
	
	private ArrayList<TileAnimation<TextureHolder>> spriteStack;
	//private ArrayList<String> spriteNames;
	//private final HashMap<String, Float> animationDeltas = new HashMap<>(16);
	
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
				TileAnimation<TextureHolder> animation = spriteStack.get(i);
				//String name = spriteNames.get(i);
				//float timeSinceLastUpdate = animationDeltas.get(name) + delta;
				batch.draw(animation.getKeyFrame(this).texture, (x - posOffset.x) * SIZE, (y - posOffset.y) * SIZE);
				//animationDeltas.put(name, timeSinceLastUpdate);
			}
		}
	}
	
	/** @noinspection ObjectAllocationInLoop*/
	@Override
	@SuppressWarnings("unchecked")
	public void updateSprites() {
		super.updateSprites();
		TreeMap<TileTypeEnum, TileType> allTypes = new TreeMap<>(); // overlap
		EnumMap<RelPos, EnumSet<TileTypeEnum>> typesAtPositions = new EnumMap<>(RelPos.class); // connection
		EnumMap<TileTypeEnum, EnumSet<RelPos>> typePositions = new EnumMap<>(TileTypeEnum.class); // overlap
		
		for (RelPos rp: RelPos.values()) {
			int x = rp.getX();
			int y = rp.getY();
			Tile oTile = getLevel().getTile(this.x + x, this.y + y);
			TileType[] aroundTypes = oTile != null ? oTile.getTypeStack().getTypes() : new TileType[0];
			
			EnumMap<TileTypeEnum, TileType> typeMap = new EnumMap<>(TileTypeEnum.class);
			for(TileType type: aroundTypes) {
				typeMap.put(type.getEnumType(), type);
				typePositions.computeIfAbsent(type.getEnumType(), k -> EnumSet.noneOf(RelPos.class));
				typePositions.get(type.getEnumType()).add(rp);
			}
			allTypes.putAll(typeMap);
			typesAtPositions.put(rp, typeMap.size() == 0 ? EnumSet.noneOf(TileTypeEnum.class) : EnumSet.copyOf(typeMap.keySet()));
		}
		
		// all tile types have been fetched. Now accumulate the sprites.
		ArrayList<TileAnimation<TextureHolder>> spriteStack = new ArrayList<>(16);
		//ArrayList<String> spriteNames = new ArrayList<>(16);
		
		// get overlap data, in case it's needed
		//EnumMap<TileTypeEnum, EnumSet<RelPos>> typePositions = OverlapManager.mapTileTypesAround(this);
		
		// iterate through main stack from bottom to top, adding connection and overlap sprites each level.
		TileType[] types = getTypeStack().getTypes();
		for(int i = 1; i <= types.length; i++) {
			TileType cur = i < types.length ? types[i] : null;
			TileType prev = types[i-1];
			
			// add connection sprite (or transition) for prev
			TileAnimation<TextureHolder> animation = prev.getRenderer().getConnectionSprite(this, typesAtPositions);
			spriteStack.add(animation);
			//spriteNames.add(animation.getKeyFrames()[0].name);
			
			// check for overlaps that are above prev AND below cur
			NavigableMap<TileTypeEnum, TileType> overlapMap;
			if(cur == null)
				overlapMap = allTypes.subMap(prev.getEnumType(), false, allTypes.lastKey(), !allTypes.lastKey().equals(prev.getEnumType()));
			else
				overlapMap = allTypes.subMap(prev.getEnumType(), false, cur.getEnumType(), false);
			
			if(overlapMap.size() > 0) { // add found overlaps
				overlapMap.forEach((enumType, tileType) -> {
					ArrayList<TileAnimation<TextureHolder>> sprites = tileType.getRenderer().getOverlapSprites(typePositions.get(enumType));
					for(TileAnimation<TextureHolder> sprite: sprites) {
						spriteStack.add(sprite);
						//spriteNames.add(sprite.getKeyFrames()[0].name);
					}
				});
			}
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
			// this.spriteNames = spriteNames;
			//
			// animationDeltas.keySet().retainAll(spriteNames);
			// for(String name: spriteNames)
			// 	animationDeltas.putIfAbsent(name, 0f);
		}
	}
}
