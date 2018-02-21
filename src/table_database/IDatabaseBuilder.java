package table_database;

import java.io.IOException;
import java.sql.SQLException;

import table_skeleton.TableColumn;
import xlsx_reader.TableSchema;

/**
 * Class which creates the application database. It uses an .xlsx file
 * to define the structure (tables/columns) of the database.
 * @author avonva
 *
 */
public interface IDatabaseBuilder {
	
	/**
	 * Create the application database in the defined path
	 * @param path
	 * @throws IOException
	 */
	public void create(String path) throws IOException;
	
	
	/**
	 * Create a new table
	 * @param table
	 * @throws SQLException
	 * @throws IOException 
	 */
	public void createTable(TableSchema table) throws SQLException, IOException;
	
	/**
	 * Add a column to a table
	 * @param schema
	 * @param column
	 * @throws IOException
	 * @throws SQLException 
	 */
	public void addColumnToTable(TableSchema schema, TableColumn column) throws IOException, SQLException;
	
	/**
	 * Add a foreign key to the database
	 * @param schema
	 * @param foreignKey
	 * @throws IOException
	 * @throws SQLException 
	 */
	public void addForeignKey(TableSchema schema, TableColumn foreignKey) throws IOException, SQLException;
	
	/**
	 * Remove the foreign key from the table in the database
	 * @param schema
	 * @param foreignKey
	 * @throws IOException
	 * @throws SQLException 
	 */
	public void removeForeignKey(TableSchema schema, TableColumn foreignKey) 
			throws IOException, SQLException;
	
	/**
	 * Get a foreign key property using its column name
	 * @param foreignKeyColName
	 * @return
	 * @throws SQLException
	 */
	public ForeignKey getForeignKeyByColumnName(String fkTableName, String foreignKeyColName) throws SQLException;
}
