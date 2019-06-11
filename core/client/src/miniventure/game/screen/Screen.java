package miniventure.game.screen;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.jetbrains.annotations.Nullable;

public class Screen extends Stage {
	
	@Nullable
	private Screen parent;
	private LinkedList<Screen> children;
	
	
	private void init() {
		children = new LinkedList<>();
	}
	
	protected Screen() {
		super();
		init();
	}
	
	protected Screen(Viewport viewport) {
		super(viewport);
		init();
	}
	
	protected Screen(Viewport viewport, Batch batch) {
		super(viewport, batch);
		init();
	}
	
	
	
}
