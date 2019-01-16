package miniventure.game.world.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import miniventure.game.util.param.Param;
import miniventure.game.util.param.ParamMap;
import miniventure.game.util.param.Value;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import org.jetbrains.annotations.NotNull;

public class ClientTileType extends TileType {
	
	public static void init() {
		TileTypeRenderer.init();
		for(ClientTileTypeEnum type: ClientTileTypeEnum.values)
			type.getType();
	}
	
	@FunctionalInterface
	private interface P {
		Param<Float> lightRadius = new Param<>(0f);
		Param<SwimAnimation> swimAnimation = new Param<>(null);
		Param<List<TransitionAnimation>> transitions = new Param<>(new ArrayList<>(0));
		
		ClientTileType get(TileTypeEnum type);
	}
	
	private final TileTypeRenderer renderer;
	
	private final float lightRadius;
	private final SwimAnimation swimAnimation;
	private final HashMap<String, TransitionAnimation> transitions;
	
	// all default; goes to connection+overlap
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, Value... params) {
		this(tileType, isOpaque, new ConnectionManager(tileType), new OverlapManager(tileType), params);
	}
	// connection only; goes to connection+overlap
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionManager connectionManager, Value... params) {
		this(tileType, isOpaque, connectionManager, new OverlapManager(tileType), params);
	}
	// overlap only; goes to connection+overlap
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, OverlapManager overlapManager, Value... params) {
		this(tileType, isOpaque, new ConnectionManager(tileType, RenderStyle.SINGLE_FRAME), overlapManager, params);
	}
	// overlap+connection; goes to renderer
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionManager connectionManager, OverlapManager overlapManager, Value... params) {
		this(tileType, new TileTypeRenderer(tileType, isOpaque, connectionManager, overlapManager), params);
	}
	// renderer; final
	private ClientTileType(@NotNull TileTypeEnum tileType, TileTypeRenderer renderer, Value... params) {
		super(tileType);
		this.renderer = renderer;
		ParamMap map = new ParamMap(params);
		lightRadius = map.get(P.lightRadius);
		swimAnimation = map.get(P.swimAnimation);
		
		List<TransitionAnimation> transitions = map.get(P.transitions);
		this.transitions = new HashMap<>(transitions.size());
		for(TransitionAnimation trans: transitions)
			this.transitions.put(trans.getName(), trans);
	}
	
	public static ClientTileType get(TileTypeEnum type) {
		return ClientTileTypeEnum.value(type.ordinal()).getType();
	}
	
	public TileTypeRenderer getRenderer() {
		return renderer;
	}
	
	public float getLightRadius() {
		return lightRadius;
	}
	
	public SwimAnimation getSwimAnimation() {
		return swimAnimation;
	}
	
	public TransitionAnimation getTransition(String name) { return transitions.get(name); }
	
	private enum ClientTileTypeEnum {
		
		HOLE(type -> new ClientTileType(type, true,
			new ConnectionManager(type, type, TileTypeEnum.WATER),
			P.swimAnimation.as(new SwimAnimation(type, 0.75f))
		)),
		
		DIRT(type -> new ClientTileType(type, true)),
		
		SAND(type -> new ClientTileType(type, true)),
		
		GRASS(type -> new ClientTileType(type, true)),
		
		STONE_PATH(type -> new ClientTileType(type, true)),
		
		SNOW(type -> new ClientTileType(type, true)),
		
		FLINT(type -> new ClientTileType(type, false)),
		
		WATER(type -> new ClientTileType(type, true,
			new ConnectionManager(type, new RenderStyle(PlayMode.LOOP_RANDOM, 0.2f)),
			new OverlapManager(type, new RenderStyle(true, 1/24f)),
			P.swimAnimation.as(new SwimAnimation(type))
		)),
		
		COAL_ORE(ClientTileFactory::ore),
		IRON_ORE(ClientTileFactory::ore),
		TUNGSTEN_ORE(ClientTileFactory::ore),
		RUBY_ORE(ClientTileFactory::ore),
		
		STONE(type -> new ClientTileType(type, true/*,
			new ConnectionManager(type, RenderStyle.SINGLE_FRAME, COAL_ORE.mainEnum, IRON_ORE.mainEnum, TUNGSTEN_ORE.mainEnum, RUBY_ORE.mainEnum)*/
		)),
		
		STONE_FLOOR(type -> new ClientTileType(type, true)),
		
		WOOD_WALL(ClientTileFactory::wall),
		STONE_WALL(ClientTileFactory::wall),
		
		OPEN_DOOR(type -> ClientTileFactory.door(type, true)),
		CLOSED_DOOR(type -> ClientTileFactory.door(type, false)),
		
		TORCH(type -> new ClientTileType(type, false,
			new ConnectionManager(type, new RenderStyle(1/12f)),
			P.lightRadius.as(2f),
			P.transitions.as(Collections.singletonList(
				new TransitionAnimation(type, "enter", new RenderStyle(3 / 12f, false))
			))
		)),
		
		CACTUS(type -> new ClientTileType(type, false)),
		
		CARTOON_TREE(ClientTileFactory::tree),
		DARK_TREE(ClientTileFactory::tree),
		PINE_TREE(ClientTileFactory::tree),
		POOF_TREE(ClientTileFactory::tree),
		
		AIR(type -> new ClientTileType(type, false/*,
			new ConnectionManager(type, (tile, otherType) -> {
				TileTypeEnum thisType = tile.getType().getTypeEnum();
				boolean valid = otherType == TileTypeEnum.STONE && thisType != TileTypeEnum.STONE;
				// todo after solid types / height is implemented, check it here; air matches with all tiles which have layer groups on higher levels than this air type.
				// ClientTileType ctype = get(otherType);
				// ctype.
				return valid; // would be better to be false but I want to see what happens.
			})*/
		));
		
		
		/** @noinspection NonFinalFieldInEnum*/
		private ClientTileType type = null;
		private final P fetcher;
		private final TileTypeEnum mainEnum;
		
		ClientTileTypeEnum(P fetcher) {
			this.fetcher = fetcher;
			mainEnum = TileTypeEnum.value(ordinal());
		}
		
		public ClientTileType getType() {
			if(type == null)
				type = fetcher.get(TileTypeEnum.value(ordinal()));
			return type;
		}
		
		private static final ClientTileTypeEnum[] values = ClientTileTypeEnum.values();
		public static ClientTileTypeEnum value(int ord) { return values[ord]; }
	}
	
	private interface ClientTileFactory {
		static ClientTileType ore(TileTypeEnum type) {
			return new ClientTileType(type, false,
				new ConnectionManager(type, TileTypeEnum.STONE),
				new OverlapManager(type)
			);
		}
		
		static ClientTileType wall(TileTypeEnum type) {
			return new ClientTileType(type, false);
		}
		
		static ClientTileType door(TileTypeEnum type, boolean open) {
			return new ClientTileType(type, false,
				P.transitions.as(open?Arrays.asList(
					new TransitionAnimation(type, "open", new RenderStyle(PlayMode.NORMAL, 3/24f, false)),
					new TransitionAnimation(type, "close", new RenderStyle(PlayMode.NORMAL, 3/24f, false))
					):new ArrayList<>(0)
				)
			);
		}
		
		static ClientTileType tree(TileTypeEnum type) {
			return new ClientTileType(type, false);
		}
	}
}
