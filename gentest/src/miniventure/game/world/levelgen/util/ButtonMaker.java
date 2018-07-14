package miniventure.game.world.levelgen.util;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;

public final class ButtonMaker {
	
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
		JButton btn = new JButton(new ImageIcon("up-arrow.png"));
		if(l != null)
			btn.addActionListener(l);
		return btn;
	}
	
	public static JButton downButton(ActionListener l) {
		JButton btn = new JButton(new ImageIcon("down-arrow.png"));
		
		if(l != null)
			btn.addActionListener(l);
		return btn;
	}
}
