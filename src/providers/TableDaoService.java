package providers;

import table_database.ITableDao;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import xlsx_reader.TableSchema;

public class TableDaoService implements ITableDaoService {

	private ITableDao dao;
	
	public TableDaoService(ITableDao dao) {
		this.dao = dao;
	}
	
	@Override
	public int add(TableRow row) {
		int id = dao.add(row);
		row.setId(id);
		return id;
	}

	@Override
	public boolean update(TableRow row) {
		return dao.update(row);
	}
	
	@Override
	public TableRowList getAll(TableSchema schema) {
		return dao.getAll(schema);
	}

	@Override
	public TableRow getById(TableSchema schema, int id) {
		return dao.getById(schema, id);
	}

	@Override
	public TableRowList getByParentId(TableSchema schema, String parentTable, int parentId, boolean solveFormulas) {
		return dao.getByParentId(schema, parentTable, parentId, solveFormulas);
	}
}
