package table_database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ListIterator;

import table_skeleton.TableColumn;
import table_skeleton.TableColumn.ColumnType;
import xlsx_reader.TableSchema;
import xlsx_reader.TableSchemaList;

/**
 * This class receives as input an .xlsx file which contains the
 * definitions of the tables/columns of the database, and it creates
 * a query to create these tables/columns with SQL.
 * @author avonva
 *
 */
public class DatabaseStructureCreator {

	public static final String DB_INFO_TABLE = "APP.DB_INFO";
	
	/**
	 * Get a complete query to generate the database
	 * @return 
	 * @throws IOException
	 */
	public String getCreateDatabaseQuery(TableSchemaList tables) throws IOException {
		
		StringBuilder query = new StringBuilder();
		
		// create the tables
		for (TableSchema table : tables)
			query.append(getNewTableQuery(table));
		
		query.append(getDatasetComparisonTableQuery());
		
		// add the key/value table
		query.append(getDbInfoTableQuery(DB_INFO_TABLE));
		
		// add foreign keys after having created the tables
		for (TableSchema table : tables)
			query.append("\n" + getIntegrityConstraintsQuery(table));
		
		return query.toString();
	}
	
	/**
	 * Get the query needed to add a column to an existing table
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	public String getAddNewColumnQuery(String tableName, TableColumn column) {

		// cannot say not null, since if other rows are already inserted 
		// they will get null as value and thus throwing an error
		String colType = column.getType() == ColumnType.FOREIGNKEY ? "integer" : "varchar(1000)";
		
		String query = "ALTER TABLE APP." + tableName + " ADD " 
				+ column.getId() + " " + colType + " ;";
		
		// add integrity constraint if foreign key
		if (column.isForeignKey())
			query = query + getAddForeignKeyQuery(tableName, column);
		
		return query;
	}
	
	/**
	 * Get the query to generate the table
	 * @param table
	 * @return
	 */
	public String getNewTableQuery(TableSchema table) {
		
		StringBuilder query = new StringBuilder();
		
		// create table
		query.append("\ncreate table ")
			.append(table.getSheetName())
			.append("(");
		
		// primary key of table is sheetName + Id
		query.append("\n" + table.getSheetName() + "Id")
			.append(" integer not null primary key generated always as identity (start with 1, increment by 1)");
		
		ListIterator<TableColumn> iter = table.listIterator();
		
		// after primary key add comma if needed
		if (iter.hasNext()) {
			query.append(",");
		}
		
		// for each column create one in the db
		while (iter.hasNext()) {
			
			TableColumn column = iter.next();
			
			query.append("\n" + getNewColumnQuery(column));
			
			// add also the comma if another column
			// will be added
			if (iter.hasNext()) {
				query.append(",");
			}
		}
		
		query.append(");");
		
		return query.toString();
	}
	
	/**
	 * Get the query to generate a new column inside a create table statement
	 * @param col
	 * @return
	 */
	public String getNewColumnQuery(TableColumn col) {
		
		StringBuilder query = new StringBuilder();
		
		query.append(col.getId());
		
		if (col.isForeignKey()) {
			query.append(" integer not null");
		}
		else
			query.append(" varchar(1000)");
		
		return query.toString();
	}
	
	/**
	 * Get the query needed to create the dataset comparison
	 * query (needed for amendments)
	 * @return
	 */
	private String getDatasetComparisonTableQuery() {
		
		StringBuilder query = new StringBuilder();
		
		// create also the dataset comparison table
		query.append("create table APP.DATASET_COMPARISON(\n")
			.append("ROW_ID varchar(100) not null,\n")
			.append("VERSION varchar(50) not null,\n")
			.append("XML_RECORD varchar(30000) not null,\n")
			.append("AM_TYPE varchar(100),\n")
			.append("IS_NULLIFIED varchar(1),\n")
			.append("primary key(ROW_ID, VERSION));\n");
		
		return query.toString();
	}

	/**
	 * Get the query needed to create the info table
	 * for the database (contains version, date of creation...)
	 * @param tableName
	 * @return
	 */
	private String getDbInfoTableQuery(String tableName) {
		
		StringBuilder query = new StringBuilder();
		
		query.append("\ncreate table ")
			.append(tableName)
			.append("(");
		
		query.append("\nVAR_KEY varchar(500) not null primary key,");
		query.append("\nVAR_VALUE varchar(500) not null\n");
		query.append(");\n");
		
		return query.toString();
	}
	
	/**
	 * Get the query needed to generate the foreign keys of a table
	 * @param sheetName
	 */
	public String getAddForeignKeyQuery(String tableName, TableColumn col) {
	
		StringBuilder query = new StringBuilder();

		if (!col.isForeignKey())
			return query.toString();

		// get the foreign table name from the foreign key
		// by removing the Id word
		String foreignTable = col.getId().replace("Id", "");

		query.append("alter table APP.")
		.append(tableName)
		.append(" add foreign key(")
		.append(col.getId())
		.append(") references APP.")
		.append(foreignTable)
		.append("(")
		.append(col.getId())  // (convention) the id name is the same in the foreign table
		.append(") on delete cascade;\n");  // cascade delete is default


		return query.toString();
	}
	
	/**
	 * Get the query to the remove from the database a foreign key constraint
	 * @param tableName
	 * @param foreignKeyName
	 * @return
	 * @throws SQLException
	 */
	public String getRemoveForeignKeyQuery(String tableName, String foreignKeyName)
			throws SQLException {
		
		DatabaseBuilder creator = new DatabaseBuilder();
		ForeignKey key = creator.getForeignKeyByColumnName(tableName, foreignKeyName);

		if (key == null) {
			return "";
		}
		
		StringBuilder query = new StringBuilder();
		query.append("alter table APP.")
			.append(tableName)
			.append(" drop foreign key ")
			.append(key.getName()).append(";");
		
		// make the old foreign key nullable
		query.append("alter table APP.")
			.append(tableName)
			.append(" alter column ")
			.append(foreignKeyName)
			.append(" NULL ")
			.append(";");
		
		return query.toString();
	}

	/**
	 * Get the query needed to generate the foreign keys of a table
	 * @param sheetName
	 */
	private String getIntegrityConstraintsQuery(TableSchema table) {
	
		StringBuilder query = new StringBuilder();
		
		// for each foreign key of the table
		for (TableColumn col : table) {
			
			if (col.isForeignKey()) {
				
				query.append(getAddForeignKeyQuery(table.getSheetName(), col));
			}
		}
		
		return query.toString();
	}
}
