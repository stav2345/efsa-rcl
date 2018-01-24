package app_config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import report.Report;

/**
 * Class to read an xml used to store the properties
 * @author avonva
 *
 */
public class PropertiesReader {
	
	private static final Logger LOGGER = LogManager.getLogger(PropertiesReader.class);
	
	private static final String TECH_SUPPORT_EMAIL_PROPERTY = "TechnicalSupport.Email";
	private static final String DB_REQUIRED_VERSION_PROPERTY = "Db.MinRequiredVersion";
	private static final String APP_NAME_PROPERTY = "Application.Name";
	private static final String APP_VERSION_PROPERTY = "Application.Version";
	private static final String APP_ICON_PROPERTY = "Application.Icon";
	private static final String APP_DC_PATTERN_PROPERTY = "Application.DataCollectionPattern";
	private static final String APP_DC_TABLE_PROPERTY = "Application.DataCollectionTable";
	private static final String APP_DC_TEST_PROPERTY = "Application.DataCollectionTest";
	private static final String APP_DC_STARTING_YEAR = "Application.DataCollectionStartingYear";
	private static final String APP_HELP_REPOSITORY_PROPERTY = "Application.HelpRepository";
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
			LOGGER.error("The properties file was not found. Please check!", e);
		}
		
		return properties;
	}
	
	/**
	 * Get the application name from the properties file
	 * @return
	 */
	public static String getAppName() {
		return getValue(APP_NAME_PROPERTY);
	}
	
	/**
	 * Get the version of the application from the 
	 * properties file
	 * @return
	 */
	public static String getAppVersion() {
		return getValue(APP_VERSION_PROPERTY);
	}
	
	/**
	 * Get the required database version to run
	 * the current application version
	 * @return
	 */
	public static String getMinRequiredDbVersion() {
		return getValue(DB_REQUIRED_VERSION_PROPERTY);
	}
	
	/**
	 * Get the email that the user should contact
	 * in case of technical support need
	 * @return
	 */
	public static String getSupportEmail() {
		return getValue(TECH_SUPPORT_EMAIL_PROPERTY);
	}
	
	/**
	 * Check if the current data collection is the test one
	 * @return
	 */
	public static boolean isTestDataCollection(String reportYear) {
		return getDataCollectionCode(reportYear)
				.equals(getTestDataCollectionCode());
	}
	
	/**
	 * Get the data collection code using the opened report year
	 * to identify it
	 * @return
	 */
	public static String getDataCollectionCode() {
		
		Report report = GlobalManager.getInstance().getOpenedReport();

		if (report == null) {
			LOGGER.debug("No report is opened! Returning " 
					+ getTestDataCollectionCode());
			return getTestDataCollectionCode();
		}
		
		return getDataCollectionCode(report.getYear());
	}
	
	/**
	 * Get the data collection code for which the 
	 * application was created
	 * @return
	 */
	public static String getDataCollectionCode(String reportYear) {

		String dcPattern = getValue(APP_DC_PATTERN_PROPERTY);		

		int reportYearInt = Integer.valueOf(reportYear);
		int startingYear = getDataCollectionStartingYear();
		
		String dcCode = null;
		
		// if the report year is not an available year
		// then use the test data collection
		if (reportYearInt < startingYear) {
			LOGGER.debug("The report year is < than the starting year of the data collection. Using " 
					+ getTestDataCollectionCode() + " instead.");
			dcCode = resolveDCPattern(dcPattern, getDcTestCode());
		}
		else {
			// otherwise use the report year to identify the
			// data collection
			dcCode = resolveDCPattern(dcPattern, reportYear);
		}

		return dcCode;
	}
	
	/**
	 * Get the data collection of test
	 * @return
	 */
	public static String getTestDataCollectionCode() {
		return resolveDCPattern(getValue(APP_DC_PATTERN_PROPERTY), 
				getDcTestCode());
	}
	
	private static String resolveDCPattern(String dataCollectionPattern, Object value) {
		return dataCollectionPattern.replace("yyyy", value.toString());
	}
	
	/**
	 * Get the data collection table for which the 
	 * application was created
	 * @return
	 */
	public static String getDataCollectionTable() {
		return getValue(APP_DC_TABLE_PROPERTY);
	}
	
	/**
	 * Get the code of the data collection of test
	 * @return
	 */
	public static String getDcTestCode() {
		return getValue(APP_DC_TEST_PROPERTY);
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getStartupHelpURL() {
		return getHelpRepositoryURL() + getValue(APP_STARTUP_HELP_PROPERTY);
	}
	
	public static String getHelpRepositoryURL() {
		return getValue(APP_HELP_REPOSITORY_PROPERTY) + "/";
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getAppIcon() {
		return getValue(APP_ICON_PROPERTY);
	}
	
	public static int getDataCollectionStartingYear() {
		
		String year = getValue(APP_DC_STARTING_YEAR);
		
		try {
			return Integer.valueOf(year);
		}
		catch(NumberFormatException e) {
			e.printStackTrace();
			LOGGER.error("Cannot get the data collection starting year. Expected number, found=" + year, e);
			return -1;
		}
	}
	
	
	/**
	 * Get a property value given the key
	 * @param property
	 * @return
	 */
	private static String getValue(String property) {
		
		// use cache if possible
		String cachedValue = cache.get(property);
		if (cachedValue != null)
			return cachedValue;
		
		Properties prop = PropertiesReader.getProperties(AppPaths.APP_CONFIG_FILE);
		
		if ( prop == null )
			return "!" + property + "!";
		
		String value = prop.getProperty(property);
		
		// save the new value in the cache
		cache.put(property, value);
		
		return value;
	}
}
