package report;

import app_config.AppPaths;
import dataset.RCLDatasetStatus;
import table_database.TableDao;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import table_skeleton.TableVersion;
import xlsx_reader.TableSchema;
import xlsx_reader.TableSchemaList;

public abstract class Report extends TableRow implements EFSAReport {
	
	public Report(TableRow row) {
		super(row);
	}
	
	public Report(TableSchema schema) {
		super(schema);
	}
	
	public Report() {
		super();
	}
	
	@Override
	public boolean isBaselineVersion() {
		return TableVersion.isFirstVersion(this.getVersion());
	}
	
	public String getMessageId() {
		return this.getCode(AppPaths.REPORT_MESSAGE_ID);
	}
	
	public void setMessageId(String id) {
		this.put(AppPaths.REPORT_MESSAGE_ID, id);
	}
	
	public String getId() {
		return this.getCode(AppPaths.REPORT_DATASET_ID);
	}
	
	public void setId(String id) {
		this.put(AppPaths.REPORT_DATASET_ID, id);
	}
	
	/**
	 * Get the version contained in the sender id
	 * @return
	 */
	public String getVersion() {
		return this.getCode(AppPaths.REPORT_VERSION);
	}
	
	public void setVersion(String version) {
		this.put(AppPaths.REPORT_VERSION, version);
	}
	
	public String getSenderId() {
		return this.getCode(AppPaths.REPORT_SENDER_ID);
	}
	
	public void setSenderId(String id) {
		this.put(AppPaths.REPORT_SENDER_ID, id);
	}

	/**
	 * Get the status of the dataset attached to the report
	 * @return
	 */
	public RCLDatasetStatus getRCLStatus() {
		String status = getCode(AppPaths.REPORT_STATUS);
		return RCLDatasetStatus.fromString(status);
	}
	
	public void setStatus(String status) {
		this.put(AppPaths.REPORT_PREVIOUS_STATUS, this.getRCLStatus().getLabel());
		this.put(AppPaths.REPORT_STATUS, status);
	}
	
	/**
	 * Get the previous status of the dataset
	 * @return
	 */
	public RCLDatasetStatus getPreviousStatus() {
		String status = getCode(AppPaths.REPORT_PREVIOUS_STATUS);
		
		if (status.isEmpty())
			return null;
		
		return RCLDatasetStatus.fromString(status);
	}
	
	public void setStatus(RCLDatasetStatus status) {
		this.setStatus(status.getStatus());
	}
	
	public String getYear() {
		return this.getCode(AppPaths.REPORT_YEAR);
	}
	
	public void setYear(String year) {
		this.put(AppPaths.REPORT_YEAR, 
				getTableColumnValue(year, AppPaths.YEARS_LIST));
	}
	
	public String getMonth() {
		return this.getCode(AppPaths.REPORT_MONTH);
	}
	
	public void setMonth(String month) {
		this.put(AppPaths.REPORT_MONTH, 
				getTableColumnValue(month, AppPaths.MONTHS_LIST));
	}
	
	/**
	 * Force the report to be editable
	 */
	public void makeEditable() {
		this.setStatus(RCLDatasetStatus.DRAFT);
	}
	
	/**
	 * Check if the dataset can be edited or not
	 * @return
	 */
	public boolean isEditable() {
		return getRCLStatus().isEditable();
	}
	
	/**
	 * Delete all the versions of the report from the database
	 * @return
	 */
	public boolean deleteAllVersions() {
		return deleteAllVersions(this.getSenderId());
	}
	
	/**
	 * Delete all the versions of the report from the db
	 * @param senderId
	 * @return
	 */
	public static boolean deleteAllVersions(String senderId) {
		// delete the old versions of the report (the one with the same senderId)
		TableDao dao = new TableDao();
		return dao.deleteByStringField(TableSchemaList.getByName(AppPaths.REPORT_SHEET), 
				AppPaths.REPORT_SENDER_ID, senderId);
	}
	
	public static TableRowList getAllVersions(String senderId) {
		TableDao dao = new TableDao();
		return dao.getByStringField(TableSchemaList.getByName(AppPaths.REPORT_SHEET), 
				AppPaths.REPORT_SENDER_ID, senderId);
	}
	
	/**
	 * get the name of the field that contains the rowId.
	 * The rowId is the field that identifies a record
	 * of the report
	 * @return
	 */
	public abstract String getRowIdFieldName();
}
