package miniventure.game.screen;

import java.io.IOException;
import java.io.RandomAccessFile;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;
import miniventure.game.util.MyUtils;
import miniventure.game.world.file.WorldFileInterface;
import miniventure.game.world.file.WorldFormatException;
import miniventure.game.world.file.WorldReference;

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
		
		worldList = new List<>(VisUI.getSkin());
		worldList.setItems(WorldReference.getLocalWorlds(false).toArray(new WorldReference[0]));
		
		table.defaults().padBottom(10);
		
		ScrollPane scroll = new ScrollPane(worldList);
		table.add(scroll).grow().maxHeight(getViewport().getWorldHeight()/2).row();
		
		VisLabel error = makeLabel("");
		table.add(error).row();
		
		VisLabel worldInfo = makeLabel("Select a World");
		table.add(worldInfo).row();
		
		VisTextButton load = makeButton("Load World", () -> {
			WorldReference ref = worldList.getSelected();
			if(ref == null) return;
			
			try {
				RandomAccessFile lockRef = WorldFileInterface.tryLockWorld(ref.folder);
				if(lockRef == null) {
					error.setText("Failed to load world. Ensure no other programs are using the files.");
				}
				else {
					// LoadingScreen loader = new LoadingScreen();
					// loader.pushMessage("Loading world '"+ref.worldName+"'...");
					// ClientCore.setScreen(loader);
					try {
						ClientCore.getWorld().startLocalWorld(WorldFileInterface.loadWorld(ref, lockRef));
						return;
					} catch(WorldFormatException e) {
						error.setText(MyUtils.combineThrowableCauses(e, "Failed to load world"));
					}
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
					load.setDisabled(false);
					worldInfo.setText("Version: "+ref.version+(ref.version.compareTo(GameCore.VERSION) == 0?" (current)":""));
				}
			}
		});
		
		ChangeEvent ev = new ChangeEvent();
		ev.setTarget(worldList);
		worldList.fire(ev);
		
		VisTextButton back = makeButton("Back to Main Menu", ClientCore::backToParentScreen);
		table.add(back);
		
		setScrollFocus(scroll);
	}
	
	@Override
	public boolean allowChildren() {
		return true;
	}
}
