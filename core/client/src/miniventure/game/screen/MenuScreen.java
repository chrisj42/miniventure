package miniventure.game.screen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import miniventure.game.client.ClientCore;
import miniventure.game.client.Style;
import miniventure.game.screen.util.DiscreteViewport;
import miniventure.game.screen.util.ParentScreen;
import miniventure.game.util.RelPos;
import miniventure.game.util.function.Action;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MenuScreen extends Stage {
	
	private final boolean clearGdxBackground;
	private MenuScreen parent;
	
	private final LinkedList<ActorAnchor> anchoredActors = new LinkedList<>();
	
	private HashMap<Label, Style> labels = new HashMap<>();
	
	public MenuScreen(final boolean clearGdxBackground) { this(clearGdxBackground, new DiscreteViewport(), ClientCore.getBatch()); }
	public MenuScreen(final boolean clearGdxBackground, Viewport viewport, Batch batch) {
		super(viewport, batch);
		this.clearGdxBackground = clearGdxBackground;
	}
	
	protected void setCenterGroup(Group group) { addMainGroup(group, RelPos.CENTER); }
	protected void addMainGroup(Group group, RelPos anchor) { addMainGroup(group, anchor, anchor); }
	protected void addMainGroup(Group group, RelPos containerAnchor, RelPos groupAnchor) { addMainGroup(group, containerAnchor, groupAnchor, 0, 0); }
	protected void addMainGroup(Group group, RelPos anchor, int offX, int offY) { addMainGroup(group, anchor, anchor, offX, offY); }
	protected void addMainGroup(Group group, RelPos containerAnchor, RelPos groupAnchor, int offX, int offY) {
		boolean match = false;
		for(ActorAnchor anchor: anchoredActors) {
			if(anchor.mainGroup == group) {
				match = true;
				anchor.mainContainerAnchor = containerAnchor;
				anchor.mainGroupAnchor = groupAnchor;
				anchor.offX = offX;
				anchor.offY = offY;
				break;
			}
		}
		if(!match)
			anchoredActors.add(new ActorAnchor(group, containerAnchor, groupAnchor, offX, offY));
		
		if(group.getStage() != this)
			addActor(group);
	}
	
	// called when the menu is focused, the first time and any subsequent times.
	public void focus() { layoutActors(); }
	
	protected void layoutActors() {
		for(ActorAnchor anchor: anchoredActors)
			anchor.layout();
	}
	
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
	
	protected VisTextButton makeButton(String text, Action onClick) {
		VisTextButton button = new VisTextButton(text);
		button.pad(6f);
		labels.put(button.getLabel(), Style.Default);
		button.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				onClick.act();
			}
		});
		return button;
	}
	
	protected VisLabel makeLabel(String text) { return makeLabel(text, true); }
	protected VisLabel makeLabel(String text, boolean wrapText) {
		return makeLabel(text, Style.Default, wrapText);
	}
	protected VisLabel makeLabel(String text, Style labelStyle, boolean wrap) {
		VisLabel label = new VisLabel(text, new LabelStyle(ClientCore.getFont(labelStyle), null));
		label.setWrap(wrap);
		label.setAlignment(Align.center, Align.left);
		labels.put(label, labelStyle);
		return label;
	}
	
	protected void registerLabel(Style style, Label label) { labels.put(label, style); }
	protected void deregisterLabel(Label label) { labels.remove(label); }
	
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
	
	public void resize(int width, int height) {
		getViewport().update(width, height, true);
		// System.out.println("menu screen resized to "+width+","+height+"; current menu stage size: "+getWidth()+","+getHeight()+"; new viewport: "+getViewport().getWorldWidth()+","+getViewport().getWorldHeight()+" world, "+getViewport().getScreenWidth()+","+getViewport().getScreenHeight()+" screen.");
		
		for(Map.Entry<Label, Style> labelStyle: labels.entrySet()) {
			LabelStyle style = labelStyle.getKey().getStyle();
			// float prev = style.font.getLineHeight();
			style.font = ClientCore.getFont(labelStyle.getValue());
			// System.out.println("font height changed from "+prev+" to "+style.font.getLineHeight());
			labelStyle.getKey().setStyle(style);
			labelStyle.getKey().invalidateHierarchy();
		}
		
		for(ActorAnchor actor: anchoredActors)
			if(actor.mainGroup instanceof Layout)
				((Layout)actor.mainGroup).pack();
				
		
		layoutActors();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"@"+Integer.toHexString(hashCode());
	}
	
	
	private class ActorAnchor {
		private final Group mainGroup; // usually going to be the above vGroup or table.
		private RelPos mainContainerAnchor;
		private RelPos mainGroupAnchor;
		private int offX;
		private int offY;
		
		ActorAnchor(Group mainGroup, RelPos mainContainerAnchor, RelPos mainGroupAnchor) { this(mainGroup, mainContainerAnchor, mainGroupAnchor, 0, 0); }
		ActorAnchor(Group mainGroup, RelPos mainContainerAnchor, RelPos mainGroupAnchor, int offX, int offY) {
			this.mainGroup = mainGroup;
			this.mainContainerAnchor = mainContainerAnchor;
			this.mainGroupAnchor = mainGroupAnchor;
			this.offX = offX;
			this.offY = offY;
		}
		
		void layout() {
			Vector2 containerPos = mainContainerAnchor.ofRectangle(new Rectangle(offX, offY, getWidth(), getHeight()));
			mainGroup.setPosition(containerPos.x, containerPos.y, mainGroupAnchor.getGdxAlign());
		}
	}
}
