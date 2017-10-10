package table_dialog;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import dataset.Dataset;

/**
 * Label provider of the {@link Dataset}
 * @author avonva
 *
 */
public class DatasetLabelProvider extends ColumnLabelProvider {

	private String key;
	public DatasetLabelProvider(String key) {
		this.key = key;
	}
	
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
	public String getText(Object arg0) {

		Dataset dataset = (Dataset) arg0;
		
		String text = null;
		switch(key) {
		case "id":
			text = dataset.getId();
			break;
		case "senderId":
			text = dataset.getDecomposedSenderId();
			break;
		case "status":
			text = dataset.getStatus().getStatus();
			break;
		case "revision":
			text = dataset.getVersion();
			if (text == null) {
				text = "None";
			}
			break;
		default:
			text = "";
			break;
		}

		return text;
	}
}
