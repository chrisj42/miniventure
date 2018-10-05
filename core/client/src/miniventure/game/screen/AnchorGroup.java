package miniventure.game.screen;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;

import miniventure.game.util.Action;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.SnapshotArray;

public class AnchorGroup extends WidgetGroup {
	
	private final HashMap<Actor, Action> layoutActions = new HashMap<>();
	
	public AnchorGroup() {
		setFillParent(true);
	}
	
	@Override
	protected void childrenChanged() {
		super.childrenChanged();
		
		SnapshotArray<Actor> children = getChildren();
		for(Actor actor: layoutActions.keySet().toArray(new Actor[0]))
			if(!children.contains(actor, true))
				layoutActions.remove(actor);
	}
	
	@Override
	public void layout() {
		super.layout();
		for(Action a: layoutActions.values())
			a.act();
	}
	
	public void addAnchored(Actor comp, RelPos actorAnchor, RelPos containerAnchor, int anchorOffsetX, int anchorOffsetY) {
		layoutActions.put(comp, () -> {
			float prefWidth = comp instanceof Layout ? ((Layout)comp).getPrefWidth() : comp.getWidth();
			float prefHeight = comp instanceof Layout ? ((Layout)comp).getPrefHeight() : comp.getHeight();
			float parentWidth = getWidth();
			float parentHeight = getHeight();
			
			comp.setSize(Math.min(prefWidth, parentWidth), Math.min(prefHeight, parentHeight));
			
			Point compAnchor = actorAnchor.forRectangle(
				new Rectangle((int)comp.getX(), (int)comp.getY(), (int)comp.getWidth(), (int)comp.getHeight())
			);
			Point parentAnchor = containerAnchor.forRectangle(
				new Rectangle((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight())
			);
			
			float locAnchorXDifference = comp.getX() - compAnchor.x;
			float locAnchorYDifference = comp.getY() - compAnchor.y;
			
			parentAnchor.translate(anchorOffsetX, anchorOffsetY);
			comp.setPosition(parentAnchor.x + locAnchorXDifference, parentAnchor.y + locAnchorYDifference);
		});
		addActor(comp);
	}
	public void addAnchored(Actor comp, RelPos actorAnchor, RelPos containerAnchor) {
		addAnchored(comp, actorAnchor, containerAnchor, 0, 0);
	}
	public void addAnchored(Actor comp, RelPos anchor, int anchorOffsetX, int anchorOffsetY) {
		addAnchored(comp, anchor, anchor, anchorOffsetX, anchorOffsetY);
	}
	public void addAnchored(Actor comp, RelPos anchor) {
		addAnchored(comp, anchor, anchor);
	}
	
	@Override
	public float getMinWidth() { return getStage().getWidth(); }
	
	@Override
	public float getMinHeight() { return getStage().getHeight(); }
	
	@Override
	public float getPrefWidth() { return getStage().getWidth(); }
	
	@Override
	public float getPrefHeight() { return getStage().getHeight(); }
	
	@Override
	public float getMaxWidth() { return getStage().getWidth(); }
	
	@Override
	public float getMaxHeight() { return getStage().getHeight(); }
}
