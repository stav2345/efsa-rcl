package global_utils;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import app_config.AppPaths;

public class FileUtils {
	
	private static final Logger LOGGER = LogManager.getLogger(FileUtils.class);
	
	/**
	 * Clear the temporary folder
	 */
	public static void clearTempFolder() {
		File dir = new File(AppPaths.TEMP_FOLDER);
		for (File file : dir.listFiles()) {
			file.delete();
		}
	}
	
	/**
	 * Create a folder
	 * @param folderName
	 */
	public static void createFolder(String folderName) {
		File file = new File(folderName);
		if(!file.exists()) {
			if(!file.mkdir()) {
				LOGGER.error("Failed to create folder " + folderName);
			}
		}
	}
	
	/**
	 * Generate a temporary file
	 * @return
	 */
	public static File generateTempFile(String format) {
		
		createFolder(AppPaths.TEMP_FOLDER);
		
		String filename = AppPaths.TEMP_FOLDER + "temp-" + TimeUtils.getTodayTimestamp() + format;
		
		return new File(filename);
	}
}
