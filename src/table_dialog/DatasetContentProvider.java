package table_dialog;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import dataset.Dataset;
import dataset.DatasetList;

public class DatasetContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}

	@Override
	public Object[] getElements(Object arg0) {
		
		if (arg0 instanceof DatasetList) {
			@SuppressWarnings("unchecked")
			DatasetList<Dataset> list = (DatasetList<Dataset>) arg0;
			return list.toArray();
		}
		
		if (arg0 instanceof Dataset) {
			return new Object[] {arg0};
		}
		
		return null;
	}

}
