package mocks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import table_database.ITableDao;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import xlsx_reader.TableSchema;

public class TableDaoMock implements ITableDao {

	private TableRowList db;
	
	public TableDaoMock() {
		db = new TableRowList();
	}
	
	@Override
	public int add(TableRow row) {
		db.add(row);
		return (int) (Math.random() * 10000.000);
	}

	@Override
	public boolean update(TableRow row) {
		Iterator<TableRow> iterator = db.iterator();
		
		boolean hasUpdated = false;
		
		while(iterator.hasNext()) {
			if (iterator.next().getDatabaseId() == row.getDatabaseId()) {
				iterator.remove();
				hasUpdated = true;
			}
		}

		if (hasUpdated)
			db.add(row);
		
		return hasUpdated;
	}

	@Override
	public boolean deleteAll(TableSchema schema) {
		Iterator<TableRow> iterator = db.listIterator();
		while(iterator.hasNext()) {
			if (iterator.next().getSchema().equals(schema))
				iterator.remove();
		}
		return true;
	}

	@Override
	public boolean deleteByParentId(TableSchema schema, String parentTable, int parentId) {
		
		Iterator<TableRow> iterator = db.iterator();
		
		boolean hasUpdated = false;
		
		while(iterator.hasNext()) {
			
			TableRow parent = iterator.next();
			
			boolean isParent = parent.getSchema().getSheetName().equals(parentTable);
			
			if (isParent && parent.getDatabaseId() == parentId) {
				iterator.remove();
				hasUpdated = true;
			}
		}
		
		return hasUpdated;
	}

	@Override
	public TableRow getByResultSet(TableSchema schema, ResultSet rs, boolean solveFormulas) throws SQLException {
		return null;
	}

	@Override
	public TableRowList getByParentId(TableSchema schema, String parentTable, int parentId) {
		
		TableRowList list = new TableRowList();
		
		for(TableRow row: db) {
			if(row.getSchema().getSheetName().equals(parentTable) && row.getDatabaseId() == parentId)
				list.add(row);				
		}

		return list;
	}

	@Override
	public TableRowList getByParentId(TableSchema schema, String parentTable, int parentId, boolean solveFormulas) {
		return getByParentId(schema, parentTable, parentId);
	}

	@Override
	public TableRowList getByParentId(TableSchema schema, String parentTable, int parentId, boolean solveFormulas, String order) {
		return getByParentId(schema, parentTable, parentId);
	}

	@Override
	public TableRowList getAll(TableSchema schema) {
		return db;
	}

	@Override
	public boolean delete(TableSchema schema, int rowId) {
		
		Iterator<TableRow> iterator = db.iterator();
		
		boolean hasUpdated = false;
		
		while(iterator.hasNext()) {
			if (iterator.next().getDatabaseId() == rowId) {
				iterator.remove();
				hasUpdated = true;
			}
		}
		
		return hasUpdated;
	}

	@Override
	public boolean delete(TableRowList list) {
		
		if (list.isEmpty())
			return true;
		
		TableSchema schema = list.get(0).getSchema();
		
		for(TableRow row: list)
			delete(schema, row.getDatabaseId());
		
		return true;
	}

	@Override
	public boolean deleteByStringField(TableSchema schema, String fieldName, String value) {
		Iterator<TableRow> iterator = db.iterator();
		
		boolean hasUpdated = false;
		
		while(iterator.hasNext()) {
			if (iterator.next().get(fieldName).equals(value)) {
				iterator.remove();
				hasUpdated = true;
			}
		}
		
		return hasUpdated;
	}

	@Override
	public TableRow getById(TableSchema schema, int id) {
		
		for(TableRow row: db) {
			if (row.getDatabaseId() == id)
				return row;
		}
		
		return null;
	}

	@Override
	public TableRowList getByStringField(TableSchema schema, String fieldName, String value) {
		
		TableRowList list = new TableRowList();
		for(TableRow row: db) {
			if (row.get(fieldName).equals(value))
				list.add(row);
		}
		
		return list;
	}

}
