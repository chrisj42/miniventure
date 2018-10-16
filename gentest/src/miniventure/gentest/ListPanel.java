package miniventure.gentest;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ContainerListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import miniventure.game.util.function.MapFunction;
import miniventure.game.world.levelgen.GroupNoiseMapper;
import miniventure.gentest.util.ButtonMaker;
import miniventure.gentest.util.MyPanel;
import miniventure.gentest.util.StringField;

import org.jetbrains.annotations.Nullable;

/**
 * Contains a "list" of "elements" to display that it displays in a certain fashion. toString() is used for titles.
 * @param <E> the "element" type
 */
public class ListPanel<E extends JComponent & NamedObject & Scrollable> extends MyPanel {
	
	private final HashMap<E, ElementContainer> containerMap = new HashMap<>();
	private final ArrayList<E> elementList = new ArrayList<>();
	
	private final TestPanel testPanel;
	private final Class<E> clazz;
	private final MapFunction<String, E> fetcher;
	private final JScrollPane scrollPane;
	private final MyPanel container; // elements are added here
	
	ListPanel(TestPanel testPanel, Class<E> clazz, @Nullable String descriptor, MapFunction<String, E> fetcher) {
		this.testPanel = testPanel;
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
		
		if(clazz.equals(NoiseMapEditor.class)) {
			JPanel btnPanel = new MyPanel();
			btnPanel.add(addBtn);
			JButton modBtn = new JButton("Add Function Map");
			modBtn.addActionListener(e -> {
				addElement(clazz.cast(new NoiseMapEditor(testPanel, new GroupNoiseMapper("Function Map-"+containerMap.size(), testPanel.getNoiseFunctionPanel().getElements()[0].getNoiseFunction()))));
				for(E elem: elementList)
					for(NoiseMapRegionEditor regionEditor: ((NoiseMapEditor)elem).getRegionEditors())
						regionEditor.resetNoiseMapSelector();
			});
			btnPanel.add(modBtn);
			add(btnPanel, BorderLayout.NORTH);
		}
		else
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
		
		elementList.add(e);
		//noinspection ObjectAllocationInLoop
		ElementContainer ec = new ElementContainer(e);
		containerMap.put(e, ec);
		container.add(ec, container.getComponentCount()-1);
		
		if(refresh) {
			for(ElementContainer container: containerMap.values())
				container.updateButtons();
			refresh();
		}
	}
	
	void removeElement(E e) { removeElement(e, true); }
	private void removeElement(E e, boolean refresh) {
		ElementContainer c = containerMap.remove(e);
		container.remove(c);
		elementList.remove(e);
		if(getElementCount() == 1)
			for(ElementContainer ec: containerMap.values())
				ec.removeBtn.setEnabled(false);
		
		if(refresh) {
			for(ElementContainer ec: containerMap.values())
				ec.updateButtons();
			refresh();
		}
	}
	
	void replaceElements(E[] newElements) {
		ContainerListener[] ls = getContainerListeners();
		for(ContainerListener l: ls)
			removeContainerListener(l);
		
		for(ElementContainer ec: containerMap.values())
			container.remove(ec);
		
		for(ContainerListener l: ls)
			addContainerListener(l);
		
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
		
		for(ElementContainer ec: containerMap.values())
			ec.updateButtons();
		
		refresh();
	}
	
	int getElementCount() { return elementList.size(); }
	
	@SuppressWarnings("unchecked")
	E[] getElements() {
		E[] ar = (E[]) Array.newInstance(clazz, elementList.size());
		elementList.toArray(ar);
		return ar;
	}
	
	private void refresh() {
		scrollPane.revalidate();
		scrollPane.repaint();
		
		revalidate();
		repaint();
	}
	
	private void moveElement(E e, int idx) {
		elementList.remove(e);
		elementList.add(idx, e);
		
		ContainerListener[] ls = getContainerListeners();
		for(ContainerListener l: ls)
			removeContainerListener(l);
		
		container.remove(containerMap.get(e));
		container.add(containerMap.get(e), idx);
		
		for(ContainerListener l: ls)
			addContainerListener(l);
		
		for(ElementContainer ec: containerMap.values())
			ec.updateButtons();
		
		if(clazz == NoiseMapEditor.class)
			for(NoiseMapEditor editor: testPanel.getNoiseMapperPanel().getElements())
				for(NoiseMapRegionEditor regionEditor: editor.getRegionEditors())
					regionEditor.resetNoiseMapSelector();
		
		if(clazz == NoiseFunctionEditor.class)
			for(NoiseMapEditor editor: testPanel.getNoiseMapperPanel().getElements())
				editor.resetFunctionSelector();
		
		refresh();
	}
	
	class ElementContainer extends MyPanel {
		
		final E element;
		private final JButton removeBtn;
		private final JButton upBtn, downBtn;
		
		void updateButtons() {
			int idx = elementList.indexOf(element);
			upBtn.setEnabled(idx > 0);
			downBtn.setEnabled(idx < elementList.size()-1);
		}
		
		ElementContainer(E element) {
			this.element = element;
			
			setLayout(new BorderLayout());
			
			MyPanel topPanel = new MyPanel();
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
			
			MyPanel btnPanel = new MyPanel(new GridLayout(1, 0));
			removeBtn = ButtonMaker.removeButton(ae -> removeElement(element));
			btnPanel.add(removeBtn);
			
			// MyPanel btnPanelBottom = new MyPanel(new GridLayout(1, 0));
			upBtn = ButtonMaker.upButton(e -> {
				int idx = elementList.indexOf(element);
				if(idx < 1) return;
				moveElement(element, idx-1);
			});
			btnPanel.add(upBtn);
			downBtn = ButtonMaker.downButton(e -> {
				int idx = elementList.indexOf(element);
				if(idx < 0 || idx == elementList.size()-1) return;
				moveElement(element, idx+1);
			});
			btnPanel.add(downBtn);
			
			updateButtons();
			
			topPanel.add(btnPanel);
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
