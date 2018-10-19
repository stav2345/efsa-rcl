package mocks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import table_database.ITableDao;
import table_relations.Relation;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import xlsx_reader.TableSchema;
import xlsx_reader.TableSchemaList;

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
			
			TableRow c = iterator.next();
			
			if (c.getSchema().equals(row.getSchema()) && c.getDatabaseId() == row.getDatabaseId()) {
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
		TableRowList rows = getByParentId(schema, parentTable, parentId);
		return delete(rows);
	}

	@Override
	public TableRow getByResultSet(TableSchema schema, ResultSet rs, boolean solveFormulas) throws SQLException {
		return null;
	}

	@Override
	public TableRowList getByParentId(TableSchema schema, String parentTable, int parentId) {
		
		TableRowList list = new TableRowList();
		
		for(TableRow row: db) {
			
			Relation r = schema.getRelationByParentTable(parentTable);
			
			String pId = row.getCode(r.getForeignKey());
			
			if (pId.isEmpty())
				continue;
			
			// get the parent from the db
			TableRow parent = getById(TableSchemaList.getByName(parentTable), 
					Integer.valueOf(pId));

			if(row.getSchema().equals(schema) && parent.getDatabaseId() == parentId)
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
		
		TableRowList out = new TableRowList();
		
		for(TableRow row: db)
			if (row.getSchema().equals(schema))
				out.add(row);
		
		return out;
	}

	@Override
	public boolean delete(TableSchema schema, int rowId) {
		
		Iterator<TableRow> iterator = db.iterator();
		
		boolean hasUpdated = false;
		
		while(iterator.hasNext()) {
			TableRow row = iterator.next();
			
			if (row.getSchema().equals(schema) && row.getDatabaseId() == rowId) {
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

		for(TableRow row: list)
			delete(row.getSchema(), row.getDatabaseId());
		
		return true;
	}

	@Override
	public boolean deleteByStringField(TableSchema schema, String fieldName, String value) {
		Iterator<TableRow> iterator = db.iterator();
		
		boolean hasUpdated = false;
		
		while(iterator.hasNext()) {
			
			TableRow row = iterator.next();
			
			if (row.getSchema().equals(schema) && row.get(fieldName).equals(value)) {
				iterator.remove();
				hasUpdated = true;
			}
		}
		
		return hasUpdated;
	}

	@Override
	public TableRow getById(TableSchema schema, int id) {
		
		for(TableRow row: db) {
			if (row.getSchema().equals(schema) && row.getDatabaseId() == id)
				return row;
		}
		
		return null;
	}

	@Override
	public TableRowList getByStringField(TableSchema schema, String fieldName, String value) {
		
		TableRowList list = new TableRowList();
		for(TableRow row: db) {
			if (row.getSchema().equals(schema) && row.getCode(fieldName).equals(value))
				list.add(row);
		}
		
		return list;
	}

}
