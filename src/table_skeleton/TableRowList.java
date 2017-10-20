package table_skeleton;

import java.util.ArrayList;
import java.util.Collection;

import table_database.TableDao;
import xlsx_reader.TableSchema;

public class TableRowList extends ArrayList<TableRow> {
	private static final long serialVersionUID = -3747461146664774866L;
	private TableSchema schema;
	
	public TableRowList(Collection<TableRow> rows) {
		super(rows);
	}
	
	/**
	 * List of table rows. Note that all the table rows should follow the same TableSchema!
	 */
	public TableRowList(TableSchema schema) {
		this.schema = schema;
	}
	
	/**
	 * Get a lighter list in which all the invisible fields
	 * are filtered out. However, note that foreign keys and id
	 * are preserved.
	 * @return
	 */
	public TableRowList filterInvisibleFields() {
		
		TableRowList visibleRows = new TableRowList(schema);
		for (TableRow row : this) {
			TableRow lightRow = row.getVisibleFields();
			visibleRows.add(lightRow);
		}
		
		return visibleRows;
	}
	
	/**
	 * Get an element of the list by the row id
	 * @param id
	 * @return
	 */
	public TableRow getElementById(int id) {
		
		for (TableRow row : this) {
			if (row.getId() == id) {
				return row;
			}
		}
		
		return null;
	}

	/**
	 * Delete the entire list from the db
	 */
	public void deleteAll() {
		TableDao dao = new TableDao(schema);
		dao.delete(this);
	}
}
