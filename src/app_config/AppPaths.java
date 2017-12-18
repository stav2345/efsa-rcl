package app_config;

public class AppPaths {
	
	// application folders
	public static final String XML_FOLDER = "picklists" + System.getProperty("file.separator");
	public static final String CONFIG_FOLDER = "config" + System.getProperty("file.separator");
	public static final String TEMP_FOLDER = "temp" + System.getProperty("file.separator");
	public static final String DB_FOLDER = "database" + System.getProperty("file.separator");
	public static final String COMPAT_FOLDER = "compat" + System.getProperty("file.separator");
	
	// config files
	public static final String TABLES_SCHEMA_FILENAME = "tablesSchema";
	public static final String TABLES_SCHEMA_FORMAT = ".xlsx";
	public static final String TABLES_SCHEMA_FILE = CONFIG_FOLDER + TABLES_SCHEMA_FILENAME + TABLES_SCHEMA_FORMAT;
	public static final String APP_CONFIG_FILE = CONFIG_FOLDER + "appConfig.xml";
	public static final String MESSAGE_GDE2_XSD = CONFIG_FOLDER + "GDE2_message.xsd";
	public static final String REPORT_ERRORS_HEAD_FILE = CONFIG_FOLDER + "ReportErrorsHtmlHead.txt";
	
	// TABLES_SCHEMA_FILE special sheets used for other purposes
	public static final String RELATIONS_SHEET = "Relations";
	public static final String TABLES_SHEET = "Tables";
	public static final String MESSAGE_CONFIG_SHEET = "MessageConfig";

	// op type which will be used in the .xml
	public static final String MESSAGE_CONFIG_OP_TYPE = "opType";
	
	// op type code which is used just to identify the action
	// this should not be exported
	public static final String MESSAGE_CONFIG_INTERNAL_OP_TYPE = "internalOpType";
	
	// the catalogue of months
	public static final String YEARS_LIST = "yearsList";
	public static final String MONTHS_LIST = "monthsList";
	
	public static final String CHILDREN_CONTAIN_ERRORS_COL = "childrenContainErrors";

	public static final String REPORT_SHEET = "Report";
	public static final String REPORT_YEAR = "reportYear";
	public static final String REPORT_MONTH = "reportMonth";
	public static final String REPORT_SENDER_ID = "reportSenderId";
	public static final String REPORT_MESSAGE_ID = "reportMessageId";
	public static final String REPORT_DATASET_ID = "reportDatasetId";
	public static final String REPORT_STATUS = "reportStatus";
	public static final String REPORT_VERSION = "reportVersion";
	public static final String REPORT_VERSION_REGEX = "(\\.\\d{2})?";  // either .01, .02 or .10, .50 (always two digits)
}
