package miniventure.game.screen;

import miniventure.game.GameCore;
import miniventure.game.util.Action;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public abstract class MenuScreen extends Stage {
	
	private final boolean clearGdxBackground;
	private MenuScreen parent;
	
	// private AnchorGroup anchorGroup;
	
	// protected VerticalGroup vGroup;
	// protected Table table;
	
	private Group mainGroup; // usually going to be the above vGroup or table.
	private RelPos mainContainerAnchor;
	private RelPos mainGroupAnchor;
	
	// private final LinkedList<VisLabel> labels = new LinkedList<>();
	
	public MenuScreen(final boolean clearGdxBackground) {
		super(new ScreenViewport(/*Scaling.fill, GameCore.DEFAULT_SCREEN_WIDTH, GameCore.DEFAULT_SCREEN_HEIGHT*/)/*new DiscreteViewport()*/, GameCore.getBatch());
		this.clearGdxBackground = clearGdxBackground;
		// vGroup = new VerticalGroup();
		// vGroup.space(10);
		// vGroup.setPosition(getWidth()/2, getHeight()*2/3, Align.center);
		//
		// anchorGroup = new AnchorGroup();
		// addActor(anchorGroup);
		// anchorGroup.setDebug(true);
		// setDebugAll(true);
	}
	
	/*protected VerticalGroup getVGroup() {
		if(vGroup == null)
			vGroup = new VerticalGroup();
		return vGroup;
	}
	protected Table getTable() {
		if(table == null)
			table = new Table();
		return table;
	}*/
	
	protected void setCenterGroup(Group group) { setMainGroup(group, RelPos.CENTER); }
	protected void setMainGroup(Group group, RelPos anchor) { setMainGroup(group, anchor, anchor); }
	protected void setMainGroup(Group group, RelPos containerAnchor, RelPos groupAnchor) {
		this.mainGroup = group;
		mainContainerAnchor = containerAnchor;
		mainGroupAnchor = groupAnchor;
		if(group != null && group.getStage() != this)
			addActor(group);
	}
	
	// called when the menu is focused, the first time and any subsequent times.
	public void focus() { layoutActors(); }
	
	protected void layoutActors() {
		if(mainGroup != null) {
			if(mainGroup instanceof Layout) {
				// ((Layout) mainGroup).invalidate();
				// ((Layout) mainGroup).layout();
				// ((Layout) mainGroup).validate();
			}
			Vector2 containerPos = mainContainerAnchor.ofRectangle(new Rectangle(0, 0, getWidth(), getHeight()));
			mainGroup.setPosition(containerPos.x, containerPos.y, mainGroupAnchor.getGdxAlign());
		}
	}
	
	// protected AnchorGroup getAnchor() { return anchorGroup; }
	
	public void setParent(MenuScreen parent) {
		if(parent != null && !(parent instanceof ParentScreen))
			setParent(parent.getParent());
		else
			this.parent = parent;
	}
	public MenuScreen getParent() { return parent; }
	
	public boolean usesWholeScreen() { return clearGdxBackground; }
	
	@Override
	public void draw() {
		if(clearGdxBackground)
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.draw();
	}
	
	@Override
	public void dispose() { dispose(true); }
	public void dispose(boolean disposeParent) {
		if(disposeParent && parent != null) parent.dispose();
		super.dispose();
	}
	
	protected static VisTextButton makeButton(String text, Action onClick) {
		VisTextButton button = new VisTextButton(text);
		button.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				onClick.act();
			}
		});
		return button;
	}
	
	/*protected void addCentered(Actor comp) { addCentered(comp, 0); }
	protected void addCentered(Actor comp, int space) {
		Container<Actor> box = addComponent(comp, space);
		box.center();
	}*/
	
	protected static VisLabel makeLabel(String text) {
		VisLabel label = new VisLabel(text, new LabelStyle(GameCore.getFont(), Color.WHITE));
		label.setWrap(true);
		label.setAlignment(Align.center, Align.left);
		return label;
	}
	
	protected VerticalGroup useVGroup(float space) { return useVGroup(space, true); }
	protected VerticalGroup useVGroup(float space, int alignment) { return useVGroup(space, alignment, true); }
	protected VerticalGroup useVGroup(float space, boolean addMainCenter) { return useVGroup(space, Align.center, addMainCenter); }
	protected VerticalGroup useVGroup(float space, int alignment, boolean addMainCenter) {
		VerticalGroup vGroup = new VerticalGroup();
		if(addMainCenter)
			setCenterGroup(vGroup);
		vGroup.space(space);
		vGroup.align(alignment);
		vGroup.columnAlign(alignment);
		return vGroup;
	}
	
	protected Table useTable() { return useTable(true); }
	protected Table useTable(int alignment) { return useTable(alignment, true); }
	protected Table useTable(boolean addMainCenter) { return useTable(Align.center, addMainCenter); }
	protected Table useTable(int alignment, boolean addMainCenter) {
		Table table = new Table();
		if(addMainCenter)
			setCenterGroup(table);
		table.align(alignment);
		return table;
	}
	
	/*protected <T extends Actor> Container<T> addComponent(T comp) { return addComponent(0, comp, 0); }
	protected <T extends Actor> Container<T> addComponent(int spacing, T comp) { return addComponent(spacing, comp, 0); }
	protected <T extends Actor> Container<T> addComponent(T comp, int spacing) { return addComponent(0, comp, spacing); }
	protected <T extends Actor> Container<T> addComponent(int preSpacing, T comp, int postSpacing) {
		if(vGroup.getParent() == null)
			anchorGroup.addAnchored(vGroup, RelPos.CENTER);
		Container<T> box = new Container<>(comp);
		box.padTop(preSpacing);
		box.padBottom(postSpacing);
		vGroup.addActor(box);
		return box;
	}*/
	
	/*protected static <T extends Actor> Container<T> boxComponent(T comp) {
		Container<T> box = new Box<>(comp);
		if(comp instanceof Layout)
			((Layout)comp).setFillParent(true);
		return box;
	}*/
	
	public void resize(int width, int height) {
		getViewport().update(width, height, true);
		// System.out.println("menu screen resized to "+width+","+height+"; current menu stage size: "+getWidth()+","+getHeight()+"; new viewport: "+getViewport().getWorldWidth()+","+getViewport().getWorldHeight()+" world, "+getViewport().getScreenWidth()+","+getViewport().getScreenHeight()+" screen.");
		// anchorGroup.invalidate();
		layoutActors();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"@"+Integer.toHexString(hashCode());
	}
}
