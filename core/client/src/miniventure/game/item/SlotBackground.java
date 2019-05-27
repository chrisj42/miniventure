package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.function.FetchFunction;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

public class SlotBackground extends BaseDrawable {
	
	private static final Color selectionColor = new Color(.85f, .87f, 0, 1);
	private static final Color backgroundColor = new Color(.64f, .64f, .64f, 1);
	
	private static final float SHRINK_RATIO = .75f;
	
	private final FetchFunction<Boolean> drawSlot;
	private final FetchFunction<Boolean> drawSelected;
	
	private final TextureHolder backgroundTexture;
	// private final TextureHolder backgroundTexture;
	// private final TextureHolder selectionTexture;
	
	SlotBackground(FetchFunction<Boolean> drawSlot, FetchFunction<Boolean> drawSelected) {
		this.drawSlot = drawSlot;
		this.drawSelected = drawSelected;
		
		backgroundTexture = GameCore.icons.get("fade-rect");
		// backgroundTexture = GameCore.icons.get("inv-slot-bg");
		// selectionTexture = GameCore.icons.get("inv-slot-select");
	}
	
	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		if(!drawSlot.get())
			return;
		
		Color prev = batch.getColor();
		
		if(drawSelected.get()) {
			batch.setColor(selectionColor);
			batch.draw(backgroundTexture.texture, x, y, width, height);
		}
		
		batch.setColor(backgroundColor);
		float nwidth = width * SHRINK_RATIO;
		float nheight = height * SHRINK_RATIO;
		float xoff = (width - nwidth) / 2;
		float yoff = (height - nheight) / 2;
		batch.draw(backgroundTexture.texture, x+xoff, y+yoff, nwidth, nheight);
		
		batch.setColor(prev);
	}
	
	
}
