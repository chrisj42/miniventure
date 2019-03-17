package miniventure.game.server;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.chat.command.CommandInputParser;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.VersionInfo;
import miniventure.game.world.SaveLoadInterface;
import miniventure.game.world.SaveLoadInterface.WorldDataSet;
import miniventure.game.world.ServerWorld;
import miniventure.game.world.tile.ServerTileType;

import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.NotNull;

public class ServerCore {
	
	private static final ServerWorld serverWorld = new ServerWorld();
	public static InetSocketAddress host = null;
	
	private static CommandInputParser commandParser = null;
	
	@NotNull
	public static ServerWorld getWorld() { return serverWorld; }
	public static GameServer getServer() { return serverWorld.getServer(); }
	public static CommandInputParser getCommandInput() { return commandParser; }
	
	public static void main(String[] args) throws IOException {
		args = ArrayUtils.mapArray(args, String.class, String::toLowerCase);
		LinkedList<String> arglist = new LinkedList<>(Arrays.asList(args));
		
		String worldname = null;
		boolean create = false;
		boolean overwrite = false;
		String seedString = null;
		int port = GameProtocol.PORT;
		
		while(arglist.size() > 0) {
			String arg = arglist.pop();
			switch(arg) {
				case "--create": create = true; break;
				
				case "--overwrite": overwrite = true; break;
				
				case "--port":
					if(arglist.size() == 0) {
						System.out.println("error: --port option requires an argument.");
						return;
					}
					try {
						port = Integer.parseInt(arglist.pop());
					} catch(NumberFormatException e) {
						System.out.println("error: --port argument is invalid; must be an integer.");
						return;
					}
					break;
				
				case "--seed":
					if(arglist.size() == 0) {
						System.out.println("error: --seed option requires an argument.");
						return;
					}
					seedString = arglist.pop();
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
		Path world = SaveLoadInterface.getLocation(worldname);
		System.out.println("looking for worlds in: "+SaveLoadInterface.getLocation("").toAbsolutePath());
		boolean exists = Files.exists(world);
		
		if(!exists && !create) {
			// doesn't exist but didn't say create; prompt for creation
			create = prompt("world \""+worldname+"\" does not exist. Create it? (y/n) ").equals("y");
			
			if(!create)
				System.out.println("world not created.");
		}
		
		if(create && exists) {
			// prompt for overwrite
			if(!overwrite)
				overwrite = prompt("world \""+worldname+"\" already exists. Are you sure you want to overwrite it? type \"yes\" to overwrite. (yes/no) ").equals("yes");
			
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
			Files.createDirectories(world);
		}
		
		RandomAccessFile lockHolder = SaveLoadInterface.tryLockWorld(world);
		if(lockHolder == null) {
			System.err.println("Failed to acquire world lock; is it currently loaded by another instance?");
			return;
		}
		
		// lock acquired, we can now be reasonably sure it's going to work out, so let's initialize everything.
		
		GameCore.initNonGdxTextures();
		ServerTileType.init();
		
		System.out.println("loading server world...");
		
		WorldDataSet worldInfo;
		
		if(load) // LOAD
			worldInfo = SaveLoadInterface.loadWorld(world, lockHolder);
		else // CREATE
			worldInfo = SaveLoadInterface.createWorld(world, lockHolder, seedString);
		
		if(worldInfo == null) {
			System.err.println("Error occurred during world load, world init failed.");
			return;
		}
		
		serverWorld.loadWorld(port, true, worldInfo);
		
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
	
	// returns port server is running on.
	public static int initSinglePlayer(WorldDataSet worldInfo) throws IOException {
		int tries = 0;
		int port = GameProtocol.PORT;
		while(true) {
			try {
				serverWorld.loadWorld(port, false, worldInfo);
				return port;
			} catch(IOException e) {
				if(tries == 0)
					e.printStackTrace();
				tries++;
				if(tries > 10)
					throw new IOException("Failed to find valid port for internal server.", e);
				else
					port += MathUtils.random(1, 10);
			}
		}
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
		/*if(ask != null) {
			ask.close();
			ask = null;
		}*/
		
		// start scanner thread
		if(commandParser != null)
			commandParser.end();
		
		commandParser = new CommandInputParser();
		new Thread(commandParser, "CommandInputParser").start();
		
		Arrays.fill(frameTimes, 0);
		
		long lastNow = System.nanoTime();
		long lastInterval = lastNow;
		
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
	}
	
	// stop the server.
	public static void quit() {
		serverWorld.exitWorld();
	}
}
