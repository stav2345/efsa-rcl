package table_importer;

import java.util.Collection;

import table_database.TableDao;
import table_relations.Relation;
import table_skeleton.TableRow;
import xlsx_reader.TableSchema;

/**
 * Copy all the children of a parent table into the children
 * of another parent table. 
 * @author avonva
 *
 */
public abstract class TableImporter {

	/**
	 * Copy all the children of a parent table into the children
	 * of another parent table.
	 * @param childSchema schema of the children
	 * @param parentToCopy parent whose rows will be copied
	 * @param parentToWrite parent whose rows will be replaced by the copied ones
	 */
	public void copyByParent(TableSchema childSchema, 
			TableRow parentToCopy, TableRow parentToWrite) {
		
		TableDao dao = new TableDao(childSchema);
		
		String parentTable = parentToCopy.getSchema().getSheetName();
		int parentToCopyId = parentToCopy.getDatabaseId();
		
		// load all the rows of the parent we want to copy
		Collection<TableRow> rowsToCopy = dao.getByParentId(parentTable, parentToCopyId);
		
		// remove all the rows from the parent we want to override
		TableDao writeDao = new TableDao(childSchema);
		int parentToWriteId = parentToWrite.getDatabaseId();
		writeDao.deleteByParentId(parentTable, parentToWriteId);
		
		// for each copied row, insert it into the
		// parentToWrite table
		for (TableRow row : rowsToCopy) {
			
			// set as new parent the parentToWrite parent
			Relation.injectParent(parentToWrite, row);
			
			filterRowData(row);
			
			// add the row
			writeDao.add(row);
		}
	}
	
	/**
	 * Manage and filter the row which will be inserted in the new report
	 * @param row
	 */
	public abstract void filterRowData(TableRow row);
}
