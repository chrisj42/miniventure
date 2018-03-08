package miniventure.game.world.entitynew.property;

import miniventure.game.GameCore;
import miniventure.game.world.entitynew.DataCarrier;
import miniventure.game.world.entitynew.Entity;
import miniventure.game.world.entitynew.InstanceData;
import miniventure.game.world.entitynew.property.RenderProperty.TextSprite.TextSpriteData;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public abstract class RenderProperty implements EntityProperty {
	
	/*
		sprite properties:
			- animations
			- text sprites (uhhh...
			- single textures
	 */
	
	public static final RenderProperty DEFAULT = new RenderProperty() {
		@Override
		public void render(Entity e, float delta, SpriteBatch batch, float x, float y) {}
	};
	
	
	public abstract void render(Entity e, float delta, SpriteBatch batch, float x, float y);
	
	
	public static class TextSprite extends RenderProperty implements DataCarrier<TextSpriteData> {
		@Override
		public void render(Entity e, float delta, SpriteBatch batch, float x, float y) {
			TextSpriteData data = e.getDataObject(TextSprite.class, getUniquePropertyClass());
			data.font.draw(batch, data.text, x, y);
		}
		
		/* Data is:
			- text
			- color
			- outline bool
			- size
		 */
		@Override
		public String[] getInitialData() {
			FreeTypeFontParameter config = GameCore.getDefaultFontConfig();
			return new String[] {"", config.color.toString(), "true", config.size+""};
		}
		
		public class TextSpriteData extends InstanceData {
			
			private String text;
			private Color color;
			private boolean outline;
			private int size;
			
			private BitmapFont font;
			
			public TextSpriteData() {}
			public TextSpriteData(String text, Color color, boolean outline, int size) {
				this.text = text;
				this.color = color;
				this.outline = outline;
				this.size = size;
			}
			
			public void init(String text, Color color) {
				this.text = text;
				this.color = color;
				updateFont();
			}
			
			@Override
			public String[] serializeData() {
				return new String[] {
						text,
						color.toString(),
						outline+"",
						size+""
				};
			}
			
			@Override
			public void parseData(String[] data) {
				text = data[0];
				color = Color.valueOf(data[1]);
				outline = Boolean.valueOf(data[2]);
				size = Integer.parseInt(data[3]);
			}
			
			public void updateFont() {
				FreeTypeFontParameter config = GameCore.getDefaultFontConfig();
				config.color = color;
				config.borderWidth = outline ? 1 : 0;
				config.size = size;
				
				font = GameCore.getFont(config);
			}
		}
	}
	
	
	public static RenderProperty oneTexture(final String spriteName) {
		return new RenderProperty() {
			
			private final TextureRegion texture = GameCore.entityAtlas.findRegion(spriteName);
			
			@Override
			public void render(Entity e, float delta, SpriteBatch batch, float x, float y) {
				batch.draw(texture, x, y);
			}
		};
	}
	
	
	@Override
	public Class<? extends EntityProperty> getUniquePropertyClass() { return RenderProperty.class; }
}
