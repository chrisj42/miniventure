package miniventure.game.world.entitynew;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public interface RenderProperty extends EntityProperty {
	
	RenderProperty DEFAULT = new RenderProperty() {
		@Override public void drawSprite(float x, float y, SpriteBatch batch, float delta, Entity e) {}
		@Override public Vector2 getSize() { return new Vector2(); }
	};
	
	void drawSprite(float x, float y, SpriteBatch batch, float delta, Entity e);
	
	Vector2 getSize();
}
