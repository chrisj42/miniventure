package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

abstract class RenderableListItem extends Button {
	
	public static final float MAX_HEIGHT = Item.ICON_SIZE;
	
	private final Item item;
	private final int idx;
	private ItemSelectionTable table;
	
	private final float width, height;
	
	RenderableListItem(Item item, int idx) {
		super(GameCore.getSkin());
		this.item = item;
		this.idx = idx;
		
		width = item.getRenderWidth()+10;
		height = MAX_HEIGHT;//item.getRenderHeight()+10;
		
		addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(table == null)
					return false;
				
				switch(keycode) {
					case Keys.RIGHT: table.moveFocusX(1); return true;
					case Keys.LEFT: table.moveFocusX(-1); return true;
					case Keys.UP: table.moveFocusY(-1); return true;
					case Keys.DOWN: table.moveFocusY(1); return true;
					
					case Keys.ENTER: select(idx); return true;
					
					default: RenderableListItem.this.keyDown(event, keycode); return true;
				}
			}
			
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				if(table != null)
					table.moveFocus(idx);
				else
					getStage().setKeyboardFocus(RenderableListItem.this);
			}
		});
		
		addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				select(idx);
			}
		});
	}
	
	void setTable(ItemSelectionTable table) { this.table = table; }
	
	void keyDown(InputEvent event, int keycode) {}
	
	abstract void select(int idx);
	
	protected abstract int getStackSize(int idx);
	protected boolean showName() { return true; }
	
	@Override public float getPrefWidth() { return width; }
	@Override public float getPrefHeight() { return height; }
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		if(getStage().getKeyboardFocus() == this)
			MyUtils.fillRect(getX(), getY(), getPrefWidth(), getPrefHeight(), .8f, .8f, .8f, 0.5f*parentAlpha, batch);
		
		item.drawItem(getStackSize(idx), batch, getX()+5, getY()+5, getItemTextColor(), showName());
	}
	
	protected Color getItemTextColor() { return Color.WHITE; }
	
	@Override
	public String toString() { return "Renderable "+item; }
}
