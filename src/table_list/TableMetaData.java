package table_list;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import app_config.AppPaths;
import html_viewer.HtmlViewer;

public class TableMetaData {

	private static Collection<TableMetaData> tables;
	
	private String tableName;
	private String htmlFileName;
	
	public TableMetaData(String tableName, String htmlFileName) {
		this.tableName = tableName;
		this.htmlFileName = htmlFileName;
	}
	
	public String getTableName() {
		return tableName;
	}
	public String getHtmlFileName() {
		return htmlFileName;
	}
	
	/**
	 * Get all the contents of the tables list sheet
	 * @return
	 * @throws IOException
	 */
	public static Collection<TableMetaData> getTables() throws IOException {
		
		// if no cache, parse file and save cache
		if (tables == null) {
			
			tables = new ArrayList<>();
			
			TableListParser parser = new TableListParser(AppPaths.TABLES_SCHEMA_FILE);
			tables = parser.read();
			parser.close();
		}
		

		return tables;
	}
	
	/**
	 * Get a single table of the sheet by its name
	 * @param tableName
	 * @return
	 * @throws IOException
	 */
	public static TableMetaData getTableByName(String tableName) {
		
		TableMetaData table = null;
		
		try {
			
			for (TableMetaData h : getTables()) {
				if (h.getTableName().equals(tableName))
					table = h;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return table;
	}
	
	/**
	 * Open the help in the html viewer
	 * @param shell
	 */
	public void openHelp() {
		
		File file = new File(AppPaths.HELP_FOLDER + htmlFileName);
		HtmlViewer viewer = new HtmlViewer();
		viewer.open(file);
	}
}
