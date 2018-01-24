package report;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;

import ack.DcfAck;
import ack.DcfAckLog;
import app_config.PropertiesReader;
import dataset.RCLDatasetStatus;
import global_utils.Message;
import global_utils.Warnings;
import i18n_messages.Messages;
import soap.MySOAPException;

public class RefreshStatusThread extends Thread {

	private static final Logger LOGGER = LogManager.getLogger(RefreshStatusThread.class);
	
	private ThreadFinishedListener listener;
	private EFSAReport report;
	private Message result;
	
	public RefreshStatusThread(EFSAReport report) {
		this.report = report;
	}
	
	public void setListener(ThreadFinishedListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void run() {
		result = refreshStatus();
		
		if (listener != null)
			listener.finished(this);
	}
	
	public Message getLog() {
		return result;
	}
	
	/**
	 * Refresh the status of a report
	 * @param shell
	 * @param report
	 * @param listener
	 */
	public Message refreshStatus() {
		
		// if the report status is not able of
		// getting an ack, then simply align its status
		// with the one in dcf
		if (!report.getRCLStatus().canGetAck()) {
			return alignReportStatusWithDCF();
		}

		// else if local status UPLOADED, SUBMISSION_SENT, REJECTION_SENT

		// if no connection return
		DcfAck ack = null;
		try {
			ack = report.getAck();
		} catch (MySOAPException e) {
			e.printStackTrace();
			LOGGER.error("Cannot get the ack for the report=" + report.getSenderId(), e);
			return Warnings.createSOAPWarning(e);
		}

		// if no ack return
		if (ack == null) {
			return Warnings.create(Messages.get("error.title"), 
					Messages.get("ack.not.available"), SWT.ICON_ERROR);
		}

		// if ack is ready then check if the report status
		// is the same as the one in the get dataset list
		if (ack.isReady()) {
			
			DcfAckLog log = ack.getLog();
			
			// if TRXOK
			if (log.isOk()) {
				
				// update the report status if required
				report.updateStatusWithAck(ack);
				
				RCLDatasetStatus status = RCLDatasetStatus
						.fromDcfStatus(log.getDatasetStatus());
				
				// if no dataset retrieved for the current report
				if (!status.existsInDCF()) {

					// warn the user, the ack cannot be retrieved yet
					String title = Messages.get("success.title");
					String message = Messages.get("ack.invalid", 
							status.getLabel());
					int style = SWT.ICON_ERROR;

					return Warnings.create(title, message, style);
				}

				return alignReportStatusWithDCF();
			}
			else {
			
				// errors
				if (log.hasErrors()) {
					
					LOGGER.warn("Error found in ack=" + log.getOpResError());
					LOGGER.warn("Error description found in ack=" + log.getOpResLog());
					
					String[] warning = Warnings.getAckOperationWarning(log);
					return Warnings.create(warning[0], warning[1], SWT.ICON_ERROR);
				}
				else {
					// not reachable
					LOGGER.error("Wrong ack structure. The log is TRXKO but no errors found!");
				}
			}
		}
		else {

			// warn the user, the ack cannot be retrieved yet
			String title = Messages.get("warning.title");
			String message = Messages.get("ack.processing");
			int style = SWT.ICON_INFORMATION;
			return Warnings.create(title, message, style);
		}
		
		return null;
	}
	
	/**
	 * Update the report status with the dataset contained in the DCF
	 * @param shell
	 * @param report
	 * @throws ReportException 
	 * @throws MySOAPException 
	 */
	public Message alignReportStatusWithDCF() {

		Message mb = null;

		try {

			RCLDatasetStatus oldStatus = report.getRCLStatus();
			RCLDatasetStatus newStatus = report.alignStatusWithDCF();
			
			// if we have the same status then ok stop
			// we have the report updated
			if (oldStatus == newStatus) {
				mb = Warnings.create(Messages.get("success.title"), 
						Messages.get("refresh.status.success", newStatus.getLabel()), 
								SWT.ICON_INFORMATION);
			}

			// if the report was in status submitted
			// and in dcf ACCEPTED_DWH or REJECTED_EDITABLE
			else if (oldStatus == RCLDatasetStatus.SUBMITTED && 
					(newStatus == RCLDatasetStatus.ACCEPTED_DWH 
						|| newStatus == RCLDatasetStatus.REJECTED_EDITABLE)) {

				mb = Warnings.create(Messages.get("success.title"), 
						Messages.get("refresh.status.success", newStatus.getLabel()), 
						SWT.ICON_INFORMATION);
			}
			else {

				// otherwise if report is not in status submitted
				// check dcf status
				switch(newStatus) {
				case DELETED:
				case REJECTED:

					mb = Warnings.create(Messages.get("warning.title"), Messages.get("refresh.auto.draft", 
							newStatus.getLabel(), RCLDatasetStatus.DRAFT.getLabel()), SWT.ICON_WARNING);

					break;

					// otherwise inconsistent status
				default:
					
					mb = Warnings.create(Messages.get("error.title"), Messages.get("refresh.error", newStatus.getLabel(), 
							PropertiesReader.getSupportEmail()), SWT.ICON_ERROR);

					break;
				}
			}
		}
		catch (MySOAPException e) {
			e.printStackTrace();
			LOGGER.error("Cannot refresh status", e);
			mb = Warnings.createSOAPWarning(e);

		} catch (ReportException e) {
			e.printStackTrace();
			
			LOGGER.error("Cannot refresh status", e);
			
			Warnings.create(Messages.get("error.title"), 
					Messages.get("refresh.failed.no.senderId", PropertiesReader.getSupportEmail(), e.getMessage()), 
					SWT.ICON_ERROR);
		}

		// if we have an error show it and stop the process
		return mb;
	}
}
