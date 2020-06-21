package miniventure.game.screen;

import java.util.HashMap;

import miniventure.game.core.GdxCore;
import miniventure.game.core.InputHandler.Control;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Point;
import miniventure.game.world.management.Level;
import miniventure.game.world.management.WorldManager;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTextButton;

import org.jetbrains.annotations.NotNull;

public class MapScreen extends MenuScreen {
	
	// private boolean requested;
	
	private HashMap<Point, Cell> mapCells;
	
	private Table table;
	
	{
		table = useTable();
	}
	
	public MapScreen(@NotNull Level curLevel) {
		super();
		
		// Level curLevel = GameCore.getWorld().getLevel();
		int curId = curLevel.getLevelId();
		for(int i = 0; i < 3; i++) {
			// Point p = null;//mapRequest.islands[i];
			final int levelid = i;
			if(levelid == curId) continue;
			VisTextButton btn = makeButton("Island "+(i+1)+": "+MyUtils.toTitleFormat(WorldManager.getIslandType(i+1).name()), () -> {
				// Level curLevel = ClientCore.getWorld().getMainPlayer().getLevel();
				if(!(curLevel.getLevelId() == levelid))
					curLevel.getWorld().requestLevel(levelid);
					// GameCore.getClient().send(new LevelChange(levelid));
				GdxCore.setScreen(null);
			});
			table.add(btn).row();
		}
	}
	
	@Override
	public void act(float delta) {
		if(GdxCore.input.pressingControl(Control.CANCEL))
			GdxCore.setScreen(null);
		else
			super.act(delta);
	}
}
