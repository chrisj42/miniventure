package miniventure.game.server;

import java.util.Arrays;

import miniventure.game.GameCore;
import miniventure.game.chat.command.CommandInputParser;

public class ServerCore {
	
	private static ServerWorld serverWorld;
	
	private static CommandInputParser commandParser = new CommandInputParser();
	
	public static ServerWorld getWorld() { return serverWorld; }
	public static GameServer getServer() { return serverWorld.getServer(); }
	public static CommandInputParser getCommandInput() { return commandParser; }
	
	public static void main(String[] args) {
		boolean success = args.length == 3 && args[0].equalsIgnoreCase("--server");
		
		if(success) {
			GameCore.initNonGdx();
			
			System.out.println("loading server world...");
			try {
				initServer(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
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
			run();
		}
	}
	
	public static void initServer(int width, int height) {
		if(serverWorld != null)
			serverWorld.exitWorld();
		
		serverWorld = new ServerWorld();
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
		commandParser.start();
		
		Arrays.fill(frameTimes, 0);
		
		long lastNow = System.nanoTime();
		long lastInterval = lastNow;
		
		//noinspection InfiniteLoopStatement
		while(true) {
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
			
			try {
				Thread.sleep(10);
			} catch(InterruptedException ignored) {}
		}
	}
	
	// stop the server.
	public static void quit() {
		getWorld().exitWorld(true);
		getServer().stop();
		System.exit(0);
	}
}
