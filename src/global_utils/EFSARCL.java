package global_utils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import app_config.AppPaths;
import table_database.Database;
import table_database.DatabaseVersionException;

/**
 * Component which initialises the EFSA Report Creator Library
 * @author avonva
 *
 */
public class EFSARCL {

	/**
	 * Initialize the EFSA Report Creator Library. This will check important files existance
	 * and will update the database if needed.
	 * @throws IOException
	 * @throws SQLException
	 * @throws DatabaseVersionException 
	 */
	public static void init() throws IOException, SQLException, DatabaseVersionException {
		
		// create folders if they do not exist
		FileUtils.createFolder(AppPaths.CONFIG_FOLDER);
		FileUtils.createFolder(AppPaths.TEMP_FOLDER);
		FileUtils.createFolder(AppPaths.XML_FOLDER);
		
		checkConfigFiles(AppPaths.APP_CONFIG_FILE, AppPaths.CONFIG_FOLDER);
		checkConfigFiles(AppPaths.MESSAGE_GDE2_XSD, AppPaths.CONFIG_FOLDER);
		checkConfigFiles(AppPaths.TABLES_SCHEMA_FILE, AppPaths.CONFIG_FOLDER);
		
		// clear the temporary folder
		FileUtils.clearTempFolder();
		
		// update the database if needed
		Database db = new Database();
		db.update();
	}
	
	/**
	 * Check if a file is present
	 * @param filePath
	 * @param folder
	 * @throws IOException
	 */
	public static void checkConfigFiles(String filePath, String folder) throws IOException {
		
		File configFile = new File(filePath);
		if(!configFile.exists()) {
			throw new IOException("The " + filePath 
					+ " is not present in the " 
					+ folder 
					+ ". The library cannot work without that.");
		}
	}
}
