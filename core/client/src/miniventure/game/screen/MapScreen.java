package miniventure.game.screen;

import java.util.HashMap;

import miniventure.game.GameProtocol.LevelChange;
import miniventure.game.GameProtocol.MapRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.world.Point;
import miniventure.game.world.level.Level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MapScreen extends MenuScreen {
	
	private boolean requested = false;
	
	
	private HashMap<Point, Cell> mapCells;
	
	private Table table;
	
	public MapScreen() {
		super(false);
		
		table = useTable();
		table.add(makeLabel("waiting..."));
	}
	
	@Override
	public void focus() {
		super.focus();
		if(!requested) {
			requested = true;
			ClientCore.getClient().send(new MapRequest());
		}
	}
	
	@Override
	public void act(float delta) {
		if(Gdx.input.isKeyPressed(Keys.ESCAPE))
			ClientCore.setScreen(null);
		else
			super.act(delta);
	}
	
	public void mapUpdate(MapRequest mapRequest) {
		Gdx.app.postRunnable(() -> {
			table.clearChildren();
			for(int i = 0; i < mapRequest.islands.length; i++) {
				Point p = null;//mapRequest.islands[i];
				final int levelid = i;
				VisTextButton btn = makeButton("Island "+(i+1), () -> {
					Level playerLevel = ClientCore.getWorld().getMainPlayer().getLevel();
					if(!(playerLevel != null && playerLevel.getLevelId() == levelid))
						ClientCore.getClient().send(new LevelChange(levelid));
					// LoadingScreen loader = new LoadingScreen();
					// loader.pushMessage("Loading new level...");
					// ClientCore.setScreen(loader);
					ClientCore.setScreen(null);
				});
				table.add(btn).row();
			}
		});
	}
}
