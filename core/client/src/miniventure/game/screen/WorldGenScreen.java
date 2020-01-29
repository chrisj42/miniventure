package miniventure.game.screen;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

import miniventure.game.core.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;
import miniventure.game.world.file.WorldDataSet;
import miniventure.game.world.file.WorldFileInterface;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class WorldGenScreen extends BackgroundInheritor {
	
	/*
		world creation; currently only needs name, and seed.
	 */
	
	public WorldGenScreen() {
		super(new ScreenViewport());
		
		Table table = useTable(true);
		table.defaults().pad(10, 5, 10, 5);
		
		table.add(makeLabel("World Name:", false));
		TextField nameField = makeField("");
		table.add(nameField);
		table.row();
		
		VisLabel seedLabel = makeLabel("World Seed:", false);
		seedLabel.setAlignment(Align.center, Align.right);
		table.add(seedLabel);
		TextField seedField = makeField("");
		seedField.setMessageText("(blank for random)");
		table.add(seedField);
		table.row();
		
		VisTextButton genButton = makeButton("Generate World", () -> {
			String name = nameField.getText();
			if(name.length() == 0)
				return;
			
			Path path = WorldFileInterface.getLocation(name);
			RandomAccessFile lockRef;
			try {
				lockRef = WorldFileInterface.tryLockWorld(path);
			} catch(IOException e) {
				// e.printStackTrace();
				ClientCore.setScreen(new ErrorScreen(e.getMessage()));
				return;
			}
			if(lockRef == null) {
				ClientCore.setScreen(new ErrorScreen("World exists and is currently being managed by another process. Please ensure no other programs (or miniventure processes) are modifying the world files, then try again."));
				return;
			}
			
			String seed = seedField.getText();
			
			LoadingScreen loader = new LoadingScreen();
			loader.pushMessage("creating world files", true);
			ClientCore.setScreen(loader);
			
			new Thread(() -> {
				WorldDataSet worldInfo = WorldFileInterface.createWorld(path, lockRef, seed);
				ClientCore.getWorld().startLocalWorld(worldInfo, loader);
			}).start();
		});
		table.add(genButton);
		
		VisTextButton cancelBtn = makeButton("Cancel", ClientCore::backToParentScreen);
		table.add(cancelBtn);
		
		mapFieldButtons(nameField, genButton, cancelBtn);
		mapFieldButtons(seedField, genButton, cancelBtn);
		setKeyboardFocus(nameField);
	}
	
	
}
