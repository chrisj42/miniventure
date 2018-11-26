package miniventure.game.world.entity;

import miniventure.game.GameCore;
import miniventure.game.world.entity.EntityRenderer.BlinkRenderer;
import miniventure.game.world.entity.EntityRenderer.TextRenderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Align;

import org.jetbrains.annotations.NotNull;

public class ClientEntityRenderer {
	
	private ClientEntityRenderer() {}
	
	public static EntityRenderer deserialize(String[] data) {
		EntityRenderer orig = EntityRenderer.deserialize(data);
		if(orig instanceof TextRenderer)
			return new ClientTextRenderer((TextRenderer)orig);
		
		if(orig instanceof BlinkRenderer) {
			EntityRenderer main = ((BlinkRenderer)orig).mainRenderer;
			if(main instanceof TextRenderer)
				((BlinkRenderer)orig).mainRenderer = new ClientTextRenderer((TextRenderer)main);
		}
		
		return orig;
	}
	
	public static class ClientTextRenderer extends TextRenderer {
		public ClientTextRenderer(@NotNull String text, Color main, Color shadow) {
			super(text, main, shadow);
		}
		
		private ClientTextRenderer(TextRenderer model) {
			super(model);
		}
		
		@Override
		public void render(float x, float y, Batch batch, float drawableHeight) {
			BitmapFont font = GameCore.getFont();
			font.setColor(shadow);
			font.draw(batch, text, x-1, y+1, 0, Align.center, false);
			font.setColor(main);
			try {
				font.draw(batch, text, x, y, 0, Align.center, false);
			} catch(Exception e) {
				System.err.println("error drawing text");
				e.printStackTrace();
			}
		}
	}
}
