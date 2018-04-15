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
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

abstract class RenderableListItem extends Button {
	
	private final Item item;
	private final int idx;
	private final VerticalGroup table;
	
	private final float width, height;
	
	RenderableListItem(Item item, int idx, VerticalGroup table) {
		super(GameCore.getSkin());
		this.item = item;
		this.idx = idx;
		this.table = table;
		
		width = item.getRenderWidth()+10;
		height = item.getRenderHeight()+10;
		
		addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.DOWN)
					moveFocus(1);
				else if(keycode == Keys.UP)
					moveFocus(-1);
				else if(keycode == Keys.ENTER)
					select(idx);
				else 
					RenderableListItem.this.keyDown(event, keycode);
				
				return true;
			}
			
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
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
	
	private void moveFocus(int amt) {
		Actor focused = getStage().getKeyboardFocus();
		int row = table.getChildren().indexOf(focused, true);
		row += amt;
		while(row < 0) row += table.getChildren().size;
		getStage().setKeyboardFocus(table.getChildren().get(row % table.getChildren().size));
	}
	
	void keyDown(InputEvent event, int keycode) {}
	
	abstract void select(int idx);
	
	protected abstract int getStackSize(int idx);
	
	@Override
	public float getPrefWidth() { return width; }
	public float getPrefHeight() { return height; }
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		if(getStage().getKeyboardFocus() == this)
			MyUtils.fillRect(getX(), getY(), getPrefWidth(), getPrefHeight(), .8f, .8f, .8f, 0.5f*parentAlpha, batch);
		
		item.drawItem(getStackSize(idx), batch, getX()+5, getY()+5, getItemTextColor());
	}
	
	protected Color getItemTextColor() { return Color.WHITE; }
}
