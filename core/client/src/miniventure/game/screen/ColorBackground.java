package miniventure.game.screen;

import miniventure.game.util.MyUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ColorBackground implements Drawable {
	
	private Color color;
	private Actor actor;
	
	public ColorBackground(Actor actor, Color color) {
		this.color = color;
		this.actor = actor;
	}
	
	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		MyUtils.fillRect(x, y, width, height, color.cpy().mul(batch.getColor()), batch);
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public Actor getActor() {
		return actor;
	}
	
	public void setActor(Actor actor) {
		this.actor = actor;
	}
	
	@Override
	public float getLeftWidth() {
		return actor.getWidth()/2;
	}
	
	@Override
	public void setLeftWidth(float leftWidth) {
		
	}
	
	@Override
	public float getRightWidth() {
		return actor.getWidth()/2;
	}
	
	@Override
	public void setRightWidth(float rightWidth) {
		
	}
	
	@Override
	public float getTopHeight() {
		return actor.getHeight()/2;
	}
	
	@Override
	public void setTopHeight(float topHeight) {
		
	}
	
	@Override
	public float getBottomHeight() {
		return actor.getHeight()/2;
	}
	
	@Override
	public void setBottomHeight(float bottomHeight) {
		
	}
	
	@Override
	public float getMinWidth() {
		return actor.getWidth();
	}
	
	@Override
	public void setMinWidth(float minWidth) {
		
	}
	
	@Override
	public float getMinHeight() {
		return actor.getHeight();
	}
	
	@Override
	public void setMinHeight(float minHeight) {
		
	}
}
