package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.TreeSet;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;
import miniventure.game.world.Point;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.management.Level;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.tile.TileType.Prop;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Tile implements WorldObject {
	
	/*
		So. The client, in terms of properties, doesn't have to worry about tile interaction properties. It always sends a request to the server when interactions should occur.
		The server, in turn, doesn't have to worry about animation or rendering properties.
		
		So, it seems like we have a situation where there are ServerProperties and ClientProperties, and some property types are both.
		
		This could mean that I should just take the client and server properties out of the main game module and into their own respective modules. But that would mean they can never be referenced in the main package, and I like how the property types are all given in the same place, the TileType class. So maybe I can leave the shell there, and just separate the stuff that actually does the heavy lifting..?
		
		 Hey, that actually gives me an idea: What if the property types/classes I specify in the TileType class weren't actually the objects that I used for the tile behaviors, in the end? What if they were just markers, and the actual property instances were instantiated later? For entities, they would obviously be instantiated on entity creation, but since there are so many tiles loaded at one time, we have to do tiles different...
		 Hey, I know: how about we have a tile property instance fetcher, that creates all the actual tile property instances within the respective client/server modules, with the right classes, based on the given main property class? That could work! Then, whenever a tile property was asked for, it would fetch it from the fetcher, given the TileType and property class/type. With entities, each would simply have their own list, their own fetcher.
		 The fetchers would be created in ClientCore and ServerCore, more or less. or maybe the Worlds, since the WorldManager class would have to have a way to fetch a property instance given a TileType and and Property class/type. For entities, the fetcher would be given the entity instance too. Or maybe each entity would just already have its properties. Yeah that'll probably be the case.
		 
		 So! End result. Actual property instances are created in Client/Server individually, not in the TileType enum. That is only where the basic templates go. The is a fetcher that can take a property type instance, and return a completed property instance of that type.
		 Note, we might end up having a property type enum as well as a tile type enum...
	 */
	
	public static final int RESOLUTION = 32;
	public static final int SCALE = 4;
	public static final int SIZE = RESOLUTION * SCALE;
	
	// private TileStack tileStack;
	private Array<TileType> typeStack;
	private Array<Object[]> typeData;
	private Object[] topData;
	private TileContext contextCache;
	
	@NotNull private final Level level;
	final int x;
	final int y;
	// private final TileTag tag;
	// final EnumMap<TileType, SerialMap> dataMaps = new EnumMap<>(TileType.class);
	
	private float lastUpdate;
	
	private Array<TileAnimation> spriteStack;
	private Array<Integer> spritesPerLayer;
	private boolean updateSprites = true;
	
	
	// the TileType array is ALWAYS expected in order of bottom to top.
	public Tile(@NotNull Level level, int x, int y, @NotNull TileType[] types, @Nullable TileStackData dataMaps) {
		this.level = level;
		this.x = x;
		this.y = y;
		// this.tag = new TileTag(this);
		contextCache = new TileContext();
		typeStack = new Array<>(true, 4, TileType.class);
		typeData = new Array<>(true, 4, Object[].class);
		setStack(types, dataMaps);
		spriteStack = new Array<>(true, 16, TileAnimation.class);
		spritesPerLayer = new Array<>(true, 4, Integer.class);
	}
	
	public Tile(@NotNull Level level, int x, int y, String data) {
		this.level = level;
		this.x = x;
		this.y = y;
		
		contextCache = new TileContext();
		typeStack = new Array<>(true, 4, TileType.class);
		typeData = new Array<>(true, 4, Object[].class);
		
		spriteStack = new Array<>(true, 16, TileAnimation.class);
		spritesPerLayer = new Array<>(true, 4, Integer.class);
		
		String[] dataAr = MyUtils.parseLayeredString(data);
		for(int i = 0; i < dataAr.length; i++) {
			String typeData = dataAr[i];
			String typeName = typeData.substring(0, typeData.indexOf(','));
			String dataValues = typeData.substring(typeName.length()+1);
			TileType type = TileType.valueOf(typeName);
			addLayer(type);
			type.deserializeData(dataValues, this.typeData.get(i), topData);
		}
	}
	
	public String save() {
		String[] data = new String[typeStack.size];
		for (int i = 0; i < data.length; i++) {
			data[i] = typeStack.get(i).name()+','+typeStack.get(i).serializeData(typeData.get(i), i == data.length - 1 ? topData : null);
		}
		
		return MyUtils.encodeStringArray(data);
	}
	
	// abstract TileStack makeStack(@NotNull TileType[] types, @Nullable TileTypeDataMap[] dataMaps);
	
	// void setTileStack(TileStack stack) { this.tileStack = stack; }
	
	// init and client
	void setStack(TileType[] types, @Nullable TileStackData dataMaps) {
		typeStack.clear();
		typeData.clear();
		topData = null;
		for(int i = 0; i < types.length; i++) {
			TileType type = types[i];//getWorld().getTileType(types[i]);
			typeStack.add(type);
			typeData.add(type.createDataArray());
			TileContext context = setMainContext();
			if(i == types.length - 1)
				topData = type.createTopDataArray();
			// add given properties
			if(dataMaps != null) {
				dataMaps.deserializeTypeData(i, context);
			}
		}
	}
	
	int getStackSize() { return typeStack.size; }
	
	TileType getLayer(int layer) { return typeStack.get(layer); }
	
	TileType getOnBreakType() {
		return typeStack.size == 1 ? TileType.HOLE/*getWorld().getTileType(getType().getUnderType())*/ : typeStack.get(typeStack.size - 2);
	}
	
	Iterable<TileType> getStack() {
		return typeStack;
	}
	
	/*TileType[] getStackCopy() {
		return typeStack.toArray();
	}*/
	
	// internal 
	private void addLayer(TileType newLayer) {
		typeStack.add(newLayer);
		typeData.add(newLayer.createDataArray());
		topData = newLayer.createTopDataArray();
	}
	
	// internal
	private TileType removeLayer() {
		TileType removed = typeStack.removeIndex(typeStack.size - 1);
		typeData.removeIndex(typeData.size - 1);
		
		if(typeStack.size == 0) // bottom of stack; ask type for new type
			addLayer(TileType.HOLE/*getWorld().getTileType(removed.getUnderType())*/);
		else // type is in stack
			topData = typeStack.get(typeStack.size - 1).createTopDataArray();
		
		return removed;
	}
	
	@NotNull @Override
	public WorldManager getWorld() { return level.getWorld(); }
	
	@NotNull @Override public Level getLevel() { return level; }
	
	@NotNull
	@Override public Rectangle getBounds() { return new Rectangle(x, y, 1, 1); }
	@Override public Vector2 getCenter() { return new Vector2(x+0.5f, y+0.5f); }
	
	public Point getLocation() { return new Point(x, y); }
	
	@Override
	public boolean isPermeable() {
		return getType().isWalkable();
	}
	
	public TileType getType() { return typeStack.get(typeStack.size - 1); }
	// public TileStack getTypeStack() { return tileStack; }
	
	// using the word "set" instead of "get" to better convey that calling this at the wrong time could have adverse effects
	protected TileContext setContext(int layer) {
		contextCache.layer = layer;
		return contextCache;
	}
	protected TileContext setMainContext() {
		return setContext(typeStack.size - 1);
	}
	
	// public SerialMap getDataMap(TileType tileType) { return getDataMap(tileType); }
	/*@NotNull
	public TileDataTag.TileTypeDataMap getDataMap(TileType tileType) {
		TileTypeDataMap map = tileStack.getDataMap(tileType);
		// should never happen, especially with the new synchronization. But this will stay, just in case.
		if(map == null) {
			GameCore.error("ERROR: tile " + toLocString() + " came back with a null data map for tiletype " + tileType + "; stack: " + tileStack.getDebugString(), true, true);
			map = new TileTypeDataMap();
		}
		return map;
	}*/
	
	/*@NotNull
	public TileDataTag.TileDataMap getDataMap(TileType tileType) {
		TileDataMap map = tileStack.getDataMap(tileType);
		// should never happen, especially with the new synchronization. But this will stay, just in case.
		if(map == null) {
			GameCore.error("ERROR: tile " + toLocString() + " came back with a null cache map for tiletype " + tileType + "; stack: " + tileStack.getDebugString(), true, true);
			map = new TileDataMap();
		}
		return map;
	}*/
	
	// hacky little method to force a tile to be permeable; no transitions or normal break systems
	public void forcePermeable() {
		while(!isPermeable())
			removeLayer();
	}
	
	// public void addTile(@NotNull TileType newType) { addTile(new TileTypeInfo(newType)); }
	public void addTile(@NotNull TileType newType) { addTile(newType, getType()); }
	// not synchronizing this only because it's always called in a synchronized context.
	private void addTile(@NotNull TileType newType, @NotNull TileType prevType) {
		moveEntities(newType);
		
		addLayer(newType);
		
		// check for an entrance animation
		if(!newType.get(Prop.TRANS).tryStartAnimation(setMainContext(), prevType))
			getLevel().onTileUpdate(this/*, newType*/); // trigger update manually since tile still changed, just without an animation; tryStartAnimation only triggers updates for transition state changes.
	}
	
	// starting point to break a tile
	boolean breakTile() {
		return removeTile(true, null);
	}
	// starting point to replace a tile
	boolean replaceTile(@NotNull TileType newType) { return removeTile(true, newType); }
	// boolean replaceTile(@NotNull TileTypeInfo newType) {
	// 	return removeTile(true, newType);
	// }
	// can be called down the line after either method above, after the exit animation plays
	boolean breakTile(@Nullable TileType replacementType) {
		return removeTile(false, replacementType);
	}
	private boolean removeTile(boolean checkForExitAnim, @Nullable TileType replacementType) {
		TileType type = getType();
		if(checkForExitAnim) {
			boolean addNext = replacementType != null;
			TileType nextType = replacementType == null ? getOnBreakType() : replacementType;
			if(type.get(Prop.TRANS).tryStartAnimation(setMainContext(), nextType, addNext)) {
				// transitioning successful, tile will be broken after exit animation
				return true; // don't actually break the tile yet (but line above, still signal for update)
			}
		}
		
		// Action destroyAction = getDataMap(type).get(TileDataTag.DestroyAction);
		TileType prevType = removeLayer();
		// if(destroyAction != null)
		// 	destroyAction.act();
		
		if(replacementType != null) {
			// don't worry if a tile type was removed or not, add the next one anyway.
			addTile(replacementType, type); // handles entity movement and tile update
			return true;
		}
		else if(prevType != null) {
			// a tile type was removed
			moveEntities(getType());
			getLevel().onTileUpdate(this/*, null*/);
			return true;
		}
		
		return false; // cannot break this tile any further.
	}
	
	private void moveEntities(TileType newType) {
		// check for entities that will not be allowed on the new tile, and move them to the closest adjacent tile they are allowed on.
		if(newType.isWalkable()) return; // no worries for this type.
		
		HashSet<Tile> surroundingTileSet = getAdjacentTiles(true);
		Tile[] surroundingTiles = surroundingTileSet.toArray(new Tile[0]);
		for(Entity entity: getLevel().getOverlappingEntities(getBounds())) {
			// for each entity, check if it can walk on the new tile type. If not, fetch the surrounding tiles, remove those the entity can't walk on, and then fetch the closest tile of the remaining ones.
			// if(newType.isWalkable()) continue; // no worries for this entity.
			
			Array<Tile> aroundTiles = new Array<>(surroundingTiles);
			for(int i = 0; i < aroundTiles.size; i++) {
				if(!entity.canPermeate(aroundTiles.get(i))) {
					aroundTiles.removeIndex(i);
					i--;
				}
			}
			
			// if none remain (returned tile is null), take no action for that entity.
			if(aroundTiles.size == 0)
				continue;
			
			// from the remaining tiles, find the one that is closest to the entity.
			WorldObject.sortByDistance(aroundTiles, entity.getCenter());
			Tile closest = aroundTiles.removeIndex(0);
			// move the entity just barely inside the new tile.
			Rectangle tileBounds = closest.getBounds();
			
			if(aroundTiles.size > 0) {
				// this section here attempts to find a second tile that's touching the first closest, so that movements aren't unnecessarily jarring
				Tile secClosest = null;
				do {
					Tile next = aroundTiles.removeIndex(0);
					if(next.isAdjacent(closest, false))
						secClosest = next;
				} while (aroundTiles.size > 0 && secClosest == null);
				if (secClosest != null)
					// expand the rect that the player can be moved to so it's not so large.
					tileBounds.merge(secClosest.getBounds());
			}
			
			Rectangle entityBounds = entity.getBounds();
			MyUtils.moveRectInside(entityBounds, tileBounds, 0.05f);
			entity.moveTo(entityBounds.x, entityBounds.y);
		}
	}
	
	public float update() {
		float min = 0;
		
		TileContext context = setMainContext();
		
		float now = getWorld().getGameTime();
		float delta = now - lastUpdate;
		
		// if playing an exit animation, then don't update the tile.
		TransitionManager man = context.getProp(Prop.TRANS);
		if(man.playingAnimation(context))
			return man.tryFinishAnimation(context);
		
		for(int i = 0; i < getStackSize(); i++) {
			context = setContext(i);
			float wait = context.getProp(Prop.UPDATE).update(context, delta);
			if(min == 0)
				min = wait;
			else if(wait != 0)
				min = Math.min(wait, min);
		}
		// setMainContext().setData(TileDataTag.LastUpdate, now);
		this.lastUpdate = now;
		return min;
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
	
	public void updateSprites() {
		updateSprites = true;
	}
	
	// compile sprites in each layer group separately; additionally, for each layer, end it with an air tile. Transparency will be implemented afterwards, once I've implemented this and also changed tile stacks; see in TileStack.java. Following initial implementation of transparency, and the rest, test it out with the level gen.
	
	/** @noinspection ObjectAllocationInLoop*/
	@SuppressWarnings("unchecked")
	private void compileSprites() {
		TreeSet<TileType> allTypes = new TreeSet<>(); // overlap
		EnumMap<RelPos, EnumSet<TileType>> typesAtPositions = new EnumMap<>(RelPos.class); // connection
		EnumMap<TileType, EnumSet<RelPos>> typePositions = new EnumMap<>(TileType.class); // overlap
		
		for (RelPos rp: RelPos.values()) {
			int x = rp.getX();
			int y = rp.getY();
			Tile oTile = getLevel().getTile(this.x + x, this.y + y);
			EnumSet<TileType> typeSet = EnumSet.noneOf(TileType.class);
			if(oTile != null) {
				for (TileType type: oTile.getStack()) {
					typeSet.add(type);
					typePositions.computeIfAbsent(type, k -> EnumSet.noneOf(RelPos.class)).add(rp);
				}
				if (oTile.getType() == TileType.STONE && getType() != TileType.STONE) {
					typeSet.add(TileType.AIR);
					typePositions.computeIfAbsent(TileType.AIR, k -> EnumSet.noneOf(RelPos.class)).add(rp);
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
			TileType cur = i < getStackSize() ? getLayer(i) : null;
			TileType prev = getLayer(i-1);
			
			int startSize = spriteStack.size;
			// add connection sprite (or transition) for prev
			prev.get(Prop.RENDER).addCoreSprites(setContext(i-1), typesAtPositions, spriteStack);
			
			// check for overlaps that are above prev AND below cur
			NavigableSet<TileType> overlapSet;
			if(cur == null)
				overlapSet = safeSubSet(allTypes, prev, false, allTypes.last(), !allTypes.last().equals(prev));
			else
				overlapSet = safeSubSet(allTypes, prev, false, cur, false);
			
			if(overlapSet.size() > 0) { // add found overlaps
				overlapSet.forEach(tileType -> {
					tileType.get(Prop.RENDER).addOverlapSprites(typePositions.get(tileType), spriteStack);
				});
			}
			// record the number of sprites added this layer
			spritesPerLayer.add(spriteStack.size - startSize);
		}
		
		updateSprites = false;
	}
	
	private static <E extends Enum<E>> NavigableSet<E> safeSubSet(TreeSet<E> set,
																  E fromElement, boolean fromInclusive,
																  E toElement, boolean toInclusive) {
		if(fromElement.compareTo(toElement) > 0)
			return new TreeSet<>();
		else
			return set.subSet(fromElement, fromInclusive, toElement, toInclusive);
	}
	
	public HashSet<Tile> getAdjacentTiles(boolean includeCorners) {
		if(includeCorners)
			return level.getAreaTiles(x, y, 1, false);
		else {
			HashSet<Tile> tiles = new HashSet<>();
			if(x > 0) tiles.add(level.getTile(x-1, y));
			if(y < level.getHeight()-1) tiles.add(level.getTile(x, y+1));
			if(x < level.getWidth()-1) tiles.add(level.getTile(x+1, y));
			if(y > 0) tiles.add(level.getTile(x, y-1));
			tiles.remove(null);
			return tiles;
		}
	}
	
	public boolean isAdjacent(@NotNull Tile other, boolean includeCorners) {
		if(includeCorners)
			return Math.abs(x - other.x) <= 1 && Math.abs(y - other.y) <= 1;
		int xdiff = Math.abs(x - other.x);
		int ydiff = Math.abs(y - other.y);
		return xdiff == 1 && ydiff == 0 || xdiff == 0 && ydiff == 1;
	}
	
	@Override
	public float getLightRadius() {
		float maxRadius = 0;
		for(TileType type: getStack())
			maxRadius = Math.max(maxRadius, type.get(Prop.RENDER).getLightRadius());
		
		return maxRadius;
	}
	
	@Override
	public Result attackedBy(WorldObject obj, @Nullable Item item, int damage) {
		if(getType().get(Prop.TRANS).playingExitAnimation(setMainContext()))
			return Result.NONE;
		return getType().attacked(setMainContext(), obj, item, damage);
	}
	
	@Override
	public Result interactWith(Player player, @Nullable Item heldItem) {
		if(getType().get(Prop.TRANS).playingExitAnimation(setMainContext()))
			return Result.NONE;
		return getType().interact(this, player, heldItem);
	}
	
	@Override
	public boolean touchedBy(Entity entity) {
		if(getType().get(Prop.TRANS).playingExitAnimation(setMainContext()))
			return false;
		return getType().touched(this, entity, true);
	}
	
	@Override
	public void touching(Entity entity) {
		if(getType().get(Prop.TRANS).playingExitAnimation(setMainContext()))
			return;
		getType().touched(this, entity, false);
	}
	
	@Override
	public String toString() { return getType().getName()+' '+getClass().getSimpleName(); }
	
	public String toLocString() { return x+","+y+" ("+toString()+')'; }
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Tile)) return false;
		Tile o = (Tile) other;
		return level.equals(o.level) && x == o.x && y == o.y;
	}
	
	@Override
	public int hashCode() { return Point.javaPointHashCode(x, y) + level.getLevelId() * 17; }
	
	/*public static class TileTag implements Tag<Tile> {
		public final int x;
		public final int y;
		public final int levelId;
		
		private TileTag() { this(0, 0, 0); }
		public TileTag(Tile tile) { this(tile.x, tile.y, tile.getLevel().getLevelId()); }
		public TileTag(int x, int y, int levelId) {
			this.x = x;
			this.y = y;
			this.levelId = levelId;
		}
		
		@Override
		public Tile getObject(WorldManager world) {
			Level level = world.getLevel(levelId);
			if(level != null)
				return level.getTile(x, y);
			return null;
		}
	}
	
	@Override
	public Tag<Tile> getTag() { return tag; }*/
	
	public class TileContext {
		
		private int layer;
		
		public WorldManager getWorld() { return getTile().getWorld(); }
		
		public Level getLevel() { return getTile().getLevel(); }
		
		public Tile getTile() { return Tile.this; }
		
		public TileType getType() {
			return typeStack.get(layer);
		}
		
		public <T extends TileProperty> T getProp(Prop<T> tileProp) {
			return getType().get(tileProp);
		}
		
		public <T> T getData(TileDataTag<T> dataTag) {
			return getType().getData(dataTag, typeData.get(layer), topData);
		}
		public <T> T getData(TileDataTag<T> dataTag, T defaultValue) {
			T data = getData(dataTag);
			return data == null ? defaultValue : data;
		}
		
		public <T> void setData(TileDataTag<T> dataTag, T data) {
			getType().setData(dataTag, data, typeData.get(layer), topData);
		}
		
		public <T> T clearData(TileDataTag<T> dataTag) {
			return updateData(dataTag, null);
		}
		
		// returns old value.
		public <T> T updateData(TileDataTag<T> dataTag, T data) {
			T value = getData(dataTag);
			setData(dataTag, data);
			return value;
		}
		
		// sets the data if not present, then returns now-current value
		public <T> T getOrInitData(TileDataTag<T> dataTag, T putIfAbsent) {
			T value = getData(dataTag);
			if(value == null) {
				setData(dataTag, putIfAbsent);
				value = putIfAbsent;
			}
			return value;
		}
	}
	
	// todo fix tile data; tile data maps are carefully indexed with the data types that will be used, including both saving and non-saving data tags. When saving a tile, I need to be able to turn them all into strings, but exclude the unnecessary keys. Then go in reverse. It might work to make another ordered data set that contains the orderings of saved and serialized sets... but any more of those sets and things are getting cumbersome. there ought to be a better way.
	// tile data objects are made to:
	// - send tile data to clients
	// - save tile data to file
	// these are the only two use cases.
	/*public static class TileData {
		public final int[] typeOrdinals;
		public final String[] data;
		
		private TileData() { typeOrdinals = null; data = null; }
		public TileData(Tile tile*//*, boolean save*//*) {
			typeOrdinals = new int[tile.typeStack.size];
			for(int i = 0; i < typeOrdinals.length; i++) {
				TileType type = tile.typeStack.get(i);
				typeOrdinals[i] = type.ordinal();
			}
			
			this.data = new String[typeOrdinals.length];
			for(int i = 0; i < data.length; i++) {
				// only include the top data for the top tile type
				Object[] topData = i == data.length - 1 ? tile.topData : null;
				data[i] = tile.typeStack.get(i).serializeData(*//*save, *//*tile.typeData.get(i), topData);
			}
		}
		
		public TileData(Version dataVersion, String tileData) {
			String[] all = MyUtils.parseLayeredString(tileData);
			data = Arrays.copyOfRange(all, 1, all.length);
			
			typeOrdinals = ArrayUtils.mapArray(all[0].split(","), int.class, int[].class, Integer::parseInt);
		}
		
		public String serialize() {
			String[] all = new String[data.length+1];
			System.arraycopy(data, 0, all, 1, data.length);
			
			all[0] = ArrayUtils.arrayToString(typeOrdinals, ",");
			
			return MyUtils.encodeStringArray(all);
		}
		
		public TileType[] getTypes() { return getTypes(typeOrdinals); }
		public static TileType[] getTypes(int[] typeOrdinals) {
			TileType[] types = new TileType[typeOrdinals.length];
			for(int i = 0; i < types.length; i++) {
				types[i] = TileType.values[typeOrdinals[i]];
			}
			return types;
		}
		
		public TileStackData getDataMaps() { return getDataMaps(data); }
		public static TileStackData getDataMaps(String[] data) {
			return new TileStackData(data);
		}
		
	}*/
}
