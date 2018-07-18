package miniventure.gentest.util;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("serial")
class ListItemTransferHandler extends TransferHandler {
	protected final DataFlavor localObjectFlavor;
	protected int[] indices;
	protected int addIndex = -1; // Location where items were added
	protected int addCount; // Number of items added.
	
	@FunctionalInterface
	public interface TransferListener {
		void orderChanged();
	}
	
	private final ArrayList<TransferListener> listeners = new ArrayList<>();
	
	public ListItemTransferHandler() {
		super();
		// localObjectFlavor = new ActivationDataFlavor(
		//   Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
		localObjectFlavor = new DataFlavor(Object.class, "single item");
	}
	
	public void addTransferListener(TransferListener l) { listeners.add(l); }
	public void removeTransferListener(TransferListener l) { listeners.remove(l); }
	
	@Override
	protected Transferable createTransferable(JComponent c) {
		JList<?> source = (JList<?>) c;
		c.getRootPane().getGlassPane().setVisible(true);
		
		indices = new int[] {source.getSelectedIndex()};
		Object selectedValue = source.getSelectedValue();
		// return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
		return new Transferable() {
			@Override public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] {localObjectFlavor};
			}
			@Override public boolean isDataFlavorSupported(DataFlavor flavor) {
				return Objects.equals(localObjectFlavor, flavor);
			}
			@Override public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
				if (isDataFlavorSupported(flavor)) {
					return selectedValue;
				} else {
					throw new UnsupportedFlavorException(flavor);
				}
			}
		};
	}
	
	@Override
	public boolean canImport(TransferSupport info) {
		return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
	}
	
	@Override
	public int getSourceActions(JComponent c) {
		Component glassPane = c.getRootPane().getGlassPane();
		glassPane.setCursor(DragSource.DefaultMoveDrop);
		return MOVE; // COPY_OR_MOVE;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferSupport info) {
		DropLocation tdl = info.getDropLocation();
		if (!canImport(info) || !(tdl instanceof JList.DropLocation)) {
			return false;
		}
		
		JList.DropLocation dl = (JList.DropLocation) tdl;
		JList target = (JList) info.getComponent();
		DefaultListModel listModel = (DefaultListModel) target.getModel();
		int max = listModel.getSize();
		int index = dl.getIndex();
		index = index < 0 ? max : index; // If it is out of range, it is appended to the end
		index = Math.min(index, max);
		
		addIndex = index;
		
		try {
			Object value = info.getTransferable().getTransferData(localObjectFlavor);
			listModel.add(index, value);
			target.addSelectionInterval(index, index);
			addCount = 1;
			return true;
		} catch (UnsupportedFlavorException | IOException ex) {
			ex.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
		c.getRootPane().getGlassPane().setVisible(false);
		cleanup(c, action == MOVE);
	}
	
	private void cleanup(JComponent c, boolean remove) {
		if (remove && Objects.nonNull(indices)) {
			if (addCount > 0) {
				// https://github.com/aterai/java-swing-tips/blob/master/DragSelectDropReordering/src/java/example/MainPanel.java
				for (int i = 0; i < indices.length; i++) {
					if (indices[i] >= addIndex) {
						indices[i] += addCount;
					}
				}
			}
			JList source = (JList) c;
			DefaultListModel model = (DefaultListModel) source.getModel();
			for (int i = indices.length - 1; i >= 0; i--) {
				model.remove(indices[i]);
			}
		}
		
		indices = null;
		addCount = 0;
		addIndex = -1;
		
		for(TransferListener l: listeners)
			l.orderChanged();
	}
}
