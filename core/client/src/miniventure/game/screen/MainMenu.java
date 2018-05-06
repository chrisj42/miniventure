package miniventure.game.screen;

import javax.swing.JOptionPane;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;
import miniventure.game.client.DisplayLevel;
import miniventure.game.client.LevelViewport;
import miniventure.game.world.TimeOfDay;
import miniventure.game.world.levelgen.LevelGenerator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MainMenu extends MenuScreen {
	
	private boolean dialog = false;
	
	private final Table table;
	
	private final DisplayLevel backgroundLevel;
	private final LevelViewport levelView;
	private final Color lightOverlay;
	private final Vector2 cameraPos, cameraDir;
	
	private static final float PAN_SPEED = 3f; // in tiles/second.
	
	public MainMenu() {
		super();
		
		ClientWorld world = ClientCore.getWorld();
		
		table = new Table();
		addLabel("Welcome to Miniventure!", 20);
		addLabel("You are playing version " + GameCore.VERSION, 15);
		
		VisTextButton button = new VisTextButton("Play");
		
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				if(dialog) return;
				world.createWorld(0, 0);
			}
		});
		
		table.setPosition(getWidth()/2, getHeight()*2/3, Align.center);
		
		addActor(table);
		table.add(button);
		
		VisTextButton joinBtn = new VisTextButton("Join Server");
		joinBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				if(dialog) return;
				dialog = true;
				LoadingScreen loader = new LoadingScreen();
				loader.pushMessage("Preparing to connect...");
				ClientCore.setScreen(loader);
				new Thread(() -> {
					String ipAddress = JOptionPane.showInputDialog("Enter the IP Address you want to connect to.");
					Gdx.app.postRunnable(() -> {
						if(ipAddress != null)
							world.createWorld(ipAddress);
						else
							ClientCore.backToParentScreen();
					});
					dialog = false;
				}).start();
			}
		});
		
		table.row();
		table.add(joinBtn);
		table.row().space(20);
		
		VisTextButton helpBtn = makeButton("Instructions", () -> ClientCore.setScreen(new InstructionsScreen()));
		table.add(helpBtn).row();
		
		table.setPosition(getWidth()/2, getHeight()/2);
		
		// setup level scrolling in background
		
		levelView = new LevelViewport();
		TimeOfDay time = TimeOfDay.values[MathUtils.random(TimeOfDay.values.length-1)];
		lightOverlay = TimeOfDay.getSkyColor(time.getStartOffsetSeconds());
		
		LevelGenerator generator = new LevelGenerator(MathUtils.random.nextLong(), 100, 60, 8, 6);
		backgroundLevel = new DisplayLevel(generator);
		
		Vector2 halfSize = new Vector2(levelView.getViewWidth(), levelView.getViewHeight()).scl(0.5f);
		cameraPos = new Vector2(MathUtils.random(halfSize.x, backgroundLevel.getWidth()-halfSize.x), MathUtils.random(halfSize.y, backgroundLevel.getHeight()-halfSize.y));
		
		cameraDir = new Vector2().setLength(PAN_SPEED).setToRandomDirection().setLength(PAN_SPEED);
		
		// setup background music
		Music song = ClientCore.setMusicTrack(Gdx.files.internal("audio/music/title.mp3"));
		/*song.setOnCompletionListener(music -> MyUtils.delay(MathUtils.random(5000, 10000), () -> {
			music.stop();
			music.play();
		}));*/
		//song.setVolume(0.5f);
		//MyUtils.delay(100, song::play);
		song.setLooping(true);
		song.play();
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	@Override
	public void draw() {
		levelView.render(cameraPos, lightOverlay, backgroundLevel);
		
		cameraPos.add(cameraDir.cpy().scl(Gdx.graphics.getDeltaTime()));
		cameraDir.x = velDir(cameraPos.x, cameraDir.x, levelView.getViewWidth()/2, backgroundLevel.getWidth() - levelView.getViewWidth()/2);
		cameraDir.y = velDir(cameraPos.y, cameraDir.y, levelView.getViewHeight()/2, backgroundLevel.getHeight() - levelView.getViewHeight()/2);
		
		super.draw();
	}
	
	private float velDir(float pos, float vel, float min, float max) {
		if((pos >= max && vel >= 0) || (pos <= min && vel <= 0)) {
			vel += MathUtils.random(-PAN_SPEED/4, PAN_SPEED/4);
			vel = -vel;
		}
		
		return vel;
	}
		
	private void addLabel(String msg, int spacing) {
		table.add(new VisLabel(msg, new LabelStyle(GameCore.getFont(), Color.WHITE)));
		table.row().space(spacing);
	}
}
