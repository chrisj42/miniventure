package miniventure.game.screen;

import miniventure.game.GameCore;
import miniventure.game.network.GameProtocol;
import miniventure.game.client.AudioException;
import miniventure.game.client.ClientCore;
import miniventure.game.client.FontStyle;
import miniventure.game.client.LevelViewport;
import miniventure.game.screen.InfoScreen.CreditsScreen;
import miniventure.game.screen.InfoScreen.InstructionsScreen;
import miniventure.game.screen.util.BackgroundProvider;
import miniventure.game.screen.util.MyLinkLabel;
import miniventure.game.util.Version;
import miniventure.game.util.VersionInfo;
import miniventure.game.world.level.RenderLevel;
import miniventure.game.world.management.ClientWorld;
import miniventure.game.world.management.DisplayWorld;
import miniventure.game.world.management.TimeOfDay;
import miniventure.game.world.file.WorldReference;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

import org.jetbrains.annotations.NotNull;

public class MainMenu extends BackgroundProvider {
	
	private final Table table;
	
	private final RenderLevel backgroundLevel;
	private final LevelViewport levelView;
	private final Color lightOverlay;
	private final Vector2 cameraPos, cameraDir;
	
	private static final float PAN_SPEED = 4.5f; // in tiles/second.
	
	public MainMenu() {
		super(false, true, new ScreenViewport()); // level renderer clears it
		
		ClientWorld world = ClientCore.getWorld();
		
		table = useTable();
		
		addLabel("Welcome to Miniventure!", 20);
		addLabel("You are playing " + Version.CURRENT.toString().replace("The","the"), 25);
		
		VisLabel updateLabel = addLabel("Checking for higher versions...", 45);
		setVersionUpdateLabel(updateLabel);
		
		VisTextButton playButton = makeButton("New World", () -> {
			if(!ClientCore.viewedInstructions)
				ClientCore.setScreen(new InstructionsScreen(true));
			else
				ClientCore.setScreen(new WorldGenScreen());
		});
		
		table.add(playButton).spaceBottom(20);
		table.row();
		
		if(WorldReference.getLocalWorlds(false).size() > 0) {
			VisTextButton loadBtn = makeButton("Load World", () -> ClientCore.setScreen(new WorldSelectScreen()));
			table.add(loadBtn).spaceBottom(20).row();
		}
		
		VisTextButton joinBtn = makeButton("Join Local Game", () -> ClientCore.setScreen(new InputScreen("Enter the IP Address you want to connect to (optionally including port):", input -> {
			/*
				TODO condense player name input into server ip input
				- if the username is taken, display the same page again, with the same data in them but also an error message saying the username is taken.
				TODO also save a default player name for new servers, and login data for all servers that have previously been connected to.
				- This suggests a renovation for the "join server" screen: a text field at the top where you enter a server ip, a text field below it filled with the default username, and at the bottom there's a list of all servers previously connected to.
				-   --- password later
				- the list has the ip, and username you connected with
				- when you type into the IP field, the displayed servers change live
					- only those matching the input are shown
					- if the ip matches exactly (or the server is clicked, which modifies the ip field), then the server is highlighted and the username is changed to the stored value for that server
					- changing the ip removes the highlight
					- changing the username is allowed, but a confirmation will be shown, confirming that you realize you will not have the same stuff as your last account, and still wish to connect.
			*/
			
			if(input.contains(":")) {
				String ip = input.substring(0, input.indexOf(":"));
				String portstr = input.substring(input.indexOf(":")+1);
				int port;
				try {
					port = Integer.parseInt(portstr);
				} catch(NumberFormatException e) {
					ClientCore.setScreen(new ErrorScreen("Given ip address is invalid; appeared to have port number '"+portstr+"', but port was not a valid integer."));
					return;
				}
				world.joinWorld(ip, port);
			} else
				world.joinWorld(input, GameProtocol.PORT);
		})));
		
		table.add(joinBtn).spaceBottom(20).row();
		
		VisTextButton helpBtn = makeButton("Instructions", () -> ClientCore.setScreen(new InstructionsScreen()));
		table.add(helpBtn).spaceBottom(20).row();
		
		VisTextButton creditsBtn = makeButton("Credits", () -> ClientCore.setScreen(new CreditsScreen()));
		table.add(creditsBtn).spaceBottom(20).row();
		
		VisTextButton exitBtn = makeButton("Quit", () -> Gdx.app.exit());
		table.add(exitBtn).row();
		
		// setup level scrolling in background
		
		levelView = new LevelViewport();
		levelView.zoom(-1);
		TimeOfDay time = TimeOfDay.values[MathUtils.random(TimeOfDay.values.length-1)];
		lightOverlay = TimeOfDay.getSkyColor(time.getStartOffsetSeconds());
		
		backgroundLevel = new DisplayWorld().getLevel();
		
		Vector2 size = new Vector2(levelView.getViewWidth(), levelView.getViewHeight());//.scl(0.5f);
		cameraPos = new Vector2(MathUtils.random(size.x, backgroundLevel.getWidth()-size.x), MathUtils.random(size.y, backgroundLevel.getHeight()-size.y));
		
		cameraDir = new Vector2().setLength(PAN_SPEED).setToRandomDirection().setLength(PAN_SPEED);
		
		if(ClientCore.PLAY_MUSIC) {
			// setup background music
			try {
				Music song = ClientCore.setMusicTrack(Gdx.files.internal("audio/music/title.mp3"));
				song.setLooping(true);
				song.play();
			} catch(AudioException e) {
				System.err.println("Failed to fetch main menu music.");
				// e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean allowChildren() {
		return true;
	}
	
	@Override
	public void renderBackground() {
		levelView.render(cameraPos, lightOverlay, backgroundLevel);
		
		cameraPos.add(cameraDir.cpy().scl(GameCore.getDeltaTime()));
		cameraDir.x = velDir(cameraPos.x, cameraDir.x, levelView.getViewWidth()/2, backgroundLevel.getWidth() - levelView.getViewWidth()/2);
		cameraDir.y = velDir(cameraPos.y, cameraDir.y, levelView.getViewHeight()/2, backgroundLevel.getHeight() - levelView.getViewHeight()/2);
	}
	
	@Override
	public void resizeBackground(int width, int height) {
		levelView.resize(width, height);
	}
	
	private float velDir(float pos, float vel, float min, float max) {
		if((pos >= max && vel >= 0) || (pos <= min && vel <= 0)) {
			vel += MathUtils.random(-PAN_SPEED/4, PAN_SPEED/4);
			vel = -vel;
		}
		
		return vel;
	}
	
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
