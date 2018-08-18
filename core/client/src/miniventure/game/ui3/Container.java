package miniventure.game.ui3;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class Container extends Component {
	
	private ArrayList<Component> children = new ArrayList<>();
	// private boolean valid = false;
	
	@NotNull private Layout layout;
	
	protected Container() { this(null); }
	protected Container(Layout layout) {
		this.layout = layout == null ? new DefaultLayout() : layout;
	}
	
	/*@Override
	public void invalidate() {
		super.invalidate();
		valid = false;
	}*/
	
	public void addChild(Component c) {
		children.remove(c);
		children.add(c);
		
		if(c.getParent() != this)
			c.setParent(this);
		
		invalidate();
	}
	
	public void removeChild(Component c) {
		if(children.remove(c)) {
			c.setParent(null);
			invalidate();
		}
	}
	
	public Component[] getChildArray() { return children.toArray(new Component[0]); }
	public ArrayList<Component> getChildList() { return new ArrayList<>(children); }
	
	@Override
	public void render(Batch batch) {
		super.render(batch);
		
		for(Component c: getChildArray())
			c.render(batch);
	}
	
	@Override
	protected void configureForSize(Vector2 size) { layout.applyLayout(size, getChildArray()); }
	
	@Override
	protected Vector2 calcPrefSize(Vector2 rt) { return layout.calcPrefSize(getChildArray(), rt); }
	
	public void setLayout(Layout layout) {
		this.layout = layout == null ? new DefaultLayout() : layout;
		invalidate();
	}
}
