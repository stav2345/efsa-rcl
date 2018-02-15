package report;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import ack.DcfAck;
import amend_manager.ReportXmlBuilder;
import app_config.AppPaths;
import app_config.PropertiesReader;
import config.Config;
import dataset.Dataset;
import dataset.DatasetList;
import dataset.IDataset;
import dataset.RCLDatasetStatus;
import message.MessageConfigBuilder;
import message.MessageResponse;
import message.SendMessageException;
import message_creator.OperationType;
import progress_bar.ProgressListener;
import soap.GetAck;
import soap.GetDatasetsList;
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

	private static final Logger LOGGER = LogManager.getLogger(Report.class);
	
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
	
	/**
	 * Given a report and its state, get the operation
	 * that is correct for sending it to the dcf.
	 * For example, if the report was never sent then the operation
	 * will be {@link OperationType#INSERT}.
	 * @param report
	 * @return
	 * @throws ReportException 
	 * @throws DetailedSOAPException 
	 */
	public ReportSendOperation getSendOperation() throws DetailedSOAPException, ReportException {
		
		OperationType opType = OperationType.NOT_SUPPORTED;
		
		Dataset dataset = this.getLatestDataset();
		
		String senderId = TableVersion.mergeNameAndVersion(this.getSenderId(), 
				this.getVersion());
		
		LOGGER.info("Searching report with sender dataset id=" + senderId);
		
		// if no dataset is present => we do an insert
		if (dataset == null || !dataset.getSenderId().equals(senderId)) {
			LOGGER.debug("No valid dataset found in DCF, using INSERT as operation");
			return new ReportSendOperation(null, OperationType.INSERT);
		}
		
		// otherwise we check the dataset status
		RCLDatasetStatus status = dataset.getRCLStatus();
		
		LOGGER.debug("Found dataset in DCF in status " + status);
		
		switch (status) {
		case REJECTED_EDITABLE:
		case VALID:
		case VALID_WITH_WARNINGS:
		case REJECTED:
			opType = OperationType.REPLACE;
			break;
		case DELETED:
			opType = OperationType.INSERT;
			break;
		default:
			opType = OperationType.NOT_SUPPORTED;
			//throw new ReportException("No send operation for status " 
			//		+ status + " is supported");
		}
		
		ReportSendOperation operation = new ReportSendOperation(dataset, opType);
		
		return operation;
	}
	
	/**
	 * Get all the datasets in the DCF that have the as senderDatasetId
	 * the one given in input.
	 * @param report
	 * @return
	 * @throws SOAPException
	 * @throws ReportException 
	 */
	public DatasetList getDatasets() throws DetailedSOAPException, ReportException {
		
		DatasetList output = new DatasetList();
		
		Config config = new Config();
		
		// check if the Report is in the DCF
		GetDatasetsList<IDataset> request = new GetDatasetsList<>();
		
		String senderDatasetId = this.getSenderId();
		
		if (senderDatasetId == null) {
			throw new ReportException("Cannot retrieve the report sender id for " + this);
		}

		request.getList(config.getEnvironment(), User.getInstance(), 
				PropertiesReader.getDataCollectionCode(this.getYear()), output);
		
		return output.filterBySenderId(senderDatasetId + AppPaths.REPORT_VERSION_REGEX);
	}
	
	/**
	 * Get the dataset related to this report (only metadata!). 
	 * Note that only the newer one will
	 * be returned. If you need all the datasets related to this report use
	 * {@link #getDatasets()}.
	 * @return
	 * @throws ReportException
	 * @throws DetailedSOAPException 
	 */
	public Dataset getLatestDataset() throws ReportException, DetailedSOAPException {

		DatasetList datasets = getDatasets();

		// use the dataset id if we have it
		if (this.getId() != null && !this.getId().isEmpty()) {
			datasets = datasets.filterByDatasetId(this.getId());
		}
		else {
			
			// otherwise use the sender dataset id to filter
			// the old versions and get the last one
			datasets = datasets.filterOldVersions();
		}
		
		return (Dataset) datasets.getMostRecentDataset();
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
	
	/**
	 * Get an acknowledgement of the dataset related to the report
	 * @return
	 * @throws SOAPException
	 */
	public DcfAck getAck() throws DetailedSOAPException {

		// make get ack request
		String messageId = this.getMessageId();
		
		// if no message id => the report was never sent
		if (messageId.isEmpty()) {
			return null;
		}
		
		Config config = new Config();
		GetAck req = new GetAck();
		
		// get state
		DcfAck ack = req.getAck(config.getEnvironment(), User.getInstance(), messageId);
		
		return ack;
	}

	public RCLDatasetStatus updateStatusWithAck(DcfAck ack) {

		// if we have something in the ack
		if (ack.isReady()) {
			
			if (ack.getLog().isOk()) {
				
				// save id
				String datasetId = ack.getLog().getDatasetId();
				this.setId(datasetId);
				
				// save status
				RCLDatasetStatus status = RCLDatasetStatus.fromDcfStatus(
						ack.getLog().getDatasetStatus());
				
				this.setStatus(status);
				
				// permanently save data
				this.update();
				
				LOGGER.info("Ack successful for message id " + this.getMessageId() + ". Retrieved datasetId=" 
						+ datasetId + " with status=" + this.getRCLStatus());
			}
			else {
				
				// Reset the status with the previous if possible
				if(this.getPreviousStatus() != null) {
					this.setStatus(this.getPreviousStatus());
					this.update();
				}
			}
		}
		
		return this.getRCLStatus();
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
	
	@Override
	public RCLDatasetStatus alignStatusWithDCF() throws DetailedSOAPException, ReportException {
		
		// get the dataset related to the report from the
		// GetDatasetList request
		Dataset dataset = this.getLatestDataset();
		
		// if not dataset is retrieved
		if (dataset == null) {
			return this.getRCLStatus();
		}

		// if equal, ok
		if (dataset.getRCLStatus() == this.getRCLStatus())
			return this.getRCLStatus();
		
		// if the report is submitted
		if (this.getRCLStatus() == RCLDatasetStatus.SUBMITTED 
				&& (dataset.getRCLStatus() == RCLDatasetStatus.ACCEPTED_DWH 
					|| dataset.getRCLStatus() == RCLDatasetStatus.REJECTED_EDITABLE)) {
			
			this.setStatus(dataset.getRCLStatus());
			this.update();

		}
		else {
	
			// if not in status submitted
			switch(dataset.getRCLStatus()) {
			// if deleted/rejected then make the report editable
			case DELETED:
			case REJECTED:
				
				// put the report in draft (status automatically changed)
				this.makeEditable();
				return dataset.getRCLStatus();
				//break;
				
			// otherwise inconsistent status
			default:
				break;
			}
			return dataset.getRCLStatus();
		}
		
		return this.getRCLStatus();
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
