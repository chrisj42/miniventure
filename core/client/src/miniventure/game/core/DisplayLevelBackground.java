package miniventure.game.core;

import miniventure.game.util.MyUtils;
import miniventure.game.util.pool.VectorPool;
import miniventure.game.world.level.RenderLevel;
import miniventure.game.world.management.DisplayWorld;
import miniventure.game.world.management.TimeOfDay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class DisplayLevelBackground {
	
	private static final float PAN_SPEED = 4.5f; // in tiles/second.
	
	private final RenderLevel backgroundLevel;
	private final LevelViewport levelView;
	private final Color lightOverlay;
	private final Vector2 cameraPos, cameraDir;
	
	DisplayLevelBackground() {
		levelView = new LevelViewport();
		levelView.zoom(-1);
		TimeOfDay time = TimeOfDay.values[MathUtils.random(TimeOfDay.values.length-1)];
		lightOverlay = TimeOfDay.getSkyColor(time.getStartOffsetSeconds());
		
		backgroundLevel = new DisplayWorld().getLevel();
		
		Vector2 size = VectorPool.POOL.obtain(levelView.getViewWidth(), levelView.getViewHeight());//.scl(0.5f);
		cameraPos = VectorPool.POOL.obtain(MathUtils.random(size.x, backgroundLevel.getWidth()-size.x), MathUtils.random(size.y, backgroundLevel.getHeight()-size.y));
		VectorPool.POOL.free(size);
		
		cameraDir = VectorPool.POOL.obtain().setLength(PAN_SPEED).setToRandomDirection().setLength(PAN_SPEED);
	}
	
	void render(float delta) {
		levelView.render(cameraPos, lightOverlay, backgroundLevel);
		
		cameraPos.add(cameraDir.cpy().scl(MyUtils.getDeltaTime()));
		cameraDir.x = velDir(cameraPos.x, cameraDir.x, levelView.getViewWidth()/2, backgroundLevel.getWidth() - levelView.getViewWidth()/2);
		cameraDir.y = velDir(cameraPos.y, cameraDir.y, levelView.getViewHeight()/2, backgroundLevel.getHeight() - levelView.getViewHeight()/2);
	}
	
	private float velDir(float pos, float vel, float min, float max) {
		if((pos >= max && vel >= 0) || (pos <= min && vel <= 0)) {
			vel += MathUtils.random(-PAN_SPEED/4, PAN_SPEED/4);
			vel = -vel;
		}
		
		return vel;
	}
	
	void resize(int width, int height) {
		levelView.resize(width, height);
	}
	
	void dispose() {
		levelView.dispose();
		VectorPool.POOL.free(cameraDir);
		VectorPool.POOL.free(cameraPos);
	}
}
