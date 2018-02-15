package report;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;

import ack.DcfAck;
import ack.IDcfAckLog;
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
import message_creator.OperationType;
import soap.DetailedSOAPException;
import soap_interface.IGetAck;
import soap_interface.IGetDatasetsList;
import table_skeleton.TableVersion;
import user.User;

public class ReportService implements IReportService {

	private static final Logger LOGGER = LogManager.getLogger(ReportService.class);
	
	private IGetAck getAck;
	private IGetDatasetsList<IDataset> getDatasetsList;
	
	public ReportService(IGetAck getAck, IGetDatasetsList<IDataset> getDatasetsList) {
		this.getAck = getAck;
		this.getDatasetsList = getDatasetsList;
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
			report.update();

		}
		else {

			// if not in status submitted
			switch(dataset.getRCLStatus()) {
			// if deleted/rejected then make the report editable
			case DELETED:
			case REJECTED:

				// put the report in draft (status automatically changed)
				report.makeEditable();
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
				report.update();

				LOGGER.info("Ack successful for message id " + report.getMessageId() + ". Retrieved datasetId=" 
						+ datasetId + " with status=" + report.getRCLStatus());
			}
			else {

				// Reset the status with the previous if possible
				if(report.getPreviousStatus() != null) {
					report.setStatus(report.getPreviousStatus());
					report.update();
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
