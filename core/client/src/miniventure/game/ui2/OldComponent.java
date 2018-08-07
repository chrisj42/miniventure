package miniventure.game.ui2;

import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class OldComponent implements InputProcessor {
	
	/*
		Selectable, size, fill axis?, relpos position,
		onselect action listeners, input management listeners,
		background rendering,
		parent.
		
		for input, components will be given the chance to receive mouse and key events.
		
		for selection, there will be a basic "onHighlight" method.
		when the keyboard is used for selection, 
	 */
	
	/*public enum MainAxisParentFill {
		NONE, RATIO, EQUAL
	}
	public enum OffAxisParentFill {
		NONE, SIBLING, PARENT
	}*/
	
	private float x;
	private float y;
	private float width;
	private float height;
	
	// private boolean fillX;
	// private fillY;
	
	@Nullable
	private Background background;
	
	private boolean selectable;
	private boolean showHighlight;
	
	@Nullable
	private OldContainer parent;
	
	@NotNull
	private RelPos relPos = RelPos.CENTER; // this determines where, in the area reserved for the component, it should be placed.
	
	
	
	public OldComponent() {
		
	}
	
	
	
	protected void render(Batch batch, float alpha, boolean focused) {
		if(background != null)
			background.draw(batch, alpha, x, y, width, height);
		
		if(focused && showHighlight)
			MyUtils.drawRect(x, y, width, height, Color.YELLOW, alpha, batch);
	}
	
	
	public float getX() { return x; }
	public void setX(float x) { this.x = x; }
	
	public float getY() { return y; }
	public void setY(float y) { this.y = y; }
	
	public Vector2 getPosition(Vector2 v) { return v.set(x, y); }
	
	public void setPosition(Vector2 v) { setPosition(v.x, v.y); }
	public void setPosition(float x, float y) { this.x = x; this.y = y; }
	
	public float getWidth() { return width; }
	public void setWidth(float width) { this.width = width; }
	
	public float getHeight() { return height; }
	public void setHeight(float height) { this.height = height; }
	
	public Vector2 getSize(Vector2 v) { return v.set(width, height); }
	
	public void setSize(Vector2 v) { setSize(v.x, v.y); }
	public void setSize(float width, float height) { this.width = width; this.height = height; }
	
	public Rectangle getBounds(Rectangle rect) { return rect.set(x, y, width, height); }
	public void setBounds(Rectangle rect) { setSize(rect.width, rect.height); setPosition(rect.x, rect.y); }
	
	// public boolean getFillX() { return fillX; }
	// public boolean getFillY() { return fillY; }
	// public void setFillX(boolean fillX) { this.fillX = fillX; }
	// public void setFillY(boolean fillY) { this.fillY = fillY; }
	// public void setFillParent(boolean fillX, boolean fillY) { setFillX(fillX); setFillY(fillY); }
	
	@Nullable
	public OldContainer getParent() { return parent; }
	public void setParent(@Nullable OldContainer parent) { this.parent = parent; }
	
	@NotNull
	public RelPos getRelPos() { return relPos; }
	
	public void setRelPos(@NotNull RelPos relPos) { this.relPos = relPos; }
	
	@Nullable
	public Background getBackground() { return background; }
	public void setBackground(@Nullable Background background) { this.background = background; }
	
	public boolean isSelectable() { return selectable && (parent == null || parent.isSelectable()); }
	public void setSelectable(boolean selectable) { this.selectable = selectable; }
	
	public boolean showHighlight() { return showHighlight; }
	public void setShowHighlight(boolean showHighlight) { this.showHighlight = showHighlight; }
	
	public abstract Vector2 getPreferredSize(Vector2 v);
	
	public Vector2 getMinimumSize(Vector2 v) { return getPreferredSize(v); }
	public Vector2 getMaximumSize(Vector2 v) { return getPreferredSize(v); }
	
	// note: if no parent, and fill axis, then dims become screen width and height.
	
	@Override public boolean keyDown(int keycode) { return false; } // should generally not use
	@Override public boolean keyUp(int keycode) { return false; } // should generally not use
	@Override public boolean keyTyped(char character) { return false; }
	@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
	@Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
	@Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
	@Override public boolean mouseMoved(int screenX, int screenY) { return false; }
	@Override public boolean scrolled(int amount) { return false; }
	
	// in addition to the above, a handleInput method will exist to make use of the game InputHandler, with repeated scrolling.
	
	/*public abstract void focus();
	
	public boolean hasFocus() { return parent == null || parent.hasFocus(this); }
	
	public void handleInput() {
		if(ClientCore.input.pressingKey(Keys.ESCAPE) && parent != null)
			parent.focus();
		// when overriding this method, hasFocus should be checked before continuing.
	}*/
}
