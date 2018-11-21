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
	
	public static void init() {}
	
	@FunctionalInterface
	private interface P {
		Param<Float> lightRadius = new Param<>(0f);
		Param<Float> zOffset = new Param<>(0f);
		Param<SwimAnimation> swimAnimation = new Param<>(null);
		Param<List<TransitionAnimation>> transitions = new Param<>(new ArrayList<>(0));
		
		ClientTileType get(TileTypeEnum type);
	}
	
	private final TileTypeRenderer renderer;
	
	private final float lightRadius;
	private final float zOffset;
	private final SwimAnimation swimAnimation;
	private final HashMap<String, TransitionAnimation> transitions;
	
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, Value... params) {
		this(tileType, isOpaque, ConnectionManager.DEFAULT(tileType), params);
	}
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionManager connectionManager, Value... params) {
		this(tileType, isOpaque, connectionManager, OverlapManager.NONE(tileType), params);
	}
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, OverlapManager overlapManager, Value... params) {
		this(tileType, isOpaque, ConnectionManager.DEFAULT(tileType), overlapManager, params);
	}
	private ClientTileType(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionManager connectionManager, OverlapManager overlapManager, Value... params) {
		this(tileType, new TileTypeRenderer(tileType, isOpaque, connectionManager, overlapManager), params);
	}
	
	private ClientTileType(@NotNull TileTypeEnum tileType, TileTypeRenderer renderer, Value... params) {
		super(tileType);
		this.renderer = renderer;
		ParamMap map = new ParamMap(params);
		lightRadius = map.get(P.lightRadius);
		zOffset = map.get(P.zOffset);
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
	
	public float getZOffset() {
		return zOffset;
	}
	
	public SwimAnimation getSwimAnimation() {
		return swimAnimation;
	}
	
	public TransitionAnimation getTransition(String name) { return transitions.get(name); }
	
	private enum ClientTileTypeEnum {
		
		HOLE(type -> new ClientTileType(type, true,
			new ConnectionManager(type, RenderStyle.SINGLE_FRAME, type, TileTypeEnum.valueOf("WATER")),
			P.swimAnimation.as(new SwimAnimation(type, 0.75f))
		)),
		
		DIRT(type -> new ClientTileType(type, true)),
		
		SAND(type -> new ClientTileType(type, true,
			new OverlapManager(type, RenderStyle.SINGLE_FRAME)
		)),
		
		GRASS(type -> new ClientTileType(type, true,
			new OverlapManager(type, RenderStyle.SINGLE_FRAME)
		)),
		
		STONE_PATH(type -> new ClientTileType(type, true,
			new OverlapManager(type, RenderStyle.SINGLE_FRAME)
		)),
		
		SNOW(type -> new ClientTileType(type, true,
			new OverlapManager(type, RenderStyle.SINGLE_FRAME)
		)),
		
		FLINT(type -> new ClientTileType(type, false)),
		
		WATER(type -> new ClientTileType(type, true,
			new ConnectionManager(type, new RenderStyle(PlayMode.LOOP_RANDOM, 0.2f)),
			new OverlapManager(type, new RenderStyle(true, 1/24f)),
			P.swimAnimation.as(new SwimAnimation(type))
		)),
		
		COAL(ClientTileFactory::ore),
		IRON(ClientTileFactory::ore),
		TUNGSTEN(ClientTileFactory::ore),
		RUBY(ClientTileFactory::ore),
		
		STONE(type -> new ClientTileType(type, true,
			new OverlapManager(type, RenderStyle.SINGLE_FRAME)
		)),
		
		STONE_FLOOR(type -> new ClientTileType(type, true,
			new ConnectionManager(type, RenderStyle.SINGLE_FRAME, type)
		)),
		
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
		POOF_TREE(ClientTileFactory::tree);
		
		
		/** @noinspection NonFinalFieldInEnum*/
		private ClientTileType type = null;
		private final P fetcher;
		
		ClientTileTypeEnum(P fetcher) {
			this.fetcher = fetcher;
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
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			);
		}
		
		static ClientTileType wall(TileTypeEnum type) {
			return new ClientTileType(type, false,
				P.zOffset.as(0.4f)
			);
		}
		
		static ClientTileType door(TileTypeEnum type, boolean open) {
			return new ClientTileType(type, false,
				P.zOffset.as(0.4f),
				P.transitions.as(open?Arrays.asList(
					new TransitionAnimation(type, "open", new RenderStyle(PlayMode.NORMAL, 3/24f, false)),
					new TransitionAnimation(type, "close", new RenderStyle(PlayMode.NORMAL, 3/24f, false))
					):new ArrayList<>(0)
				)
			);
		}
		
		static ClientTileType tree(TileTypeEnum type) {
			return new ClientTileType(type, false,
				new ConnectionManager(type, RenderStyle.SINGLE_FRAME, type),
				P.zOffset.as(0.25f)
			);
		}
	}
}
