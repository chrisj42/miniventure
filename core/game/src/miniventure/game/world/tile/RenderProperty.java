package miniventure.game.world.tile;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface RenderProperty extends TilePropertyInstance {
	
	void render(Batch batch, float x, float y, float frameDelta);
	
}
