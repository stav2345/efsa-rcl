package report;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.xml.sax.SAXException;

import amend_manager.ReportXmlBuilder;
import app_config.AppPaths;
import config.Config;
import dataset.RCLDatasetStatus;
import message.MessageConfigBuilder;
import message.MessageResponse;
import message.SendMessageException;
import message_creator.OperationType;
import progress_bar.ProgressListener;
import soap.DetailedSOAPException;
import soap.SendMessage;
import table_database.TableDao;
import table_relations.Relation;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import table_skeleton.TableVersion;
import user.User;
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
	
	/**
	 * Check if the a report with the chosen senderDatasetId 
	 * is already present in the database
	 * @param senderDatasetId
	 * @return
	 */
	public static boolean isLocallyPresent(String senderDatasetId) {
		
		if (senderDatasetId == null)
			return false;
		
		// check if the report is already in the db
		TableDao dao = new TableDao(TableSchemaList.getByName(AppPaths.REPORT_SHEET));
		
		for (TableRow row : dao.getAll()) {

			String otherSenderDatasetId = row.getCode(AppPaths.REPORT_SENDER_ID);
			
			// if same sender dataset id then return true
			if (otherSenderDatasetId != null 
					&& otherSenderDatasetId.equals(senderDatasetId))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Send the report contained in the file
	 * and update the report status accordingly.
	 * NOTE only for expert users. Otherwise use
	 * {@link #exportAndSend()} to send the report
	 * with an atomic operation.
	 * @param file
	 * @throws SOAPException
	 * @throws SendMessageException
	 * @throws  
	 */
	public void send(File file, OperationType opType) throws SOAPException, SendMessageException {

		Config config = new Config();
		
		// send the report and get the response to the message
		SendMessage req = new SendMessage();
		MessageResponse response;
		try {
			response = req.send(config.getEnvironment(), User.getInstance(), file);
		} catch (IOException e) {
			e.printStackTrace();
			throw new SendMessageException(e);
		}

		// if correct response then save the message id
		// into the report
		if (response.isCorrect()) {

			// save the message id
			this.setMessageId(response.getMessageId());
			
			// update report status based on the request operation type
			RCLDatasetStatus newStatus;
			switch(opType) {
			case INSERT:
			case REPLACE:
				newStatus = RCLDatasetStatus.UPLOADED;
				break;
			case REJECT:
				newStatus = RCLDatasetStatus.REJECTION_SENT;
				break;
			case SUBMIT:
				newStatus = RCLDatasetStatus.SUBMISSION_SENT;
				break;
			default:
				newStatus = null;
				break;
			}
			
			if (newStatus != null) {
				this.setStatus(newStatus);
				this.update();
			}
		}
		else {

			// set upload failed status if message is not valid
			this.setStatus(RCLDatasetStatus.UPLOAD_FAILED);
			this.update();

			throw new SendMessageException(response);
		}
	}
	
	public File export(MessageConfigBuilder messageConfig) 
			throws IOException, ParserConfigurationException, SAXException, ReportException {
		return this.export(messageConfig, null);
	}
	
	@Override
	public File export(MessageConfigBuilder messageConfig, ProgressListener progressListener)
			throws IOException, ParserConfigurationException, SAXException, ReportException {
		
		if (messageConfig.needEmptyDataset())
			return ReportXmlBuilder.createEmptyReport(messageConfig);
		else {
			
			Relation.emptyCache();

			// get the previous report version to process amendments
			try(ReportXmlBuilder creator = new ReportXmlBuilder(this, 
					messageConfig, getRowIdFieldName());) {
				
				creator.setProgressListener(progressListener);
				
				return creator.exportReport();
			}
		}
	}
	
	/**
	 * Export and send without tracking progresses
	 * @param opType
	 * @throws DetailedSOAPException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws SendMessageException
	 * @throws ReportException
	 */
	public void exportAndSend(OperationType opType) 
			throws DetailedSOAPException, IOException, ParserConfigurationException, 
			SAXException, SendMessageException, ReportException {
		this.exportAndSend(opType, null);
	}
	
	/**
	 * Export the report and send it to the DCF
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws SendMessageException
	 * @throws ReportException 
	 * @throws SOAPException
	 */
	public void exportAndSend(OperationType opType, ProgressListener progressListener) 
			throws IOException, ParserConfigurationException, 
		SAXException, SendMessageException, DetailedSOAPException, ReportException {

		MessageConfigBuilder messageConfig = getDefaultExportConfiguration(opType);
		
		// export the report and get an handle to the exported file
		File file = this.export(messageConfig, progressListener);

		try {
			
			this.send(file, opType);
			
			// delete file also if exception occurs
			file.delete();
		}
		catch (SOAPException e) {

			// delete file also if exception occurs
			file.delete();

			// then rethrow the exception
			throw new DetailedSOAPException(e);
		}
		
		if (progressListener != null)
			progressListener.progressCompleted();
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
		this.update();
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
		TableDao dao = new TableDao(TableSchemaList.getByName(AppPaths.REPORT_SHEET));
		return dao.deleteByStringField(AppPaths.REPORT_SENDER_ID, senderId);
	}
	
	public static TableRowList getAllVersions(String senderId) {
		TableDao dao = new TableDao(TableSchemaList.getByName(AppPaths.REPORT_SHEET));
		return dao.getByStringField(AppPaths.REPORT_SENDER_ID, senderId);
	}
	
	/**
	 * get the name of the field that contains the rowId.
	 * The rowId is the field that identifies a record
	 * of the report
	 * @return
	 */
	public abstract String getRowIdFieldName();
}
