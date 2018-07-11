package miniventure.game.world.levelgen.util;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

public abstract class ValueField<T> extends JTextField {
	
	private final Stringifier<T> stringifier;
	
	@FunctionalInterface
	protected interface Stringifier<T> {
		String get(T value);
	}
	
	private ArrayList<ValueListener<T>> listeners = new ArrayList<>();
	private final Object listenerLock = new Object();
	
	public ValueField(T value) { this(value, String::valueOf); }
	public ValueField(T value, Stringifier<T> stringifier) {
		super(stringifier.get(value));
		this.stringifier = stringifier;
		init();
	}
	public ValueField(T value, int columns) { this(value, columns, String::valueOf); }
	public ValueField(T value, int columns, Stringifier<T> stringifier) {
		super(stringifier.get(value), columns);
		this.stringifier = stringifier;
		init();
	}
	
	private void init() {
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				SwingUtilities.invokeLater(ValueField.this::onTextChange);
			}
		});
		
		addFocusListener(new FocusListener() {
			@Override public void focusGained(FocusEvent e) {}
			@Override public void focusLost(FocusEvent e) { replace(); }
		});
		
		addActionListener(e -> replace());
	}
	
	private void replace() {
		setText(stringifier.get(getValue()));
		synchronized (listenerLock) {
			for(ValueListener<T> l: listeners)
				l.onValueSet(getValue());
		}
	}
	
	protected abstract void onTextChange();
	@NotNull
	public abstract T getValue();
	public void setValue(@NotNull T value) {
		setText(stringifier.get(value));
		onTextChange();
	}
	
	public void addValueListener(ValueListener<T> l) { synchronized (listenerLock) {listeners.add(l);} }
	public void removeValueListener(ValueListener<T> l) { synchronized (listenerLock) {listeners.remove(l);} }
	
}
