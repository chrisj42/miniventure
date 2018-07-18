package miniventure.gentest.util;

import javax.swing.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Objects;

import miniventure.gentest.NamedObject;

public class MyList<E extends JPanel & NamedObject> extends JPanel {
	
	private final JList<E> list;
	private final JPanel componentPanel;
	
	private final HashMap<E, ElementContainer> containerMap = new HashMap<>();
	private E mouseElement;
	
	@SafeVarargs
	public MyList(E... elements) {
		DefaultListModel<E> model = new DefaultListModel<>();
		for (E e: elements)
			model.addElement(e);
		
		list = new JList<>(model);
		ListItemTransferHandler handler = new ListItemTransferHandler();
		handler.addTransferListener(this::refreshElementList);
		list.setTransferHandler(handler);
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setDropMode(DropMode.INSERT);
		list.setDragEnabled(true);
		list.setCellRenderer(new CellRenderer());
		list.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				final int i = list.locationToIndex(e.getPoint());
				if(i < 0) {
					mouseElement = null;
					list.repaint();
					return;
				}
				final Rectangle bounds = list.getCellBounds(i, i+1);
				if(bounds.contains(e.getPoint()))
					mouseElement = list.getModel().getElementAt(i);
				else
					mouseElement = null;
				list.repaint();
			}
		});
		list.addMouseListener(new MouseAdapter() {
			@Override public void mouseExited(MouseEvent e) { mouseElement = null; list.repaint(); }
		});
		
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(Box.createHorizontalGlue());
		add(list);
		
		componentPanel = new JPanel();
		componentPanel.setLayout(new BoxLayout(componentPanel, BoxLayout.PAGE_AXIS));
		
		for(E e: elements) {
			ElementContainer ec = new ElementContainer(e);
			componentPanel.add(ec);
			containerMap.put(e, ec);
		}
		
		componentPanel.add(Box.createVerticalGlue());
		
		add(componentPanel);
		add(Box.createHorizontalGlue());
	}
	
	private void refreshElementList() {
		componentPanel.removeAll();
		for(int i = 0; i < list.getModel().getSize(); i++)
			componentPanel.add(containerMap.get(list.getModel().getElementAt(i)));
		
		componentPanel.add(Box.createVerticalGlue());
		revalidate();
		repaint();
	}
	
	public void addElement(E e) {
		((DefaultListModel<E>)list.getModel()).addElement(e);
		ElementContainer ec = new ElementContainer(e);
		componentPanel.add(ec, componentPanel.getComponentCount()-1);
		containerMap.put(e, ec);
		revalidate();
		repaint();
	}
	
	public void removeElement(E e) {
		((DefaultListModel<E>)list.getModel()).removeElement(e);
		componentPanel.remove(containerMap.remove(e));
		revalidate();
		repaint();
	}
	
	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	
	private class CellRenderer extends JLabel implements ListCellRenderer<E> {
		
		public CellRenderer() {
			super("", RIGHT);
			setOpaque(true);
			// setHorizontalAlignment(RIGHT);
		}
		
		@Override
		public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
			setText(value.getObjectName());
			lastValue = value;
			setBackground(Objects.equals(mouseElement, value) ? list.getSelectionBackground() : list.getBackground());
			setForeground(Objects.equals(mouseElement, value) ? list.getSelectionForeground() : list.getForeground());
			return this;
		}
		
		private E lastValue;
		
		@Override
		public Dimension getPreferredSize() {
			if(containerMap.get(lastValue) == null)
				return super.getPreferredSize();
			return new Dimension(super.getPreferredSize().width, containerMap.get(lastValue).getPreferredSize().height);
		}
	}
	
	private class ElementContainer extends JPanel {
		public ElementContainer(E element) {
			add(ButtonMaker.removeButton(e -> removeElement(element)));
			JButton editButton = new JButton("Edit");
			add(editButton);
			add(element);
		}
	}
}
