package miniventure.game.server;

import miniventure.game.GameCore;
import miniventure.game.util.command.CommandInputParser;

public class ServerCore {
	
	private static ServerWorld serverWorld;
	
	public static ServerWorld getWorld() { return serverWorld; }
	public static GameServer getServer() { return serverWorld.getServer(); }
	
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
			System.out.println("\tspecify 0 for width and/or height to use the maximum value for that dimension.");
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
	
	public static void run() {
		// start scanner thread
		new CommandInputParser().start();
		
		long lastNow = System.nanoTime();
		
		//noinspection InfiniteLoopStatement
		while(true) {
			long now = System.nanoTime();
			
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
}
