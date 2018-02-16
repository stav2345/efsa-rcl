package table_database;

import java.sql.ResultSet;
import java.sql.SQLException;

import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import xlsx_reader.TableSchema;

/**
 * Dao which communicates with the database and all the tables
 * that follow a {@link TableSchema}. These tables are automatically
 * generated starting from the excel file, therefore the dao
 * adapts the query using their structure.
 * @author avonva
 *
 */
public interface ITableDao {
	
	/**
	 * Add a new row to the table
	 * @param row
	 * @return
	 */
	public int add(TableRow row);
	
	/**
	 * Add a new row to the table
	 * @param row
	 * @return
	 */
	public boolean update(TableRow row);

	/**
	 * Delete all the rows from the table
	 * @param row
	 * @return
	 */
	public boolean deleteAll(TableSchema schema);
	
	/**
	 * Remove all the rows where the parent id is equal to {@code parentId} in the parent table {@code parentTable}
	 * @param row
	 * @return
	 */
	public boolean deleteByParentId(TableSchema schema, String parentTable, int parentId);
	
	/**
	 * Get a row from the result set
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public TableRow getByResultSet(TableSchema schema, ResultSet rs, boolean solveFormulas) throws SQLException;
	
	public TableRowList getByParentId(TableSchema schema, String parentTable, int parentId);
	
	public TableRowList getByParentId(TableSchema schema, String parentTable, int parentId, boolean solveFormulas);
	
	/**
	 * Get all the rows that has as parent the {@code parentId} in the parent table {@code parentTable}
	 * @param row
	 * @return
	 */
	public TableRowList getByParentId(TableSchema schema, String parentTable, int parentId, boolean solveFormulas, String order);
	
	/**
	 * Get all the rows from the table
	 * @param row
	 * @return
	 */
	public TableRowList getAll(TableSchema schema);
	
	/**
	 * Remove a row by its id
	 * @param rowId
	 * @return
	 */
	public boolean delete(TableSchema schema, int rowId);
	
	public boolean delete(TableRowList list);

	/**
	 * Delete all the records by a database field
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public boolean deleteByStringField(TableSchema schema, String fieldName, String value);
	/**
	 * Get the row by its id
	 * @param id
	 * @return
	 */
	public TableRow getById(TableSchema schema, int id);

	/**
	 * Get the all the rows that matches the fieldName with value
	 * @param id
	 * @return
	 */
	public TableRowList getByStringField(TableSchema schema, String fieldName, String value);
}
