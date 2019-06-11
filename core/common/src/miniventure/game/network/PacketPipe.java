package miniventure.game.network;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import miniventure.game.util.function.ValueAction;

import org.jetbrains.annotations.Nullable;

public class PacketPipe {
	
	@FunctionalInterface
	public interface PacketHandler extends ValueAction<Object> {
		default void onDisconnect() {}
	}
	
	// this is the list of packets that have been "sent" and have yet to be processed.
	private final LinkedList<Object> packetQueue;
	
	// this is the list of actions that should be called for every packet
	private final List<PacketHandler> listeners;
	
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
		listeners = Collections.synchronizedList(new LinkedList<>());
		
		reader = new PacketPipeReader();
		writer = new PacketPipeWriter();
	}
	
	public PacketPipeReader getPipeReader() { return reader; }
	public PacketPipeWriter getPipeWriter() { return writer; }
	
	
	private void send(Object obj) {
		synchronized (packetQueue) {
			packetQueue.add(obj);
		}
	}
	
	private void addListener(PacketHandler handler) {
		listeners.add(handler);
	}
	
	private void closePipe(boolean finishPackets) {
		canExit = false;
		running = finishPackets;
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
			PacketPipe.this.send(packet);
		}
	}
	
	// can only read/listen through this end
	public class PacketPipeReader extends PacketPipeInterface implements Runnable {
		
		public void addListener(PacketHandler handler) {
			PacketPipe.this.addListener(handler);
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
			while(running) {
				Object packet;
				synchronized (packetQueue) {
					packet = packetQueue.pollFirst();
				}
				
				if(packet == null) {
					if(canExit)
						running = false;
					continue;
				}
				
				synchronized (listeners) {
					for(PacketHandler handler: listeners)
						handler.act(packet);
				}
			}
			
			synchronized (listeners) {
				for(PacketHandler handler: listeners)
					handler.onDisconnect();
			}
		}
	}
}
