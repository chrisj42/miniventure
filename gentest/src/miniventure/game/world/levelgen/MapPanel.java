package miniventure.game.world.levelgen;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
			// HashSet<Point> points = new HashSet<>(200_000);
			forEachTile(p -> {});
			// queueTiles(points);
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
		int cnt = 0;
		for(Point p: points) {
			genTile(p);
			cnt++;
			if(cnt > 10_000) {
				repaint();
				cnt %= 10_000;
			}
		}
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
		
		// HashSet<Point> points = new HashSet<>(5_000);
		// int[] i = {0};
		forEachTile(p -> {
			/*if(!tiles.containsKey(p)) {
				// points.add(p);
				return;
			}*/
			// i[0]++;
			bufg.setColor(tiles.get(p));
			bufg.fillRect(getWidth()/2+(p.x-worldOffX)*pixelDensity, getHeight()/2+(p.y-worldOffY)*pixelDensity, pixelDensity, pixelDensity);
		});
		// System.out.println("rendered "+i[0]+" tiles");
		// queueTiles(points);
		
		g.drawImage(buf, 0, 0, null);
	}
	
	private Point getInput() {
		int x = 0, y = 0;
		if(keyPresses.get(KeyEvent.VK_UP))
			y--;
		if(keyPresses.get(KeyEvent.VK_DOWN))
			y++;
		if(keyPresses.get(KeyEvent.VK_LEFT))
			x--;
		if(keyPresses.get(KeyEvent.VK_RIGHT))
			x++;
		
		return new Point(x, y);
	}
	
	private void forEachTile(VoidMonoFunction<Point> action) {
		int zoom = testPanel.getGlobalPanel().zoomField.getValue();
		Point radius = new Point(Math.min(width, getWidth())/2/zoom, Math.min(height, getHeight())/2/zoom);
		
		int spd = testPanel.getGlobalPanel().speedField.getValue();
		float buffer = testPanel.getGlobalPanel().bufferField.getValue();
		Point input = getInput();
		Point bufferRadiusNeg = new Point(radius.x + (int)(spd*buffer*(input.x<0?-input.x:0)), radius.y + (int)(spd*buffer*(input.y<0?-input.y:0)));
		Point bufferRadiusPos = new Point(radius.x + (int)(spd*buffer*(input.x>0?input.x:0)), radius.y + (int)(spd*buffer*(input.y>0?input.y:0)));
		// System.out.println("width = "+getWidth()+" height ="+getHeight());
		// System.out.println("iterating through "+(radius.x*2*radius.y*2)+" tiles");
		HashSet<Point> points = new HashSet<>((bufferRadiusNeg.x+bufferRadiusPos.x)*(bufferRadiusNeg.y+bufferRadiusPos.y)/2);
		for(int x = -bufferRadiusNeg.x; x < bufferRadiusPos.x; x++) {
			for(int y = -bufferRadiusNeg.y; y < bufferRadiusPos.y; y++) {
				Point p = new Point(x + worldOffX, y + worldOffY);
				if(!tiles.containsKey(p))
					points.add(p);
				else if(!(p.x < 0 || p.y < 0 || p.x >= width || p.y >= height) && Math.abs(x) <= radius.x && Math.abs(y) <= radius.y)
					action.act(p);
			}
		}
		queueTiles(points);
	}
	
	private void queueTiles(HashSet<Point> points) {
		if(points.size() > 0) {
			synchronized (genLock) {
				genQueue.addLast(points);
				while(genQueue.size() > testPanel.getGlobalPanel().queueField.getValue())
					genQueue.pollFirst();
			}
		}
	}
	
	private Thread inputThread = null;
	private boolean run = true;
	@Override
	public void run() {
		inputThread = Thread.currentThread();
		// System.out.println("input thread started");
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
		
		// System.out.println("input thread stopped.");
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
			// System.out.println("starting");
			run = true;
			boolean start = false;
			while(run) {
				if(genQueue.size() > 0) {
					if(!start) {
						// System.out.println("starting queue");
						start = true;
					}
					// System.out.println(genQueue.size());
					HashSet<Point> points;
					synchronized (genLock) {
						points = genQueue.pollLast();
					}
					// System.out.println("generating "+points.size()+" tiles");
					genTile(points);
					if(tiles.size() > 2_500_000)
						tiles.clear();
					// System.out.println(tiles.size());
					if(genQueue.size() == 0) {
						// System.out.println("gen queue finished");
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
			
			// System.out.println("finished");
		}
	}
}
