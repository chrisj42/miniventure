package miniventure.game.world.entity;

import java.util.Arrays;
import java.util.HashMap;

import miniventure.game.client.ClientCore;
import miniventure.game.client.FontStyle;
import miniventure.game.util.MyUtils;
import miniventure.game.util.blinker.Blinker;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.entity.EntityRenderer.AnimationRenderer;
import miniventure.game.world.entity.EntityRenderer.BlinkRenderer;
import miniventure.game.world.entity.EntityRenderer.DirectionalAnimationRenderer;
import miniventure.game.world.entity.EntityRenderer.ItemSpriteRenderer;
import miniventure.game.world.entity.EntityRenderer.SpriteRenderer;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import org.jetbrains.annotations.NotNull;

public class ClientEntityRenderer {
	
	private ClientEntityRenderer() {}
	
	private static HashMap<String, RendererType> classMap = new HashMap<>();
	
	public static void init() {
		// so turns out that inner enums are not initialized when the class is, so classMap won't get filled until we use RendererType directly. So, instead we're just going to fill it directly right here.
		for(RendererType type: RendererType.values())
			classMap.put(type.clazz.getName(), type);
	}
	
	enum RendererType {
		Sprite(SpriteRenderer.class, data -> new SpriteRenderer(data[0])),
		
		ItemSprite(ItemSpriteRenderer.class, ItemSpriteRenderer::new),
		
		Animation(AnimationRenderer.class, AnimationRenderer::new),
		
		DirectionalAnimation(DirectionalAnimationRenderer.class, DirectionalAnimationRenderer::new),
		
		Blink(BlinkRenderer.class, data -> new BlinkRenderer(
			ClientEntityRenderer.deserialize(MyUtils.parseLayeredString(data[0])), data[1].equals("null")?null:Color.valueOf(data[1]), Float.parseFloat(data[2]), Boolean.parseBoolean(data[3]), Blinker.deserialize(MyUtils.parseLayeredString(data[4]))
		)),
		
		Text(TextRenderer.class, TextRenderer::new),
		
		Blank(EntityRenderer.BLANK.getClass(), data -> EntityRenderer.BLANK);
		
		private final Class<? extends EntityRenderer> clazz;
		private final MapFunction<String[], EntityRenderer> deserializer;
		
		RendererType(Class<? extends EntityRenderer> clazz, MapFunction<String[], EntityRenderer> deserializer) {
			this.clazz = clazz;
			this.deserializer = deserializer;
		}
		
		public EntityRenderer deserialize(String[] data) {
			return deserializer.get(data);
		}
	}
	
	@NotNull
	public static EntityRenderer deserialize(String[] allData) {
		if (allData[0].length() == 0) return EntityRenderer.BLANK;
		
		String className = allData[0];
		String[] data = Arrays.copyOfRange(allData, 1, allData.length);
		
		RendererType type = classMap.get(className);
		if(type == null) {
			System.err.println("Error: could not deserialize EntityRenderer; unrecognized class '"+className+'\'');
			return EntityRenderer.BLANK;
		}
		
		return type.deserialize(data);
	}
		
	public static class TextRenderer extends EntityRenderer {
		
		@NotNull private final String text;
		private final Color main;
		private final Color shadow;
		
		public TextRenderer(@NotNull String text, Color main, Color shadow) {
			this.text = text;
			this.main = main;
			this.shadow = shadow;
		}
		private TextRenderer(String[] data) {
			this(data[0], Color.valueOf(data[1]), Color.valueOf(data[2]));
		}
		
		@Override
		protected String[] serialize() { return new String[] {text, main.toString(), shadow.toString()}; }
		
		@Override
		public void render(float x, float y, Batch batch, float drawableHeight) {
			BitmapFont font = ClientCore.getFont(FontStyle.KeepSizeScaled);
			font.setColor(shadow);
			font.draw(batch, text, x-Tile.SCALE, y+Tile.SCALE, 0, Align.center, false);
			font.setColor(main);
			try {
				font.draw(batch, text, x, y, 0, Align.center, false);
			} catch(Exception e) {
				System.err.println("error drawing text");
				e.printStackTrace();
			}
		}
		
		@Override
		public Vector2 getSize() { return new Vector2(); }
	}
}
