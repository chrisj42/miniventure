package miniventure.game.screen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import miniventure.game.client.ClientCore;
import miniventure.game.client.FontStyle;
import miniventure.game.screen.util.DiscreteViewport;
import miniventure.game.util.RelPos;
import miniventure.game.util.function.Action;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuScreen extends Stage {
	
	private final boolean clearGdxBackground;
	private MenuScreen parent;
	
	private final LinkedList<ActorAnchor> anchoredActors = new LinkedList<>();
	
	private HashMap<Label, FontStyle> labels = new HashMap<>();
	private HashSet<TextField> textFields = new HashSet<>();
	
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
		if(parent != null && !parent.allowChildren())
			setParent(parent.getParent());
		else
			this.parent = parent;
	}
	public MenuScreen getParent() { return parent; }
	
	public boolean allowChildren() { return false; }
	
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
		labels.put(button.getLabel(), FontStyle.Default);
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
		return makeLabel(text, FontStyle.Default, wrapText);
	}
	protected VisLabel makeLabel(String text, FontStyle style, boolean wrap) {
		VisLabel label = new VisLabel(text, new LabelStyle(ClientCore.getFont(style), null));
		label.setWrap(wrap);
		label.setAlignment(Align.center, Align.left);
		labels.put(label, style);
		return label;
	}
	
	protected void registerLabel(FontStyle style, Label label) {
		labels.put(label, style);
		Gdx.app.postRunnable(() -> {
			// refresh the style font
			label.getStyle().font = ClientCore.getFont(style);
			label.setStyle(label.getStyle());
		});
	}
	protected void deregisterLabels(Label... labels) {
		for(Label label: labels)
			this.labels.remove(label);
	}
	
	protected TextField makeField(String text) {
		TextField field = new TextField(text, VisUI.getSkin());
		registerField(field);
		return field;
	}
	
	protected void registerField(TextField field) { textFields.add(field); }
	protected void deregisterField(TextField field) { textFields.remove(field); }
	
	protected void mapFieldButtons(@NotNull TextField field, @Nullable VisTextButton confirmBtn, @Nullable VisTextButton cancelBtn) { mapButtons(field, confirmBtn, cancelBtn); }
	protected void mapButtons(@NotNull Actor focus, @Nullable VisTextButton confirmBtn, @Nullable VisTextButton cancelBtn) {
		focus.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Keys.ENTER && confirmBtn != null) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchDown);
					confirmBtn.fire(event1);
					setKeyboardFocus(focus);
					return true;
				}
				if(keycode == Keys.ESCAPE && cancelBtn != null) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchDown);
					cancelBtn.fire(event1);
					setKeyboardFocus(focus);
					return true;
				}
				return false;
			}
			
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if(keycode == Keys.ENTER && confirmBtn != null) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchUp);
					confirmBtn.fire(event1);
					return true;
				}
				if(keycode == Keys.ESCAPE && cancelBtn != null) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchUp);
					cancelBtn.fire(event1);
					return true;
				}
				return false;
			}
		});
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
		Table table = new Table() {
			// I'm not sure if this actually does anything...
			@Override
			public float getMaxWidth() {
				return MenuScreen.this.getWidth();
			}
			
			@Override
			public float getMaxHeight() {
				return MenuScreen.this.getHeight();
			}
		};
		if(addMainCenter)
			setCenterGroup(table);
		table.align(alignment);
		return table;
	}
	
	public void resize(int width, int height) {
		getViewport().update(width, height, true);
		// System.out.println("menu screen resized to "+width+","+height+"; current menu stage size: "+getWidth()+","+getHeight()+"; new viewport: "+getViewport().getWorldWidth()+","+getViewport().getWorldHeight()+" world, "+getViewport().getScreenWidth()+","+getViewport().getScreenHeight()+" screen.");
		
		for(Map.Entry<Label, FontStyle> labelStyle: labels.entrySet()) {
			LabelStyle style = labelStyle.getKey().getStyle();
			style.font = ClientCore.getFont(labelStyle.getValue());
			labelStyle.getKey().setStyle(style);
		}
		
		for(TextField field: textFields) {
			field.getStyle().font = ClientCore.getFont(FontStyle.TextField);
			field.setStyle(field.getStyle());
			// this is a hack to make the text field update its cursor position, which otherwise would be displayed wrong; the data is right, but the cached screen position is wrong.
			field.setPasswordMode(false);
		}
		
		layoutActors();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+'@'+Integer.toHexString(hashCode());
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
			if(mainGroup instanceof Layout)
				((Layout) mainGroup).pack();
			
			Vector2 containerPos = mainContainerAnchor.ofRectangle(new Rectangle(offX, offY, getWidth(), getHeight()));
			mainGroup.setPosition(containerPos.x, containerPos.y, mainGroupAnchor.getGdxAlign());
		}
	}
}
