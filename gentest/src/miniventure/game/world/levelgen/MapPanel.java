package miniventure.game.world.levelgen;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.HashMap;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.VoidMonoFunction;
import miniventure.game.world.Point;
import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.tile.TileType.TileTypeEnum;

public class MapPanel extends MyPanel implements Runnable {
	
	private final TestPanel testPanel;
	private boolean gen = false;
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
		
		tiles = new HashMap<>(Math.min(10_000, width*height));
		
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
				if((inputThread == null || !inputThread.isAlive()) && (width >= getWidth() || height >= getHeight()))
					new Thread(MapPanel.this).start();
			}
			@Override
			public void focusLost(FocusEvent e) {
				gen = false;
				msgLabel.setText("click regen world button to restart generation.");
				msgLabel.setVisible(true);
				run = false;
				revalidate();
			}
		});
		
		setFocusable(true);
		
		addComponentListener(new ComponentAdapter() {
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
		});
		
		msgLabel = new JLabel("click regen world");
		msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
		setLayout(new GridLayout(1, 1));
		add(msgLabel);
	}
	
	public void generate(int worldWidth, int worldHeight) {
		tiles.clear();
		//gen = false;
		int width = worldWidth <= 0 ? LevelGenerator.MAX_WORLD_SIZE : worldWidth;
		int height = worldHeight <= 0 ? LevelGenerator.MAX_WORLD_SIZE : worldHeight;
		for(NoiseFunctionEditor editor: testPanel.getNoiseFunctionPanel().getElements())
			editor.getNoiseFunction().resetFunction();
		msgLabel.setText("Generating tiles");
		repaint();
		SwingUtilities.invokeLater(() -> {
			this.width = width;
			this.height = height;
			worldOffX = width/2;
			worldOffY = height/2;
			forEachTile(p -> genTile(p.x, p.y));
			gen = true;
			msgLabel.setVisible(false);
			requestFocus();
			repaint();
			new Thread(MapPanel.this).start();
		});
	}
	
	private void genTile(int x, int y) {
		TileTypeEnum type = testPanel.getNoiseMapperPanel().getElements()[0].getNoiseMap().getTileType(x, y);
		tiles.put(new Point(x, y), type.color);
	}
	
	/*@Override
	public Dimension getPreferredSize() {
		Dimension p = super.getPreferredSize();
		return new Dimension(width, height);
	}*/
	
	
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		forEachTile(p -> {
			if(!tiles.containsKey(p)) {
				if(gen)
					genTile(p.x, p.y);
				else
					return;
			}
			g.setColor(tiles.get(p));
			g.fillRect(getWidth()/2+p.x-worldOffX, getHeight()/2+p.y-worldOffY, 1, 1);
		});
	}
	
	private void forEachTile(VoidMonoFunction<Point> action) {
		Point radius = new Point(Math.min(width, getWidth())/2, Math.min(height, getHeight())/2);
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
		while(run) {
			MyUtils.sleep(20);
			
			if(width < getWidth() && height < getHeight())
				break;
			
			int worldOffX = this.worldOffX;
			int worldOffY = this.worldOffY;
			
			if(keyPresses.get(KeyEvent.VK_UP) && height >= getHeight() && worldOffY > 0)
				worldOffY--;
			if(keyPresses.get(KeyEvent.VK_DOWN) && height >= getHeight() && worldOffY < height)
				worldOffY++;
			if(keyPresses.get(KeyEvent.VK_LEFT) && width >= getWidth() && worldOffX > 0)
				worldOffX--;
			if(keyPresses.get(KeyEvent.VK_RIGHT) && width >= getWidth() && worldOffX < width)
				worldOffX++;
			
			if(this.worldOffX != worldOffX || this.worldOffY != worldOffY)
				repaint();
		}
		
		System.out.println("input thread stopped.");
	}
}
