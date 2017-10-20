package xlsx_reader;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Sheet;

import app_config.AppPaths;
import table_list.TableListParser;
import table_relations.RelationParser;

public class TableSchemaList extends ArrayList<TableSchema> {

	private static final long serialVersionUID = 1L;
	
	private static TableSchemaList schemasCache;

	/**
	 * Get all the table schemas which were defined by the user
	 * @return
	 * @throws IOException
	 */
	public static TableSchemaList getAll() throws IOException {
		
		// if first time
		if (schemasCache == null) {
			
			schemasCache = new TableSchemaList();
			
			SchemaReader parser = new SchemaReader(AppPaths.TABLES_SCHEMA_FILE);
			
			for (int i = 0; i < parser.getNumberOfSheets(); ++i) {
				
				parser = new SchemaReader(AppPaths.TABLES_SCHEMA_FILE);
				
				Sheet sheet = parser.getSheetAt(i);
				
				// skip special sheets
				if (RelationParser.isRelationsSheet(sheet.getSheetName())
						|| TableListParser.isTablesSheet(sheet.getSheetName()))
					continue;
				
				// parse
				parser.read(sheet.getSheetName());
				
				// get parsed schema
				TableSchema schema = parser.getSchema();
				
				// add to cache
				schemasCache.add(schema);
			}

			parser.close();
		}

		return schemasCache;
	}

	/**
	 * Load a generic schema from the {@link CustomPaths#TABLES_SCHEMA_FILE} file
	 * using the {@code sheetName} sheet
	 * @param sheetName
	 * @return
	 * @throws IOException
	 */
	public static TableSchema getByName(String sheetName) {
		
		TableSchemaList schemas;
		try {
			schemas = getAll();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		for (TableSchema schema : schemas) {
			
			if (schema.getSheetName().equals(sheetName))
				return schema;
		}

		return null;
	}
	
}
