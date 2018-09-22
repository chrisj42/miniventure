package miniventure.game.screen;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * A subclass of MenuScreen that implements this interface is communicating
 * that it uses libGDX to render some or all of its graphics.
 * 
 * Note that the graphics rendered this way will always appear under any graphics
 * rendered through swing; unless
 * com.sun.awt.AWTUtilities.setComponentMixingCutoutShape(Component, Shape)
 * is used to cutout an area from a swing component that otherwise would have covered
 * the libGDX graphics.
 */
public interface GLRenderer {
	
	/**
	 * Implementing MenuScreens are expected to have their own camera and batch (or use GameCore.getBatch());
	 * as such, it is the responsibility of said subclasses to set the batch projection matrix,
	 * start the batch, and end the batch.
	 */
	void glDraw();
	
}
