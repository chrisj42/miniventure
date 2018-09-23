package miniventure.game.screen;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;
import miniventure.game.client.LevelViewport;
import miniventure.game.screen.InfoScreen.CreditsScreen;
import miniventure.game.screen.InfoScreen.InstructionsScreen;
import miniventure.game.util.VersionInfo;
import miniventure.game.world.DisplayLevel;
import miniventure.game.world.TimeOfDay;
import miniventure.game.world.levelgen.LevelGenerator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class MainMenu extends MenuScreen {
	
	private boolean dialog = false;
	
	private final DisplayLevel backgroundLevel;
	private final LevelViewport levelView;
	private final com.badlogic.gdx.graphics.Color lightOverlay;
	private final Vector2 cameraPos, cameraDir;
	
	private final JPanel labelPanel = new JPanel();
	
	private static final float PAN_SPEED = 4.5f; // in tiles/second.
	
	public MainMenu() {
		super(true, true);
		ClientWorld world = ClientCore.getWorld();
		
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
		labelPanel.setBackground(new Color(163, 227, 232));
		labelPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.CYAN, Color.GRAY), BorderFactory.createEmptyBorder(20, 30, 20, 20)));
		
		// table = new Table();
		// table.setDebug(true);
		addLabel("Welcome to Miniventure!", 20);
		addLabel("You are playing version " + GameCore.VERSION, 25);
		
		LinkLabel updateLabel = new LinkLabel("Checking for higher versions...", null);
		labelPanel.add(updateLabel);
		add(labelPanel);
		add(Box.createVerticalStrut(45));
		setVersionUpdateLabel(updateLabel);
		
		JButton playButton = new JButton("Play");
		
		playButton.addActionListener(e -> {
			if(dialog) return;
			world.createWorld(0, 0);
		});
		
		add(playButton);
		add(Box.createVerticalStrut(20));
		
		JButton joinBtn = new JButton("Join Server");
		joinBtn.addActionListener(e -> {
			if(dialog) return;
			dialog = true;
			LoadingScreen loader = new LoadingScreen();
			loader.pushMessage("Preparing to connect...");
			ClientCore.setScreen(loader);
			EventQueue.invokeLater(() -> {
				String ipAddress = JOptionPane.showInputDialog("Enter the IP Address you want to connect to.");
				Gdx.app.postRunnable(() -> {
					if(ipAddress != null)
						world.createWorld(ipAddress);
					else
						ClientCore.backToParentScreen();
				});
				dialog = false;
			});
		});
		
		add(joinBtn);
		add(Box.createVerticalStrut(20));
		
		JButton helpBtn = makeButton("Instructions", () -> ClientCore.setScreen(new InstructionsScreen()));
		add(helpBtn);
		add(Box.createVerticalStrut(20));
		
		JButton creditsBtn = makeButton("Credits", () -> ClientCore.setScreen(new CreditsScreen()));
		add(creditsBtn);
		add(Box.createVerticalStrut(20));
		
		JButton exitBtn = makeButton("Quit", () -> Gdx.app.exit());
		add(exitBtn);
		
		// setup level scrolling in background
		
		levelView = new LevelViewport();
		levelView.zoom(-1);
		TimeOfDay time = TimeOfDay.values[MathUtils.random(TimeOfDay.values.length-1)];
		lightOverlay = TimeOfDay.getSkyColor(time.getStartOffsetSeconds());
		
		LevelGenerator generator = new LevelGenerator(MathUtils.random.nextLong(), 200, 100);
		backgroundLevel = new DisplayLevel(generator);
		
		Vector2 size = new Vector2(levelView.getViewWidth(), levelView.getViewHeight());//.scl(0.5f);
		cameraPos = new Vector2(MathUtils.random(size.x, backgroundLevel.getWidth()-size.x), MathUtils.random(size.y, backgroundLevel.getHeight()-size.y));
		
		cameraDir = new Vector2().setLength(PAN_SPEED).setToRandomDirection().setLength(PAN_SPEED);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Gdx.app.postRunnable(() -> levelView.resize(getWidth(), getHeight()));
			}
		});
		
		// setup background music
		Music song = ClientCore.setMusicTrack(Gdx.files.internal("audio/music/title.mp3"));
		song.setLooping(true);
		song.play();
	}
	
	@Override
	public void glDraw() {
		super.glDraw();
		// levelView.handleInput();
		levelView.render(cameraPos, lightOverlay, backgroundLevel);
		
		cameraPos.add(cameraDir.cpy().scl(GameCore.getDeltaTime()));
		cameraDir.x = velDir(cameraPos.x, cameraDir.x, levelView.getViewWidth()/2, backgroundLevel.getWidth() - levelView.getViewWidth()/2);
		cameraDir.y = velDir(cameraPos.y, cameraDir.y, levelView.getViewHeight()/2, backgroundLevel.getHeight() - levelView.getViewHeight()/2);
	}
	
	private float velDir(float pos, float vel, float min, float max) {
		if((pos >= max && vel >= 0) || (pos <= min && vel <= 0)) {
			vel += MathUtils.random(-PAN_SPEED/4, PAN_SPEED/4);
			vel = -vel;
		}
		
		return vel;
	}
	
	private void addLabel(String msg, int spacing) {
		JLabel label = makeLabel(msg);
		labelPanel.add(label);
		labelPanel.add(Box.createVerticalStrut(spacing));
	}
	
	@NotNull
	private JLabel makeLabel(String text) {
		JLabel lbl = new JLabel(text);
		lbl.setForeground(Color.BLACK);
		return lbl;
	}
	
	private void setVersionUpdateLabel(LinkLabel label) {
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
			
			if(latestVersion.version.compareTo(GameCore.VERSION) > 0) // link new version
				label.setValue("Miniventure " + latestVersion.releaseName + " Now Available! Click here to download.", latestVersion.assetUrl);
			else if(latestVersion.releaseName.length() > 0)
				label.setValue("You have the latest version.", null);
			else
				label.setValue("Connection failed, could not check for updates.", null);
			
			// table.pack();
			// table.setPosition(getWidth()/2, getHeight()/2, Align.center);
		}
	}
}
