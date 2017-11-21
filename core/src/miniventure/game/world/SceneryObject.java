package miniventure.game.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class SceneryObject extends Entity {
	
	private TextureRegion textureRegion;
	private Rectangle bounds;
	
	public SceneryObject(TextureRegion textureRegion) {
		this(textureRegion, 0, 0);
	}
	public SceneryObject(TextureRegion textureRegion, int x, int y) {
		super(x, y);
		this.textureRegion = textureRegion;
		bounds = new Rectangle(x, y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
	}
	
	@Override
	public void render(SpriteBatch batch, float delta) {
		batch.draw(textureRegion, bounds.x, bounds.y);
	}
}
