package data_collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import data_collection.DcfDataCollectionsList;

public class DataCollectionContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}

	@Override
	public Object[] getElements(Object arg0) {
		
		if (arg0 instanceof DcfDataCollectionsList) {
			DcfDataCollectionsList list = (DcfDataCollectionsList) arg0;
			return list.toArray();
		}
		
		return null;
	}
}
