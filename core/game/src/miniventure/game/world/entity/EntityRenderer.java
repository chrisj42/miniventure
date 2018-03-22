package miniventure.game.world.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.item.Item;
import miniventure.game.util.Blinker;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public abstract class EntityRenderer {
	
	protected abstract String[] save();
	
	public abstract void render(float x, float y, Batch batch, float delta);
	
	public abstract Vector2 getSize();
	
	public static class SpriteRenderer extends EntityRenderer {
		
		private static final HashMap<String, AtlasRegion> textures = new HashMap<>();
		
		private final String spriteName;
		private final TextureRegion sprite;
		
		public SpriteRenderer(String spriteName, TextureRegion texture) {
			this.spriteName = spriteName;
			this.sprite = texture;
		}
		public SpriteRenderer(String spriteName) {
			this.spriteName = spriteName;
			
			if(!textures.containsKey(spriteName))
				textures.put(spriteName, GameCore.entityAtlas.findRegion(spriteName));
			
			sprite = textures.get(spriteName);
		}
		protected SpriteRenderer(String[] data) {
			this(data[0]);
		}
		
		@Override
		protected String[] save() { return new String[] {spriteName}; }
		
		public String getName() { return spriteName; }
		
		@Override
		public void render(float x, float y, Batch batch, float delta) { batch.draw(sprite, x, y); }
		
		@Override
		public Vector2 getSize() { return new Vector2(sprite.getRegionWidth(), sprite.getRegionHeight()); }
	}
	
	
	public static class ItemSpriteRenderer extends SpriteRenderer {
		
		private final Item item;
		
		public ItemSpriteRenderer(Item item) {
			super(item.getName(), item.getTexture());
			this.item = item;
		}
		
		protected ItemSpriteRenderer(String[] data) {
			this(Item.load(data));
		}
		
		@Override
		protected String[] save() { return item.save(); }
	}
	
	
	public static class AnimationRenderer extends EntityRenderer {
		
		private static final HashMap<String, Array<AtlasRegion>> animationFrames = new HashMap<>();
		
		private final String animationName;
		private final boolean isFrameDuration;
		private final float duration;
		
		private final Animation<TextureRegion> animation;
		private float elapsedTime = 0;
		
		public AnimationRenderer(String animationName, float frameTime) { this(animationName, frameTime, true); }
		public AnimationRenderer(String animationName, float duration, boolean isFrameDuration) {
			this.animationName = animationName;
			this.duration = duration;
			this.isFrameDuration = isFrameDuration;
			
			if(!animationFrames.containsKey(animationName))
				animationFrames.put(animationName, GameCore.entityAtlas.findRegions(animationName));
			
			Array<AtlasRegion> frames = animationFrames.get(animationName);
			
			animation = new Animation<>(frames.size==0?1:isFrameDuration?duration:duration/frames.size, frames);
		}
		private AnimationRenderer(String[] data) {
			this(data[0], Float.parseFloat(data[1]), Boolean.parseBoolean(data[2]));
		}
		
		@Override
		protected String[] save() {
			return new String[] {animationName, duration+"", isFrameDuration+""};
		}
		
		public String getName() { return animationName; }
		
		@Override
		public void render(float x, float y, Batch batch, float delta) {
			elapsedTime += delta;
			batch.draw(animation.getKeyFrame(elapsedTime, true), x, y);
		}
		
		@Override
		public Vector2 getSize() {
			TextureRegion frame = animation.getKeyFrame(elapsedTime, true);
			return new Vector2(frame.getRegionWidth(), frame.getRegionHeight());
		}
	}
	
	
	/*public static class DirectionalAnimationRenderer extends EntityRenderer {
		
		private Direction dir;
		
		public DirectionalAnimationRenderer() {
			
		}
		
		protected DirectionalAnimationRenderer(String[] data) {
			this();
		}
		
		@Override
		protected String[] save() {
			return new String[0];
		}
		
		@Override
		public void render(float x, float y, Batch batch, float delta) {
			
		}
		
		@Override
		public Vector2 getSize() {
			return null;
		}
	}*/
	
	
	public static class TextRenderer extends EntityRenderer {
		
		private final String text;
		private final Color main;
		private final Color shadow;
		private final float width;
		private final float height;
		
		public TextRenderer(String text, Color main, Color shadow) {
			this.text = text;
			this.main = main;
			this.shadow = shadow;
			
			GlyphLayout layout = GameCore.getTextLayout(text);
			this.width = layout.width;
			this.height = layout.height;
		}
		private TextRenderer(String[] data) {
			this(data[0], Color.valueOf(data[1]), Color.valueOf(data[2]));
		}
		
		@Override
		protected String[] save() { return new String[] {text, main.toString(), shadow.toString()}; }
		
		@Override
		public void render(float x, float y, Batch batch, float delta) {
			BitmapFont font = GameCore.getFont();
			font.setColor(shadow);
			font.draw(batch, text, x-1, y+1, 0, Align.center, false);
			font.setColor(main);
			font.draw(batch, text, x, y, 0, Align.center, false);
		}
		
		@Override
		public Vector2 getSize() { return new Vector2(width, height); }
	}
	
	// NOTE, I won't be using this for mobs. I want to just enforce blinking no matter the sprite underneath, and only for a period of time...
	public static class BlinkRenderer extends EntityRenderer {
		
		private EntityRenderer mainRenderer;
		private final boolean blinkFirst;
		private final float initialDuration;
		private final Blinker blinker;
		
		private float elapsedTime;
		
		public BlinkRenderer(EntityRenderer mainRenderer, float initialDuration, boolean blinkFirst, Blinker blinker) {
			this.mainRenderer = mainRenderer;
			this.initialDuration = initialDuration;
			this.blinkFirst = blinkFirst;
			this.blinker = blinker;
		}
		
		private BlinkRenderer(String[] data) {
			this(EntityRenderer.deserialize(MyUtils.parseLayeredString(data[0])), Float.parseFloat(data[1]), Boolean.parseBoolean(data[2]), Blinker.load(MyUtils.parseLayeredString(data[3])));
		}
		
		public void setRenderer(EntityRenderer renderer) {
			mainRenderer = renderer;
		}
		
		@Override
		protected String[] save() {
			return new String[] {
				MyUtils.encodeStringArray(EntityRenderer.serialize(mainRenderer)),
				initialDuration +"",
				blinkFirst+"",
				MyUtils.encodeStringArray(blinker.save())
			};
		}
		
		@Override
		public void render(float x, float y, Batch batch, float delta) {
			elapsedTime += delta;
			
			if(blinkFirst) {
				blinker.update(delta);
				
				if(elapsedTime > initialDuration || blinker.shouldRender())
					mainRenderer.render(x, y, batch, delta);
			}
			else {
				boolean blinking = elapsedTime > initialDuration;
				
				if(blinking) blinker.update(delta);
				
				if(!blinking || blinker.shouldRender())
					mainRenderer.render(x, y, batch, delta);
			}
		}
		
		@Override
		public Vector2 getSize() { return mainRenderer.getSize(); }
	}
	
	
	public static final EntityRenderer BLANK = new EntityRenderer() {
		@Override public void render(float x, float y, Batch batch, float delta) {}
		@Override public Vector2 getSize() { return new Vector2(); }
		@Override protected String[] save() { return new String[0]; }
	};
	
	
	public static String[] serialize(EntityRenderer renderer) {
		String[] data = renderer.save();
		String[] allData = new String[data.length+1];
		
		String className = renderer.getClass().getCanonicalName();
		if(className == null)
			allData[0] = "";
		else
			allData[0] = className.replace(EntityRenderer.class.getCanonicalName()+".", "");
		
		System.arraycopy(data, 0, allData, 1, data.length);
		
		return allData;
	}
	
	@NotNull
	public static EntityRenderer deserialize(String[] allData) {
		if(allData[0].length() == 0) return BLANK;
		
		String className = allData[0];
		String[] data = Arrays.copyOfRange(allData, 1, allData.length);
		
		try {
			//Class<?> clazz = Class.forName(EntityRenderer.class.getPackage().getName()+"."+className);
			for(Class<?> inner: EntityRenderer.class.getDeclaredClasses()) {
				//System.out.println("found inner class " + inner.getSimpleName());
				if(inner.getSimpleName().equals(className)) {
					
					Class<? extends EntityRenderer> erClass = inner.asSubclass(EntityRenderer.class);
					//noinspection JavaReflectionMemberAccess
					Constructor<? extends EntityRenderer> constructor = erClass.getDeclaredConstructor(String[].class);
					
					constructor.setAccessible(true);
					return constructor.newInstance((Object)data);
				}
			}
			
			throw new ClassNotFoundException(className);
			
		} catch(ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			return BLANK;
		}
	}
}
