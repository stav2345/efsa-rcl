package app_config;

public class AppPaths {
	
	// application folders
	public static final String XML_FOLDER = "data" + System.getProperty("file.separator");
	public static final String CONFIG_FOLDER = "config" + System.getProperty("file.separator");
	public static final String HELP_FOLDER = "help" + System.getProperty("file.separator");
	public static final String TEMP_FOLDER = "temp" + System.getProperty("file.separator");
	public static final String DB_FOLDER = "database" + System.getProperty("file.separator");
	
	// config files
	public static final String TABLES_SCHEMA_FILE = CONFIG_FOLDER + "tablesSchema.xlsx";
	public static final String APP_CONFIG_FILE = CONFIG_FOLDER + "appConfig.xml";
	public static final String MESSAGE_GDE2_XSD = CONFIG_FOLDER + "GDE2_message.xsd";
	
	// TABLES_SCHEMA_FILE special sheets used for other purposes
	public static final String RELATIONS_SHEET = "Relations";
	public static final String TABLES_SHEET = "Tables";
	public static final String MESSAGE_CONFIG_SHEET = "MessageConfig";
	
	// column of the op type field in the message config sheet
	public static final String MESSAGE_CONFIG_OP_TYPE = "opType";
	
	// the catalogue of months
	public static final String MONTHS_LIST = "monthsList";

	public static final String REPORT_SENDER_ID = "reportSenderId";
	public static final String REPORT_MESSAGE_ID = "reportMessageId";
	public static final String REPORT_DATASET_ID = "reportDatasetId";
	public static final String REPORT_STATUS = "reportStatus";
	public static final String REPORT_VERSION = "reportVersion";
}
