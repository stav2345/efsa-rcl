package table_dialog;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import table_skeleton.TableRowList;

/**
 * Content provider of the {@link TableView}
 * @author avonva
 *
 */
public class TableContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer arg0, Object oldInput, Object newInput) {}

	@Override
	public Object[] getElements(Object arg0) {
		return ((TableRowList) arg0).filterInvisibleFields().toArray();
	}
}
