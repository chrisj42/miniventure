package miniventure.game.world.tile;

import java.util.*;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.RelPos;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RenderTile extends Tile {
	
	private TreeMap<Integer, ArrayList<TileAnimation<TextureHolder>>> spriteStacks;
	private TreeMap<Integer, ArrayList<ClientTileType>> typeStacks;
	private boolean updateSprites;
	
	private final Object spriteLock = new Object();
	
	public RenderTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types, @Nullable SerialMap[] data) {
		super(level, x, y, types);
		
		for(int i = 0; i < types.length; i++)
			this.dataMaps.put(types[i], data == null ? new SerialMap() : data[i]);
	}
	
	@Override
	ClientTileStack makeStack(@NotNull TileTypeEnum[] types) { return new ClientTileStack(types); }
	
	@Override
	public ClientTileStack getTypeStack() { return (ClientTileStack) super.getTypeStack(); }
	
	@Override
	public ClientTileType getType() { return (ClientTileType) super.getType(); }
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		if(getLevel().getTile(x, y) == null) return; // cannot render if there are no tiles.
		
		synchronized (spriteLock) {
			// cannot render if there are no sprites.
			if(spriteStacks == null || updateSprites)
				compileSprites(); // since the lock can be reacquired by the same thread, this is fine to put in a synchronized statement.
		}
		
		synchronized (spriteLock) {
			//noinspection KeySetIterationMayUseEntrySet
			for(int height: spriteStacks.keySet()) {
				ArrayList<TileAnimation<TextureHolder>> spriteStack = spriteStacks.get(height);
				ArrayList<ClientTileType> typeStack = typeStacks.get(height);
				for(int i = 0; i < spriteStack.size(); i++) {
					TileAnimation<TextureHolder> animation = spriteStack.get(i);
					//typeStack.get(i).getRenderer().transitionManager.tryFinishAnimation(this);
					batch.draw(animation.getKeyFrame(this).texture, (x - posOffset.x) * SIZE, (y - posOffset.y + typeStack.get(i).getZOffset()) * SIZE + height * SIZE/2f);
				}
			}
		}
	}
	
	@Override
	public float getLightRadius() {
		float maxRadius = 0;
		for(ClientTileType type: getTypeStack().getTypes())
			maxRadius = Math.max(maxRadius, type.getLightRadius());
		
		return maxRadius;
	}
	
	@Override public boolean isPermeable() { return getType().isWalkable(); }
	@Override public Result interactWith(Player player, @Nullable Item heldItem) { return Result.NONE; }
	@Override public Result attackedBy(WorldObject obj, @Nullable Item item, int dmg) { return Result.NONE; }
	@Override public boolean touchedBy(Entity entity) { return false; }
	@Override public void touching(Entity entity) {}
	
	public void updateSprites() {
		synchronized (spriteLock) {
			updateSprites = true;
		}
	}
	
	/** @noinspection ObjectAllocationInLoop*/
	@SuppressWarnings("unchecked")
	private void compileSprites() {
		TreeMap<Integer, TreeMap<TileTypeEnum, ClientTileType>> allTypes = new TreeMap<>(); // overlap
		TreeMap<Integer, EnumMap<RelPos, EnumSet<TileTypeEnum>>> typesAtPositions = new TreeMap<>(); // connection
		TreeMap<Integer, EnumMap<TileTypeEnum, EnumSet<RelPos>>> typePositions = new TreeMap<>(); // overlap/overhang
		// TreeMap<Integer, EnumSet<RelPos>> overhangPositions = new TreeMap<>(); // overhang; contains positions where there is overhang from the given tile, at the given height. Unlike overlap, overhang sprites are applied by the same tile and tile type that they are fetched from.
		
		for (RelPos rp: RelPos.values()) {
			int x = rp.getX();
			int y = rp.getY();
			RenderTile oTile = (RenderTile) getLevel().getTile(this.x + x, this.y + y);
			List<ClientTileType> aroundTypes = oTile != null ? oTile.getTypeStack().getTypes() : Collections.emptyList();
			
			// filling the type positions for overlap
			EnumMap<TileTypeEnum, ClientTileType> typeMap = new EnumMap<>(TileTypeEnum.class); // for allTypes
			EnumSet<TileTypeEnum> typeCache = EnumSet.noneOf(TileTypeEnum.class); // for typesAtPositions
			int curHeight = 0;
			// iterating over each visible TileType in the adjacent tile
			for(ClientTileType type: aroundTypes) {
				typeMap.put(type.getTypeEnum(), type); // for allTypes
				typeCache.add(type.getTypeEnum()); // for typesAtPositions
				typePositions.computeIfAbsent(curHeight, k -> new EnumMap<>(TileTypeEnum.class)).computeIfAbsent(type.getTypeEnum(), k -> EnumSet.noneOf(RelPos.class)).add(rp); // for typePositions
				if(type.hasVerticality()) { // store types that were at this height
					// only add 
					// todo before I can progress any further, I need to know how overhang sprites will work for the sides and top of the sprites.
					// overhangPositions.computeIfAbsent(curHeight, k -> new EnumMap<>(TileTypeEnum.class)).computeIfAbsent(type.getTypeEnum(), k -> EnumSet.noneOf(RelPos.class)).add(rp); // for overhangTypes
					// store in bulk
					allTypes.computeIfAbsent(curHeight, k -> new TreeMap<>()).putAll(typeMap);
					System.out.println("added: "+typeMap);
					typeMap.clear();
					// store by position
					typesAtPositions.computeIfAbsent(curHeight, k -> new EnumMap<>(RelPos.class)).computeIfAbsent(rp, k -> EnumSet.noneOf(TileTypeEnum.class)).addAll(typeCache);
					typeCache.clear();
					// inc height
					curHeight++;
				}
			}
			
			allTypes.computeIfAbsent(curHeight, k -> new TreeMap<>()).putAll(typeMap);
			// System.out.println("added: "+typeMap);
			typeMap.clear();
			// store by position
			typesAtPositions.computeIfAbsent(curHeight, k -> new EnumMap<>(RelPos.class)).computeIfAbsent(rp, k -> EnumSet.noneOf(TileTypeEnum.class)).addAll(typeCache);
			typeCache.clear();
		}
		
		// System.out.println(allTypes);
		
		// all tile types have been fetched. Now accumulate the sprites.
		TreeMap<Integer, ArrayList<TileAnimation<TextureHolder>>> spriteStacks = new TreeMap<>();
		TreeMap<Integer, ArrayList<ClientTileType>> typeStacks = new TreeMap<>();
		// ArrayList<TileAnimation<TextureHolder>> spriteStack = new ArrayList<>(16);
		// ArrayList<ClientTileType> typeStack = new ArrayList<>(16);
		//ArrayList<String> spriteNames = new ArrayList<>(16);
		
		// get overlap data, in case it's needed
		//EnumMap<TileTypeEnum, EnumSet<RelPos>> typePositions = OverlapManager.mapTileTypesAround(this);
		
		// iterate through main stack from bottom to top, adding connection and overlap and overhang sprites each level.
		List<ClientTileType> types = getTypeStack().getTypes();
		int curHeight = getTypeStack().getLowestRenderHeight();
		for(int i = 1; i <= types.size(); i++) {
			ClientTileType cur = i < types.size() ? types.get(i) : null;
			ClientTileType prev = types.get(i-1);
			int prevHeight = curHeight;
			if(prev.hasVerticality())
				curHeight++;
			
			ArrayList<TileAnimation<TextureHolder>> spriteStack = spriteStacks.computeIfAbsent(prevHeight, k -> new ArrayList<>(16));
			ArrayList<ClientTileType> typeStack = typeStacks.computeIfAbsent(prevHeight, k -> new ArrayList<>(16));
			
			// add connection sprite (or transition) for prev
			TileAnimation<TextureHolder> animation = prev.getRenderer().getConnectionSprite(this, typesAtPositions.get(prevHeight));
			spriteStack.add(animation);
			typeStack.add(prev);
			//spriteNames.add(animation.getKeyFrames()[0].name);
			
			// check for overlaps that are above prev AND below cur; but they must be on the same level
			NavigableMap<TileTypeEnum, ClientTileType> overlapMap;
			TreeMap<TileTypeEnum, ClientTileType> typesAtHeight = allTypes.get(prevHeight);
			if(cur == null) // top of the stack
				overlapMap = typesAtHeight.subMap(prev.getTypeEnum(), false, typesAtHeight.lastKey(), !typesAtHeight.lastKey().equals(prev.getTypeEnum()));
			else
				overlapMap = curHeight == prevHeight ? typesAtHeight.subMap(prev.getTypeEnum(), false, cur.getTypeEnum(), false) : new TreeMap<>();
			
			// we've found any overlap sprites; draw those, then check for overhang sprites
			
			if(overlapMap.size() > 0) { // add found overlaps
				overlapMap.forEach((enumType, tileType) -> {
					ArrayList<TileAnimation<TextureHolder>> sprites = tileType.getRenderer().getOverlapSprites(typePositions.get(prevHeight).get(enumType));
					for(TileAnimation<TextureHolder> sprite: sprites) {
						spriteStack.add(sprite);
						typeStack.add(tileType);
						//spriteNames.add(sprite.getKeyFrames()[0].name);
					}
				});
			}
			
			// also note that when rendering overhangs, in order to render, the tile type must be next to a tile with a lower minimum renderable height.
			// overhang sprites are only considered when the prev tile type has verticality.
			
			// check for overhang sprites
			// overhang sprites must have a greater level than those around to render
			
		}
		
		
		// now that we have the new sprites, we need to make sure that the animations remain continuous.
		// to do that, we need to store the run duration of each one, and keep it if the animation is still here.
		// to do *that*, we need a way to identify animations: the ClientTileType name combined with the sprite name.
		// So I'll have to store them together.
		
		// The code below: set the sprites, then check each name in the deltaMap against those in the sprite stack.
		// if in both, don't touch. If in stack only, add to deltaMap with delta 0. If in map only, remove it.
		
		// remember: minimize amount of code in synchronized statements (aka split up synchronized statements where possible), but obviously make sure any breaks in synchronization don't break things.
		
		synchronized (spriteLock) {
			this.spriteStacks = spriteStacks;
			this.typeStacks = typeStacks;
			updateSprites = false;
		}
	}
}
