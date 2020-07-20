package miniventure.game.screen;

import java.util.HashMap;

import miniventure.game.core.ClientCore;
import miniventure.game.core.InputHandler.Control;
import miniventure.game.network.GameProtocol.LevelChange;
import miniventure.game.network.GameProtocol.MapRequest;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Point;
import miniventure.game.world.file.IslandDataManager;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.LevelId;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MapScreen extends MenuScreen {
	
	private boolean requested;
	
	private HashMap<Point, Cell> mapCells;
	
	private Table table;
	
	{
		table = useTable();
	}
	
	public MapScreen() {
		super();
		
		table.add(makeLabel("waiting..."));
		requested = false;
	}
	
	public MapScreen(MapRequest data) {
		super();
		
		requested = true;
		mapUpdate(data);
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
		if(ClientCore.input.pressingControl(Control.CANCEL))
			ClientCore.removeScreen();
		else
			super.act(delta);
	}
	
	public void mapUpdate(MapRequest mapRequest) {
		Gdx.app.postRunnable(() -> {
			table.clearChildren();
			// table.add(makeLabel("More cooler island travel coming soon!")).row();
			Level playerLevel = ClientCore.getWorld().getLevel();
			LevelId curLevel = null;
			if(playerLevel != null)
				curLevel = playerLevel.getLevelId();
			for(int i = 0; i < mapRequest.islands.length; i++) {
				final LevelId levelId = mapRequest.islands[i].surfaceLevelId;
				if(levelId == curLevel) continue;
				VisTextButton btn = makeButton("Island "+(i+1)+": "+MyUtils.toTitleFormat(mapRequest.islands[i].type.name()), () -> {
					// Level playerLevel = ClientCore.getWorld().getMainPlayer().getLevel();
					if(!(playerLevel != null && playerLevel.getLevelId() == levelId))
						ClientCore.getClient().send(new LevelChange(levelId));
					ClientCore.removeScreen();
				});
				table.add(btn).row();
			}
		});
	}
}
