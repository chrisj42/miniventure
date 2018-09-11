package miniventure.game.ui;

import miniventure.game.ui.Component.SizeCache;

import com.badlogic.gdx.math.Vector2;

public interface Layout {
	
	default Vector2 minLayoutSize(Container c, Vector2 rt) { return layoutSize(c, rt, SizeCache::getMinSize); }
	default Vector2 preferredLayoutSize(Container c, Vector2 rt) { return layoutSize(c, rt, SizeCache::getPreferredSize); }
	default Vector2 maxLayoutSize(Container c, Vector2 rt) { return layoutSize(c, rt, SizeCache::getMaxSize); }
	
	@FunctionalInterface
	interface SizeFetcher {
		Vector2 getSize(SizeCache sizeCache, Vector2 rt);
	}
	
	Vector2 layoutSize(Container container, Vector2 rt, SizeFetcher sizeFetcher);
	
	void layoutContainer(Container container);
}
