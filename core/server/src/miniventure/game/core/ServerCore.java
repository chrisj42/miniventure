package miniventure.game.core;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import miniventure.game.util.ProgressPrinter;
import miniventure.game.chat.command.CommandInputParser;
import miniventure.game.network.GameProtocol;
import miniventure.game.network.GameServer;
import miniventure.game.network.NetworkServer;
import miniventure.game.network.ServerFetcher;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.ProgressLogger;
import miniventure.game.util.Version;
import miniventure.game.util.VersionInfo;
import miniventure.game.world.file.WorldDataSet;
import miniventure.game.world.file.WorldFileInterface;
import miniventure.game.world.file.WorldFormatException;
import miniventure.game.world.file.WorldReference;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.ServerTileType;

import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.NotNull;

public class ServerCore extends Thread {
	
	@NotNull private final ServerWorld serverWorld;
	@NotNull private final CommandInputParser commandParser;
	private Thread updateThread;
	
	private final float[] frameTimes = new float[20];
	private final int FRAME_INTERVAL = 30; // how many frames are in each time (above)
	private int timeIdx = 0, frameIdx = 0;
	private boolean loopedFrames = false;
	private final Object fpsLock = new Object();
	
	public ServerCore(@NotNull ServerFetcher serverFetcher, @NotNull WorldDataSet worldInfo, ProgressLogger logger) throws IOException {
		super(new ThreadGroup("server"), "Miniventure Server");
		serverWorld = new ServerWorld(this, serverFetcher, worldInfo, logger);
		commandParser = new CommandInputParser(serverWorld);
	}
	
	@Override
	public void run() {
		MyUtils.debug("ServerCore starting up");
		updateThread = Thread.currentThread();
		
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
				serverWorld.update(MathUtils.clamp(delta, 0, MyUtils.MAX_DELTA));
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
		MyUtils.debug("ServerCore ending");
	}
	
	@NotNull
	public ServerWorld getWorld() { return serverWorld; }
	@NotNull
	public GameServer getServer() { return getWorld().getServer(); }
	
	public boolean isUpdateThread() { return Thread.currentThread() == updateThread; }
	
	public boolean isRunning() { return updateThread != null && updateThread.isAlive(); }
	
	public float getFPS() {
		synchronized (fpsLock) {
			float totalTime = 0;
			for(float duration : frameTimes)
				totalTime += duration;
			
			if(totalTime == 0) return 0;
			
			return ((loopedFrames ? frameTimes.length : timeIdx) * FRAME_INTERVAL) / totalTime;
		}
	}
	
	// creates a headless dedicated server
	public static void initHeadless(String[] args) throws IOException {
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
		
		Path oldGameDir = WorldFileInterface.getDataImportSource();
		if(oldGameDir != null) {
			if(prompt("The default save location for miniventure files has changed since the previous version, so your files have been copied to the new location. Do you wish to delete the old save location? Older versions will lose their data. (y/n): ").equals("y")) {
				if(WorldFileInterface.deleteRecursively(oldGameDir))
					System.out.println("Old save location has been deleted.");
			}
			else
				System.out.println("Old save location will be ignored.");
		}
		
		// check for an existing save with the given name
		System.out.println("looking for worlds in: "+ WorldFileInterface.getLocation("").toAbsolutePath());
		Path worldPath = WorldFileInterface.getLocation(worldname);
		boolean exists = Files.exists(worldPath);
		
		if(!exists && !create) {
			// doesn't exist but didn't say create; prompt for creation
			create = prompt("world \""+worldname+"\" does not exist. Create it? (y/n): ").equals("y");
			
			if(!create)
				System.out.println("world not created.");
		}
		
		if(create && exists) {
			// prompt for overwrite
			if(!overwrite)
				overwrite = prompt("world \""+worldname+"\" already exists. Are you sure you want to overwrite it? type \"yes\" to overwrite. (yes/no): ").equals("yes");
			
			if(!overwrite)
				System.out.println("Not overwriting; world not created.");
		}
		
		boolean make = create && (!exists || overwrite);
		boolean load = !create && exists;
		
		if(!make && !load) {
			// no action.
			return;
		}
		
		RandomAccessFile lockHolder = WorldFileInterface.tryLockWorld(worldPath);
		if(lockHolder == null) {
			System.err.println("Failed to acquire world lock; is it currently loaded by another instance?");
			return;
		}
		
		// lock acquired, we can now be reasonably sure it's going to work out, so let's initialize everything.
		
		GameCore.initNonGdxTextures();
		// GenericEnum.init();
		ServerTileType.init();
		
		System.out.println("loading server world...");
		
		WorldDataSet worldInfo;
		
		if(load) { // LOAD
			WorldReference worldRef = new WorldReference(worldPath);
			try {
				worldInfo = WorldFileInterface.loadWorld(worldRef, lockHolder);
			} catch(WorldFormatException e) {
				System.err.println(MyUtils.combineThrowableCauses(e, "Error occurred during world load"));
				return;
			}
		}
		else // CREATE
			worldInfo = WorldFileInterface.createWorld(worldPath, lockHolder, seedString);
		
		final int portf = port;
		ServerCore core = new ServerCore((world, pdata) -> new NetworkServer(world, portf, addr -> false, pdata), worldInfo, new ProgressPrinter());
		
		System.out.println("server ready");
		if(!GameCore.determinedLatestVersion())
			System.out.println("Checking for newer versions...");
		VersionInfo info = GameCore.getLatestVersion();
		if(info.version.compareTo(Version.CURRENT) > 0) {
			// there's a newer version
			System.out.println("Newer game version found: "+info.version+". Download the jar file here: "+info.assetUrl);
		}
		//noinspection CallToThreadRun
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
