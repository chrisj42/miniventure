package miniventure.game.server;

import java.util.Arrays;
import java.util.List;

import miniventure.game.GameCore;
import miniventure.game.chat.command.CommandInputParser;
import miniventure.game.util.MyUtils;
import miniventure.game.util.VersionInfo;

public class ServerCore {
	
	private static ServerWorld serverWorld;
	
	private static CommandInputParser commandParser = null;
	
	public static ServerWorld getWorld() { return serverWorld; }
	public static GameServer getServer() { return serverWorld.getServer(); }
	public static CommandInputParser getCommandInput() { return commandParser; }
	
	public static void main(String[] args) {
		args = MyUtils.mapArray(args, String.class, String::toLowerCase);
		List<String> argList = Arrays.asList(args);
		
		int sizeIdx = argList.indexOf("--server") + 1;
		boolean success = sizeIdx > 0 && args.length > sizeIdx+1;
		
		if(argList.contains("--debug"))
			GameCore.debug = true;
		
		if(success) {
			try {
				int width = Integer.parseInt(args[sizeIdx]);
				int height = Integer.parseInt(args[sizeIdx+1]);
				
				GameCore.initNonGdx();
				
				System.out.println("loading server world...");
				
				initServer(width, height, true);
			} catch(NumberFormatException ex) {
				success = false;
			}
		}
		
		if(!success) {
			System.out.println("Usage: miniventure.server.ServerCore --server <world width> <world height>");
			System.out.println("    specify 0 for width and/or height to use the maximum value for that dimension.");
		}
		else {
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
	
	public static void initServer(int width, int height, boolean standalone) {
		if(serverWorld != null)
			serverWorld.exitWorld();
		
		serverWorld = new ServerWorld(standalone);
		serverWorld.createWorld(width, height);
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
				serverWorld.update((now - lastNow) / 1E9f);
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
		getWorld().exitWorld(true);
	}
}
