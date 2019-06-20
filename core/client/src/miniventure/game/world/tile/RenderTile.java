package miniventure.game.world.tile;

import java.util.*;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.util.RelPos;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.level.Level;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RenderTile extends Tile {
	
	private ArrayList<TileAnimation> spriteStack;
	private boolean updateSprites;
	
	// spriteLock is unnecessary because all access occurs in the same thread: the libGDX render thread.
	// private final Object spriteLock = new Object();
	
	public RenderTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types, @Nullable SerialMap[] data) {
		super(level, x, y, types, data);
		
		// for(int i = 0; i < types.length; i++)
		// 	this.dataMaps.put(types[i], data == null ? new SerialMap() : data[i]);
	}
	
	@Override
	ClientTileStack makeStack(@NotNull TileTypeEnum[] types, @Nullable SerialMap[] dataMaps) {
		return new ClientTileStack(types, dataMaps);
	}
	
	@Override
	public ClientTileStack getTypeStack() { return (ClientTileStack) super.getTypeStack(); }
	
	@Override
	public ClientTileType getType() { return (ClientTileType) super.getType(); }
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		if(getLevel().getTile(x, y) == null) return; // cannot render if there are no tiles.
		
		// cannot render if there are no sprites.
		if(spriteStack == null || updateSprites)
			compileSprites();
		
		renderSprites(batch, spriteStack, posOffset);
	}
	
	public void renderSprites(SpriteBatch batch, List<TileAnimation> animations, Vector2 posOffset) {
		for(TileAnimation animation: animations)
			batch.draw(animation.getKeyFrame(this).texture, (x - posOffset.x) * SIZE, (y - posOffset.y) * SIZE);
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
			List<ClientTileType> aroundTypes = oTile != null ? oTile.getTypeStack().getTypes() : Collections.emptyList();
			if(aroundTypes.size() > 0 && aroundTypes.get(aroundTypes.size()-1).getTypeEnum() == TileTypeEnum.STONE && getType().getTypeEnum() != TileTypeEnum.STONE)
				aroundTypes.add(ClientTileType.get(TileTypeEnum.AIR));
			
			EnumSet<TileTypeEnum> typeSet = EnumSet.noneOf(TileTypeEnum.class);
			for(ClientTileType type: aroundTypes) {
				typeSet.add(type.getTypeEnum());
				typePositions.computeIfAbsent(type.getTypeEnum(), k -> EnumSet.noneOf(RelPos.class)).add(rp);
			}
			allTypes.addAll(typeSet);
			typesAtPositions.put(rp, typeSet);
		}
		
		// all tile types have been fetched. Now accumulate the sprites.
		ArrayList<TileAnimation> spriteStack = new ArrayList<>(16);
		
		// iterate through main stack from bottom to top, adding connection and overlap sprites each level.
		List<ClientTileType> types = getTypeStack().getTypes();
		for(int i = 1; i <= types.size(); i++) {
			ClientTileType cur = i < types.size() ? types.get(i) : null;
			ClientTileType prev = types.get(i-1);
			
			// add connection sprite (or transition) for prev
			spriteStack.addAll(prev.getRenderer().getConnectionSprites(this, typesAtPositions));
			
			// check for overlaps that are above prev AND below cur
			NavigableSet<TileTypeEnum> overlapSet;
			if(cur == null)
				overlapSet = safeSubSet(allTypes, prev.getTypeEnum(), false, allTypes.last(), !allTypes.last().equals(prev.getTypeEnum()));
			else
				overlapSet = safeSubSet(allTypes, prev.getTypeEnum(), false, cur.getTypeEnum(), false);
			
			if(overlapSet.size() > 0) { // add found overlaps
				overlapSet.forEach(enumType -> {
					ClientTileType tileType = ClientTileType.get(enumType);
					ArrayList<TileAnimation> sprites = tileType.getRenderer().getOverlapSprites(typePositions.get(enumType));
					spriteStack.addAll(sprites);
				});
			}
		}
		
		this.spriteStack = spriteStack;
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
