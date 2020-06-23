package miniventure.game.network;

import java.util.LinkedList;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.ValueAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacketPipe {
	
	@FunctionalInterface
	public interface PacketHandler extends ValueAction<Object> {
		default void onDisconnect() {}
	}
	
	// this is the list of packets that have been "sent" and have yet to be processed.
	private final LinkedList<Object> packetQueue;
	// A locking object for the new thread
	private final Object lock = new Object();
	
	private boolean running = false; // whether the packet pipe is running and processing packets / is ready to process packets
	
	private final PacketPipeReader reader;
	private final PacketPipeWriter writer;
	
	public PacketPipe() { this(null); }
	public PacketPipe(@Nullable String readerThreadLabel) {
		packetQueue = new LinkedList<>(); // I need pollFirst, so I can't use collections synchronization.
		
		reader = new PacketPipeReader(readerThreadLabel);
		writer = new PacketPipeWriter();
	}
	
	public PacketPipeReader getPipeReader() { return reader; }
	public PacketPipeWriter getPipeWriter() { return writer; }
	
	/// below are classes to restrict access to the pipe, so one end may be passed to part of the code without letting that code use the other end too.
	
	// simple way to define methods common to both ends in one place.
	private class PacketPipeInterface {
		public void close() {
			running = false;
			synchronized (lock) {
				// wake up the reader thread so that it can realize it should shut down
				lock.notify();
			}
		}
		
		public boolean isOpen() {
			return running;
		}
		
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
			synchronized (lock) {
				packetQueue.add(packet);
				// Start the thread again to read a new packet
				lock.notify();
			}
		}
	}
	
	// can only read/listen through this end
	public class PacketPipeReader extends PacketPipeInterface implements Runnable {
		
		@Nullable
		private final String readerThreadLabel;
		
		@NotNull
		private PacketHandler listener = obj -> {};
		
		PacketPipeReader(@Nullable String readerThreadLabel) {
			this.readerThreadLabel = readerThreadLabel;
		}
		
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
			MyUtils.debug("Starting pipe flow: "+readerThreadLabel);
			
			while(running) {
				Object packet;
				synchronized (lock) {
					packet = packetQueue.pollFirst();
					if(packet == null) {
						// Wait until a new packet
						// to avoid executing the thread yet
						try {
							lock.wait();
						} catch (InterruptedException ignored) {
						}
						continue;
					}
				}
				
				// GameCore.debug("PacketPipeReader \""+readerThreadLabel+"\" got packet: "+packet.getClass().getSimpleName());
				listener.act(packet);
			}
			
			listener.onDisconnect();
			
			MyUtils.debug("Pipe ending: "+readerThreadLabel);
		}
	}
}
