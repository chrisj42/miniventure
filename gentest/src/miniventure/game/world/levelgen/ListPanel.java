package miniventure.game.world.levelgen;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.lang.reflect.Array;
import java.util.HashMap;

import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.levelgen.util.MyPanel;
import miniventure.game.world.levelgen.util.StringField;

/**
 * Contains a "list" of "elements" to display that it displays in a certain fashion. toString() is used for titles.
 * @param <E> the "element" type
 */
public class ListPanel<E extends JComponent & NamedObject> extends MyPanel {
	
	private HashMap<E, ElementContainer> containers = new HashMap<>();
	
	private JScrollPane scrollPane;
	private JPanel container; // elements are added here
	
	@SafeVarargs
	public ListPanel(ValueFunction<E> fetcher, E... elements) {
		container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		container.add(Box.createVerticalGlue());
		
		setLayout(new GridLayout(1, 1));
		scrollPane = new JScrollPane(container);
		
		JButton addBtn = new JButton("Add");
		addBtn.addActionListener(e -> addElement(fetcher.get()));
		
		scrollPane.setColumnHeaderView(addBtn);
		add(scrollPane);
		
		if(elements.length > 0)
			addElement(elements);
		else
			addElement(fetcher.get());
	}
	
	@SafeVarargs
	public final void addElement(E... elements) {
		for(E e: elements) {
			ElementContainer ec = new ElementContainer(e);
			containers.put(e, ec);
			container.add(ec);
		}
		refresh();
	}
	
	public void removeElement(E e) {
		container.remove(containers.remove(e));
		refresh();
	}
	
	@SuppressWarnings("unchecked")
	public E[] getElements(Class<E> clazz) {
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
			
			JPanel btnPanel = new JPanel();
			btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.PAGE_AXIS));
			// JButton addBtn = new JButton("Add");
			JButton removeBtn = new JButton("Remove");
			removeBtn.addActionListener(ae -> removeElement(e));
			btnPanel.add(removeBtn);
			
			add(btnPanel);
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
			
			//System.out.println("name of "+e+": "+e.getObjectName());
			StringField nameField = new StringField(e.getObjectName(), 30);
			//noinspection Convert2MethodRef
			nameField.addValueListener(val -> e.setObjectName(val));
			mainPanel.add(nameField);
			
			mainPanel.add(e);
			add(mainPanel);
		}
	}
	
	@Override
	public Dimension getPreferredSize() { return getParent().getSize(); }
}
