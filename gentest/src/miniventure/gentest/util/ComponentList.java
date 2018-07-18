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

import miniventure.game.util.function.MonoValueFunction;
import miniventure.gentest.NoisePanel;
import miniventure.gentest.util.ListItemTransferHandler.TransferListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentList<E extends Component> extends JPanel {
	
	private final JList<E> list;
	private final ListItemTransferHandler transferHandler;
	private final JPanel componentPanel;
	
	private final JPanel headerPanel, listPanel;
	
	private final HashMap<E, ElementContainer> containerMap = new HashMap<>();
	private E mouseElement;
	
	@SafeVarargs
	public ComponentList(@Nullable MonoValueFunction<E, String> stringifier, E... elements) {
		// if stringifier is null, then use icon instead
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		DefaultListModel<E> model = new DefaultListModel<>();
		for (E e: elements)
			model.addElement(e);
		
		list = new JList<>(model);
		transferHandler = new ListItemTransferHandler();
		transferHandler.addTransferListener(this::refreshElementList);
		list.setTransferHandler(transferHandler);
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setDropMode(DropMode.INSERT);
		list.setDragEnabled(true);
		list.setCellRenderer(stringifier == null ? new CellRenderer() : new CellRenderer(stringifier));
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
		
		listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		listPanel.add(Box.createHorizontalGlue());
		listPanel.add(list);
		
		componentPanel = new JPanel();
		componentPanel.setLayout(new BoxLayout(componentPanel, BoxLayout.PAGE_AXIS));
		
		for(E e: elements) {
			ElementContainer ec = new ElementContainer(e);
			componentPanel.add(ec);
			containerMap.put(e, ec);
		}
		
		componentPanel.add(Box.createVerticalGlue());
		
		listPanel.add(componentPanel);
		listPanel.add(Box.createHorizontalGlue());
		
		
		headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));
		headerPanel.add(Box.createHorizontalGlue());
		headerPanel.add(Box.createHorizontalGlue());
		add(headerPanel);
		
		add(listPanel);
		add(Box.createVerticalGlue());
	}
	
	public void addTransferListener(TransferListener l) { transferHandler.addTransferListener(l); }
	public void removeTransferListener(TransferListener l) { transferHandler.removeTransferListener(l); }
	
	public void addHeaderComponent(Component c) {
		headerPanel.add(c, headerPanel.getComponentCount()-1);
		revalidate();
		repaint();
	}
	
	public void removeHeaderComponent(Component c) {
		headerPanel.remove(c);
		revalidate();
		repaint();
	}
	
	public void addPreElementComponent(Component c) {
		for(ElementContainer ec: containerMap.values())
			ec.add(c, ec.getComponentCount()-1);
		revalidate();
		repaint();
	}
	
	public void removePreElementComponent(Component c) {
		for(ElementContainer ec: containerMap.values())
			ec.remove(c);
		revalidate();
		repaint();
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
		
		private final MonoValueFunction<E, String> stringifier;
		
		public CellRenderer() {
			super("icon", RIGHT); // TODO put icon
			this.stringifier = null;
			setOpaque(true);
		}
		public CellRenderer(@NotNull MonoValueFunction<E, String> stringifier) {
			super("", RIGHT);
			this.stringifier = stringifier;
			setOpaque(true);
		}
		
		@Override
		public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
			if(stringifier != null)
				setText(stringifier.get(value));
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
		private final E element;
		
		public ElementContainer(E element) {
			this.element = element;
			// add(ButtonMaker.removeButton(e -> removeElement(element)));
			// JButton editButton = new JButton("Edit");
			// add(editButton);
			add(element);
		}
	}
}
