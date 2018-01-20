package miniventure.game.screen;

import miniventure.game.GameCore;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class RespawnScreen extends MenuScreen {
	
	public RespawnScreen() {
		super();
		
		table.add(new VisLabel("You died!"));
		table.row();
		table.add(new VisTextButton("Respawn", new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				GameCore.getWorld().respawn();
				GameCore.setScreen(null);
			}
		}));
	}
	
	/*@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.1f, 0.5f, 0.1f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		game.getBatch().begin();
		
		MyUtils.drawTextCentered(GameCore.getFont(), game.getBatch(),"You Died! Click or press space to respawn.", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		game.getBatch().end();
		
		if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
			game.setScreen(gameScreen);
	}*/
	
}
