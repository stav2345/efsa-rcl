package app_config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import report.Report;

/**
 * Class to read an xml used to store the properties
 * @author avonva
 *
 */
public class PropertiesReader {

	private static final String APP_NAME_PROPERTY = "Application.Name";
	private static final String APP_VERSION_PROPERTY = "Application.Version";
	private static final String APP_ICON_PROPERTY = "Application.Icon";
	private static final String APP_DC_PATTERN_PROPERTY = "Application.DataCollectionPattern";
	private static final String APP_DC_TABLE_PROPERTY = "Application.DataCollectionTable";
	private static final String APP_TEST_REPORT_PROPERTY = "Application.TestReportCode";
	private static final String APP_STARTUP_HELP_PROPERTY = "Application.StartupHelpFile";
	
	// cache properties, they do not change across time. We avoid
	// continuous access to the file
	private static final HashMap<String, String> cache = new HashMap<>();
	
	/**
	 * Read the application properties from the xml file
	 * @return
	 */
	public static Properties getProperties(String filename) {
		
		Properties properties = null;

		try {
			
			properties = new Properties();

			// fileStream from default properties xml file
			FileInputStream in = new FileInputStream(filename);
			properties.loadFromXML(in);

			in.close();
		}
		catch (IOException e) {
			System.err.println("The default properties file was not found. Please check!");
		}
		
		return properties;
	}
	
	/**
	 * Get the application name from the properties file
	 * @return
	 */
	public static String getAppName() {
		return getValue(APP_NAME_PROPERTY, "not found");
	}
	
	/**
	 * Get the version of the application from the 
	 * properties file
	 * @return
	 */
	public static String getAppVersion() {
		return getValue(APP_VERSION_PROPERTY, "not found");
	}
	
	
	/**
	 * Get the data collection code for which the 
	 * application was created
	 * @return
	 */
	public static String getDataCollectionCode() {
		
		GlobalManager manager = GlobalManager.getInstance();
		
		String value = getValue(APP_DC_PATTERN_PROPERTY, "not found");
		
		boolean isTest = manager.isDcTest();
		
		// if we need to test set the test code
		if (isTest) {
			value = resolveDCPattern(value, getTestReportCode());
		}
		else {
			// get the opened report and set its year in the data
			// collection pattern
			Report openedReport = manager.getOpenedReport();

			if (openedReport != null) {
				value = resolveDCPattern(value, openedReport.getCode(AppPaths.REPORT_YEAR));
			}
		}

		return value;
	}
	
	/**
	 * Get the data collection of test
	 * @return
	 */
	public static String getTestDataCollectionCode() {
		return resolveDCPattern(getValue(APP_DC_PATTERN_PROPERTY, "not found"), 
				getTestReportCode());
	}
	
	private static String resolveDCPattern(String dataCollectionPattern, String value) {
		return dataCollectionPattern.replace("yyyy", value);
	}
	
	/**
	 * Get the data collection table for which the 
	 * application was created
	 * @return
	 */
	public static String getDataCollectionTable() {
		return getValue(APP_DC_TABLE_PROPERTY, "not found");
	}
	
	/**
	 * Get the test report code
	 * @return
	 */
	public static String getTestReportCode() {
		return getValue(APP_TEST_REPORT_PROPERTY, "not found");
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getStartupHelpFileName() {
		return getValue(APP_STARTUP_HELP_PROPERTY, "not found");
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getAppIcon() {
		return getValue(APP_ICON_PROPERTY, "not found");
	}
	
	
	/**
	 * Get a property value given the key
	 * @param property
	 * @return
	 */
	private static String getValue(String property, String defaultValue) {
		
		// use cache if possible
		String cachedValue = cache.get(property);
		if (cachedValue != null)
			return cachedValue;
		
		Properties prop = PropertiesReader.getProperties(AppPaths.APP_CONFIG_FILE);
		
		if ( prop == null )
			return defaultValue;
		
		String value = prop.getProperty(property);
		
		// save the new value in the cache
		cache.put(property, value);
		
		return value;
	}
}
