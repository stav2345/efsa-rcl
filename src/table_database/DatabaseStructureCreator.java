package table_database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import table_list.TableListParser;
import table_relations.RelationParser;
import table_skeleton.TableColumn.ColumnType;
import xlsx_reader.TableHeaders.XlsxHeader;
import xlsx_reader.XlsxReader;

/**
 * This class receives as input an .xlsx file which contains the
 * definitions of the tables/columns of the database, and it creates
 * a query to create these tables/columns with SQL.
 * @author avonva
 *
 */
public class DatabaseStructureCreator extends XlsxReader {

	private StringBuilder query;  // it contains the query to create the database
	private ColumnType currentColumnType;
	private String currentColumnId;
	private List<String> foreignKeys;
	private boolean isCompositeCode;
	private boolean isCompositeLabel;
	
	/**
	 * Initialize the creator.
	 * @param filename the .xlsx file which contains the tables schema
	 * @throws IOException
	 */
	public DatabaseStructureCreator(String filename) throws IOException {
		super(filename);
		this.isCompositeCode = false;
		this.isCompositeLabel = false;
		this.foreignKeys = new ArrayList<>();
	}

	/**
	 * Get a complete query to generate the database
	 * @return 
	 * @throws IOException
	 */
	public String getQuery() throws IOException {
		
		query = new StringBuilder();
		
		// for each excel sheet create the table with the proper columns
		// which are defined in the columns schema
		for (Sheet sheet : getSheets()) {
			
			// skip special sheets
			if (RelationParser.isRelationsSheet(sheet.getSheetName())
					|| TableListParser.isTablesSheet(sheet.getSheetName()))
				continue;
			
			addTableStatement(sheet.getSheetName());
		}
		System.out.println(query.toString());
		return query.toString();
	}

	/**
	 * Add a single table statement with all the columns defined
	 * in the sheet rows
	 * @param sheetName
	 * @throws IOException
	 */
	private void addTableStatement(String sheetName) throws IOException {
		
		// add the "create table" statement
		// using the sheet name as table name
		addCreateStatement(sheetName);
		
		// add the primary key
		addPrimaryKeyStatement(sheetName + "Id");
		
		// add the columns to the statement (also primary key)
		addColumnsStatement(sheetName);
		
		// add foreign keys
		addIntegrityConstraints(sheetName);
	}
	
	/**
	 * Add the create table statement
	 * @param tableName
	 */
	private void addCreateStatement(String tableName) {
		
		query.append("create table APP.")
			.append(tableName)
			.append("(\n");
	}
	
	/**
	 * Add a primary key to the table, it should be the first variable
	 * @param primaryKeyName
	 */
	private void addPrimaryKeyStatement(String primaryKeyName) {

		query.append(primaryKeyName)
			.append(" integer not null primary key generated always as identity (start with 1, increment by 1)");
	}
	
	/**
	 * Add all the columns statement using the id field of the row of the sheet
	 * as column name. All the columns are by default varchar.
	 * @param sheetName
	 * @throws IOException
	 */
	private void addColumnsStatement(String sheetName) throws IOException {

		// read the sheet to activate processCell and start/end row methods
		this.read(sheetName);
	}
	

	/**
	 * 
	 * @param sheetName
	 */
	private void addIntegrityConstraints(String sheetName) {
	
		for (String foreignKey : foreignKeys) {
			
			// get the foreign table name from the foreign key
			// by removing the Id word
			String foreignTable = foreignKey.replace("Id", "");
			
			query.append("alter table APP.")
				.append(sheetName)
				.append(" add foreign key(")
				.append(foreignKey)
				.append(") references APP.")
				.append(foreignTable)
				.append("(")
				.append(foreignKey)  // (convention) the id name is the same in the foreign table
				.append(") on delete cascade;\n");  // cascade delete is default
		}
		
		// restart
		foreignKeys.clear();
	}
	
	@Override
	public void processCell(String header, String value) {

		XlsxHeader h = null;
		try {
			h = XlsxHeader.fromString(header);  // get enum from string
		}
		catch(IllegalArgumentException e) {
			return;
		}
		
		// we need just the ID to create the table
		if (h == XlsxHeader.ID) {
			this.currentColumnId = value;
		}
		else if (h == XlsxHeader.TYPE) {

			// get the type of field
			this.currentColumnType = ColumnType.fromString(value);
		}
		/*else if (h == XlsxHeader.CODE_FORMULA) {
			this.isCompositeCode = !(value == null || value.isEmpty() || value.equals("null"));
		}
		else if (h == XlsxHeader.LABEL_FORMULA) {
			this.isCompositeLabel = !(value == null || value.isEmpty() || value.equals("null"));
		}*/
	}

	@Override
	public void startRow(Row row) {
		
		// reset values
		this.isCompositeCode = false;
		this.isCompositeLabel = false;
	}

	@Override
	public void endRow(Row row) {

		if (this.currentColumnType == null) {
			
			System.err.println("Empty " + XlsxHeader.TYPE + " field found. Setting " 
					+ ColumnType.STRING + " as default.");
			
			this.currentColumnType = ColumnType.STRING;
		}


		// add the field just for non composite fields
		//if (!this.isCompositeCode && !this.isCompositeLabel) {

			// new variable
			query.append(",\n");
			
			// append the id name as variable name
			// set the field as string
			switch (this.currentColumnType) {
			case FOREIGNKEY:
				
				// save the foreign key column id
				foreignKeys.add(this.currentColumnId);
				
				query.append(this.currentColumnId)
				.append(" integer not null");
				break;
			default:
				query.append(this.currentColumnId)
				.append(" varchar(1000)");
				break;
			}
		//}
		
			
		int last = row.getSheet().getLastRowNum();
			
		boolean isLast = row.getRowNum() == last;
			
		if (isLast) {
			// if last row, then close the
			// create table statement and put a semicolon
			query.append(");\n\n");
		}
	}
}
