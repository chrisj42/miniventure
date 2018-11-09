package miniventure.game.world.tile;

import java.util.*;

import miniventure.game.item.Item;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RenderTile extends Tile {
	
	private ArrayList<TileAnimation<TextureHolder>> spriteStack;
	private ArrayList<ClientTileType> typeStack;
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
			if(spriteStack == null || updateSprites)
				compileSprites(); // since the lock can be reacquired by the same thread, this is fine to put in a synchronized statement.
		}
		
		synchronized (spriteLock) {
			for(int i = 0; i < spriteStack.size(); i++) {
				TileAnimation<TextureHolder> animation = spriteStack.get(i);
				//typeStack.get(i).getRenderer().transitionManager.tryFinishAnimation(this);
				batch.draw(animation.getKeyFrame(this).texture, (x - posOffset.x) * SIZE, (y - posOffset.y + typeStack.get(i).getZOffset()) * SIZE);
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
	@Override public boolean interactWith(Player player, @Nullable Item heldItem) { return false; }
	@Override public boolean attackedBy(WorldObject obj, @Nullable Item item, int dmg) { return false; }
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
		TreeMap<TileTypeEnum, ClientTileType> allTypes = new TreeMap<>(); // overlap
		EnumMap<RelPos, EnumSet<TileTypeEnum>> typesAtPositions = new EnumMap<>(RelPos.class); // connection
		EnumMap<TileTypeEnum, EnumSet<RelPos>> typePositions = new EnumMap<>(TileTypeEnum.class); // overlap
		
		for (RelPos rp: RelPos.values()) {
			int x = rp.getX();
			int y = rp.getY();
			RenderTile oTile = (RenderTile) getLevel().getTile(this.x + x, this.y + y);
			List<ClientTileType> aroundTypes = oTile != null ? oTile.getTypeStack().getTypes() : Collections.emptyList();
			
			EnumMap<TileTypeEnum, ClientTileType> typeMap = new EnumMap<>(TileTypeEnum.class);
			for(ClientTileType type: aroundTypes) {
				typeMap.put(type.getTypeEnum(), type);
				typePositions.computeIfAbsent(type.getTypeEnum(), k -> EnumSet.noneOf(RelPos.class));
				typePositions.get(type.getTypeEnum()).add(rp);
			}
			allTypes.putAll(typeMap);
			typesAtPositions.put(rp, MyUtils.enumSet(typeMap.keySet()));
		}
		
		// all tile types have been fetched. Now accumulate the sprites.
		ArrayList<TileAnimation<TextureHolder>> spriteStack = new ArrayList<>(16);
		ArrayList<ClientTileType> typeStack = new ArrayList<>(16);
		//ArrayList<String> spriteNames = new ArrayList<>(16);
		
		// get overlap data, in case it's needed
		//EnumMap<TileTypeEnum, EnumSet<RelPos>> typePositions = OverlapManager.mapTileTypesAround(this);
		
		// iterate through main stack from bottom to top, adding connection and overlap sprites each level.
		List<ClientTileType> types = getTypeStack().getTypes();
		for(int i = 1; i <= types.size(); i++) {
			ClientTileType cur = i < types.size() ? types.get(i) : null;
			ClientTileType prev = types.get(i-1);
			
			// add connection sprite (or transition) for prev
			TileAnimation<TextureHolder> animation = prev.getRenderer().getConnectionSprite(this, typesAtPositions);
			spriteStack.add(animation);
			typeStack.add(prev);
			//spriteNames.add(animation.getKeyFrames()[0].name);
			
			// check for overlaps that are above prev AND below cur
			NavigableMap<TileTypeEnum, ClientTileType> overlapMap;
			if(cur == null)
				overlapMap = allTypes.subMap(prev.getTypeEnum(), false, allTypes.lastKey(), !allTypes.lastKey().equals(prev.getTypeEnum()));
			else
				overlapMap = allTypes.subMap(prev.getTypeEnum(), false, cur.getTypeEnum(), false);
			
			if(overlapMap.size() > 0) { // add found overlaps
				overlapMap.forEach((enumType, tileType) -> {
					ArrayList<TileAnimation<TextureHolder>> sprites = tileType.getRenderer().getOverlapSprites(typePositions.get(enumType));
					for(TileAnimation<TextureHolder> sprite: sprites) {
						spriteStack.add(sprite);
						typeStack.add(tileType);
						//spriteNames.add(sprite.getKeyFrames()[0].name);
					}
				});
			}
		}
		
		
		// now that we have the new sprites, we need to make sure that the animations remain continuous.
		// to do that, we need to store the run duration of each one, and keep it if the animation is still here.
		// to do *that*, we need a way to identify animations: the ClientTileType name combined with the sprite name.
		// So I'll have to store them together.
		
		// The code below: set the sprites, then check each name in the deltaMap against those in the sprite stack.
		// if in both, don't touch. If in stack only, add to deltaMap with delta 0. If in map only, remove it.
		
		// remember: minimize amount of code in synchronized statements (aka split up synchronized statements where possible), but obviously make sure any breaks in synchronization don't break things.
		
		synchronized (spriteLock) {
			this.spriteStack = spriteStack;
			this.typeStack = typeStack;
			updateSprites = false;
		}
	}
}
