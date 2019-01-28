package miniventure.game.server;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

import miniventure.game.GameCore;
import miniventure.game.chat.command.CommandInputParser;
import miniventure.game.file.WorldFile;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.VersionInfo;
import miniventure.game.world.ServerWorld;
import miniventure.game.world.tile.ServerTileType;

import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.NotNull;

public class ServerCore {
	
	private static ServerWorld serverWorld;
	
	private static CommandInputParser commandParser = null;
	
	public static ServerWorld getWorld() { return serverWorld; }
	public static GameServer getServer() { return serverWorld.getServer(); }
	public static CommandInputParser getCommandInput() { return commandParser; }
	
	public static void main(String[] args) {
		args = ArrayUtils.mapArray(args, String.class, String::toLowerCase);
		List<String> arglist = Arrays.asList(args);
		
		String worldname = null;
		boolean create = false;
		boolean overwrite = false;
		String seedString = null;
		
		while(arglist.size() > 0) {
			String arg = arglist.remove(0);
			switch(arg) {
				case "--create": create = true; break;
				
				case "--overwrite": overwrite = true; break;
				
				case "--seed":
					if(arglist.size() == 0) {
						System.out.println("error: --seed option requires an argument.");
						return;
					}
					seedString = arglist.remove(0);
					break;
				
				default:
					if(worldname == null)
						worldname = arg;
					else
						System.out.println("Ignoring unrecognized argument: \""+arg+"\".");
			}
		}
		
		if(worldname == null) {
			System.out.println("error: world name is required.");
			return;
		}
		
		// check for an existing save with the given name
		File world = WorldFile.getLocation(worldname);
		System.out.println("looking for worlds in: "+GameCore.GAME_DIR.resolve("saves").toFile().getAbsolutePath());
		boolean exists = world.exists();
		
		if(!exists && !create) {
			// doesn't exist but didn't say create; prompt for creation
			create = prompt("world \""+worldname+"\" does not exist. Create it? (y/n) ").equals("y");
			
			if(!create)
				System.out.println("world not created.");
		}
		
		if(create && exists) {
			// prompt for overwrite
			if(!overwrite)
				overwrite = prompt("world \""+worldname+"\" already exists. Are you sure you want to overwrite it? type \"yes\" to overwrite. (yes/no)").equals("yes");
			
			if(!overwrite)
				System.out.println("Not overwriting; world not created.");
		}
		
		boolean make = create && (!exists || overwrite);
		boolean load = !create && exists;
		
		if(!make && !load) {
			// no action.
			return;
		}
		
		if(make) {
			// create folders to acquire lock
			world.mkdirs();
		}
		
		RandomAccessFile lockHolder = WorldFile.tryLockWorld(world);
		if(lockHolder == null) {
			System.out.println("failed to acquire world lock; is it currently loaded by another instance?");
			return;
		}
		
		// lock acquired, we can now be reasonably sure it's going to work out, so let's initialize everything.
		
		GameCore.initNonGdxTextures();
		ServerTileType.init();
		
		System.out.println("loading server world...");
		
		WorldFile worldFile;
		
		if(load) // LOAD
			worldFile = WorldFile.loadWorld(world, lockHolder);
		else // CREATE
			worldFile = WorldFile.createWorld(world, lockHolder, seedString);
		
		initServer(worldFile, true);
		
		System.out.println("server ready");
		if(!GameCore.determinedLatestVersion())
			System.out.println("Checking for newer versions...");
		VersionInfo info = GameCore.getLatestVersion();
		if(info.version.compareTo(GameCore.VERSION) > 0) {
			// there's a newer version
			System.out.println("Newer game version found: "+info.version+". Download the jar file here: "+info.assetUrl);
		}
		run();
	}
	
	private static Scanner ask;
	private static String prompt(String prompt) {
		if(ask == null) {
			ask = new Scanner(System.in);
			// ask.useDelimiter(System.lineSeparator());
		}
		
		System.out.print(System.lineSeparator()+prompt);
		
		try {
			return ask.nextLine().trim().toLowerCase();
		} catch(NoSuchElementException ex) {
			// probable case: user pressed CTRL-D to send EOF. Comply by quitting the program.
			System.out.println("cancelled.");
			System.exit(0);
		}
		
		return ""; // impossible to reach.
	}
	
	public static boolean initServer(WorldFile worldFile, boolean standalone) {
		if(serverWorld != null)
			serverWorld.exitWorld();
		
		try {
			serverWorld = new ServerWorld(standalone);
		} catch(IOException e) {
			e.printStackTrace();
			serverWorld = null;
			return false;
		}
		
		serverWorld.loadWorld(worldFile);
		
		return true;
	}
	
	private static final float[] frameTimes = new float[20];
	private static final int FRAME_INTERVAL = 30; // how many frames are in each time (above)
	private static int timeIdx = 0, frameIdx = 0;
	private static boolean loopedFrames = false;
	
	public static float getFPS() {
		float totalTime = 0;
		for(float duration: frameTimes)
			totalTime += duration;
		
		return ((loopedFrames ? frameTimes.length : timeIdx) * FRAME_INTERVAL) / totalTime;
	}
	
	private static final List<Runnable> runnables = Collections.synchronizedList(new LinkedList<>());
	
	public static void postRunnable(@NotNull Runnable r) {
		synchronized (runnables) {
			runnables.add(r);
		}
	}
	
	public static void run() {
		if(ask != null) {
			ask.close();
			ask = null;
		}
		
		// start scanner thread
		if(commandParser != null)
			commandParser.end();
		
		commandParser = new CommandInputParser();
		new Thread(commandParser, "CommandInputParser").start();
		
		Arrays.fill(frameTimes, 0);
		
		long lastNow = System.nanoTime();
		long lastInterval = lastNow;
		
		//noinspection InfiniteLoopStatement
		while(serverWorld.worldLoaded()) {
			long now = System.nanoTime();
			
			frameIdx = (frameIdx + 1) % FRAME_INTERVAL;
			if(frameIdx == 0) {
				frameTimes[timeIdx] = (float) ((now - lastInterval) / 1E9D);
				lastInterval = now;
				timeIdx = (timeIdx + 1) % frameTimes.length;
				if(timeIdx == 0)
					loopedFrames = true;
			}
			
			try {
				serverWorld.update(MathUtils.clamp((now - lastNow) / 1E9f, 0, GameCore.MAX_DELTA));
				
				// run any runnables that were posted during the above update
				Runnable[] lastRunnables;
				synchronized (runnables) {
					lastRunnables = runnables.toArray(new Runnable[0]);
					runnables.clear();
				}
				for(Runnable r: lastRunnables)
					r.run(); // any runnables added here will be run next update
				
			} catch(Throwable t) {
				getServer().stop();
				throw t;
			}
			
			lastNow = now;
			
			MyUtils.sleep(10);
		}
		
		commandParser.end();
		serverWorld = null;
	}
	
	// stop the server.
	public static void quit() {
		getWorld().exitWorld();
	}
}
