package miniventure.game;

import java.nio.file.Path;
import java.util.HashMap;

import miniventure.game.texture.TextureAtlasHolder;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.Version;
import miniventure.game.util.VersionInfo;
import miniventure.game.world.file.WorldFileInterface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.math.MathUtils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.jetbrains.annotations.NotNull;

/** @noinspection StaticNonFinalField*/
public class GameCore {
	
	public static final Version VERSION = new Version("2.2.1.4"); // the last digit increments without release or tag
	
	public static boolean isCompatible(Version dataVersion) {
		return dataVersion.atOrAfter("2.2.1.4");
	}
	
	public static boolean debug = false;
	
	public static void debug(String msg) {
		if(debug)
			System.out.println(msg);
	}
	
	public static void errorFull(String error) { error(error, false, true); }
	public static void error(String error) { error(error, true); }
	public static void error(String error, boolean debugModeOnly) { error(error, debugModeOnly, false); }
	public static void error(String error, boolean debugModeOnly, boolean dumpStack) {
		if(debugModeOnly && !debug) return;
		if(dumpStack) {
			System.err.println(error + " Printing stack trace:");
			Thread.dumpStack();
		} else
			System.err.println(error);
	}
	
	@NotNull public static final String DEFAULT_GAME_DIR;
	public static Path GAME_DIR = null;
	static {
		String home = System.getProperty("user.home");
		if(System.getProperty("os.name").contains("Windows")) {
			DEFAULT_GAME_DIR = home + "/Documents/My Games/Miniventure/";
			WorldFileInterface.migrate(home + "/AppData/Roaming/Miniventure/");
		}
		else
			DEFAULT_GAME_DIR = home + "/.miniventure/";
	}
	
	public static final float MAX_DELTA = 0.25f; // the maximum time that the game will clamp getDeltaTime to, to prevent huge jumps after a lag spike.
	
	private static final long START_TIME = System.nanoTime();
	
	public static final float SOUND_RADIUS = 10; // 10 tiles
	
	public static final Color DEFAULT_CHAT_COLOR = Color.WHITE;
	
	// public static final int DEFAULT_WORLD_SIZE = 200;
	
	public static TextureAtlasHolder entityAtlas, tileAtlas, descaledTileAtlas, scaledIconAtlas;
	public static TextureAtlas tileConnectionAtlas = new TextureAtlas(); // tile overlap atlas not needed b/c the overlap sprite layout is simple enough to code; it goes in binary. However, the tile connection sprite layout is more complicated, so a map is needed to compare against.
	
	private static TextureAtlas iconAtlas;
	public static final HashMap<String, TextureHolder> icons = new HashMap<>();
	
	private static boolean initialized = false;
	
	public static void initGdxTextures() {
		if(initialized) return;
		initialized = true;
		entityAtlas = new TextureAtlasHolder(new TextureAtlas("sprites/entities.txt"));
		tileAtlas = new TextureAtlasHolder(new TextureAtlas("sprites/tiles4x.txt"));
		tileConnectionAtlas = new TextureAtlas("sprites/tileconnectmap.txt");
		iconAtlas = new TextureAtlas("sprites/icons.txt");
		scaledIconAtlas = new TextureAtlasHolder(new TextureAtlas("sprites/icons4x.txt"));
		
		for(AtlasRegion region: iconAtlas.getRegions())
			icons.put(region.name, new TextureHolder(region));
		
		descaledTileAtlas = new TextureAtlasHolder(new TextureAtlas("sprites/tiles.txt"));
	}
	
	public static void initNonGdxTextures() {
		if(initialized) return;
		initialized = true;
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
	}
	
	public static void dispose () {
		entityAtlas.dispose();
		tileAtlas.dispose();
		descaledTileAtlas.dispose();
		tileConnectionAtlas.dispose();
		iconAtlas.dispose();
		scaledIconAtlas.dispose();
	}
	
	public static float getDeltaTime() { return MathUtils.clamp(Gdx.graphics.getDeltaTime(), 0, MAX_DELTA); }
	
	public static float getElapsedProgramTime() { return (System.nanoTime() - START_TIME)/1E9f; }
	
	
	private static VersionInfo latestVersion = null;
	
	public static boolean determinedLatestVersion() { return latestVersion != null; }
	@NotNull
	public static VersionInfo getLatestVersion() {
		if(latestVersion != null)
			return latestVersion;
		
		// fetch the latest version from github
		try {
			HttpResponse<JsonNode> response = Unirest.get("https://api.github.com/repos/chrisj42/miniventure/releases").asJson();
			if(response.getStatus() != 200) {
				System.err.println("version request returned status code "+response.getStatus()+": "+response.getStatusText());
				System.err.println("response body: "+response.getBody());
				return latestVersion = new VersionInfo(VERSION, "", "");
			}
			else {
				return latestVersion = new VersionInfo(response.getBody().getArray().getJSONObject(0));
			}
		} catch(UnirestException e) {
			e.printStackTrace();
			return latestVersion = new VersionInfo(VERSION, "", "");
		}
	}
}
