package miniventure.game.screen;

import java.io.IOException;
import java.io.RandomAccessFile;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.file.WorldFileInterface;
import miniventure.game.world.file.WorldFormatException;
import miniventure.game.world.file.WorldReference;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class WorldSelectScreen extends BackgroundInheritor {
	
	private List<WorldReference> worldList;
	
	// TODO use the info strings in WorldReference to highlight the world a certain color,
	//  and display info about it.
	// TODO add a checkbox to have invalid worlds displayed (missing files will be shown).
	public WorldSelectScreen() {
		
		Table table = useTable();
		
		// add delete function later
		
		worldList = new List<WorldReference>(VisUI.getSkin()) {
			@Override
			protected GlyphLayout drawItem(Batch batch, BitmapFont font, int index, WorldReference item, float x, float y, float width) {
				if(!item.equivalent) {
					Color color = font.getColor();
					font.setColor(item.compatible ? Color.YELLOW : Color.RED);
					GlyphLayout layout = super.drawItem(batch, font, index, item, x, y, width);
					font.setColor(color);
					return layout;
				}
				
				return super.drawItem(batch, font, index, item, x, y, width);
			}
		};
		worldList.setItems(WorldReference.getLocalWorlds(false).toArray(new WorldReference[0]));
		
		table.defaults().padBottom(10);
		
		ScrollPane scroll = new ScrollPane(worldList);
		table.add(scroll).grow().maxHeight(getViewport().getWorldHeight()/2).row();
		
		VisLabel worldInfo = makeLabel("Select a World");
		table.add(worldInfo).row();
		
		VisLabel error = makeLabel("");
		table.add(error).row();
		
		VisTextButton load = makeButton("Load World", () -> {
			WorldReference ref = worldList.getSelected();
			if(ref == null) return;
			
			try {
				RandomAccessFile lockRef = WorldFileInterface.tryLockWorld(ref.folder);
				if(lockRef == null) {
					error.setText("Failed to load world. Ensure no other programs are using the files.");
				}
				else {
					LoadingScreen loader = new LoadingScreen();
					loader.pushMessage("Loading world '"+ref.worldName+'\'', true);
					ClientCore.setScreen(loader);
					new Thread(() -> {
						try {
							ClientCore.getWorld().startLocalWorld(WorldFileInterface.loadWorld(ref, lockRef), loader);
						} catch(WorldFormatException e) {
							Gdx.app.postRunnable(() -> {
								error.setText(MyUtils.combineThrowableCauses(e, "Failed to load world"));
								ClientCore.setScreen(this);
							});
						}
					}).start();
					return;
				}
			} catch(IOException e) {
				error.setText(e.getMessage());
			}
			// code reaches here if an error occurred
			error.invalidateHierarchy();
			ClientCore.setScreen(this);
		});
		table.add(load).row();
		
		load.setDisabled(true);
		
		worldList.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				error.setText("");
				WorldReference ref = worldList.getSelected();
				if(ref == null) {
					worldInfo.setText("Select a World");
					load.setDisabled(true);
				}
				else {
					worldInfo.setText("Version: "+ref.version+(Version.CURRENT.equals(ref.version)?" (current)":""));
					load.setDisabled(!ref.compatible);
					if(!ref.compatible)
						error.setText("The world version is not compatible with the current version.\n"+
							// if it was supported on later versions, display what the latest version was.
							(ref.lastCompatible.equals(ref.version)
								?""
								:"It was last supported in version "+ref.lastCompatible+".\n"
							)+
							// display whether updating to the current version is possible
							(ref.updatable
								?"To restore compatibility, load and save the world in that version, then try again." 
								:"It is not possible to update it to the current version."
							)
						);
					else if(!ref.equivalent) // notify the player that things will be changing
						error.setText("The save format has changed since this world was last saved.\nThe data will be migrated.");
				}
			}
		});
		
		ChangeEvent ev = new ChangeEvent();
		ev.setTarget(worldList);
		worldList.fire(ev);
		
		VisTextButton back = makeButton("Back to Main Menu", ClientCore::backToParentScreen);
		table.add(back);
		
		mapButtons(table, load, back);
		setKeyboardFocus(table);
		setScrollFocus(scroll);
	}
	
	@Override
	public boolean allowChildren() {
		return true;
	}
}
