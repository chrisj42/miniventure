package miniventure.game.screen;

import miniventure.game.util.MyUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public abstract class ColorDrawable implements Drawable {
	
	private Color color;
	
	public ColorDrawable(Color color) {
		this.color = color;
	}
	
	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		MyUtils.fillRect(x, y, width, height, color.cpy().mul(batch.getColor()), batch);
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
}
