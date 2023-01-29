package table_list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.poi.ss.usermodel.Row;

import app_config.AppPaths;
import xlsx_reader.XlsxReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Read all the help files from the .xlsx
 * @author avonva
 *
 */
public class TableListParser extends XlsxReader {
	
	private static final Logger LOGGER = LogManager.getLogger(TableListParser.class);

	private String tableName;
	private String htmlFilename;
	
	private Collection<TableMetaData> helps;
	
	public TableListParser(String filename) throws IOException {
		super(filename);
		helps = new ArrayList<>();
	}
	
	/**
	 * Read all the relations and returns it
	 * @return
	 * @throws IOException
	 */
	public Collection<TableMetaData> read() throws IOException {
		super.read(AppPaths.TABLES_SHEET);
		return helps;
	}
	
	public static boolean isTablesSheet(String sheetName) {
		return AppPaths.TABLES_SHEET.equals(sheetName);
	}

	@Override
	public void processCell(String header, String value) {
		
		TablesHeader h = null;
		try {
			h = TablesHeader.fromString(header);  // get enum from string
		}
		catch(IllegalArgumentException e) {
			LOGGER.error("Error in processing cell ", e);
			return;
		}

		if(h == null)
			return;
		
		switch(h) {
		case TABLE_NAME:
			
			this.tableName = value;
			break;
		case HTML_FILENAME:
			this.htmlFilename = value;
			break;
		}
	}

	@Override
	public void startRow(Row row) {}

	@Override
	public void endRow(Row row) {
		
		if(tableName == null || htmlFilename == null)
			return;
		
		// create a new help and put it into the collection
		TableMetaData help = new TableMetaData(tableName, htmlFilename);
		helps.add(help);
	}
}
