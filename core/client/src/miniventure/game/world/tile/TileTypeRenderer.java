package miniventure.game.world.tile;

import java.util.*;

import miniventure.game.core.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;
import miniventure.game.world.Point;
import miniventure.game.world.tile.SpriteManager.SpriteCompiler;
import miniventure.game.world.tile.TileTypeToAnimationMap.IndexedTileTypeToAnimationMap;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileTypeRenderer implements TileProperty {
	
	static final TileTypeToAnimationMap<Integer> mainAnimations = new IndexedTileTypeToAnimationMap();
	static final TileTypeToAnimationMap<Integer> connectAnimations = new IndexedTileTypeToAnimationMap();
	static final TileTypeToAnimationMap<Integer> overlapAnimations = new IndexedTileTypeToAnimationMap();
	
	static {
		MyUtils.debug("init TileTypeRenderer class");
	}
	
	// moved into a static init method instead of static init block in an attempt to fix some strange errors someone was having.
	static void init() {
		MyUtils.debug("reading tile sprites...");
		
		GameCore.tileAtlas.iterRegions(region -> {
			final int split = region.name.indexOf('/');
			
			TileTypeEnum tileType = TileTypeEnum.valueOf(region.name.substring(0, split).toUpperCase());
			
			String spriteName = region.name.substring(split+1);
			
			TileTypeToAnimationMap<?> animationMap;
			switch(spriteName.substring(0, 1)) {
				case "m": animationMap = mainAnimations;
					if(spriteName.equals("main"))
						spriteName = "m00";
					break;
				case "c": animationMap = connectAnimations; break;
				case "o": animationMap = overlapAnimations; break;
				case "t": animationMap = TransitionAnimation.tileAnimations; break;
				default:
					if(!spriteName.equals("swim")) // these are not part of the main animation system
						System.err.println("Unknown Tile Sprite Frame for " + tileType + ": " + spriteName);
					return;
			}
			
			animationMap.addFrame(tileType, spriteName.substring(1), region);
		});
		
		MyUtils.debug("tile sprites read successfully.");
	}
	
	@FunctionalInterface
	interface ConnectionCheck {
		boolean connects(RenderTile tile, TileTypeEnum adjacentType);
		
		static ConnectionCheck list(TileTypeEnum... connectingTypes) {
			final EnumSet<TileTypeEnum> matches = MyUtils.enumSet(connectingTypes);
			return (tile, type) -> matches.contains(type);
		}
	}
	
	static RendererBuilder buildRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque) {
		return buildRenderer(tileType, isOpaque, RenderStyle.SINGLE_FRAME);
	}
	static RendererBuilder buildRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque, RenderStyle defaultStyle) {
		return buildRenderer(tileType, isOpaque, defaultStyle, RenderStyle.SINGLE_FRAME);
	}
	static RendererBuilder buildRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque, RenderStyle defaultCoreStyle, RenderStyle defaultOverlapStyle) {
		return buildRenderer(tileType, isOpaque, defaultCoreStyle, defaultCoreStyle, defaultOverlapStyle);
	}
	static RendererBuilder buildRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque, RenderStyle defaultMainStyle, RenderStyle defaultConnectionStyle, RenderStyle defaultOverlapStyle) {
		return new RendererBuilder(tileType, isOpaque, defaultMainStyle, defaultConnectionStyle, defaultOverlapStyle);
	}
	
	static class RendererBuilder {
		
		private final TileTypeEnum tileType;
		private final boolean isOpaque;
		private final SpriteCompiler<Integer> mainSpriteManager;
		private final SpriteCompiler<Integer> connectionSpriteManager;
		private final SpriteCompiler<Integer> overlapSpriteManager;
		// if later on, animation framerates become more standardized, then I'll consider making transitions a SpriteManager too. But for now it's special.
		private final HashMap<String, TransitionAnimation> transitions;
		
		@Nullable // a null check means that we explicitly want to ignore any connection sprites even if they exist
		private ConnectionCheck connectionCheck;
		
		private RendererBuilder(@NotNull TileTypeEnum tileType, boolean isOpaque, RenderStyle defaultMainStyle, RenderStyle defaultConnectionStyle, RenderStyle defaultOverlapStyle) {
			this.tileType = tileType;
			this.isOpaque = isOpaque;
			
			mainSpriteManager = new SpriteCompiler<>(this, tileType, defaultMainStyle, mainAnimations, "main");
			connectionSpriteManager = new SpriteCompiler<>(this, tileType, defaultConnectionStyle, connectAnimations, "connection");
			overlapSpriteManager = new SpriteCompiler<Integer>(this, tileType, defaultOverlapStyle, overlapAnimations, "overlap") {
				@Override
				protected void validateStyle(RenderStyle style) {
					// force overlap animations to be synchronous
					if(!style.isSync())
						throw new IllegalArgumentException("Overlap animations must be synchronous.");
				}
			};
			
			transitions = new HashMap<>();
			
			// by default, if connection sprites exist, the tile type will connect only to itself
			if(connectAnimations.hasAnimations(tileType) && !tileType.multi)
				connectionCheck = ConnectionCheck.list(tileType);
			else
				connectionCheck = null;
		}
		
		// MAIN SPRITES
		SpriteCompiler<Integer> mainSprites() { return mainSpriteManager; }
		
		// OVERLAP SPRITES
		SpriteCompiler<Integer> overlapSprites() { return overlapSpriteManager; }
		
		// CONNECTION SPRITES
		RendererBuilder connect(@Nullable ConnectionCheck connectionCheck) {
			this.connectionCheck = connectionCheck;
			return this;
		}
		SpriteCompiler<Integer> connectionSprites() { return connectionSpriteManager; }
		SpriteCompiler<Integer> connections(@Nullable ConnectionCheck connectionCheck) {
			this.connectionCheck = connectionCheck;
			return connectionSprites();
		}
		
		// TRANSITION SPRITES
		RendererBuilder addTransition(String name, @NotNull RenderStyle renderStyle) {
			transitions.put(name, new TransitionAnimation(tileType, name, renderStyle));
			return this;
		}
		
		TileTypeRenderer build() {
			return new TileTypeRenderer(tileType, isOpaque, connectionCheck, mainSpriteManager.getManager(), connectionSpriteManager.getManager(), overlapSpriteManager.getManager(), transitions);
		}
	}
	
	private final TileTypeEnum tileType;
	private final boolean isOpaque;
	private final ConnectionCheck connectionCheck;
	private final SpriteManager<Integer> mainSpriteManager;
	private final SpriteManager<Integer> connectionSpriteManager;
	private final SpriteManager<Integer> overlapSpriteManager;
	
	private final HashMap<String, TransitionAnimation> transitions;
	
	private TileTypeRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionCheck connectionCheck, SpriteManager<Integer> mainSpriteManager, SpriteManager<Integer> connectionSpriteManager, SpriteManager<Integer> overlapSpriteManager, HashMap<String, TransitionAnimation> transitions) {
		this.tileType = tileType;
		this.isOpaque = isOpaque;
		this.connectionCheck = connectionCheck;
		this.mainSpriteManager = mainSpriteManager;
		this.connectionSpriteManager = connectionSpriteManager;
		this.overlapSpriteManager = overlapSpriteManager;
		this.transitions = transitions;
	}
	
	@Override
	public void registerDataTypes(TileType tileType) {
		if(transitions.size() > 0) {
			tileType.registerData(TileDataTag.TransitionName);
			// client doesn't use this data tag
			// tileType.registerData(TileDataTag.AnimationStart);
		}
	}
	
	public boolean isOpaque() { return isOpaque; }
	
	// whenever a tile changes its TileTypeEnum stack in any way, all 9 tiles around it re-fetch their overlap and main animations. Then they keep that stack of animations until the next fetch.
	
	// fetches sprites that represent this TileType on the given tile, including a main sprite and/or a connection sprite; or a transition sprite, if there is a current transition.
	public LinkedList<TileAnimation> getCoreSprites(@NotNull RenderTile tile, EnumMap<RelPos, EnumSet<TileTypeEnum>> aroundTypes) {
		LinkedList<TileAnimation> sprites = new LinkedList<>();
		String tName = transitions.size() > 0 ? tile.getDataMap(tileType).get(TileDataTag.TransitionName) : null;
		if(tName != null)
			sprites.add(transitions.get(tName).getAnimation(tile));
		else {
			addMainSprite(sprites, tile);
			addConnectionSprite(sprites, tile, aroundTypes);
		}
		
		// check for multi
		/*if(tileType.multi) {
			// move texture regions
			Point anchor = tile.getDataMap(tileType).get(TileDataTag.AnchorPos);
			int xo = tile.x - anchor.x;
			int yo = tile.y - anchor.y;
			xo *= Tile.SIZE;
			yo *= Tile.SIZE;
			LinkedList<TileAnimation> relocSprites = new LinkedList<>();
			for(TileAnimation anim: sprites) {
				TileAnZ
			}
		}*/
		
		return sprites;
	}
	
	// todo later on, if I decide to have multiple named main sprites, I'll need to provide a way to specify which one to use; but until then, this method will expect them to be indexed if there is more than one.
	private void addMainSprite(List<TileAnimation> sprites, RenderTile tile) {
		final int mainAnimCount = mainAnimations.getAnimationCount(tileType);
		if(mainAnimCount <= 0)
			return;
		
		int idx = 0;
		if(mainAnimCount > 1) {
			// pick a random sprite based on the tile position and tile type
			Random rand = MyUtils.getRandom(tile.x * 17 + tile.y * 131 + tileType.ordinal() * 79);
			idx = rand.nextInt(mainAnimCount);
		}
		
		sprites.add(mainSpriteManager.getAnimation(tile, idx));
	}
	
	/// Checks the given aroundTypes for all types
	private void addConnectionSprite(List<TileAnimation> sprites, RenderTile tile, EnumMap<RelPos, EnumSet<TileTypeEnum>> aroundTypes) {
		if(connectAnimations.getAnimationCount(tileType) <= 0)
			return;
		
		if(connectionCheck == null) {
			sprites.add(connectionSpriteManager.getAnimation(tile, 0));
			return;
		}
		
		EnumMap<RelPos, Boolean> tileConnections = new EnumMap<>(RelPos.class);
		
		for(RelPos rp: RelPos.values) {
			// check if each surrounding tile has something in the connectingTypes array
			boolean connects = false;
			for(TileTypeEnum aroundType: aroundTypes.get(rp)) {
				if(connectionCheck.connects(tile, aroundType)) {
					connects = true;
					break;
				}
			}
			tileConnections.put(rp, connects);
		}
		
		int spriteIdx = 0;
		for (int i = 0; i < TileTouchCheck.connectionChecks.length; i++) {
			if (TileTouchCheck.connectionChecks[i].checkMatch(tileConnections)) {
				spriteIdx = i;
				break;
			}
		}
		
		sprites.add(connectionSpriteManager.getAnimation(tile, spriteIdx));
	}
	
	// gets the overlap sprite (sides + any isolated corners) for this tiletype overlapping a tile at the given positions.
	/// returns sprites for the stored type assuming it is overlapping at the given positions.
	public ArrayList<TileAnimation> getOverlapSprites(EnumSet<RelPos> ovLayout, RenderTile tile) {
		ArrayList<TileAnimation> animations = new ArrayList<>();
		
		if(!overlapAnimations.hasAnimations(tileType))
			return animations;
		
		Array<Integer> indexes = new Array<>(Integer.class);
		
		int[] bits = new int[4];
		if(ovLayout.contains(RelPos.LEFT))   bits[0] = 1; // 1
		if(ovLayout.contains(RelPos.TOP))    bits[1] = 1; // 5
		if(ovLayout.contains(RelPos.RIGHT))  bits[2] = 1; // 7
		if(ovLayout.contains(RelPos.BOTTOM)) bits[3] = 1; // 3
		int total = 0, value = 1;
		for(int num: bits) {
			total += num * value;
			value *= 2;
		}
		if(total > 0) indexes.add(total+3); // don't care to add if all zeros, because then it's just blank. Also, the +3 is to skip past the first 4 sprites, which are the corners (we add 3 instead of 4 because total will start at 1 rather than 0).
		// four corners; NOTE, the artist should work on the corner sprites in one tile-sized image, to make sure that they only use a quarter of it at absolute most.
		if(ovLayout.contains(RelPos.TOP_LEFT)     && bits[0] == 0 && bits[1] == 0) indexes.add(0); // 2
		if(ovLayout.contains(RelPos.TOP_RIGHT)    && bits[1] == 0 && bits[2] == 0) indexes.add(1); // 8
		if(ovLayout.contains(RelPos.BOTTOM_RIGHT) && bits[2] == 0 && bits[3] == 0) indexes.add(2); // 6
		if(ovLayout.contains(RelPos.BOTTOM_LEFT)  && bits[3] == 0 && bits[0] == 0) indexes.add(3); // 0
		for(Integer idx: indexes) {
			animations.add(overlapSpriteManager.getAnimation(tile, idx));
		}
		
		return animations;
	}
}
