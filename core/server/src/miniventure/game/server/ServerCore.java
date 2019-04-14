package miniventure.game.server;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.ProgressPrinter;
import miniventure.game.chat.command.CommandInputParser;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.ProgressLogger;
import miniventure.game.util.VersionInfo;
import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.world.management.WorldFileInterface;
import miniventure.game.world.management.WorldDataSet;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.management.WorldFormatException;
import miniventure.game.world.management.WorldReference;
import miniventure.game.world.tile.ServerTileType;

import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.NotNull;

public class ServerCore implements Runnable {
	
	@NotNull private final ServerWorld serverWorld;
	@NotNull private final CommandInputParser commandParser;
	private Thread thread;
	
	private final float[] frameTimes = new float[20];
	private final int FRAME_INTERVAL = 30; // how many frames are in each time (above)
	private int timeIdx = 0, frameIdx = 0;
	private boolean loopedFrames = false;
	private final Object fpsLock = new Object();
	
	private ServerCore(int port, boolean multiplayer, WorldDataSet worldInfo, ProgressLogger logger) throws IOException {
		serverWorld = new ServerWorld(this, port, multiplayer, worldInfo, logger);
		commandParser = new CommandInputParser(serverWorld);
	}
	
	@Override
	public void run() {
		thread = Thread.currentThread();
		// start command parser thread
		new Thread(commandParser, "CommandInputParser").start();
		
		Arrays.fill(frameTimes, 0);
		
		long prevStartTime = System.nanoTime();
		long lastInterval = prevStartTime;
		
		while(serverWorld.worldLoaded()) {
			long frameStartTime = System.nanoTime();
			
			synchronized (fpsLock) {
				frameIdx = (frameIdx + 1) % FRAME_INTERVAL;
				if(frameIdx == 0) {
					frameTimes[timeIdx] = (float) ((frameStartTime - lastInterval) / 1E9D);
					lastInterval = frameStartTime;
					timeIdx = (timeIdx + 1) % frameTimes.length;
					if(timeIdx == 0)
						loopedFrames = true;
				}
			}
			
			final float delta = (frameStartTime - prevStartTime) / 1E9f;
			
			if(delta * 1000 < 10)
				MyUtils.sleep(10 - (int)(delta*1000));
			
			try {
				serverWorld.update(MathUtils.clamp(delta, 0, GameCore.MAX_DELTA));
			} catch(Throwable t) {
				try {
					getServer().stop(false);
					commandParser.end();
				} catch(Throwable t2) {
					System.err.println("exception while attempting to clean up after a previous exception during server world update:");
					t2.printStackTrace();
				}
				throw t;
			}
			
			prevStartTime = frameStartTime;
		}
		
		commandParser.end();
	}
	
	@NotNull
	public ServerWorld getWorld() { return serverWorld; }
	@NotNull
	public GameServer getServer() { return getWorld().getServer(); }
	
	public boolean isUpdateThread() { return Thread.currentThread() == thread; }
	
	public boolean isRunning() { return thread != null && thread.isAlive(); }
	
	public float getFPS() {
		synchronized (fpsLock) {
			float totalTime = 0;
			for(float duration : frameTimes)
				totalTime += duration;
			
			if(totalTime == 0) return 0;
			
			return ((loopedFrames ? frameTimes.length : timeIdx) * FRAME_INTERVAL) / totalTime;
		}
	}
	
	
	// single player server
	public static ServerCore initSinglePlayer(WorldDataSet worldInfo, ProgressLogger logger) throws IOException {
		int tries = 0;
		int port = GameProtocol.PORT;
		while(true) {
			try {
				return new ServerCore(port, false, worldInfo, logger);
			} catch(IOException e) {
				if(tries == 0)
					e.printStackTrace();
				tries++;
				if(tries > 10)
					throw new IOException("Failed to find valid port for internal server after "+tries+" tries. Ensure you have port "+GameProtocol.PORT+" or subsequent ports available.", e);
				else
					port++;// += MathUtils.random(1, 10);
			}
		}
	}
	
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
		Path world = WorldFileInterface.getLocation(worldname);
		System.out.println("looking for worlds in: "+ WorldFileInterface.getLocation("").toAbsolutePath());
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
		
		RandomAccessFile lockHolder = WorldFileInterface.tryLockWorld(world);
		if(lockHolder == null) {
			System.err.println("Failed to acquire world lock; is it currently loaded by another instance?");
			return;
		}
		
		// lock acquired, we can now be reasonably sure it's going to work out, so let's initialize everything.
		
		GameCore.initNonGdxTextures();
		GenericEnum.init();
		ServerTileType.init();
		
		System.out.println("loading server world...");
		
		WorldDataSet worldInfo;
		
		if(load) { // LOAD
			WorldReference worldRef = new WorldReference(world);
			try {
				worldInfo = WorldFileInterface.loadWorld(worldRef, lockHolder);
			} catch(WorldFormatException e) {
				System.err.println(MyUtils.combineThrowableCauses(e, "Error occurred during world load"));
				return;
			}
		}
		else // CREATE
			worldInfo = WorldFileInterface.createWorld(world, lockHolder, seedString);
		
		ServerCore core = new ServerCore(port, true, worldInfo, new ProgressPrinter());
		
		System.out.println("server ready");
		if(!GameCore.determinedLatestVersion())
			System.out.println("Checking for newer versions...");
		VersionInfo info = GameCore.getLatestVersion();
		if(info.version.compareTo(GameCore.VERSION) > 0) {
			// there's a newer version
			System.out.println("Newer game version found: "+info.version+". Download the jar file here: "+info.assetUrl);
		}
		core.run();
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
	
}
