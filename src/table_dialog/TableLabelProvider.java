package table_dialog;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import table_skeleton.TableColumn;
import table_skeleton.TableColumnValue;
import table_skeleton.TableRow;

/**
 * Label provider of the {@link TableView}
 * @author avonva
 *
 */
public class TableLabelProvider extends ColumnLabelProvider {

	private String key;
	public TableLabelProvider(String key) {
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

		TableRow row = (TableRow) arg0;
		TableColumnValue cell = row.get(key);

		if (cell == null || cell.getLabel() == null)
			return null;
		
		TableColumn col = row.getSchema().getById(key);
		
		if (col.isPassword()) {
			// show as password with dots
			String ECHARSTR = Character.toString((char)9679);
			return cell.getLabel().replaceAll(".", ECHARSTR);
		}
		else
			return cell.getLabel();
	}
}
