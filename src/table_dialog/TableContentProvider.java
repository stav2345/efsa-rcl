package table_dialog;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import table_skeleton.TableRow;

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

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object arg0) {
		return ((Collection<TableRow>) arg0).toArray();
	}
}
