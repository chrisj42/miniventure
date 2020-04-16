package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.NavigableSet;
import java.util.TreeSet;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.util.RelPos;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.level.Level;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RenderTile extends Tile {
	
	private Array<TileAnimation> spriteStack;
	private Array<Integer> spritesPerLayer;
	private boolean updateSprites = true;
	
	// spriteLock is unnecessary because all access occurs in the same thread: the libGDX render thread.
	// private final Object spriteLock = new Object();
	
	public RenderTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types, @Nullable TileStackData data) {
		super(level, x, y, types, data);
		spriteStack = new Array<>(true, 16, TileAnimation.class);
		spritesPerLayer = new Array<>(true, 4, int.class);
	}
	
	@Override
	public ClientTileType getType() { return (ClientTileType) super.getType(); }
	
	@Override
	ClientTileType getLayer(int layer) { return (ClientTileType) super.getLayer(layer); }
	
	@Override
	Iterable<ClientTileType> getStack() {
		return (Iterable<ClientTileType>) super.getStack();
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		if(getLevel().getTile(x, y) == null) return; // cannot render if there are no tiles.
		
		// make sure the sprites are up to date before rendering
		if(updateSprites)
			compileSprites();
		
		renderSprites(batch, posOffset);
	}
	
	public void renderSprites(SpriteBatch batch, Vector2 posOffset) {
		int layer = -1;
		int spritesLeft = 0;
		for(TileAnimation animation: spriteStack) {
			while(spritesLeft == 0) {
				layer++;
				spritesLeft = spritesPerLayer.get(layer);
			}
			batch.draw(animation.getKeyFrame(setContext(layer)).texture, (x - posOffset.x) * SIZE, (y - posOffset.y) * SIZE);
			spritesLeft--;
		}
	}
	
	@Override
	public float getLightRadius() {
		float maxRadius = 0;
		for(ClientTileType type: getStack())
			maxRadius = Math.max(maxRadius, type.getLightRadius());
		
		return maxRadius;
	}
	
	@Override public boolean isPermeable() { return getType().isWalkable(); }
	@Override public Result interactWith(Player player, @Nullable Item heldItem) { return Result.NONE; }
	@Override public Result attackedBy(WorldObject obj, @Nullable Item item, int dmg) { return Result.NONE; }
	@Override public boolean touchedBy(Entity entity) { return false; }
	@Override public void touching(Entity entity) {}
	
	public void updateSprites() {
		updateSprites = true;
	}
	
	// compile sprites in each layer group separately; additionally, for each layer, end it with an air tile. Transparency will be implemented afterwards, once I've implemented this and also changed tile stacks; see in TileStack.java. Following initial implementation of transparency, and the rest, test it out with the level gen.
	
	/** @noinspection ObjectAllocationInLoop*/
	@SuppressWarnings("unchecked")
	private void compileSprites() {
		TreeSet<TileTypeEnum> allTypes = new TreeSet<>(); // overlap
		EnumMap<RelPos, EnumSet<TileTypeEnum>> typesAtPositions = new EnumMap<>(RelPos.class); // connection
		EnumMap<TileTypeEnum, EnumSet<RelPos>> typePositions = new EnumMap<>(TileTypeEnum.class); // overlap
		
		for (RelPos rp: RelPos.values()) {
			int x = rp.getX();
			int y = rp.getY();
			RenderTile oTile = (RenderTile) getLevel().getTile(this.x + x, this.y + y);
			EnumSet<TileTypeEnum> typeSet = EnumSet.noneOf(TileTypeEnum.class);
			if(oTile != null) {
				for (TileType type: oTile.getStack()) {
					typeSet.add(type.getTypeEnum());
					typePositions.computeIfAbsent(type.getTypeEnum(), k -> EnumSet.noneOf(RelPos.class)).add(rp);
				}
				if (oTile.getType().getTypeEnum() == TileTypeEnum.STONE && getType().getTypeEnum() != TileTypeEnum.STONE) {
					typeSet.add(TileTypeEnum.AIR);
					typePositions.computeIfAbsent(TileTypeEnum.AIR, k -> EnumSet.noneOf(RelPos.class)).add(rp);
				}
			}
			allTypes.addAll(typeSet);
			typesAtPositions.put(rp, typeSet);
		}
		
		// all tile types have been fetched. Now accumulate the sprites.
		// ArrayList<TileAnimation> spriteStack = new ArrayList<>(16);
		spriteStack.clear();
		spritesPerLayer.clear();
		
		// iterate through main stack from bottom to top, adding connection and overlap sprites each level.
		for(int i = 1; i <= getStackSize(); i++) {
			ClientTileType cur = i < getStackSize() ? getLayer(i) : null;
			ClientTileType prev = getLayer(i-1);
			
			int startSize = spriteStack.size;
			// add connection sprite (or transition) for prev
			prev.getRenderer().addCoreSprites(setContext(i-1), typesAtPositions, spriteStack);
			
			// check for overlaps that are above prev AND below cur
			NavigableSet<TileTypeEnum> overlapSet;
			if(cur == null)
				overlapSet = safeSubSet(allTypes, prev.getTypeEnum(), false, allTypes.last(), !allTypes.last().equals(prev.getTypeEnum()));
			else
				overlapSet = safeSubSet(allTypes, prev.getTypeEnum(), false, cur.getTypeEnum(), false);
			
			if(overlapSet.size() > 0) { // add found overlaps
				overlapSet.forEach(enumType -> {
					ClientTileType tileType = ClientTileType.get(enumType);
					tileType.getRenderer().addOverlapSprites(typePositions.get(enumType), spriteStack);
				});
			}
			// record the number of sprites added this layer
			spritesPerLayer.add(spriteStack.size - startSize);
		}
		
		updateSprites = false;
	}
	
	private static <E extends Enum<E>> NavigableSet<E> safeSubSet(TreeSet<E> set,
											 E fromElement, boolean fromInclusive,
											 E toElement,   boolean toInclusive) {
		if(fromElement.compareTo(toElement) > 0)
			return new TreeSet<>();
		else
			return set.subSet(fromElement, fromInclusive, toElement, toInclusive);
	}
}
