package providers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.xml.sax.SAXException;

import ack.DcfAck;
import ack.IDcfAckLog;
import amend_manager.ReportXmlBuilder;
import app_config.AppPaths;
import app_config.PropertiesReader;
import config.Config;
import dataset.Dataset;
import dataset.DatasetList;
import dataset.IDataset;
import dataset.RCLDatasetStatus;
import global_utils.Message;
import global_utils.Warnings;
import html_viewer.HtmlViewer;
import i18n_messages.Messages;
import message.MessageConfigBuilder;
import message.MessageResponse;
import message.SendMessageException;
import message_creator.OperationType;
import progress_bar.ProgressListener;
import report.EFSAReport;
import report.Report;
import report.ReportException;
import report.ReportSendOperation;
import soap.DetailedSOAPException;
import soap_interface.IGetAck;
import soap_interface.IGetDatasetsList;
import soap_interface.ISendMessage;
import table_relations.Relation;
import table_skeleton.TableRow;
import table_skeleton.TableVersion;
import user.User;
import xlsx_reader.TableSchemaList;

public class ReportService implements IReportService {

	private static final Logger LOGGER = LogManager.getLogger(ReportService.class);
	
	private IGetAck getAck;
	private IGetDatasetsList<IDataset> getDatasetsList;
	private ITableDaoService daoService;
	private ISendMessage sendMessage;
	
	public ReportService(IGetAck getAck, IGetDatasetsList<IDataset> getDatasetsList, 
			ISendMessage sendMessage, ITableDaoService daoService) {
		this.getAck = getAck;
		this.getDatasetsList = getDatasetsList;
		this.sendMessage = sendMessage;
		this.daoService = daoService;
	}
	
	public File export(Report report, MessageConfigBuilder messageConfig) 
			throws IOException, ParserConfigurationException, SAXException, ReportException {
		return this.export(report, messageConfig, null);
	}
	
	public File export(Report report, MessageConfigBuilder messageConfig, ProgressListener progressListener) 
			throws ParserConfigurationException, SAXException, IOException, ReportException {
		
		if (messageConfig.needEmptyDataset())
			return ReportXmlBuilder.createEmptyReport(messageConfig);
		else {
			
			Relation.emptyCache();

			// get the previous report version to process amendments
			try(ReportXmlBuilder creator = new ReportXmlBuilder(report, 
					messageConfig, report.getRowIdFieldName());) {
				
				creator.setProgressListener(progressListener);
				
				return creator.exportReport();
			}
		}
	}
	
	/**
	 * @throws IOException 
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
	private MessageResponse send(File file, OperationType opType) 
			throws DetailedSOAPException, IOException {

		Config config = new Config();
		
		// send the report and get the response to the message
		MessageResponse response = sendMessage.send(config.getEnvironment(), User.getInstance(), file);

		return response;
	}
	
	/**
	 * Update a report with a send message response
	 * @param report
	 * @param requiredSendOp
	 * @param response
	 * @return
	 */
	private RCLDatasetStatus updateReportWithSendResponse(Report report, OperationType requiredSendOp, 
			MessageResponse response) {
		
		RCLDatasetStatus newStatus;
		
		// if correct response then save the message id
		// into the report
		if (response.isCorrect()) {

			// save the message id
			report.setMessageId(response.getMessageId());
			
			// update report status based on the request operation type
			switch(requiredSendOp) {
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
		}
		else {
			// set upload failed status if message is not valid
			newStatus = RCLDatasetStatus.UPLOAD_FAILED;
		}
		
		if (newStatus != null) {
			report.setStatus(newStatus);
			daoService.update(report);
		}
		
		return newStatus;
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
	public MessageResponse exportAndSend(Report report, OperationType opType, ProgressListener progressListener) 
			throws IOException, ParserConfigurationException, 
		SAXException, SendMessageException, DetailedSOAPException, ReportException {

		MessageConfigBuilder messageConfig = report.getDefaultExportConfiguration(opType);
		
		// export the report and get an handle to the exported file
		File file = this.export(report, messageConfig, progressListener);

		MessageResponse response;
		try {
			
			response = this.send(file, opType);
			
			// Update the report
			updateReportWithSendResponse(report, opType, response);
			
			// if wrong response
			if (!response.isCorrect()) {
				throw new SendMessageException(response);
			}
			
			// delete file also if exception occurs
			file.delete();
		}
		catch (DetailedSOAPException e) {

			// delete file also if exception occurs
			file.delete();

			// then rethrow the exception
			throw e;
		}
		
		if (progressListener != null)
			progressListener.progressCompleted();
		
		return response;
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
	public MessageResponse exportAndSend(Report report, OperationType opType) 
			throws DetailedSOAPException, IOException, ParserConfigurationException, 
			SAXException, SendMessageException, ReportException {
		return this.exportAndSend(report, opType, null);
	}
	
	@Override
	public DcfAck getAckOf(String messageId) throws DetailedSOAPException {

		// if no message id => the report was never sent
		if (messageId.isEmpty()) {
			return null;
		}

		Config config = new Config();

		// get state
		DcfAck ack = getAck.getAck(config.getEnvironment(), User.getInstance(), messageId);

		return ack;
	}

	@Override
	public DatasetList getDatasetsOf(String senderDatasetId, String dcYear) throws DetailedSOAPException {

		DatasetList output = new DatasetList();

		Config config = new Config();
		
		getDatasetsList.getList(config.getEnvironment(), User.getInstance(), 
				PropertiesReader.getDataCollectionCode(dcYear), output);
		
		return output.filterBySenderId(senderDatasetId + AppPaths.REPORT_VERSION_REGEX);
	}

	@Override
	public Dataset getDatasetById(String senderDatasetId, String dcYear, String datasetId) 
			throws DetailedSOAPException {

		DatasetList datasets = getDatasetsOf(senderDatasetId, dcYear);
		datasets = datasets.filterByDatasetId(datasetId);

		IDataset mostRecent = datasets.getMostRecentDataset();

		if (mostRecent == null)
			return null;

		return (Dataset) mostRecent;
	}

	@Override
	public Dataset getLatestDataset(String senderDatasetId, String dcYear) throws DetailedSOAPException {

		DatasetList datasets = getDatasetsOf(senderDatasetId, dcYear);
		datasets = datasets.filterOldVersions();

		IDataset mostRecent = datasets.getMostRecentDataset();

		if (mostRecent == null)
			return null;

		return (Dataset) mostRecent;
	}

	@Override
	public Dataset getLatestDataset(EFSAReport report) throws DetailedSOAPException {

		// use the dataset id if we have it
		if (report.getId() != null && !report.getId().isEmpty())
			return getDatasetById(report.getSenderId(), report.getYear(), report.getId());
		else
			return getLatestDataset(report.getSenderId(), report.getYear());
	}
	
	@Override
	public boolean isLocallyPresent(String senderDatasetId) {
		
		for (TableRow row : daoService.getAll(TableSchemaList.getByName(AppPaths.REPORT_SHEET))) {

			String otherSenderId = row.getLabel(AppPaths.REPORT_SENDER_ID);
			
			// if same sender dataset id then return true
			if (otherSenderId != null 
					&& otherSenderId.equals(senderDatasetId))
				return true;
		}
		
		return false;
	}
	
	public RCLError create(Report report) throws DetailedSOAPException {
		
		// if the report is already present
		// show error message
		if (isLocallyPresent(report.getSenderId())) {
			return new RCLError("WARN304");
		}
		
		Dataset oldReport = getLatestDataset(report);
		
		// if the report already exists
		// with the selected sender dataset id
		if (oldReport != null) {
			
			// check if there are errors
			RCLError error = getCreateReportError(report, oldReport);

			if (error != null)
				return error;

			// if no errors, then we are able to create the report
			
			switch (oldReport.getRCLStatus()) {
			case REJECTED:
				// we mantain the same dataset id
				// of the rejected dataset, but actually
				// we create a new report with that
				report.setId(oldReport.getId());
				break;
				
			default:
				break;
			}
		}

		// if no conflicts create the new report
		daoService.add(report);
		
		return null;
	}
	
	/**
	 * get the error message that needs to be displayed if
	 * an old report already exists
	 * @param oldReport
	 * @return
	 */
	private RCLError getCreateReportError(IDataset report, IDataset oldReport) {

		String code = null;
		
		switch(oldReport.getRCLStatus()) {
		case ACCEPTED_DWH:
			code = "WARN301";
			break;
		case SUBMITTED:
			code = "WARN302";
			break;
		case VALID:
		case VALID_WITH_WARNINGS:
		case REJECTED_EDITABLE:
			code = "WARN303";
			break;
		case PROCESSING:
			code = "WARN300";
			break;
		case DELETED:
		case REJECTED:
			break;
		default:
			code = "ERR300";
			break;
		}
		
		if (code == null)
			return null;
		
		return new RCLError(code, oldReport);
	}

	/**
	 * Get the report status comparing the local status
	 * and the one in dcf. Change also the status if needed
	 * @param report
	 * @return
	 * @throws DetailedSOAPException
	 * @throws ReportException
	 */
	private RCLDatasetStatus getRealReportStatus(Report report) throws DetailedSOAPException, ReportException {

		// get the dataset related to the report from the
		// GetDatasetList request
		Dataset dataset = this.getLatestDataset(report);
		
		// if not dataset is retrieved
		if (dataset == null) {
			return report.getRCLStatus();
		}
		
		// if equal, ok
		if (dataset.getRCLStatus() == report.getRCLStatus())
			return report.getRCLStatus();
		
		// if the report is submitted
		if (report.getRCLStatus() == RCLDatasetStatus.SUBMITTED 
				&& (dataset.getRCLStatus() == RCLDatasetStatus.ACCEPTED_DWH 
				|| dataset.getRCLStatus() == RCLDatasetStatus.REJECTED_EDITABLE)) {

			report.setStatus(dataset.getRCLStatus());
			
			daoService.update(report);
		}
		else {

			// if not in status submitted
			switch(dataset.getRCLStatus()) {
			// if deleted/rejected then make the report editable
			case DELETED:
			case REJECTED:

				// put the report in draft (status automatically changed)
				report.makeEditable();
				daoService.update(report);
				
				return dataset.getRCLStatus();
				//break;

				// otherwise inconsistent status
			default:
				break;
			}
			return dataset.getRCLStatus();
		}

		return report.getRCLStatus();
	}

	/**
	 * Update the local status with the one in the ack
	 * @param report
	 * @param ack
	 * @return
	 */
	public RCLDatasetStatus updateStatusWithAck(Report report, DcfAck ack) {
		
		// if we have something in the ack
		if (ack.isReady()) {

			if (ack.getLog().isOk()) {

				// save id
				String datasetId = ack.getLog().getDatasetId();
				report.setId(datasetId);

				// save status
				RCLDatasetStatus status = RCLDatasetStatus.fromDcfStatus(
						ack.getLog().getDatasetStatus());

				report.setStatus(status);

				// permanently save data
				daoService.update(report);

				LOGGER.info("Ack successful for message id " + report.getMessageId() + ". Retrieved datasetId=" 
						+ datasetId + " with status=" + report.getRCLStatus());
			}
			else {

				// Reset the status with the previous if possible
				if(report.getPreviousStatus() != null) {
					report.setStatus(report.getPreviousStatus());
					daoService.update(report);
				}
			}
		}

		return report.getRCLStatus();
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
	public ReportSendOperation getSendOperation(EFSAReport report) throws DetailedSOAPException, ReportException {
		
		OperationType opType = OperationType.NOT_SUPPORTED;
		
		Dataset dataset = this.getLatestDataset(report);
		
		String senderId = TableVersion.mergeNameAndVersion(report.getSenderId(), 
				report.getVersion());
		
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
	 * Refresh the status of a report
	 * @param shell
	 * @param report
	 * @param listener
	 */
	public Message refreshStatus(Report report) {
		
		// if the report status is not able of
		// getting an ack, then simply align its status
		// with the one in dcf
		if (!report.getRCLStatus().canGetAck()) {
			return alignReportStatusWithDCF(report);
		}

		// else if local status UPLOADED, SUBMISSION_SENT, REJECTION_SENT

		// if no connection return
		DcfAck ack = null;
		try {
			ack = this.getAckOf(report.getMessageId());
		} catch (DetailedSOAPException e) {
			e.printStackTrace();
			LOGGER.error("Cannot get the ack for the report=" + report.getSenderId(), e);
			return Warnings.createSOAPWarning(e);
		}

		// if no ack return
		if (ack == null) {
			Message m = Warnings.create(Messages.get("error.title"), 
					Messages.get("ack.not.available"), SWT.ICON_ERROR);
			m.setCode("ERR803");
			return m;
		}
		
		// if ack is ready then check if the report status
		// is the same as the one in the get dataset list
		if (ack.isReady()) {
			
			IDcfAckLog log = ack.getLog();
			
			// if TRXOK
			if (log.isOk()) {
				
				this.updateStatusWithAck(report, ack);
				
				RCLDatasetStatus status = RCLDatasetStatus
						.fromDcfStatus(log.getDatasetStatus());
				
				// if no dataset retrieved for the current report
				if (!status.existsInDCF()) {

					// warn the user, the ack cannot be retrieved yet
					String title = Messages.get("success.title");
					String message = Messages.get("ack.invalid", 
							status.getLabel());
					int style = SWT.ICON_ERROR;

					Message m = Warnings.create(title, message, style);
					m.setCode("ERR804");
					
					return m;
				}

				return alignReportStatusWithDCF(report);
			}
			else {
			
				// errors
				if (log.hasErrors()) {
					
					LOGGER.warn("Error found in ack=" + log.getOpResError());
					LOGGER.warn("Error description found in ack=" + log.getOpResLog());
					
					return Warnings.getAckOperationWarning(report, log);
				}
				else {
					// not reachable
					LOGGER.error("Wrong ack structure. The log is TRXKO but no errors found!");
					Message m = Warnings.createFatal(Messages.get("ack.ko.no.errors", 
							PropertiesReader.getSupportEmail()), report);
					m.setCode("ERR806");
					return m;
				}
			}
		}
		else {

			// warn the user, the ack cannot be retrieved yet
			String title = Messages.get("warning.title");
			String message = Messages.get("ack.processing");
			int style = SWT.ICON_INFORMATION;
			Message m = Warnings.create(title, message, style);
			m.setCode("WARN500");
			return m;
		}
	}
	
	/**
	 * Update the report status with the dataset contained in the DCF
	 * @param shell
	 * @param report
	 * @throws ReportException 
	 * @throws DetailedSOAPException 
	 */
	public Message alignReportStatusWithDCF(Report report) {

		Message mb = null;

		try {

			RCLDatasetStatus oldStatus = report.getRCLStatus();
			RCLDatasetStatus newStatus = this.getRealReportStatus(report);
			
			// if we have the same status then ok stop
			// we have the report updated
			if (oldStatus == newStatus) {
				mb = Warnings.create(Messages.get("success.title"), 
						Messages.get("refresh.status.success", newStatus.getLabel()), 
								SWT.ICON_INFORMATION);
				mb.setCode("OK500");
			}

			// if the report was in status submitted
			// and in dcf ACCEPTED_DWH or REJECTED_EDITABLE
			else if (oldStatus == RCLDatasetStatus.SUBMITTED && 
					(newStatus == RCLDatasetStatus.ACCEPTED_DWH 
						|| newStatus == RCLDatasetStatus.REJECTED_EDITABLE)) {

				mb = Warnings.create(Messages.get("success.title"), 
						Messages.get("refresh.status.success", newStatus.getLabel()), 
						SWT.ICON_INFORMATION);
				mb.setCode("OK500");
			}
			else {

				// otherwise if report is not in status submitted
				// check dcf status
				switch(newStatus) {
				case DELETED:
				case REJECTED:

					mb = Warnings.create(Messages.get("warning.title"), Messages.get("refresh.auto.draft", 
							newStatus.getLabel(), RCLDatasetStatus.DRAFT.getLabel()), SWT.ICON_WARNING);
					mb.setCode("WARN501");
					break;

					// otherwise inconsistent status
				default:
					
					mb = Warnings.createFatal(Messages.get("refresh.error", newStatus.getLabel(), 
							PropertiesReader.getSupportEmail()), report);
					mb.setCode("ERR501");
					break;
				}
			}
		}
		catch (DetailedSOAPException e) {
			e.printStackTrace();
			LOGGER.error("Cannot refresh status", e);
			mb = Warnings.createSOAPWarning(e);

		} catch (ReportException e) {
			e.printStackTrace();
			
			LOGGER.error("Cannot refresh status", e);
			
			mb = Warnings.createFatal(Messages.get("refresh.failed.no.senderId", 
					PropertiesReader.getSupportEmail()), report);
			
			mb.setCode("ERR700");
		}

		// if we have an error show it and stop the process
		return mb;
	}
	
	/**
	 * Display an ack in the browser
	 * @param shell
	 * @param report
	 */
	public Message displayAck(String messageId) {
		
		// if no message id found
		if (messageId == null || messageId.isEmpty()) {
			Message m = Warnings.create(Messages.get("error.title"), 
					Messages.get("ack.no.message.id"), SWT.ICON_ERROR);
			m.setCode("ERR800");
			return m;
		}
		
		// if no connection return
		DcfAck ack = null;
		try {
			ack = getAckOf(messageId);
		} catch (DetailedSOAPException e) {
			e.printStackTrace();
			LOGGER.error("Cannot get ack for messageId=" + messageId, e);
			return Warnings.createSOAPWarning(e);
		}

		// if no ack return
		if (ack == null || !ack.isReady() || ack.getLog() == null) {
			String message = Messages.get("ack.not.available");
			Message m = Warnings.create(Messages.get("error.title"), message, SWT.ICON_ERROR);
			m.setCode("ERR803");
			return m;
		}
		
		// get the raw log to send the .xml to the browser
		InputStream rawLog = ack.getLog().getRawLog();
		
		// write it into a file in the temporary folder
		// in order to be able to open it in the browser
		String filename = AppPaths.TEMP_FOLDER + "ack_" + System.currentTimeMillis() + ".xml";
		File targetFile = new File(filename);
		
		try {
			
			Files.copy(rawLog, targetFile.toPath());
			
			// close input stream
			rawLog.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("Cannot copy the ack into local disk (for displaying the html with the browser)", e);
			Message m = Warnings.create(Messages.get("error.title"), 
					Messages.get("ack.file.not.found"), SWT.ICON_ERROR);
			m.setCode("ERR802");
			return m;
		}
		
		// open the ack in the browser to see it formatted
		HtmlViewer viewer = new HtmlViewer();
		viewer.open(targetFile);
		
		return null;
	}
}
