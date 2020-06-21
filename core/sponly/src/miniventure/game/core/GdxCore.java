package miniventure.game.core;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.core.FontStyle.StyleData;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.NotifyScreen;
import miniventure.game.texture.TextureAtlasHolder;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.tile.TileTypeRenderer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @noinspection StaticNonFinalField*/
public class GdxCore extends ApplicationAdapter {
	
	public static final float DEFAULT_BATCH_COLOR = Color.WHITE.toFloatBits();
	
	public static final int DEFAULT_SCREEN_WIDTH = 1400;
	public static final int DEFAULT_SCREEN_HEIGHT = 800;
	public static final Color DEFAULT_CHAT_COLOR = Color.WHITE;
	
	public static final float MAX_DELTA = 0.25f; // the maximum time that the game will clamp getDeltaTime to, to prevent huge jumps after a lag spike.
	public static float getDeltaTime() { return MathUtils.clamp(Gdx.graphics.getDeltaTime(), 0, MAX_DELTA); }
	
	private static GameScreen gameScreen;
	// private static WorldManager clientWorld;
	
	// public static boolean viewedInstructions = false;
	
	public static final InputHandler input = new InputHandler();
	
	private static boolean hasMenu = false;
	private static MenuScreen menuScreen;
	
	private static final Object screenLock = new Object();
	private static SpriteBatch batch;
	private static Skin skin;
	private static FreeTypeFontGenerator fontGenerator;
	private static GlyphLayout layout = new GlyphLayout();
	// private static HashMap<Integer, BitmapFont> fonts = new HashMap<>();
	
	// private static boolean initialized = false;
	public static TextureAtlasHolder entityAtlas;
	public static TextureAtlasHolder tileAtlas;
	public static TextureAtlasHolder descaledTileAtlas;
	public static TextureAtlas tileConnectionAtlas = new TextureAtlas(); // tile overlap atlas not needed b/c the overlap sprite layout is simple enough to code; it goes in binary. However, the tile connection sprite layout is more complicated, so a map is needed to compare against.
	private static TextureAtlas iconAtlas;
	public static TextureAtlasHolder scaledIconAtlas; // these two are exclusively used for item entities
	public static final HashMap<String, TextureHolder> icons = new HashMap<>();
	
	/*public static void initGdxTextures() {
		// if(initialized) return;
		// initialized = true;
		
	}
	
	public static void initNonGdxTextures() {
		// if(initialized) return;
		// initialized = true;
		// initialize entity atlas and icon atlas, b/c that's what the server needs to determine entity sizes (icons b/c of item entities)
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		
		// maybe if I manually created a TextureAtlasData?
		FileHandle spriteFolder = Gdx.files.internal("sprites");
		TextureAtlasData entityData = new TextureAtlasData(spriteFolder.child("entities.txt"), spriteFolder, false);
		TextureAtlasData iconData = new TextureAtlasData(spriteFolder.child("icons.txt"), spriteFolder, false);
		TextureAtlasData iconScaledData = new TextureAtlasData(spriteFolder.child("icons4x.txt"), spriteFolder, false);
		TextureAtlasData tileData = new TextureAtlasData(spriteFolder.child("tiles4x.txt"), spriteFolder, false);
		
		entityAtlas = new TextureAtlasHolder(entityData);
		tileAtlas = new TextureAtlasHolder(tileData);
		descaledTileAtlas = new TextureAtlasHolder(new TextureAtlasData(spriteFolder.child("tiles.txt"), spriteFolder, false));
		scaledIconAtlas = new TextureAtlasHolder(iconScaledData);
		for(Region region: iconData.getRegions()) {
			TextureHolder tex = new TextureHolder(region);
			icons.put(tex.name, tex);
		}
	}*/
	
	@Override
	public void create () {
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
		skin = new Skin(Gdx.files.internal("skins/visui/uiskin.json"));
		// Style.loadStyles(skin);
		VisUI.load(skin);
		
		LoadingScreen loader = LoadingScreen.initLoader();
		loader.pushMessage("Initializing");
		setScreen(loader);
		//System.out.println("start delay");
		MyUtils.delay(0, () -> Gdx.app.postRunnable(() -> {
			//System.out.println("end delay");
			
			entityAtlas = new TextureAtlasHolder(new TextureAtlas("sprites/entities.txt"));
			tileAtlas = new TextureAtlasHolder(new TextureAtlas("sprites/tiles4x.txt"));
			tileConnectionAtlas = new TextureAtlas("sprites/tileconnectmap.txt");
			iconAtlas = new TextureAtlas("sprites/icons.txt");
			scaledIconAtlas = new TextureAtlasHolder(new TextureAtlas("sprites/icons4x.txt"));
			
			for(AtlasRegion region: iconAtlas.getRegions())
				icons.put(region.name, new TextureHolder(region));
			
			descaledTileAtlas = new TextureAtlasHolder(new TextureAtlas("sprites/tiles.txt"));
			
			if(batch == null)
				batch = new SpriteBatch();
			
			// GenericEnum.init();
			TileTypeRenderer.init();
			TileType.init();
			// EntityRenderer.init();
			// serverStarter.init();
			
			// gameScreen = new GameScreen();
			// clientWorld = new WorldManager();
			
			MenuScreen devNotice = new NotifyScreen(() -> setScreen(new MainMenu()),
				"Continue",
				"Welcome to Miniventure Alpha!",
				"",
				"This game is not finished!!! Not even close.",
				"Though the back-end may change from time to time, who knows.",
				"",
				"The utmost effort is made to prevent bugs from getting into releases, but since",
				"the sole developer has a life and very few testers, this effort only goes so far.",
				"",
				"Also, please keep in mind that, while the gameplay systems are (fairly) stable,",
				"the majority of content is yet to come.",
				
				"You may have to create new worlds to take advantage of some of the bigger updates,",
				"as the world is still being fleshed out.",
				"",
				"Enjoy the game!"
			);
			
			/*Path oldDataPath = WorldFileInterface.getDataImportSource();
			if(oldDataPath != null)
				setScreen(new ConfirmScreen("The default save location for miniventure files has changed since the previous version,\nso your files have been copied to the new location.\nDo you wish to delete the old save location? Older versions will lose their data.", () -> {
					WorldFileInterface.deleteRecursively(oldDataPath);
					setScreen(devNotice);
				}, () -> setScreen(devNotice)));
			else
				*/setScreen(devNotice);
		}));
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		
		fontGenerator.dispose();
		for(HashMap<Integer, BitmapFont> sizedFonts: fonts.values())
			for(BitmapFont font: sizedFonts.values())
				font.dispose();
		fonts.clear();
		
		if(gameScreen != null)
			gameScreen.dispose();
		
		if(menuScreen != null)
			menuScreen.dispose();
		
		entityAtlas.dispose();
		tileAtlas.dispose();
		descaledTileAtlas.dispose();
		tileConnectionAtlas.dispose();
		iconAtlas.dispose();
		scaledIconAtlas.dispose();
		
		VisUI.dispose();
	}
	
	public static void useDefaultBackgroundColor() {
		// a green color
		Gdx.gl.glClearColor(0.1f, 0.5f, 0.1f, 1); // these are floats from 0 to 1.
	}
	
	@Override
	public void render() {
		// clear the screen to prep for the next render
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// reset batch color just in case its gotten messed up by something TODO actually it getting messed up is a good indication that I messed something up, so I may want to remove this sometimes for debugging purposes.
		getBatch().setColor(DEFAULT_BATCH_COLOR);
		
		final WorldManager world = GameCore.getWorld();
		
		// before the render method is called, gdx input events are processed which will add pressed keys
		// this adds any repeating keys
		input.onFrameStart();
		
		if (world != null)
			world.updateAndRender(getDeltaTime());
		
		synchronized (screenLock) {
			hasMenu = menuScreen != null;
			
			if(menuScreen != null)
				menuScreen.act();
			if(menuScreen != null)
				menuScreen.draw();
		}
		
		// clear the pressed keys in this frame
		input.onFrameEnd();
	}
	
	private static void resetInputProcessor() {
		input.resetDelay();
		
		if(gameScreen == null) {
			Gdx.input.setInputProcessor(menuScreen == null ? input : new InputMultiplexer(input, menuScreen));
		} else {
			Gdx.input.setInputProcessor(menuScreen == null ? new InputMultiplexer(gameScreen.getGuiStage(), input) : new InputMultiplexer(input.repressDelay(.1f), menuScreen, gameScreen.getGuiStage()));
		}
	}
	
	// todo postpone menu updates until the next frame - we don't want to change it in the middle of a level update
	private static void changeScreen(@Nullable MenuScreen screen) {
		synchronized (screenLock) {
			// if(screen == menuScreen) return;
			
			// handle error screens
			// if(screen instanceof ErrorScreen && (menuScreen instanceof MainMenu || menuScreen instanceof ErrorScreen))
			// 	return; // ignore it.
			
			// manage possible parent relationship
			// trusting that the addScreen method will be used with care
			/*if(screen != null) {
				// determine if the given screen instance is already somewhere on the "screen" hierarchy, and if so go back to that one.
				MenuScreen check = menuScreen;
				while(check != null && check != screen)
					check = check.getParent();
				
				if(check != null) {
					// screen found
					while(menuScreen != null && menuScreen != screen)
						backToParentScreen();
					
					return;
				}
			}*/
			
			// if(menuScreen instanceof BackgroundProvider && screen instanceof BackgroundInheritor)
			// 	((BackgroundInheritor) screen).setBackground((BackgroundProvider) menuScreen);
			
			if(screen == null && GameCore.getWorld() == null)
				screen = new MainMenu();
			
			// handle switching to a new screen
			if(screen != null)
				screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			
			MyUtils.debug("setting screen to " + screen);
			
			// input.resetDelay();
			menuScreen = screen;
			resetInputProcessor();
			if(menuScreen != null) menuScreen.focus();
		}
	}
	public static void setScreen(@Nullable MenuScreen screen) {
		synchronized (screenLock) {
			if(screen == menuScreen) return;
			
			MenuScreen current = menuScreen;
			changeScreen(screen);
			if(current != null && !current.isPersistent())
				current.dispose();
		}
	}
	// add a screen as the child of the current one
	public static void addScreen(@NotNull MenuScreen screen) {
		synchronized (screenLock) {
			if(screen == menuScreen) return;
			
			MenuScreen current = menuScreen;
			changeScreen(screen);
			screen.setParent(current);
		}
	}
	public static void backToParentScreen() {
		synchronized (screenLock) {
			if(menuScreen == null) return;
			
			MenuScreen parent = menuScreen.getParent();
			MenuScreen current = menuScreen;
			changeScreen(parent);
			
			if(!current.isPersistent())
				current.dispose(false);
		}
	}
	
	/*public static GameScreen newGameScreen(GameScreen screen) {
		gameScreen = screen;
		resetInputProcessor();
		gameScreen.getGuiStage().focus();
		return screen;
	}*/
	
	/*public static void manageChatPackets(Object obj) {
		if(gameScreen == null) return;
		if(obj instanceof Message) {
			gameScreen.chatOverlay.addMessage((Message)obj);
			gameScreen.chatScreen.addMessage((Message)obj);
		}
		else if(obj instanceof InfoMessage) {
			gameScreen.chatOverlay.addMessage((InfoMessage)obj);
			gameScreen.chatScreen.addMessage((InfoMessage)obj);
		}
		else if(obj == DatalessRequest.Clear_Console) {
			gameScreen.chatOverlay.reset();
			gameScreen.chatScreen.reset();
		}
	}*/
	
	@Override
	public void resize(int width, int height) {
		if(gameScreen != null) // null check in case this is called before app is fully initialized
			gameScreen.resize(width, height);
		
		MenuScreen menu = getScreen();
		if(menu != null)
			menu.resize(width, height);
	}
	
	
	public static SpriteBatch getBatch() {
		if(batch == null) batch = new SpriteBatch();
		return batch;
	}
	
	public static GlyphLayout getTextLayout(String text) { return getTextLayout(getFont(), text); }
	public static GlyphLayout getTextLayout(BitmapFont font, String text) {
		if(fontGenerator != null)
			layout.setText(font, text);
		return layout;
	}
	
	private static EnumMap<FontStyle, HashMap<Integer, BitmapFont>> fonts = new EnumMap<>(FontStyle.class);
	
	public static BitmapFont getFont() { return getFont(FontStyle.Default); }
	public static BitmapFont getFont(FontStyle fontStyle) {
		// determine size difference in current window size vs default/minimum window size
		int minDefault = Math.min(DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT);
		int minCurrent = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		// get the font parameter object named by the given style (throw error if none found)
		HashMap<Integer, BitmapFont> sizeFonts = fonts.computeIfAbsent(fontStyle, style -> new HashMap<>());
		
		StyleData style = fontStyle.get();
		FreeTypeFontParameter params = style.font;
		// apply font size modification
		// the process should maintain the ratios: orig font size / min default screen = new font size / min current screen
		// aka, new font size = orig font size * min current size / min default size
		if(style.resize) {
			params.size = params.size * minCurrent / minDefault;
			params.borderWidth = params.borderWidth * minCurrent / minDefault;
		}
		
		return sizeFonts.computeIfAbsent(params.size, fontSize -> fontGenerator.generateFont(params));
	}
	
	public static boolean hasMenu() { synchronized (screenLock) { return hasMenu; } }
	
	@Nullable
	public static MenuScreen getScreen() { synchronized (screenLock) { return menuScreen; } }
	
	public static LoadingScreen ensureLoadingScreen(String message) {
		LoadingScreen loader;
		synchronized (screenLock) {
			loader = menuScreen instanceof LoadingScreen ? (LoadingScreen) menuScreen : new LoadingScreen();
			loader.pushMessage(message, true);
			setScreen(loader);
		}
		return loader;
	}
	
	@Override
	public void resume() {
		synchronized (screenLock) {
			if(menuScreen != null) {
				menuScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			}
		}
	}
}
