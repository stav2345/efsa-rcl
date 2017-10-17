package global_utils;

import java.io.File;
import java.io.IOException;

import app_config.AppPaths;

public class EFSARCL {

	public static void initialize() throws IOException {
		
		// create folders if they do not exist
		FileUtils.createFolder(AppPaths.CONFIG_FOLDER);
		FileUtils.createFolder(AppPaths.HELP_FOLDER);
		FileUtils.createFolder(AppPaths.TEMP_FOLDER);
		FileUtils.createFolder(AppPaths.XML_FOLDER);
		
		checkConfigFiles(AppPaths.APP_CONFIG_FILE, AppPaths.CONFIG_FOLDER);
		checkConfigFiles(AppPaths.MESSAGE_GDE2_XSD, AppPaths.CONFIG_FOLDER);
		checkConfigFiles(AppPaths.TABLES_SCHEMA_FILE, AppPaths.CONFIG_FOLDER);
		
		// clear the temporary folder
		FileUtils.clearTempFolder();
	}
	
	/**
	 * Check if a file is present
	 * @param filePath
	 * @param folder
	 * @throws IOException
	 */
	private static void checkConfigFiles(String filePath, String folder) throws IOException {
		
		File configFile = new File(filePath);
		if(!configFile.exists()) {
			throw new IOException("The " + filePath 
					+ " is not present in the " 
					+ folder 
					+ ". The library cannot work without that.");
		}
	}
}
