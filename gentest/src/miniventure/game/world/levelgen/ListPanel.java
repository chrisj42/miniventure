package miniventure.game.world.levelgen;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.lang.reflect.Array;
import java.util.HashMap;

import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.levelgen.util.StringField;

/**
 * Contains a "list" of "elements" to display that it displays in a certain fashion. toString() is used for titles.
 * @param <E> the "element" type
 */
public class ListPanel<E extends JComponent & NamedObject & Scrollable> extends MyPanel {
	
	private HashMap<E, ElementContainer> containers = new HashMap<>();
	
	private final Class<E> clazz;
	private JScrollPane scrollPane;
	private MyPanel container; // elements are added here
	
	@SafeVarargs
	public ListPanel(Class<E> clazz, ValueMonoFunction<String, E> fetcher, E... elements) {
		this.clazz = clazz;
		container = new MyPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		container.add(Box.createVerticalGlue());
		
		setLayout(new GridLayout(1, 1));
		scrollPane = new JScrollPane(container);
		
		String type = clazz.getSimpleName().replace("Noise", "").replace("Editor", "");
		
		JButton addBtn = new JButton("Add "+type);
		addBtn.addActionListener(e -> addElement(fetcher.get(type+'-'+containers.size())));
		
		scrollPane.setColumnHeaderView(addBtn);
		add(scrollPane);
		
		if(elements.length > 0)
			addElement(elements);
		else
			addElement(fetcher.get("master"));
	}
	
	@SafeVarargs
	public final void addElement(E... elements) {
		for(E e: elements) {
			ElementContainer ec = new ElementContainer(e);
			containers.put(e, ec);
			container.add(ec, container.getComponentCount()-1);
		}
		refresh();
	}
	
	public void removeElement(E e) {
		container.remove(containers.remove(e));
		refresh();
	}
	
	@SuppressWarnings("unchecked")
	public E[] getElements() {
		E[] ar = (E[]) Array.newInstance(clazz, containers.size());
		containers.keySet().toArray(ar);
		return ar;
	}
	
	public void refresh() {
		scrollPane.revalidate();
		scrollPane.repaint();
		
		revalidate();
		repaint();
	}
	
	
	private class ElementContainer extends MyPanel {
		
		private final E e;
		
		ElementContainer(E e) {
			this.e = e;
			
			setLayout(new BorderLayout());
			MyPanel btnPanel = new MyPanel();
			btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.PAGE_AXIS));
			JButton removeBtn = new JButton("Remove");
			removeBtn.addActionListener(ae -> removeElement(e));
			btnPanel.add(removeBtn);
			btnPanel.add(Box.createVerticalGlue());
			
			add(btnPanel, BorderLayout.WEST);
			
			MyPanel mainPanel = new MyPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
			
			//System.out.println("name of "+e+": "+e.getObjectName());
			StringField nameField = new StringField(e.getObjectName(), 30);
			nameField.setMaximumSize(new Dimension(nameField.getMaximumSize().width, nameField.getPreferredSize().height));
			//noinspection Convert2MethodRef
			nameField.addValueListener(val -> e.setObjectName(val));
			mainPanel.add(nameField);
			
			mainPanel.add(e);
			add(mainPanel, BorderLayout.CENTER);
		}
	}
	
	@Override
	public Dimension getPreferredSize() { return getParent().getSize(); }
}
