package miniventure.gentest.util;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public final class ButtonMaker {
	
	private static BufferedImage upArrow;
	private static BufferedImage downArrow;
	
	static {
		try {
			upArrow = ImageIO.read(ButtonMaker.class.getResourceAsStream("/up-arrow.png"));
			downArrow = ImageIO.read(ButtonMaker.class.getResourceAsStream("/down-arrow.png"));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private ButtonMaker() {}
	
	public static JButton removeButton(ActionListener l) {
		JButton btn = new JButton("X");
		btn.setForeground(Color.RED);
		btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
		
		if(l != null)
			btn.addActionListener(l);
		return btn;
	}
	
	public static JButton upButton(ActionListener l) {
		JButton btn = new JButton(new ImageIcon(upArrow));
		if(l != null)
			btn.addActionListener(l);
		return btn;
	}
	
	public static JButton downButton(ActionListener l) {
		JButton btn = new JButton(new ImageIcon(downArrow));
		
		if(l != null)
			btn.addActionListener(l);
		return btn;
	}
}
