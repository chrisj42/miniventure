package miniventure.game.screen;

import miniventure.game.core.FontStyle;
import miniventure.game.core.GameCore;
import miniventure.game.core.GdxCore;
import miniventure.game.screen.InfoScreen.CreditsScreen;
import miniventure.game.screen.InfoScreen.InstructionsScreen;
import miniventure.game.screen.util.MyLinkLabel;
import miniventure.game.util.Version;
import miniventure.game.util.VersionInfo;
import miniventure.game.world.file.WorldReference;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

import org.jetbrains.annotations.NotNull;

public class MainMenu extends MenuScreen {
	
	private final Table table;
	
	// private final Level backgroundLevel;
	// private final LevelViewport levelView;
	// private final Color lightOverlay;
	// private final Vector2 cameraPos, cameraDir;
	
	// private static final float PAN_SPEED = 4.5f; // in tiles/second.
	
	public MainMenu() {
		super(new ScreenViewport());
		
		// WorldManager world = RenderCore.getWorld();
		
		table = useTable();
		
		addLabel("Welcome to Miniventure!", 20);
		addLabel("You are playing " + Version.CURRENT.toString().replace("The","the"), 25);
		
		VisLabel updateLabel = addLabel("Checking for higher versions...", 45);
		setVersionUpdateLabel(updateLabel);
		
		VisTextButton playButton = makeButton("New World", () -> GdxCore.setScreen(new WorldGenScreen()));
		
		table.add(playButton).spaceBottom(20);
		table.row();
		
		if(WorldReference.getLocalWorlds(false).size() > 0) {
			VisTextButton loadBtn = makeButton("Load World", () -> GdxCore.setScreen(new WorldSelectScreen()));
			table.add(loadBtn).spaceBottom(20).row();
		}
		
		VisTextButton helpBtn = makeButton("Instructions", () -> GdxCore.addScreen(new InstructionsScreen()));
		table.add(helpBtn).spaceBottom(20).row();
		
		VisTextButton creditsBtn = makeButton("Credits", () -> GdxCore.addScreen(new CreditsScreen()));
		table.add(creditsBtn).spaceBottom(20).row();
		
		VisTextButton exitBtn = makeButton("Quit", () -> Gdx.app.exit());
		table.add(exitBtn).row();
		
		// setup level scrolling in background
		
		// levelView = new LevelViewport();
		// levelView.zoom(-1);
		// TimeOfDay time = TimeOfDay.values[MathUtils.random(TimeOfDay.values.length-1)];
		// lightOverlay = TimeOfDay.getSkyColor(time.getStartOffsetSeconds());
		
		// backgroundLevel = new DisplayWorld().getLevel();
		
		// Vector2 size = new Vector2(levelView.getViewWidth(), levelView.getViewHeight());//.scl(0.5f);
		// cameraPos = new Vector2(MathUtils.random(size.x, backgroundLevel.getWidth()-size.x), MathUtils.random(size.y, backgroundLevel.getHeight()-size.y));
		
		// cameraDir = new Vector2().setLength(PAN_SPEED).setToRandomDirection().setLength(PAN_SPEED);
	}
	
	@Override
	public void focus() {
		super.focus();
		// todo main menu music
	}
	
	/*@Override
	public boolean allowChildren() {
		return true;
	}*/
	
	/*@Override
	public void renderBackground() {
		levelView.render(backgroundLevel, cameraPos, lightOverlay);
		
		cameraPos.add(cameraDir.cpy().scl(GdxCore.getDeltaTime()));
		cameraDir.x = velDir(cameraPos.x, cameraDir.x, levelView.getViewWidth()/2, backgroundLevel.getWidth() - levelView.getViewWidth()/2);
		cameraDir.y = velDir(cameraPos.y, cameraDir.y, levelView.getViewHeight()/2, backgroundLevel.getHeight() - levelView.getViewHeight()/2);
	}*/
	
	// @Override
	// public void resizeBackground(int width, int height) {
	// 	levelView.resize(width, height);
	// }
	
	/*private float velDir(float pos, float vel, float min, float max) {
		if((pos >= max && vel >= 0) || (pos <= min && vel <= 0)) {
			vel += MathUtils.random(-PAN_SPEED/4, PAN_SPEED/4);
			vel = -vel;
		}
		
		return vel;
	}*/
	
	@NotNull
	private VisLabel addLabel(String msg, int spacing) {
		VisLabel label = makeLabel(msg);
		table.add(label).spaceBottom(spacing);
		table.row();
		return label;
	}
	
	private void setVersionUpdateLabel(VisLabel label) {
		if(!GameCore.determinedLatestVersion()) {
			// return a "loading" label that will be replaced once the version check completes.
			new Thread(() -> {
				GameCore.getLatestVersion();
				setVersionUpdateLabel(label);
			}).start();
		}
		else {
			// add a message saying you have the latest version, or a hyperlink message to the newest jar file.
			VersionInfo latestVersion = GameCore.getLatestVersion();
			int comp = latestVersion.version.compareTo(Version.CURRENT);
			if(comp > 0) { // link new version
				MyLinkLabel linkLabel = new MyLinkLabel(latestVersion.releaseName + " Now Available! Click here to download.", latestVersion.assetUrl);
				deregisterLabels(label);
				table.getCell(label).setActor(linkLabel);
				registerLabel(FontStyle.Default, linkLabel);
			}
			else if(comp < 0)
				label.setText("You are using an unreleased version.");
			else if(latestVersion.releaseName.length() > 0)
				label.setText("You have the latest version.");
			else
				label.setText("Connection failed, could not check for updates.");
		}
	}
}
