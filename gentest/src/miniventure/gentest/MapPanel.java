package miniventure.gentest;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
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
import miniventure.game.util.function.VoidBiFunction;
import miniventure.game.world.Point;
import miniventure.game.world.levelgen.LevelGenerator;
import miniventure.game.world.tile.TileType.TileTypeEnum;

public class MapPanel extends JPanel implements Runnable {
	
	private final TestPanel testPanel;
	private final JLabel msgLabel;
	private int width, height;
	private int worldOffX, worldOffY;
	private HashMap<Point, Color> tiles;
	private boolean mapsValid;
	
	private HashSet<Integer> keyPresses;
	
	public MapPanel(TestPanel testPanel) {
		this.testPanel = testPanel;
		
		width = testPanel.getGlobalPanel().widthField.getValue();
		height = testPanel.getGlobalPanel().heightField.getValue();
		
		if(width <= 0) width = LevelGenerator.MAX_WORLD_SIZE;
		if(height <= 0) height = LevelGenerator.MAX_WORLD_SIZE;
		
		tiles = new HashMap<>(Math.min(100_000, width*height));
		
		keyPresses = new HashSet<>(4);
		
		addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) { keyPresses.add(e.getKeyCode()); }
			@Override public void keyReleased(KeyEvent e) { keyPresses.remove(e.getKeyCode()); }
			@Override public void keyTyped(KeyEvent e) {}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				requestFocus();
			}
		});
		
		addFocusListener(new FocusListener() {
			@Override public void focusGained(FocusEvent e) { focus(false); }
			@Override public void focusLost(FocusEvent e) { unfocus(false); }
		});
		
		setDoubleBuffered(true);
		setFocusable(true);
		
		msgLabel = new JLabel("click \"regen world\" to generate map.");
		msgLabel.setVerticalAlignment(SwingConstants.TOP);
		msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
		setLayout(new GridLayout(1, 1));
		add(msgLabel);
	}
	
	public void generate(int worldWidth, int worldHeight) {
		tiles.clear();
		mapsValid = true;
		int width = worldWidth <= 0 ? LevelGenerator.MAX_WORLD_SIZE : worldWidth;
		int height = worldHeight <= 0 ? LevelGenerator.MAX_WORLD_SIZE : worldHeight;
		for(NoiseFunctionEditor editor: testPanel.getNoiseFunctionPanel().getElements())
			editor.generateSeed();
		msgLabel.setText("Generating tiles...");
		msgLabel.setVisible(true);
		repaint();
		SwingUtilities.invokeLater(() -> {
			this.width = width;
			this.height = height;
			worldOffX = width/2;
			worldOffY = height/2;
			forEachTile((p, rp) -> {});
			msgLabel.setVisible(false);
			requestFocus();
			repaint();
			run = true;
			new Thread(MapPanel.this).start();
			new Thread(new TileLoader()).start();
		});
	}
	
	void unfocus(boolean total) {
		if(total) run = false;
		keyPresses.clear();
		// revalidate();
	}
	
	void focus(boolean total) {
		run = true;
		if((inputThread == null || !inputThread.isAlive()))
			new Thread(MapPanel.this).start();
		if((loader == null || !loader.isAlive()))
			new Thread(new TileLoader()).start();
	}
	
	private void genTile(HashSet<Point> points) {
		if(!mapsValid || points == null) return;
		int cnt = 0;
		for(Point p: points) {
			genTile(p);
			cnt++;
			if(cnt > 10_000) {
				repaint();
				cnt %= 10_000;
				MyUtils.sleep(10);
			}
		}
	}
	private void genTile(Point p) { genTile(p.x, p.y); }
	private void genTile(int x, int y) {
		if(!mapsValid) return;
		TileTypeEnum type = testPanel.getNoiseMapperPanel().getElements()[0].getNoiseMap().getTileType(x, y);
		tiles.put(new Point(x, y), type.color);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		final int pixelDensity = Math.max(1, testPanel.getGlobalPanel().zoomField.getValue());
		
		forEachTile((p, rp) -> {
			g.setColor(tiles.get(p));
			g.fillRect(getWidth()/2+(rp.x-worldOffX)*pixelDensity, getHeight()/2+(rp.y-worldOffY)*pixelDensity, pixelDensity, pixelDensity);
		});
	}
	
	private Point getInput() {
		int x = 0, y = 0;
		if(keyPresses.contains(KeyEvent.VK_UP))
			y--;
		if(keyPresses.contains(KeyEvent.VK_DOWN))
			y++;
		if(keyPresses.contains(KeyEvent.VK_LEFT))
			x--;
		if(keyPresses.contains(KeyEvent.VK_RIGHT))
			x++;
		
		return new Point(x, y);
	}
	
	private void forEachTile(VoidBiFunction<Point, Point> action) {
		int actualZoom = testPanel.getGlobalPanel().zoomField.getValue();
		float zoom = actualZoom < 0 ? -1f/actualZoom : actualZoom == 0 ? 1 : actualZoom;
		Point radius = new Point(Math.min(width, (int)(getWidth()/zoom))/2, Math.min(height, (int)(getHeight()/zoom))/2);
		
		int skipZoom = actualZoom < 0 ? -actualZoom : 1;
		
		
		
		int spd = testPanel.getGlobalPanel().speedField.getValue();
		float buffer = testPanel.getGlobalPanel().bufferField.getValue();
		Point input = getInput();
		Point bufferRadiusNeg = new Point(radius.x + (int)(spd*buffer*(input.x<0?-input.x:0)), radius.y + (int)(spd*buffer*(input.y<0?-input.y:0)));
		Point bufferRadiusPos = new Point(radius.x + (int)(spd*buffer*(input.x>0?input.x:0)), radius.y + (int)(spd*buffer*(input.y>0?input.y:0)));
		
		HashSet<Point> points = new HashSet<>((bufferRadiusNeg.x+bufferRadiusPos.x)/skipZoom*((bufferRadiusNeg.y+bufferRadiusPos.y)/skipZoom));
		int xo = 0;
		for(int x = -bufferRadiusNeg.x; x < bufferRadiusPos.x; x+=skipZoom) {
			int yo = 0;
			for(int y = -bufferRadiusNeg.y; y < bufferRadiusPos.y; y+=skipZoom) {
				Point p = new Point(x + worldOffX, y + worldOffY);
				if(!tiles.containsKey(p))
					points.add(p);
				else if(!(p.x < 0 || p.y < 0 || p.x >= width || p.y >= height) && Math.abs(x) <= radius.x && Math.abs(y) <= radius.y)
					action.act(p, new Point(worldOffX-bufferRadiusNeg.x/skipZoom+xo, worldOffY-bufferRadiusNeg.y/skipZoom+yo));
				yo++;
			}
			xo++;
		}
		queueTiles(points);
	}
	
	private void queueTiles(HashSet<Point> points) {
		if(!mapsValid) return;
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
		while(run) {
			MyUtils.sleep(60);
			
			int worldOffX = this.worldOffX;
			int worldOffY = this.worldOffY;
			int spd = testPanel.getGlobalPanel().speedField.getValue();
			int zoom = testPanel.getGlobalPanel().zoomField.getValue();
			if(zoom < 0)
				spd *= -zoom;
			
			if(keyPresses.contains(KeyEvent.VK_UP) && height >= getHeight() && worldOffY > 0)
				worldOffY = Math.max(0, worldOffY - spd);
			if(keyPresses.contains(KeyEvent.VK_DOWN) && height >= getHeight() && worldOffY < height)
				worldOffY = Math.min(height, worldOffY + spd);
			if(keyPresses.contains(KeyEvent.VK_LEFT) && width >= getWidth() && worldOffX > 0)
				worldOffX = Math.max(0, worldOffX - spd);
			if(keyPresses.contains(KeyEvent.VK_RIGHT) && width >= getWidth() && worldOffX < width)
				worldOffX = Math.min(width, worldOffX + spd);
			
			if(this.worldOffX != worldOffX || this.worldOffY != worldOffY) {
				this.worldOffX = worldOffX;
				this.worldOffY = worldOffY;
				repaint();
			}
		}
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
			run = true;
			boolean start = false;
			while(run) {
				if(genQueue.size() > 0) {
					if(!start) {
						start = true;
					}
					HashSet<Point> points;
					synchronized (genLock) {
						points = genQueue.pollLast();
					}
					genTile(points);
					if(tiles.size() > 2_500_000)
						tiles.clear();
					if(genQueue.size() == 0) {
						start = false;
					}
					repaint();
				}
				else
					MyUtils.sleep(20);
			}
		}
	}
	
	void invalidateMaps() {
		if(mapsValid) {
			mapsValid = false;
			msgLabel.setText("<html>Noise map references changed. World must be regenerated to ensure there are no loops.");
			msgLabel.setVisible(true);
			repaint();
		}
	}
}
