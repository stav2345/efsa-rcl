package data_collection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import dataset.Dataset;

/**
 * Label provider of the {@link Dataset}
 * @author avonva && shahaal
 *
 */
public class DataCollectionLabelProvider extends ColumnLabelProvider {

	public static final String STD_DATE_FORMAT = "yyyy-MM-dd";
	
	private String key;
	public DataCollectionLabelProvider(String key) {
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

		IDcfDataCollection dc = (IDcfDataCollection) arg0;
		
		String text = null;
		switch(key) {
		case "id":
			text = String.valueOf(dc.getId());
			break;
		case "code":
			text = dc.getCode();
			break;
		case "description":
			text = dc.getDescription();
			break;
		case "activeFrom":
			DateFormat sdf = new SimpleDateFormat(STD_DATE_FORMAT); 
			text = sdf.format(dc.getActiveFrom());
			break;
		case "activeTo":
			sdf = new SimpleDateFormat(STD_DATE_FORMAT); 
			text = sdf.format(dc.getActiveTo());
			break;
		case "category":
			text = dc.getCategory();
			break;
		case "resId":
			text = dc.getResourceId();
			break;
		default:
			text = "";
			break;
		}

		return text;
	}
}
