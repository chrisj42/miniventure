package miniventure.game.ui;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

public final class Container extends Component {
	
	private ArrayList<Component> children;
	
	private float x, y;
	
	public Container() { this(0, 0); }
	public Container(float x, float y) {
		children = new ArrayList<>();
		this.x = x;
		this.y = y;
	}
	
	public void addComponent(Component c) {
		children.remove(c);
		children.add(c);
	}
	
	public void removeComponent(Component c) {
		children.remove(c);
	}
	
	@Override
	protected void render(Batch batch) {
		super.render(batch);
		for(Component child: children)
			child.render(batch);
	}
	
	@Override
	protected Vector2 getPosition() {
		return new Vector2(x, y);
	}
	
	@Override
	protected Vector2 getSize() {
		if(children.size() == 0) {
			return new Vector2();
		}
		
		Component first = children.get(0);
		
		Vector2 minPos = first.getPosition();
		Vector2 maxPos = minPos.cpy().add(first.getSize());
		
		for(int i = 1; i < children.size(); i++) {
			Component c = children.get(i);
			Vector2 pos = c.getPosition();
			Vector2 size = c.getSize();
			minPos.x = Math.min(minPos.x, pos.x);
			minPos.y = Math.min(minPos.y, pos.y);
			maxPos.x = Math.max(maxPos.x, pos.x+size.x);
			maxPos.y = Math.max(maxPos.y, pos.y+size.y);
		}
		
		return new Vector2(maxPos.x-minPos.x, maxPos.y-minPos.y);
	}
}
