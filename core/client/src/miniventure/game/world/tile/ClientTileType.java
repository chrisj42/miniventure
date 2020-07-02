package miniventure.game.world.tile;

import miniventure.game.util.MyUtils;
import miniventure.game.util.param.Param;
import miniventure.game.util.param.ParamMap;
import miniventure.game.util.param.Value;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import org.jetbrains.annotations.NotNull;

import static miniventure.game.world.tile.TileTypeRenderer.ConnectionCheck.list;
import static miniventure.game.world.tile.TileTypeRenderer.buildRenderer;

public class ClientTileType extends TileType {
	
	public static void init() {
		MyUtils.debug("about to init TileTypeRenderer");
		TileTypeRenderer.init();
		for(ClientTileTypeEnum type: ClientTileTypeEnum.values)
			type.getType();
	}
	
	@FunctionalInterface
	private interface P {
		Param<Float> lightRadius = new Param<>(0f);
		Param<SwimAnimation> swimAnimation = new Param<>(null);
		// Param<List<TransitionAnimation>> transitions = new Param<>(new ArrayList<>(0));
		
		ClientTileType get(TileTypeEnum type);
	}
	
	private final TileTypeRenderer renderer;
	
	private final float lightRadius;
	private final SwimAnimation swimAnimation;
	
	// all default; goes to connection+overlap
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, Value<?>... params) {
		this(tileType, TileTypeRenderer.buildRenderer(tileType, isOpaque).build(), params);
	}
	/*// connection only; goes to connection+overlap
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionManager connectionManager, Value<?>... params) {
		this(tileType, isOpaque, connectionManager, new OverlapManager(tileType), params);
	}
	// overlap only; goes to connection+overlap
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, OverlapManager overlapManager, Value<?>... params) {
		this(tileType, isOpaque, new ConnectionManager(tileType, RenderStyle.SINGLE_FRAME), overlapManager, params);
	}
	// overlap+connection; goes to renderer
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionManager connectionManager, OverlapManager overlapManager, Value<?>... params) {
		this(tileType, new TileTypeRenderer(tileType, isOpaque, connectionManager, overlapManager), params);
	}*/
	// renderer; final
	private ClientTileType(@NotNull TileTypeEnum tileType, TileTypeRenderer renderer, Value<?>... params) {
		super(tileType);
		this.renderer = renderer;
		ParamMap map = new ParamMap(params);
		lightRadius = map.get(P.lightRadius);
		swimAnimation = map.get(P.swimAnimation);
		
		/*List<TransitionAnimation> transitions = map.get(P.transitions);
		this.transitions = new HashMap<>(transitions.size());
		for(TransitionAnimation trans: transitions)
			this.transitions.put(trans.getName(), trans);*/
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
	
	private enum ClientTileTypeEnum {
		
		HOLE(type -> new ClientTileType(type, buildRenderer(type, true)
				.connect(list(type, TileTypeEnum.WATER))
				.build(),
			P.swimAnimation.as(new SwimAnimation(type, 0.75f))
		)),
		
		DIRT(type -> new ClientTileType(type, true)),
		
		SAND(type -> new ClientTileType(type, true)),
		
		GRASS(type -> new ClientTileType(type, true)),
		
		STONE_PATH(type -> new ClientTileType(type, true)),
		
		SNOW(type -> new ClientTileType(type, true)),
		
		SMALL_STONE(type -> new ClientTileType(type, false)),
		
		STONE_FLOOR(type -> new ClientTileType(type, true)),
		
		WATER(type -> new ClientTileType(type, buildRenderer(type, true,
				new RenderStyle(PlayMode.LOOP_RANDOM, 5),
				new RenderStyle(true, 24)
			).build(),
			P.swimAnimation.as(new SwimAnimation(type))
		)),
		
		DOCK(type -> new ClientTileType(type, false)),
		
		COAL_ORE(ClientTileFactory::ore),
		IRON_ORE(ClientTileFactory::ore),
		TUNGSTEN_ORE(ClientTileFactory::ore),
		RUBY_ORE(ClientTileFactory::ore),
		
		STONE(type -> new ClientTileType(type, buildRenderer(type, true)
			.connect(list(type, COAL_ORE.mainEnum, IRON_ORE.mainEnum, TUNGSTEN_ORE.mainEnum, RUBY_ORE.mainEnum))
			.build()
		)),
		
		WOOD_WALL(ClientTileFactory::wall),
		STONE_WALL(ClientTileFactory::wall),
		
		OPEN_DOOR(type -> new ClientTileType(type, buildRenderer(type, false)
			.addTransition("open", new RenderStyle(PlayMode.NORMAL, 24))
			.addTransition("close", new RenderStyle(PlayMode.NORMAL, 24))
			.build()
		)),
		CLOSED_DOOR(type -> new ClientTileType(type, false)),
		
		TORCH(type -> new ClientTileType(type, buildRenderer(type, false, new RenderStyle(12))
				.addTransition("enter", new RenderStyle(12))
				.build(),
			P.lightRadius.as(2f)
		)),
		
		CACTUS(type -> new ClientTileType(type, false)),
		
		CARTOON_TREE(ClientTileFactory::tree),
		DARK_TREE(ClientTileFactory::tree),
		PINE_TREE(ClientTileFactory::tree),
		POOF_TREE(ClientTileFactory::tree),
		
		AIR(type -> new ClientTileType(type, false));
		
		
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
			return new ClientTileType(type, buildRenderer(type, false)
				.connect(list(TileTypeEnum.STONE))
				.build()
			);
		}
		
		static ClientTileType wall(TileTypeEnum type) {
			return new ClientTileType(type, false);
		}
		
		/*static ClientTileType door(TileTypeEnum type, boolean open) {
			return new ClientTileType(type, buildRenderer(type, false)
				.addTransition("open", new RenderStyle(PlayMode.NORMAL, 24))
				.addTransition("close", new RenderStyle(PlayMode.NORMAL, 24))
				.build(),
				P.transitions.as(open?Arrays.asList(
					new TransitionAnimation(type, ),
					new TransitionAnimation(type, )
					):new ArrayList<>(0)
				)
			);
		}*/
		
		static ClientTileType tree(TileTypeEnum type) {
			return new ClientTileType(type, false);
		}
	}
}
