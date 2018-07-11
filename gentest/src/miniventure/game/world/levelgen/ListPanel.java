package miniventure.game.world.levelgen;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ContainerListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.levelgen.util.StringField;

/**
 * Contains a "list" of "elements" to display that it displays in a certain fashion. toString() is used for titles.
 * @param <E> the "element" type
 */
public class ListPanel<E extends JComponent & NamedObject & Scrollable> extends MyPanel {
	
	private HashMap<E, ElementContainer> containerMap = new HashMap<>();
	private ArrayList<E> elementList = new ArrayList<>();
	
	private final Class<E> clazz;
	private JScrollPane scrollPane;
	private MyPanel container; // elements are added here
	
	@SafeVarargs
	public ListPanel(Class<E> clazz, ValueMonoFunction<String, E> fetcher, E... elements) {
		this.clazz = clazz;
		container = new MyPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		container.add(Box.createVerticalGlue());
		
		// JPanel container = new MyPanel();
		// container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		// container.add(this.container);
		// container.add(Box.createVerticalGlue());
		
		setLayout(new GridLayout(1, 1));
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
		
		scrollPane.setColumnHeaderView(addBtn);
		add(scrollPane);
		
		if(elements.length > 0)
			addElement(elements);
		else
			addElement(fetcher.get("master"));
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
	
	@SafeVarargs
	public final void addElement(E... elements) {
		for(E e: elements) {
			//noinspection ObjectAllocationInLoop
			ElementContainer ec = new ElementContainer(e);
			containerMap.put(e, ec);
			elementList.add(e);
			container.add(ec, container.getComponentCount()-1);
		}
		refresh();
	}
	
	public void removeElement(E e) {
		ElementContainer c = containerMap.remove(e);
		container.remove(c);
		elementList.remove(e);
		refresh();
	}
	
	public int getElementCount() { return elementList.size(); }
	
	@SuppressWarnings("unchecked")
	public E[] getElements() {
		E[] ar = (E[]) Array.newInstance(clazz, elementList.size());
		elementList.toArray(ar);
		return ar;
	}
	
	public void refresh() {
		scrollPane.revalidate();
		scrollPane.repaint();
		
		revalidate();
		repaint();
	}
	
	
	class ElementContainer extends MyPanel {
		
		final E element;
		
		ElementContainer(E element) {
			this.element = element;
			
			setLayout(new BorderLayout());
			MyPanel btnPanel = new MyPanel();
			btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.PAGE_AXIS));
			JButton removeBtn = new JButton("Remove");
			removeBtn.addActionListener(ae -> removeElement(element));
			btnPanel.add(removeBtn);
			btnPanel.add(Box.createVerticalGlue());
			
			add(btnPanel, BorderLayout.WEST);
			
			MyPanel mainPanel = new MyPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
			
			//System.out.println("name of "+e+": "+e.getObjectName());
			StringField nameField = new StringField(element.getObjectName(), 30);
			nameField.setMaximumSize(new Dimension(nameField.getMaximumSize().width, nameField.getPreferredSize().height));
			//noinspection Convert2MethodRef
			nameField.addValueListener(val -> element.setObjectName(val));
			mainPanel.add(nameField);
			
			mainPanel.add(element);
			add(mainPanel, BorderLayout.CENTER);
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
