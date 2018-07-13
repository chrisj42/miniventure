package miniventure.game.world.levelgen;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ContainerListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.levelgen.util.StringField;

import org.jetbrains.annotations.Nullable;

/**
 * Contains a "list" of "elements" to display that it displays in a certain fashion. toString() is used for titles.
 * @param <E> the "element" type
 */
public class ListPanel<E extends JComponent & NamedObject & Scrollable> extends MyPanel {
	
	private final HashMap<E, ElementContainer> containerMap = new HashMap<>();
	private final ArrayList<E> elementList = new ArrayList<>();
	
	private final Class<E> clazz;
	private final ValueMonoFunction<String, E> fetcher;
	private final JScrollPane scrollPane;
	private final MyPanel container; // elements are added here
	
	ListPanel(Class<E> clazz, @Nullable String descriptor, ValueMonoFunction<String, E> fetcher) {
		this.clazz = clazz;
		this.fetcher = fetcher;
		container = new MyPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		container.add(Box.createVerticalGlue());
		
		setLayout(new BorderLayout());
		scrollPane = new JScrollPane(container);
		
		String type = clazz.getSimpleName().replace("Noise", "").replace("Editor", "");
		
		JButton addBtn = new JButton("Add "+type);
		addBtn.addActionListener(e -> {
			addElement(fetcher.get(type+'-'+ containerMap.size()));
			if(clazz.equals(NoiseMapEditor.class))
				for(E elem: elementList)
					for(NoiseMapRegionEditor regionEditor: ((NoiseMapEditor)elem).getRegionEditors())
						regionEditor.resetNoiseMapSelector();
		});
		
		if(descriptor != null) {
			JLabel label = new JLabel(descriptor);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			scrollPane.setColumnHeaderView(label);
		}
		
		add(addBtn, BorderLayout.NORTH);
		
		add(scrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public synchronized void addContainerListener(ContainerListener l) {
		container.addContainerListener(l);
	}
	
	@Override
	public synchronized void removeContainerListener(ContainerListener l) {
		container.removeContainerListener(l);
	}
	
	@Override
	public synchronized ContainerListener[] getContainerListeners() {
		return container.getContainerListeners();
	}
	
	void addElement(E e) { addElement(e, true); }
	private void addElement(E e, boolean refresh) {
		if(getElementCount() == 1)
			for(ElementContainer ec: containerMap.values())
				ec.removeBtn.setEnabled(true);	
		
		//noinspection ObjectAllocationInLoop
		ElementContainer ec = new ElementContainer(e);
		containerMap.put(e, ec);
		elementList.add(e);
		container.add(ec, container.getComponentCount()-1);
		
		if(refresh)
			refresh();
	}
	
	void removeElement(E e) { removeElement(e, true); }
	private void removeElement(E e, boolean refresh) {
		ElementContainer c = containerMap.remove(e);
		container.remove(c);
		elementList.remove(e);
		if(getElementCount() == 1)
			for(ElementContainer ec: containerMap.values())
				ec.removeBtn.setEnabled(false);
		
		if(refresh)
			refresh();
	}
	
	void replaceElements(E[] newElements) {
		for(ElementContainer ec: containerMap.values())
			container.remove(ec);
		containerMap.clear();
		elementList.clear();
		if(newElements.length < 2) {
			if(newElements.length == 0)
				addElement(fetcher.get("default function"), false);
			else
				addElement(newElements[0], false);
			for(ElementContainer ec: containerMap.values())
				ec.removeBtn.setEnabled(false);
		}
		else {
			for(E e: newElements)
				addElement(e, false);
		}
		
		refresh();
	}
	
	int getElementCount() { return elementList.size(); }
	
	@SuppressWarnings("unchecked")
	E[] getElements() {
		E[] ar = (E[]) Array.newInstance(clazz, elementList.size());
		elementList.toArray(ar);
		return ar;
	}
	
	void refresh() {
		scrollPane.revalidate();
		scrollPane.repaint();
		
		revalidate();
		repaint();
	}
	
	
	class ElementContainer extends MyPanel {
		
		final E element;
		private final JButton removeBtn;
		
		ElementContainer(E element) {
			this.element = element;
			
			setLayout(new BorderLayout());
			
			MyPanel topPanel = new MyPanel();
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
			removeBtn = new JButton("Remove");
			removeBtn.addActionListener(ae -> removeElement(element));
			topPanel.add(removeBtn);
			topPanel.add(Box.createHorizontalStrut(20));
			
			StringField nameField = new StringField(element.getObjectName(), 30);
			nameField.setMaximumSize(new Dimension(nameField.getMaximumSize().width, nameField.getPreferredSize().height));
			//noinspection Convert2MethodRef
			nameField.addValueListener(val -> element.setObjectName(val));
			topPanel.add(nameField);
			
			topPanel.add(Box.createHorizontalGlue());
			topPanel.add(Box.createHorizontalStrut(50));
			
			add(topPanel, BorderLayout.NORTH);
			
			MyPanel bottomPanel = new MyPanel();
			bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
			
			bottomPanel.add(element);
			add(bottomPanel, BorderLayout.CENTER);
			
			JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL) {
				@Override
				public Dimension getPreferredSize() {
					Dimension s = super.getPreferredSize();
					s.height += 20;
					return s;
				}
			};
			add(sep, BorderLayout.SOUTH);
		}
		@Override
		public Dimension getMaximumSize() {
			Dimension size = super.getMaximumSize();
			size.height = getPreferredSize().height;
			return size;
		}
		
		@Override
		public Dimension getPreferredSize() {
			Dimension size = super.getPreferredSize();
			size.height += 20;
			return size;
		}
	}
}
