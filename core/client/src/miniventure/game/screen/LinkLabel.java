package miniventure.game.screen;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import miniventure.game.client.ClientCore;

public class LinkLabel extends JLabel implements MouseListener {
	
	private String text;
	private String url;
	private boolean down = false;
	private boolean over = false;
	
	private Color c;
	
	public LinkLabel(String text) {
		this(text, null);
	}
	public LinkLabel(String text, String url) {
		setValue(text, url);
		addMouseListener(this);
	}
	
	public void setValue(String text, String url) {
		this.text = text;
		setLinkUrl(url);
	}
	
	public void setLinkText(String text) {
		this.text = text;
		refresh();
		revalidate();
	}
	
	public void setLinkUrl(String url) {
		this.url = url;
		down = false;
		if(valid())
			setLinkColor(Color.BLUE);
		else
			setLinkColor(Color.WHITE);
	}
	
	private boolean valid() { return url != null && url.length() > 0; }
	
	private void setLinkColor(Color color) {
		this.c = color;
		refresh();
		repaint();
	}
	
	private void refresh() {
		// System.out.println("color is "+c);
		setText("<html><span style='"+(valid()?"text-decoration:underline; ":"")+"color:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")'>"+text+"</span></html>");
		// setForeground(c);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}
	
	@Override
	public void mousePressed(MouseEvent e) {
		over = true;
		if(!valid()) return;
		down = true;
		setLinkColor(Color.RED);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		down = false;
		if(!valid()) return;
		if(over)
			openLink();
		
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		setLinkColor(new Color(128, 0, 128, 255));
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		over = true;
		if(!valid()) return;
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		if(down)
			setLinkColor(Color.RED);
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		over = false;
		if(!valid()) return;
		setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		setLinkColor(Color.BLUE);
	}
	
	private void openLink() {
		if(!Desktop.isDesktopSupported())
			showLink();
		else {
			Desktop desktop = Desktop.getDesktop();
			if(!desktop.isSupported(Action.BROWSE))
				showLink();
			else {
				try {
					desktop.browse(new URI(url));
				} catch(IOException | URISyntaxException e) {
					e.printStackTrace();
					showLink();
				}
			}
		}
	}
	
	private void showLink() {
		EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(ClientCore.getUiPanel(), url, "Site Link", JOptionPane.INFORMATION_MESSAGE));
	}
}
