package global_utils;

import java.io.File;

import app_config.AppPaths;

public class FileUtils {
	
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
				System.err.println("Failed to create folder " + folderName);
			}
		}
	}
}
