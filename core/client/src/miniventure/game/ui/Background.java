package miniventure.game.ui;

import miniventure.game.util.MyUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

@FunctionalInterface
public interface Background {
	
	void draw(Batch batch, float alpha, float x, float y, float width, float height);
	
	static Background fillColor(Color c) {
		return (batch, alpha, x, y, width, height) -> MyUtils.fillRect(x, y, width, height, c, alpha, batch);
	}
}
