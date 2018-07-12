package miniventure.game.world.levelgen;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.VoidMonoFunction;
import miniventure.game.world.Point;
import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.tile.TileType.TileTypeEnum;

public class MapPanel extends MyPanel implements Runnable {
	
	private final TestPanel testPanel;
	// private boolean gen = false;
	private final JLabel msgLabel;
	private int width, height;
	private int worldOffX, worldOffY;
	private HashMap<Point, Color> tiles;
	
	private HashMap<Integer, Boolean> keyPresses;
	
	public MapPanel(TestPanel testPanel) {
		this.testPanel = testPanel;
		
		width = testPanel.getGlobalPanel().widthField.getValue();
		height = testPanel.getGlobalPanel().heightField.getValue();
		
		if(width <= 0) width = LevelGenerator.MAX_WORLD_SIZE;
		if(height <= 0) height = LevelGenerator.MAX_WORLD_SIZE;
		
		tiles = new HashMap<>(Math.min(100_000, width*height));
		
		keyPresses = new HashMap<>(4);
		keyPresses.put(KeyEvent.VK_UP, false);
		keyPresses.put(KeyEvent.VK_DOWN, false);
		keyPresses.put(KeyEvent.VK_LEFT, false);
		keyPresses.put(KeyEvent.VK_RIGHT, false);
		
		addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) { keyPresses.computeIfPresent(e.getKeyCode(), (k, v) -> true); }
			@Override public void keyReleased(KeyEvent e) { keyPresses.computeIfPresent(e.getKeyCode(), (k, v) -> false); }
			@Override public void keyTyped(KeyEvent e) {}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				requestFocus();
			}
		});
		
		addFocusListener(new FocusListener() {
			@Override public void focusGained(FocusEvent e) {
				run = true;
				if((inputThread == null || !inputThread.isAlive())/* && (width >= getWidth() || height >= getHeight())*/)
					new Thread(MapPanel.this).start();
				if((loader == null || !loader.isAlive()))
					new Thread(new TileLoader()).start();
			}
			@Override
			public void focusLost(FocusEvent e) {
				// gen = false;
				// msgLabel.setText("click regen world button to restart generation.");
				// msgLabel.setVisible(true);
				run = false;
				revalidate();
			}
		});
		
		setFocusable(true);
		
		/*addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				run = false;
				if(inputThread != null) {
					try {
						inputThread.join();
					} catch(InterruptedException ignored) {
					}
				}
				run = true;
				if(width >= getWidth() || height >= getHeight())
					new Thread(MapPanel.this).start();
			}
		});*/
		
		msgLabel = new JLabel("click regen world");
		msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
		setLayout(new GridLayout(1, 1));
		add(msgLabel);
		System.out.println(LevelGenerator.MAX_WORLD_SIZE*LevelGenerator.MAX_WORLD_SIZE);
	}
	
	public void generate(int worldWidth, int worldHeight) {
		tiles.clear();
		//gen = false;
		int width = worldWidth <= 0 ? LevelGenerator.MAX_WORLD_SIZE : worldWidth;
		int height = worldHeight <= 0 ? LevelGenerator.MAX_WORLD_SIZE : worldHeight;
		for(NoiseFunctionEditor editor: testPanel.getNoiseFunctionPanel().getElements())
			editor.getNoiseFunction().resetFunction();
		msgLabel.setText("Generating tiles");
		msgLabel.setVisible(true);
		repaint();
		SwingUtilities.invokeLater(() -> {
			this.width = width;
			this.height = height;
			worldOffX = width/2;
			worldOffY = height/2;
			HashSet<Point> points = new HashSet<>(200_000);
			forEachTile(p -> points.add(p));
			if(points.size() > 0) {
				synchronized (genLock) {
					genQueue.addLast(points);
				}
			}
			// gen = true;
			msgLabel.setVisible(false);
			requestFocus();
			repaint();
			run = true;
			new Thread(MapPanel.this).start();
			new Thread(new TileLoader()).start();
		});
	}
	
	private void genTile(HashSet<Point> points) {
		for(Point p: points)
			genTile(p);
	}
	private void genTile(Point p) { genTile(p.x, p.y); }
	private void genTile(int x, int y) {
		TileTypeEnum type = testPanel.getNoiseMapperPanel().getElements()[0].getNoiseMap().getTileType(x, y);
		tiles.put(new Point(x, y), type.color);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Image buf = createImage(getWidth(), getHeight());
		Graphics bufg = buf.getGraphics();
		bufg.setColor(getBackground());
		bufg.fillRect(0, 0, getWidth(), getHeight());
		bufg.setColor(getForeground());
		final int pixelDensity = testPanel.getGlobalPanel().zoomField.getValue();
		
		HashSet<Point> points = new HashSet<>();
		// int[] i = {0};
		forEachTile(p -> {
			if(!tiles.containsKey(p)) {
				points.add(p);
				return;
			}
			// i[0]++;
			bufg.setColor(tiles.get(p));
			bufg.fillRect(getWidth()/2+(p.x-worldOffX)*pixelDensity, getHeight()/2+(p.y-worldOffY)*pixelDensity, pixelDensity, pixelDensity);
		});
		// System.out.println("rendered "+i[0]+" tiles");
		if(points.size() > 0) {
			synchronized (genLock) {
				genQueue.addLast(points);
			}
		}
		
		g.drawImage(buf, 0, 0, null);
	}
	
	private void forEachTile(VoidMonoFunction<Point> action) {
		Point radius = new Point(Math.min(width, getWidth())/2, Math.min(height, getHeight())/2);
		// System.out.println("width = "+getWidth()+" height ="+getHeight());
		// System.out.println("iterating through "+(radius.x*2*radius.y*2)+" tiles");
		for(int x = -radius.x; x < radius.x; x++) {
			for(int y = -radius.y; y < radius.y; y++) {
				Point p = new Point(x + worldOffX, y + worldOffY);
				if(!(p.x < 0 || p.y < 0 || p.x >= width || p.y >= height))
					action.act(p);
			}
		}
	}
	
	private Thread inputThread = null;
	private boolean run = true;
	@Override
	public void run() {
		inputThread = Thread.currentThread();
		System.out.println("input thread started");
		// int delay = 0;
		while(run) {
			MyUtils.sleep(60);
			// delay += 60;
			
			// if(width < getWidth() && height < getHeight())
			// 	break;
			
			int worldOffX = this.worldOffX;
			int worldOffY = this.worldOffY;
			int spd = testPanel.getGlobalPanel().speedField.getValue();
			
			if(keyPresses.get(KeyEvent.VK_UP) && height >= getHeight() && worldOffY > 0)
				worldOffY = Math.max(0, worldOffY - spd);
			if(keyPresses.get(KeyEvent.VK_DOWN) && height >= getHeight() && worldOffY < height)
				worldOffY = Math.min(height, worldOffY + spd);
			if(keyPresses.get(KeyEvent.VK_LEFT) && width >= getWidth() && worldOffX > 0)
				worldOffX = Math.max(0, worldOffX - spd);
			if(keyPresses.get(KeyEvent.VK_RIGHT) && width >= getWidth() && worldOffX < width)
				worldOffX = Math.min(width, worldOffX + spd);
			
			if(this.worldOffX != worldOffX || this.worldOffY != worldOffY) {
				// System.out.println("button pressed");
				this.worldOffX = worldOffX;
				this.worldOffY = worldOffY;
				// System.out.println("moved to "+worldOffX+','+worldOffY);
				repaint();
			}
			/*if(delay > 500) {
				delay %= 500;
			}*/
		}
		
		System.out.println("input thread stopped.");
	}
	
	private final LinkedList<HashSet<Point>> genQueue = new LinkedList<>();
	private final Object genLock = new Object();
	private Thread loader = null;
	
	private class TileLoader implements Runnable {
		
		@Override
		public void run() {
			if(loader != null) {
				run = false;
				try {
					loader.join();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				run = true;
			}
			loader = Thread.currentThread();
			System.out.println("starting");
			run = true;
			boolean start = false;
			while(run) {
				if(genQueue.size() > 0) {
					if(!start) {
						System.out.println("starting queue");
						start = true;
					}
					System.out.println(genQueue.size());
					HashSet<Point> points;
					synchronized (genLock) {
						points = genQueue.pollFirst();
					}
					System.out.println("generating "+points.size()+" tiles");
					genTile(points);
					if(genQueue.size() == 0) {
						System.out.println("gen queue finished");
						start = false;
					}
					// 	fin = true;
					repaint();
					// }
					// if(fin)
					// 	MyUtils.sleep(10);
				}
				else
					MyUtils.sleep(20);
			}
			
			System.out.println("finished");
		}
	}
}
