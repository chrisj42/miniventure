package miniventure.game.world.levelgen.util;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ValidatedField<T> extends JTextField {
	
	@FunctionalInterface
	public interface StringConverter<T> {
		T convert(String text);
	}
	
	@FunctionalInterface
	public interface Validator<T> {
		boolean validate(T value);
	}
	
	@FunctionalInterface
	public interface ValidationListener<T> {
		void onValidate(ValidatedField<? extends T> field);
	}
	
	public static final Validator<Number> POSITIVE = n -> n.doubleValue() > 0;
	public static final Validator<Number> NON_NEGATIVE = n -> n.doubleValue() >= 0;
	public static final Validator<String> NON_EMPTY = s -> s.length() > 0;
	
	private final StringConverter<T> converter;
	private final Validator<? super T> validator;
	private T curValue = null;
	private final ArrayList<ValidationListener<? super T>> listeners = new ArrayList<>();
	
	@SafeVarargs
	public ValidatedField(@NotNull StringConverter<T> converter, @NotNull Validator<? super T>... validators) {
		this.converter = converter;
		
		if(validators.length == 1)
			this.validator = validators[0];
		else if(validators.length == 0)
			this.validator = val -> true;
		else {
			this.validator = val -> {
				for(Validator<? super T> v: validators)
					if(!v.validate(val))
						return false;
				return true;
			};
		}
		
		revalidateText(false);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				SwingUtilities.invokeLater(() -> revalidateText());
			}
		});
	}
	
	public void addListener(ValidationListener<? super T> l) {
		if(listeners.contains(l))
			listeners.remove(l);
		listeners.add(l);
	}
	public void removeListener(ValidationListener<? super T> l) {
		listeners.remove(l);
	}
	
	public boolean getValid() { return curValue != null; }
	
	public void revalidateText() { revalidateText(true); }
	public void revalidateText(boolean notifyListeners) {
		try {
			T val = converter.convert(getText() == null ? "" : getText());
			boolean valid = validator.validate(val);
			curValue = valid ? val : null;
		} catch(Throwable t) {
			curValue = null;
		}
		
		if(getValid())
			setForeground(Color.BLACK);
		else
			setForeground(Color.RED);
		
		if(notifyListeners)
			for(ValidationListener<? super T> l: listeners)
				l.onValidate(this);
	}
	
	@Override
	public void setText(String text) {
		super.setText(text);
		revalidateText(false);
	}
	
	@Nullable
	public T getValue() { return curValue; }
	
	/*@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(!getValid()) {
			g.setColor(Color.RED);
			g.fillRect(getX(), getY(), getWidth(), getHeight());
		}
	}*/
}
