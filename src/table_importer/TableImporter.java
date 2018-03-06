package table_importer;

import java.util.Collection;

import providers.ITableDaoService;
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

	private ITableDaoService daoService;
	
	public TableImporter(ITableDaoService daoService) {
		this.daoService = daoService;
	}
	
	/**
	 * Copy all the children of a parent table into the children
	 * of another parent table.
	 * @param childSchema schema of the children
	 * @param parentToCopy parent whose rows will be copied
	 * @param parentToWrite parent whose rows will be replaced by the copied ones
	 */
	public void copyByParent(TableSchema childSchema, 
			TableRow parentToCopy, TableRow parentToWrite) {
		
		String parentTable = parentToCopy.getSchema().getSheetName();
		int parentToCopyId = parentToCopy.getDatabaseId();
		
		// load all the rows of the parent we want to copy
		Collection<TableRow> rowsToCopy = daoService.getByParentId(childSchema, parentTable, parentToCopyId, true);
		
		// remove all the rows from the parent we want to override
		int parentToWriteId = parentToWrite.getDatabaseId();
		daoService.deleteByParentId(childSchema, parentTable, parentToWriteId);
		
		// for each copied row, insert it into the
		// parentToWrite table
		for (TableRow row : rowsToCopy) {
			
			TableRow copiedRow = new TableRow(row.getSchema());
			copiedRow.copyValues(row);
			
			// set as new parent the parentToWrite parent
			Relation.injectParent(parentToWrite, copiedRow);
			filterRowData(copiedRow);
			
			// add the row
			daoService.add(copiedRow);
		}
	}
	
	/**
	 * Manage and filter the row which will be inserted in the new report
	 * @param row
	 */
	public abstract void filterRowData(TableRow row);
}
