package miniventure.game.screen;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;
import miniventure.game.world.SaveLoadInterface;
import miniventure.game.world.SaveLoadInterface.WorldDataSet;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class WorldGenScreen extends BackgroundInheritor {
	
	/*
		world creation; currently only needs name, and seed.
	 */
	
	public WorldGenScreen() {
		Table table = useTable(true);
		table.defaults().pad(10, 5, 10, 5);
		
		table.add(makeLabel("World Name:", false));
		TextField nameField = makeField("");
		table.add(nameField);
		table.row();
		
		VisLabel seedLabel = makeLabel("World Seed\n(leave blank for random):", false);
		seedLabel.setAlignment(Align.center, Align.right);
		table.add(seedLabel);
		TextField seedField = makeField("");
		table.add(seedField);
		table.row();
		
		VisTextButton genButton = makeButton("Generate World", () -> {
			String name = nameField.getText();
			if(name.length() == 0)
				return;
			
			File path = SaveLoadInterface.getLocation(name);
			RandomAccessFile lockRef;
			try {
				lockRef = SaveLoadInterface.tryLockWorld(path);
			} catch(IOException e) {
				e.printStackTrace();
				ClientCore.setScreen(new ErrorScreen(e.getMessage()));
				return;
			}
			if(lockRef == null) {
				ClientCore.setScreen(new ErrorScreen("World exists and is currently being managed by another process. Only one instance of the game can load a world at a given time."));
				return;
			}
			
			LoadingScreen loader = new LoadingScreen();
			loader.pushMessage("Generating world...");
			ClientCore.setScreen(loader);
			
			String seed = seedField.getText();
			WorldDataSet worldInfo = SaveLoadInterface.createWorld(path, lockRef, seed);
			
			ClientCore.getWorld().startLocalWorld(worldInfo);
		});
		table.add(genButton);
		
		table.add(makeButton("Cancel", ClientCore::backToParentScreen));
	}
	
	
}
