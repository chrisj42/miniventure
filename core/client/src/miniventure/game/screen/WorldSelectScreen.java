package miniventure.game.screen;

import java.io.IOException;
import java.io.RandomAccessFile;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;
import miniventure.game.world.management.SaveLoadInterface;
import miniventure.game.world.management.SaveLoadInterface.WorldDataSet;
import miniventure.game.world.management.SaveLoadInterface.WorldReference;

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
	
	public WorldSelectScreen() {
		
		Table table = useTable();
		
		// add delete function later
		
		worldList = new List<>(VisUI.getSkin());
		worldList.setItems(SaveLoadInterface.getLocalWorlds());
		
		table.defaults().padBottom(10);
		
		ScrollPane scroll = new ScrollPane(worldList);
		table.add(scroll).grow().row();
		
		VisLabel error = makeLabel("");
		table.add(error).row();
		
		VisLabel worldInfo = makeLabel("Select a World");
		table.add(worldInfo).row();
		
		VisTextButton load = makeButton("Load World", () -> {
			WorldReference ref = worldList.getSelected();
			if(ref == null) return;
			
			try {
				RandomAccessFile lockRef = SaveLoadInterface.tryLockWorld(ref.folder);
				LoadingScreen loader = new LoadingScreen();
				loader.pushMessage("Loading world '"+ref.worldName+"'...");
				ClientCore.setScreen(loader);
				WorldDataSet world = SaveLoadInterface.loadWorld(ref.folder, lockRef);
				if(world == null) {
					error.setText("Failed to load world. Possible format error in game files.");
					error.invalidateHierarchy();
					ClientCore.backToParentScreen();
				} else
					ClientCore.getWorld().startLocalWorld(world);
			} catch(IOException e) {
				error.setText("Failed to load world. Ensure no other programs are using the files.");
				error.invalidateHierarchy();
				if(ClientCore.getScreen() instanceof LoadingScreen)
					ClientCore.backToParentScreen();
			}
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
