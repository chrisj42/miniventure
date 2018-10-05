package miniventure.game.screen;

import com.badlogic.gdx.graphics.Color;

public class ColorRect extends ColorDrawable {
	
	private float leftWidth, rightWidth, topHeight, bottomHeight;
	private float minWidth, minHeight;
	
	public ColorRect(Color color) {
		super(color);
	}
	
	@Override
	public float getLeftWidth() {
		return leftWidth;
	}
	
	@Override
	public void setLeftWidth(float leftWidth) {
		this.leftWidth = leftWidth;
	}
	
	@Override
	public float getRightWidth() {
		return rightWidth;
	}
	
	@Override
	public void setRightWidth(float rightWidth) {
		this.rightWidth = rightWidth;
	}
	
	@Override
	public float getTopHeight() {
		return topHeight;
	}
	
	@Override
	public void setTopHeight(float topHeight) {
		this.topHeight = topHeight;
	}
	
	@Override
	public float getBottomHeight() {
		return bottomHeight;
	}
	
	@Override
	public void setBottomHeight(float bottomHeight) {
		this.bottomHeight = bottomHeight;
	}
	
	@Override
	public float getMinWidth() {
		return minWidth;
	}
	
	@Override
	public void setMinWidth(float minWidth) {
		this.minWidth = minWidth;
	}
	
	@Override
	public float getMinHeight() {
		return minHeight;
	}
	
	@Override
	public void setMinHeight(float minHeight) {
		this.minHeight = minHeight;
	}
	
}
