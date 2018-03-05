package providers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.xml.sax.SAXException;

import ack.DcfAck;
import ack.IDcfAckLog;
import ack.MessageValResCode;
import amend_manager.AmendException;
import amend_manager.ReportXmlBuilder;
import app_config.AppPaths;
import app_config.BooleanValue;
import app_config.PropertiesReader;
import config.Config;
import dataset.Dataset;
import dataset.DatasetList;
import dataset.DatasetMetaDataParser;
import dataset.IDataset;
import dataset.NoAttachmentException;
import dataset.RCLDatasetStatus;
import formula.FormulaException;
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
import soap_interface.IGetDataset;
import soap_interface.IGetDatasetsList;
import soap_interface.ISendMessage;
import table_relations.Relation;
import table_skeleton.TableCell;
import table_skeleton.TableColumn;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import table_skeleton.TableVersion;
import user.User;
import xlsx_reader.TableHeaders.XlsxHeader;
import xlsx_reader.TableSchemaList;

public class ReportService implements IReportService {

	private static final Logger LOGGER = LogManager.getLogger(ReportService.class);
	
	private IGetAck getAck;
	private IGetDatasetsList<IDataset> getDatasetsList;
	protected ITableDaoService daoService;
	private ISendMessage sendMessage;
	private IGetDataset getDataset;
	
	protected IFormulaService formulaService;
	
	public ReportService(IGetAck getAck, 
			IGetDatasetsList<IDataset> getDatasetsList, 
			ISendMessage sendMessage, 
			IGetDataset getDataset, 
			ITableDaoService daoService,
			IFormulaService formulaService) {
		
		this.getAck = getAck;
		this.getDatasetsList = getDatasetsList;
		this.sendMessage = sendMessage;
		this.getDataset = getDataset;
		this.daoService = daoService;
		this.formulaService = formulaService;
	}
	
	public ITableDaoService getDaoService() {
		return daoService;
	}

	/**
	 * Get all the mandatory fields that are not filled
	 * @return
	 * @throws FormulaException 
	 */
	public Collection<TableColumn> getMandatoryFieldNotFilled(TableRow row) throws FormulaException {
		
		Collection<TableColumn> notFilled = new ArrayList<>();
		
		for (TableColumn column : row.getSchema()) {

			String mandatory = formulaService.solve(row, column, XlsxHeader.MANDATORY);
			
			if (BooleanValue.isTrue(mandatory)) {
				
				TableCell value = row.get(column.getId());
				
				if (value == null || value.isEmpty())
					notFilled.add(column);
			}
		}
		
		return notFilled;
	}
	
	/**
	 * Populate the dataset with the header and operation information (from DCF)
	 * @return
	 * @throws XMLStreamException
	 * @throws DetailedSOAPException
	 * @throws IOException
	 * @throws NoAttachmentException 
	 */
	public Dataset datasetFromFile(File file) throws XMLStreamException, IOException {
		
		Dataset dataset = null;
		
		try(DatasetMetaDataParser parser = new DatasetMetaDataParser(file);) {
			dataset = parser.parse();
			parser.close();
		}

		return dataset;
	}
	
	@Override
	public File download(String datasetId) throws DetailedSOAPException, NoAttachmentException {
		
		Config config = new Config();
		File file = getDataset.getDatasetFile(config.getEnvironment(), User.getInstance(), datasetId);
		
		if (file == null)
			throw new NoAttachmentException("Cannot find the attachment of the dataset with id=" + datasetId);
		
		return file;
	}
	
	public File export(Report report, MessageConfigBuilder messageConfig) 
			throws IOException, ParserConfigurationException, SAXException, ReportException, AmendException {
		return this.export(report, messageConfig, null);
	}
	
	public File export(Report report, MessageConfigBuilder messageConfig, ProgressListener progressListener) 
			throws ParserConfigurationException, SAXException, IOException, ReportException, AmendException {
		
		if (messageConfig.needEmptyDataset())
			return ReportXmlBuilder.createEmptyReport(messageConfig);
		else {
			
			Relation.emptyCache();

			// get the previous report version to process amendments
			try(ReportXmlBuilder creator = new ReportXmlBuilder(report, 
					messageConfig, report.getRowIdFieldName(), daoService, formulaService);) {
				
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
	
	public void send(Report report, Dataset dcfDataset, MessageConfigBuilder messageConfig, ProgressListener progressListener) 
			throws DetailedSOAPException, IOException, ParserConfigurationException, SAXException, 
			SendMessageException, ReportException, AmendException {
		
		// Update the report dataset id if it was found in the DCF
		// (Required if we are overwriting an existing report)
		if (dcfDataset != null) {

			switch (dcfDataset.getRCLStatus()) {
			case PROCESSING: // cannot send with these status
			case ACCEPTED_DWH:
			case OTHER:
			case SUBMITTED:
				return;
				
			case VALID: // dataset id needs to be copied to replace
			case VALID_WITH_WARNINGS:
			case REJECTED:
			case REJECTED_EDITABLE:
				
				LOGGER.info("Overwriting dataset id: " + report.getId() 
					+ " with " + dcfDataset.getId());
				
				report.setId(dcfDataset.getId());
				daoService.update(report);
				break;
				
			default:  // for deleted no action required
				break;
			}
		}
		
		// get the send operation
		ReportSendOperation op = getSendOperation(report, dcfDataset);
		messageConfig.setOpType(op.getOpType());
		
		this.exportAndSend(report, messageConfig, progressListener);
	}
	
	/**
	 * Export the report and send it to the DCF
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws SendMessageException
	 * @throws ReportException 
	 * @throws AmendException 
	 * @throws SOAPException
	 */
	public MessageResponse exportAndSend(Report report, MessageConfigBuilder messageConfig, ProgressListener progressListener) 
			throws IOException, ParserConfigurationException, 
		SAXException, SendMessageException, DetailedSOAPException, ReportException, AmendException {

		// export the report and get an handle to the exported file
		File file = this.export(report, messageConfig, progressListener);

		MessageResponse response;
		try {
			
			response = this.send(file, messageConfig.getOpType());
			
			// Update the report
			updateReportWithSendResponse(report, messageConfig.getOpType(), response);
			
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
	 * @throws AmendException 
	 */
	public MessageResponse exportAndSend(Report report, MessageConfigBuilder messageConfig) 
			throws DetailedSOAPException, IOException, ParserConfigurationException, 
			SAXException, SendMessageException, ReportException, AmendException {
		return this.exportAndSend(report, messageConfig, null);
	}
	
	@Override
	public TableRowList getAllVersions(String senderId) {
		return daoService.getByStringField(TableSchemaList.getByName(AppPaths.REPORT_SHEET), 
				AppPaths.REPORT_SENDER_ID, senderId);
	}
	
	/**
	 * Get the previous version of a report
	 * @param report
	 * @return
	 */
	public TableRow getPreviousVersion(EFSAReport report) {
		
		String currentVersion = report.getVersion();
		
		// compute the previous version
		String previousVersion = TableVersion.getPreviousVersion(currentVersion);
		
		// no previous version
		if (previousVersion == null)
			return null;
		
		// get all the report versions
		TableRowList allVersions = getAllVersions(report.getSenderId());

		// search the previous
		TableRow prev = null;
		for (TableRow current: allVersions) {

			String version = current.getCode(AppPaths.REPORT_VERSION);
			
			if (version.equals(previousVersion)) {
				prev = current;
				break;
			}
		}
		
		return prev;
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
	 * Given a report and its state, get the operation
	 * that is correct for sending it to the dcf.
	 * For example, if the report was never sent then the operation
	 * will be {@link OperationType#INSERT}.
	 * @param report
	 * @return
	 * @throws ReportException 
	 * @throws DetailedSOAPException 
	 */
	public ReportSendOperation getSendOperation(Report report, Dataset dcfDataset) throws DetailedSOAPException, ReportException {
		
		OperationType opType = OperationType.NOT_SUPPORTED;
		
		//Dataset dataset = this.getLatestDataset(report);
		
		String senderId = TableVersion.mergeNameAndVersion(report.getSenderId(), 
				report.getVersion());
		
		LOGGER.info("Searching report with sender dataset id=" + senderId);
		
		// if no dataset is present => we do an insert
		if (dcfDataset == null || !dcfDataset.getSenderId().equals(senderId)) {
			LOGGER.debug("No valid dataset found in DCF, using INSERT as operation");
			return new ReportSendOperation(null, OperationType.INSERT);
		}
		
		// otherwise we check the dataset status
		RCLDatasetStatus status = dcfDataset.getRCLStatus();
		
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
		
		ReportSendOperation operation = new ReportSendOperation(dcfDataset, opType);
		
		return operation;
	}
	
	/**
	 * Refresh the status of a report
	 * @param shell
	 * @param report
	 * @param listener
	 */
	public Message refreshStatus(Report report) {

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
		
		// if processing
		if (!ack.isReady()) {
			// warn the user, the ack cannot be retrieved yet
			String title = Messages.get("warning.title");
			String message = Messages.get("ack.processing");
			int style = SWT.ICON_INFORMATION;
			Message m = Warnings.create(title, message, style);
			m.setCode("WARN500");
			return m;
		}
		
		IDcfAckLog log = ack.getLog();
		
		boolean discarded = ack.getLog().getMessageValResCode() == MessageValResCode.DISCARDED;
		
		// if discarded or KO
		if (discarded || !log.isOk()) {
			
			// mark status as failed
			RCLDatasetStatus failedStatus = RCLDatasetStatus.getFailedVersionOf(
					report.getRCLStatus());

			if (failedStatus != null) {
				report.setStatus(failedStatus);
				
				// permanently save data
				daoService.update(report);
			}
			
			if (discarded) {
				
				String title = Messages.get("error.title");
				String message = Messages.get("ack.discarded", 
						ack.getLog().getMessageValResText());
				
				int style = SWT.ICON_ERROR;
				Message m = Warnings.create(title, message, style);
				m.setCode("ERR807");
				
				return m;
			}
			else if (!log.isOk()) {
				
				// errors
				if (log.hasErrors()) {
					
					LOGGER.warn("Error found in ack=" + log.getOpResError());
					LOGGER.warn("Error description found in ack=" + log.getOpResLog());
					
					return Warnings.getAckOperationWarning(report, log);
				}
				else {
					LOGGER.error("Wrong ack structure. The log is TRXKO but no errors found!");
					Message m = Warnings.createFatal(Messages.get("ack.ko.no.errors", 
							PropertiesReader.getSupportEmail()), report);
					m.setCode("ERR806");
					return m;
				}
			}
		}
		
		// here log can only be OK
		
		Dataset dcfDataset;
		try {
			dcfDataset = this.getLatestDataset(report);
		} catch (DetailedSOAPException e) {
			e.printStackTrace();
			LOGGER.error("Cannot get the dataset of the report=" + report.getSenderId(), e);
			return Warnings.createSOAPWarning(e);
		}
		
		// if deleted in DCF, auto draft and remove message information
		if (dcfDataset.getRCLStatus() == RCLDatasetStatus.DELETED) {
			
			report.makeEditable();
			report.remove(AppPaths.REPORT_MESSAGE_ID);
			report.remove(AppPaths.REPORT_LAST_MESSAGE_ID);
			report.remove(AppPaths.REPORT_LAST_MODIFYING_MESSAGE_ID);
			report.remove(AppPaths.REPORT_LAST_VALIDATION_MESSAGE_ID);
			this.daoService.update(report);
			
			Message mb = Warnings.create(Messages.get("warning.title"), Messages.get("refresh.auto.draft", 
					dcfDataset.getRCLStatus().getLabel(), RCLDatasetStatus.DRAFT.getLabel()), SWT.ICON_WARNING);
			mb.setCode("WARN501");
			
			return mb;
		}
		
		// if the data in DCF are actually consistent with the ones locally stored
		// update status with the one in DCF
		if (dcfDataset.getLastModifyingMessageId().equals(report.getLastModifyingMessageId())) {
			
			// update status with dataset status
			report.setStatus(dcfDataset.getRCLStatus());
			this.daoService.update(report);
			
			Message mb = Warnings.create(Messages.get("success.title"), 
					Messages.get("refresh.status.success", dcfDataset.getRCLStatus().getLabel()), 
					SWT.ICON_INFORMATION);
			mb.setCode("OK500");
			
			return mb;
		}
		
		// otherwise, inconsistency
		
		Message mb;
		switch(dcfDataset.getStatus()) {
		case REJECTED:
		case REJECTED_EDITABLE:
		case VALID:
		case VALID_WITH_WARNINGS:
			// auto draft
			mb = Warnings.create(Messages.get("error.title"), 
					Messages.get("refresh.inconsistent.auto.draft", dcfDataset.getLastModifyingMessageId(),
							dcfDataset.getRCLStatus().getLabel(), RCLDatasetStatus.DRAFT.getLabel()), 
					SWT.ICON_ERROR);
			mb.setCode("ERR504");
			break;
		default:
			// inconsistency error
			mb = Warnings.createFatal(Messages.get("refresh.inconsistent.unmodifiable", 
					dcfDataset.getLastModifyingMessageId(), dcfDataset.getRCLStatus().getLabel()), 
					report, dcfDataset);
			mb.setCode("ERR505");
			break;
		}
		
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
