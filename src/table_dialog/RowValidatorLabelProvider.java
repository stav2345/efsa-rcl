package table_dialog;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import table_skeleton.TableRow;

public abstract class RowValidatorLabelProvider extends ColumnLabelProvider {
	
	@Override
	public void addListener(ILabelProviderListener arg0) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {}

	@Override
	public Image getImage(Object arg0) {
		return null;
	}

	@Override
	public String getText(Object element) {

		TableRow row = (TableRow) element;
		
		return getText(row);
	}

	@Override
	public Color getForeground(Object element) {
		TableRow row = (TableRow) element;
		return getForeground(row);
	}
	
	/**
	 * Text that will be visualized in the data check column
	 * @param row
	 * @return
	 */
	public abstract String getText(TableRow row);
	
	/**
	 * Color of the text of the data check column
	 * @param row
	 * @return
	 */
	public abstract Color getForeground(TableRow row);
}
