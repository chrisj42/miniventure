package miniventure.game.world.entity;

import miniventure.game.client.ClientCore;
import miniventure.game.client.FontStyle;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import org.jetbrains.annotations.NotNull;

public class ClientEntityRenderer {
	
	private ClientEntityRenderer() {}
	
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
