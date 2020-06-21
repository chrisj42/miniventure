package miniventure.game.texture.layer;

import miniventure.game.texture.TextureHolder;

// an abstraction that can hold either a single sprite/texture, or an animation.
public interface RenderLayer {
	
	/*
		- holds animation, or texture
		- designed to have one instance manage multiple rendering contexts
	 */
	
	TextureHolder getSprite();
	
}
