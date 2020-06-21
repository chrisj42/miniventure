package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Random;

import miniventure.game.core.GdxCore;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;
import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.world.tile.Tile.TileContext;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileTypeRenderer implements TileProperty {
	
	// static final TileTypeToAnimationSetMap<Integer> mainAnimations = new IndexedTileTypeToAnimationMap();
	// static final TileTypeToAnimationSetMap<Integer> connectAnimations = new IndexedTileTypeToAnimationMap();
	// static final TileTypeToAnimationSetMap<Integer> overlapAnimations = new IndexedTileTypeToAnimationMap();
	
	/*private static class SpriteTypeToAnimationMap extends EnumMap<TileSpriteType, TileTypeAnimationSource> {
		public SpriteTypeToAnimationMap() {
			super(TileSpriteType.class);
		}
	}*/
	
	// private static final EnumMap<TileType, EnumMap<TileAnimType, TileAnimationSetFrames<?>>> tileTypeSpriteMap = new EnumMap<>(TileType.class);
	
	static {
		MyUtils.debug("init TileTypeRenderer class");
	}
	
	// moved into a static init method instead of static init block in an attempt to fix some strange errors someone was having.
	public static void init() {
		MyUtils.debug("reading tile sprites...");
		
		GdxCore.tileAtlas.iterRegions(region -> {
			final int split = region.name.indexOf('/');
			
			TileType tileType = TileType.valueOf(region.name.substring(0, split).toUpperCase());
			String spriteName = region.name.substring(split+1);
			
			if(spriteName.startsWith("swim")) {
				SwimAnimation.swimAnimations.addFrame(tileType.name(), region);
					// .computeIfAbsent(tileType, k -> new Array<>(TextureHolder.class))
					// .add(region);
				return;
			}
			
			for(TileAnimType<?> setType: GenericEnum.values(TileAnimType.class)) {
				if(setType.tryRegister(tileType, spriteName, region))
					return;
			}
			
			System.err.println("Unknown Tile Sprite Frame for " + tileType + ": " + spriteName);
		});
		
		MyUtils.debug("tile sprites read successfully.");
	}
	
	@Override
	public void registerDataTags(TileType tileType) {
		// no tags
	}
	
	@FunctionalInterface
	interface ConnectionCheck {
		boolean connects(TileType adjacentType);
		
		static ConnectionCheck list(TileType... connectingTypes) {
			final EnumSet<TileType> matches = MyUtils.enumSet(connectingTypes);
			return matches::contains;
		}
		static ConnectionCheck list(TileTypeSource... connectingTypes) {
			return list(ArrayUtils.mapArray(connectingTypes, TileType.class, TileTypeSource::getType));
		}
	}
	
	static TileTypeRenderer basicRenderer(@NotNull TileType tileType) {
		return buildRenderer(tileType).build();
	}
	static RendererBuilder buildRenderer(@NotNull TileType tileType) {
		return buildRenderer(tileType, RenderStyle.SINGLE_FRAME);
	}
	static RendererBuilder buildRenderer(@NotNull TileType tileType, RenderStyle defaultStyle) {
		return buildRenderer(tileType, defaultStyle, defaultStyle);
	}
	static RendererBuilder buildRenderer(@NotNull TileType tileType, RenderStyle defaultCoreStyle, RenderStyle defaultOverlapStyle) {
		return buildRenderer(tileType, defaultCoreStyle, defaultCoreStyle, defaultOverlapStyle);
	}
	static RendererBuilder buildRenderer(@NotNull TileType tileType, RenderStyle defaultMainStyle, RenderStyle defaultConnectionStyle, RenderStyle defaultOverlapStyle) {
		return new RendererBuilder(tileType, defaultMainStyle, defaultConnectionStyle, defaultOverlapStyle);
	}
	
	static class RendererBuilder {
		
		private final TileType tileType;
		// private final 
		private final AnimationSetCompiler<Integer> mainSpriteManager;
		private final AnimationSetCompiler<Integer> connectionSpriteManager;
		private final AnimationSetCompiler<Integer> overlapSpriteManager;
		// if later on, animation framerates become more standardized, then I'll consider making transitions a SpriteManager too. But for now it's special.
		// private final HashMap<String, TransitionAnimation> transitions;
		
		@Nullable // a null check means that we explicitly want to ignore any connection sprites even if they exist
		// another way of putting it is it's an automatic result that none of the adjacent tiles connect. As such, the appropriate connection sprite for no connections will be used. This is different from disabling connection sprites entirely.
		// note: connections sprites are never half-specified; either all 40-odd sprites or none. This is because the singular sprites have been converted to main sprites. So if at least one connection sprite is present, we can assume they are all present.
		private ConnectionCheck connectionCheck;
		
		private float lightRadius;
		private SwimAnimation swimAnimation;
		
		private RendererBuilder(@NotNull TileType tileType, RenderStyle defaultMainStyle, RenderStyle defaultConnectionStyle, RenderStyle defaultOverlapStyle) {
			this.tileType = tileType;
			
			mainSpriteManager = new AnimationSetCompiler<>(this, defaultMainStyle,
				TileAnimType.Main.fetchMap(tileType));
			connectionSpriteManager = new AnimationSetCompiler<>(this, defaultConnectionStyle,
				TileAnimType.Connection.fetchMap(tileType));
			overlapSpriteManager = new AnimationSetCompiler<>(this, defaultOverlapStyle,
				TileAnimType.Overlap.fetchMap(tileType));
			
			// by default, if connection sprites exist, the tile type will connect only to itself
			if(TileAnimType.Connection.hasSprites(tileType))
				connectionCheck = ConnectionCheck.list(tileType);
			else
				connectionCheck = null;
		}
		
		// MAIN SPRITES
		AnimationSetCompiler<Integer> mainSprites() { return mainSpriteManager; }
		RendererBuilder disableMain() { mainSpriteManager.disable(); return this; }
		
		// OVERLAP SPRITES
		AnimationSetCompiler<Integer> overlapSprites() { return overlapSpriteManager; }
		RendererBuilder disableOverlap() { overlapSpriteManager.disable(); return this; }
		
		// CONNECTION SPRITES
		AnimationSetCompiler<Integer> connectionSprites() { return connectionSpriteManager; }
		RendererBuilder disableConnection() { connectionSpriteManager.disable(); return this; }
		
		RendererBuilder connect(@Nullable ConnectionCheck connectionCheck) {
			this.connectionCheck = connectionCheck;
			return this;
		}
		/*AnimationSetCompiler<Integer> connections(@Nullable ConnectionCheck connectionCheck) {
			this.connectionCheck = connectionCheck;
			return connectionSprites();
		}*/
		
		// TRANSITION SPRITES
		/*RendererBuilder addTransition(String name, @NotNull RenderStyle renderStyle) {
			transitions.put(name, new TransitionAnimation(tileType, name, renderStyle));
			return this;
		}*/
		
		RendererBuilder lightRadius(float lightRadius) {
			this.lightRadius = lightRadius;
			return this;
		}
		
		RendererBuilder swim(SwimAnimation swimAnimation) {
			this.swimAnimation = swimAnimation;
			return this;
		}
		
		TileTypeRenderer build() {
			return new TileTypeRenderer(tileType, lightRadius, swimAnimation, connectionCheck, mainSpriteManager.compileAnimations(), connectionSpriteManager.compileAnimations(), overlapSpriteManager.compileAnimations());
		}
	}
	
	private final TileType tileType;
	private final float lightRadius;
	private final SwimAnimation swimAnimation;
	private final ConnectionCheck connectionCheck;
	private final HashMap<Integer, TileAnimation> mainTileAnimations;
	private final HashMap<Integer, TileAnimation> connectionTileAnimations;
	private final HashMap<Integer, TileAnimation> overlapTileAnimations;
	
	// private final HashMap<String, TransitionAnimation> transitions;
	
	private TileTypeRenderer(@NotNull TileType tileType, float lightRadius, SwimAnimation swimAnimation, ConnectionCheck connectionCheck, HashMap<Integer, TileAnimation> mainTileAnimations, HashMap<Integer, TileAnimation> connectionTileAnimations, HashMap<Integer, TileAnimation> overlapTileAnimations) {
		this.tileType = tileType;
		this.lightRadius = lightRadius;
		this.swimAnimation = swimAnimation;
		this.connectionCheck = connectionCheck;
		this.mainTileAnimations = mainTileAnimations;
		this.connectionTileAnimations = connectionTileAnimations;
		this.overlapTileAnimations = overlapTileAnimations;
		// this.transitions = new HashMap<>(transitions.size());
		// this.transitions.putAll(transitions);
	}
	
	public float getLightRadius() { return lightRadius; }
	
	public SwimAnimation getSwimAnimation() { return swimAnimation; }
	
	// whenever a tile changes its TileType stack in any way, all 9 tiles around it re-fetch their overlap and main animations. Then they keep that stack of animations until the next fetch.
	
	// fetches sprites that represent this TileType on the given tile, including a main sprite and/or a connection sprite; or a transition sprite, if there is a current transition.
	public void addCoreSprites(@NotNull Tile.TileContext context, EnumMap<RelPos, EnumSet<TileType>> aroundTypes, Array<TileAnimation> sprites) {
		TileAnimation trans = getTransitionSprite(context);
		if(trans != null)
			sprites.add(trans);
		else {
			TileAnimation main = getMainSprite(context);
			if(main != null) sprites.add(main);
			TileAnimation connect = getConnectionSprite(aroundTypes);
			if(connect != null) sprites.add(connect);
		}
	}
	
	private TileAnimation getTransitionSprite(@NotNull Tile.TileContext context) {
		ActiveTileTransition transition = context.getData(TileDataTag.Transition);
		if(transition != null)
			return transition.transition.animation;
		return null;
	}
	
	// todo later on, if I decide to have multiple named main sprites, I'll need to provide a way to specify which one to use; but until then, this method will expect them to be indexed if there is more than one.
	private TileAnimation getMainSprite(TileContext context) {
		final int mainAnimCount = mainTileAnimations.size();//TileAnimType.Main.getFor(tileType).getAnimationCount();
		if(mainAnimCount <= 0)
			return null;
		
		int idx = 0;
		if(mainAnimCount > 1) {
			// pick a random sprite based on the tile position and tile type
			Tile tile = context.getTile();
			Random rand = MyUtils.getRandom(tile.x * 17 + tile.y * 131 + tileType.ordinal() * 79);
			idx = rand.nextInt(mainAnimCount);
		}
		
		return mainTileAnimations.get(idx);
	}
	
	/// Checks the given aroundTypes for all types
	private TileAnimation getConnectionSprite(EnumMap<RelPos, EnumSet<TileType>> aroundTypes) {
		// if(!TileAnimType.Connection.hasSprites(tileType))
		if(connectionTileAnimations.size() == 0)
			return null;
		
		if(connectionCheck == null) {
			return connectionTileAnimations.get(0);
		}
		
		EnumMap<RelPos, Boolean> tileConnections = new EnumMap<>(RelPos.class);
		
		for(RelPos rp: RelPos.values) {
			// check if each surrounding tile has something in the connectingTypes array
			boolean connects = false;
			for(TileType aroundType: aroundTypes.get(rp)) {
				if(connectionCheck.connects(aroundType)) {
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
		
		return connectionTileAnimations.get(spriteIdx);
	}
	
	// gets the overlap sprite (sides + any isolated corners) for this tiletype overlapping a tile at the given positions.
	/// returns sprites for the stored type assuming it is overlapping at the given positions.
	public void addOverlapSprites(EnumSet<RelPos> ovLayout, Array<TileAnimation> sprites) {
		if(!TileAnimType.Overlap.hasSprites(tileType))
			return;
		
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
		if(total > 0) sprites.add(getAnim(total+3)); // don't care to add if all zeros, because then it's just blank. Also, the +3 is to skip past the first 4 sprites, which are the corners (we add 3 instead of 4 because total will start at 1 rather than 0).
		// four corners; NOTE, the artist should work on the corner sprites in one tile-sized image, to make sure that they only use a quarter of it at absolute most.
		if(ovLayout.contains(RelPos.TOP_LEFT)     && bits[0] == 0 && bits[1] == 0) sprites.add(getAnim(0)); // 2
		if(ovLayout.contains(RelPos.TOP_RIGHT)    && bits[1] == 0 && bits[2] == 0) sprites.add(getAnim(1)); // 8
		if(ovLayout.contains(RelPos.BOTTOM_RIGHT) && bits[2] == 0 && bits[3] == 0) sprites.add(getAnim(2)); // 6
		if(ovLayout.contains(RelPos.BOTTOM_LEFT)  && bits[3] == 0 && bits[0] == 0) sprites.add(getAnim(3)); // 0
	}
	
	private TileAnimation getAnim(int index) {
		return overlapTileAnimations.get(index);
	}
}
