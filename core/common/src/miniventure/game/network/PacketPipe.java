package miniventure.game.network;

import java.util.LinkedList;

import miniventure.game.core.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.ValueAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacketPipe {
	
	private static final int MS_EMPTY_LOOP_INTERVAL = 50; // if the reader thread runs through the loop and finds no packets to process less than this many milliseconds since the last empty pass, it will suspend the thread to reach this amount of time.
	
	@FunctionalInterface
	public interface PacketHandler extends ValueAction<Object> {
		default void onDisconnect() {}
	}
	
	// this is the list of packets that have been "sent" and have yet to be processed.
	private final LinkedList<Object> packetQueue;
	
	private boolean canExit = false; // a note that it is okay to stop checking for packets once the current ones are dealt with.
	private boolean running = false; // there are no more packets; at least, no more packets should be parsed. This is set to true if canExit is true and there are no packets in the queue, however it can be set directly if finishing the queue is not necessary.
	
	private final PacketPipeReader reader;
	private final PacketPipeWriter writer;
	
	@Nullable
	private final String readerThreadLabel;
	
	public PacketPipe() { this(null); }
	public PacketPipe(@Nullable String readerThreadLabel) {
		this.readerThreadLabel = readerThreadLabel;
		
		packetQueue = new LinkedList<>(); // I need pollFirst, so I can't use collections synchronization.
		
		reader = new PacketPipeReader();
		writer = new PacketPipeWriter();
	}
	
	public PacketPipeReader getPipeReader() { return reader; }
	public PacketPipeWriter getPipeWriter() { return writer; }
	
	
	private void closePipe(boolean finishPackets) {
		canExit = true;
		running = running && finishPackets;
	}
	
	
	/// below are classes to restrict access to the pipe, so one end may be passed to part of the code without letting that code use the other end too.
	
	// simple way to define methods common to both ends in one place.
	private class PacketPipeInterface {
		public void close(boolean finishPackets) {
			closePipe(finishPackets);
		}
		
		public boolean isOpen() { return running; }
		
		@Override
		public String toString() {
			String type = getClass().getSimpleName().replace("PacketPipe", "");
			return type+" for "+PacketPipe.this.toString();
		}
	}
	
	// can only send through this end
	public class PacketPipeWriter extends PacketPipeInterface {
		public void send(Object packet) {
			// GameCore.debug("sending packet from pipe: "+readerThreadLabel+"; packet type: "+packet.getClass().getSimpleName());
			synchronized (packetQueue) {
				packetQueue.add(packet);
			}
		}
	}
	
	// can only read/listen through this end
	public class PacketPipeReader extends PacketPipeInterface implements Runnable {
		
		@NotNull private PacketHandler listener = obj -> {};
		
		public void setListener(@NotNull PacketHandler handler) {
			this.listener = handler;
		}
		
		public void start() {
			if(readerThreadLabel != null)
				new Thread(this, readerThreadLabel).start();
			else
				new Thread(this).start();
		}
		
		@Override
		public void run() {
			running = true;
			canExit = false;
			long lastWait = System.nanoTime();
			GameCore.debug("Starting pipe flow: "+readerThreadLabel);
			while(running) {
				Object packet;
				synchronized (packetQueue) {
					packet = packetQueue.pollFirst();
				}
				
				if(packet == null) {
					if(canExit)
						running = false;
					else {
						long time = System.nanoTime();
						double msSinceLastWait = (lastWait - time) / 1E6D;
						if(msSinceLastWait < MS_EMPTY_LOOP_INTERVAL)
							MyUtils.sleep(MS_EMPTY_LOOP_INTERVAL - (int) msSinceLastWait);
					}
					lastWait = System.nanoTime();
					continue;
				}
				
				// GameCore.debug("PacketPipeReader \""+readerThreadLabel+"\" got packet: "+packet.getClass().getSimpleName());
				listener.act(packet);
			}
			
			listener.onDisconnect();
			
			GameCore.debug("Pipe ending: "+readerThreadLabel);
		}
	}
}
