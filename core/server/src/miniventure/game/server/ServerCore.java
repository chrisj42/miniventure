package miniventure.game.server;

import java.io.IOException;
import java.util.*;

import miniventure.game.GameCore;
import miniventure.game.chat.command.CommandInputParser;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.VersionInfo;
import miniventure.game.world.ServerWorld;
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.worldgen.WorldConfig;
import miniventure.game.world.worldgen.WorldConfig.CreationConfig;
import miniventure.game.world.worldgen.WorldConfig.LoadConfig;

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
		List<String> argList = Arrays.asList(args);
		
		LinkedHashMap<String, ArrayList<String>> optionList = new LinkedHashMap<>(argList.size()); // argList size is an absolute max, many args will probably be parameters to the same option
		
		ArrayList<String> curOption = new ArrayList<>();
		optionList.put("", curOption);
		for(String arg: args) {
			if(arg.startsWith("--")) {
				curOption = new ArrayList<>();
				optionList.put(arg.substring(2), curOption);
			}
			else
				curOption.add(arg);
		}
		
		WorldConfig config = null;
		
		for(String option: optionList.keySet()) {
			switch(option) {
				case "": break;
				
				case "debug":
					GameCore.debug = true;
					break;
				
				case "server":
					ArrayList<String> params = optionList.get(option);
					if(params.size() == 0) break;
					
					String worldname = params.get(0);
					if(params.size() >= 3) {
						try {
							int width = Integer.parseInt(params.get(1));
							int height = Integer.parseInt(params.get(2));
							config = new CreationConfig(worldname, width, height, new Random().nextLong());
						} catch(NumberFormatException ex) {
							System.err.println("world size parameters to option '--server' are invalid");
						}
					}
					else if(params.size() == 2)
						System.err.println("option '--server' expects 1 or 3 parameters.");
					else
						config = new LoadConfig(worldname);
					
					break;
				
				default:
					System.err.println("unrecognized option: --"+option);
			}
		}
		
		// int sizeIdx = argList.indexOf("--server") + 1;
		// boolean success = sizeIdx > 0 && args.length > sizeIdx+2;
		
		// if(argList.contains("--debug"))
		// 	GameCore.debug = true;
		
		/*if(success) {
			try {
				int width = Integer.parseInt(args[sizeIdx]);
				int height = Integer.parseInt(args[sizeIdx+1]);
				
				if(width == 0) width = GameCore.DEFAULT_WORLD_SIZE;
				if(height == 0) height = GameCore.DEFAULT_WORLD_SIZE;
				
				
			} catch(NumberFormatException ex) {
				success = false;
			}
		}*/
		
		if(config == null) {
			System.out.println("Usage: miniventure.server.ServerCore [--create] worldname");
			System.out.println("    Specify '--create' to make a new world, omit it to load.");
		}
		else {
			
			GameCore.initNonGdxTextures();
			ServerTileType.init();
			
			System.out.println("loading server world...");
			
			initServer(config, true);
			
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
	}
	
	public static boolean initServer(WorldConfig config, boolean standalone) {
		if(serverWorld != null)
			serverWorld.exitWorld();
		
		try {
			serverWorld = new ServerWorld(standalone);
		} catch(IOException e) {
			e.printStackTrace();
			serverWorld = null;
			return false;
		}
		serverWorld.createWorld(config);
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
