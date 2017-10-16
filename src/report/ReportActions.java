package report;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

import acknowledge.Ack;
import acknowledge.AckLog;
import app_config.AppPaths;
import app_config.GlobalManager;
import app_config.PropertiesReader;
import dataset.DatasetStatus;
import dataset.IDataset;
import global_utils.Warnings;
import html_viewer.HtmlViewer;
import message.MessageConfigBuilder;
import message.SendMessageException;
import webservice.GetAck;
import webservice.MySOAPException;

/**
 * Bridge between the user interface and the programmatic part.
 * It follows the documentation that can be found in the
 * ToolTSE.vsd file.
 * For downloading reports please use instead {@link ReportDownloader}.
 * @author avonva
 *
 */
public class ReportActions {
	
	/**
	 * Amend a report
	 * @param shell
	 * @param report
	 * @param listener
	 */
	public static Report amend(Shell shell, Report report) {
		
		int val = Warnings.warnUser(shell, "Warning", 
				"CONF907: Do you confirm you need to apply changes to the report already accepted in the EFSA Data Warehouse?",
				SWT.ICON_WARNING | SWT.YES | SWT.NO);
		
		// go on only if yes
		if (val == SWT.NO)
			return null;
		
		// create a new version of the report in the db
		report.createNewVersion();
		
		return report;
	}
	
	/**
	 * Reject a dataset
	 * @param shell
	 * @param report
	 */
	public static void reject(Shell shell, EFSAReport report, Listener listener) {
		
		int val = Warnings.warnUser(shell, "Warning", 
				"CONF900: After the rejection of the dataset, to provide again the same report "
				+ "into DCF you need to edit it and send it again. Do you confirm the rejection?",
				SWT.ICON_WARNING | SWT.YES | SWT.NO);
		
		// go on only if yes
		if (val == SWT.NO)
			return;
		
		String title;
		String message;
		int style = SWT.ERROR;
		
		try {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
			report.reject();
			title = "Success";
			message = "The reject request was successfully sent to DCF. Please refresh the status to check if the operation is completed.";
			style = SWT.ICON_INFORMATION;
			listener.handleEvent(null);
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			title = "Error";
			message = "ERR402: An unexpected error occurred. Please contant technical assistance.";
		} catch (SendMessageException e) {
			e.printStackTrace();
			title = "Error";
			message = "ERR404: The dataset structure was not recognized by DCF. The operation could not be completed.";
		} catch (MySOAPException e) {
			e.printStackTrace();
			String[] warning = Warnings.getSOAPWarning(e.getError());
			title = warning[0];
			message = warning[1];
		} catch (ReportException e) {
			e.printStackTrace();
			title = "Error";
			message = "ERR405: The dataset cannot be sent since the operation is not supported.";
		}
		finally {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
		
		if (message != null) {
			Warnings.warnUser(shell, title, message, style);
		}
	}
	
	/**
	 * Reject a dataset
	 * @param shell
	 * @param report
	 */
	public static void submit(Shell shell, EFSAReport report, Listener listener) {
		
		int val = Warnings.warnUser(shell, "Warning", 
				"CONF901: After the submission of the dataset, the data will be processed for being inserted into EFSA Data Warehouse. "
				+ "You will be asked to verify data again in the Validation report. Do you confirm the submission?",
				SWT.ICON_WARNING | SWT.YES | SWT.NO);
		
		// go on only if yes
		if (val == SWT.NO)
			return;
		
		String title;
		String message;
		int style = SWT.ERROR;
		
		try {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
			report.submit();
			title = "Success";
			message = "The submit request was successfully sent to DCF. Please refresh the status to check if the operation is completed.";
			style = SWT.ICON_INFORMATION;
			listener.handleEvent(null);
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			title = "Error";
			message = "ERR402: An unexpected error occurred. Please contant technical assistance.";
		} catch (SendMessageException e) {
			e.printStackTrace();
			title = "Error";
			message = "ERR404: The dataset structure was not recognized by DCF. The operation could not be completed.";
		} catch (MySOAPException e) {
			e.printStackTrace();
			String[] warning = Warnings.getSOAPWarning(e.getError());
			title = warning[0];
			message = warning[1];
		} catch (ReportException e) {
			e.printStackTrace();
			title = "Error";
			message = "ERR405: The dataset cannot be sent since the operation is not supported.";
		}
		finally {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
		
		if (message != null) {
			Warnings.warnUser(shell, title, message, style);
		}
	}
	
	/**
	 * Display an ack in the browser
	 * @param shell
	 * @param report
	 */
	public static void displayAck(Shell shell, EFSAReport report) {
		
		String messageId = report.getMessageId();
		
		// if no message id found
		if (messageId == null || messageId.isEmpty()) {
			Warnings.warnUser(shell, "Error", 
					"ERR800: The ack message is not available within the tool, please check on DCF.");
			return;
		}
		
		// retrieve ack of the message id
		GetAck req = new GetAck(messageId);
		try {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
			req.getAck();
		} catch (MySOAPException e) {
			e.printStackTrace();
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
			Warnings.showSOAPWarning(shell, e.getError());
			return;
		}
		finally {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
		
		// get the attachment (the ack in xml format)
		InputStream attachment;
		try {
			attachment = req.getRawAttachment();
		} catch (MySOAPException e) {
			e.printStackTrace();
			Warnings.warnUser(shell, "Error", 
					"ERR801: Cannot retrieve the acknowledgement in the DCF response.");
			return;
		}
		
		// write it into a file in the temporary folder
		// in order to be able to open it in the browser
		String filename = AppPaths.TEMP_FOLDER + "ack_" + System.currentTimeMillis() + ".xml";
		File targetFile = new File(filename);
		
		try {
			
			Files.copy(attachment, targetFile.toPath());
			
			// close input stream
			attachment.close();
		} catch (IOException e) {
			e.printStackTrace();
			Warnings.warnUser(shell, "Error", 
					"ERR802: Cannot process the acknowledgement.");
		}
		
		// open the ack in the browser to see it formatted
		HtmlViewer viewer = new HtmlViewer();
		viewer.open(targetFile);
	}
	
	/**
	 * Export the report and send it to the dcf.
	 * @param report
	 * @throws ReportException 
	 * @throws MySOAPException 
	 * @throws IOException
	 * @throws SOAPException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws SendMessageException 
	 */
	public static void send(Shell shell, Report report) throws MySOAPException, ReportException {
		
		int val = Warnings.warnUser(shell, "Warning", 
				"CONF904: Once the dataset is sent, the report will not be editable until "
				+ "it is completely processed by the DCF. Do you want to continue?", 
				SWT.ICON_WARNING | SWT.YES | SWT.NO);
		
		if (val == SWT.NO)
			return;
		
		// if invalid report
		if (!report.isValid()) {
			Warnings.warnUser(shell, "Error", 
					"WARN403: The report contains error, please correct them before uploading data to DCF.");
			return;
		}
		
		// if test data collection ask confirmation
		if (GlobalManager.getInstance().isDcTest()) {
			
			int val2 = Warnings.warnUser(shell, "Warning", 
					"CONF903: The dataset will be sent to " 
					+ PropertiesReader.getDataCollectionCode() 
					+ " data collection. Do you want to continue?", 
					SWT.ICON_WARNING | SWT.YES | SWT.NO);
			
			if (val2 == SWT.NO)
				return;
		}
		
		// get if we need to do an insert or a replace
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		ReportSendOperation opType = report.getSendOperation();
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));

		int val2 = showExportWarning(shell, opType);
		if (val2 == SWT.NO)
			return;
		
		String title = "Success";
		String message = "Report successfully sent to the dcf.";
		int icon = SWT.ICON_INFORMATION;
		
		try {
			
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
			
			// Update the report dataset id if it was found in the DCF
			// (Required if we are overwriting an existing report)
			if (opType.getDataset() != null)
				report.setDatasetId(opType.getDataset().getId());
			
			MessageConfigBuilder config = report.getDefaultExportConfiguration(opType.getOpType());
			report.exportAndSend(config);
			
		} catch (IOException e) {
			e.printStackTrace();
			
			title = "Error";
			message = "ERR402: Errors occurred during the export of the report.";
			icon = SWT.ICON_ERROR;
			
		} catch (MySOAPException e) {
			e.printStackTrace();
			
			String[] warnings = Warnings.getSOAPWarning(e.getError());
			title = warnings[0];
			message = warnings[1];
			icon = SWT.ICON_ERROR;
			
		} catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
			
			title = "Error";
			message = "ERR403: Errors occurred during the creation of the report. Please check if the " 
					+ AppPaths.MESSAGE_GDE2_XSD + " file is correct. Received error: " + e.getMessage();
			icon = SWT.ICON_ERROR;
			
		} catch (SendMessageException e) {
			e.printStackTrace();
			
			title = "Error";
			message = "ERR404: Send message failed. Received error: " + e.getMessage();
			icon = SWT.ICON_ERROR;
		}

		finally {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}

		// warn the user
		Warnings.warnUser(shell, title, message, icon);
	}
	
	/**
	 * Refresh the status of a report
	 * @param shell
	 * @param report
	 * @param listener
	 */
	public static void refreshStatus(Shell shell, EFSAReport report, Listener listener) {
		
		// if local status UPLOADED, SUBMISSION_SENT, REJECTION_SENT
		if (report.getStatus().canGetAck()) {
			
			// get the ack of the dataset
			Ack ack = getAck(shell, report, listener);
			
			// if ack is ready then check if the report status
			// is the same as the one in the get dataset list
			if (ack.isReady()) {
				
				AckLog log = ack.getLog();
				
				if (log == null) {
					
					// warn the user, the ack cannot be retrieved yet
					String title = "Error";
					String message = "ERR801: No log was found in the acknowledgement.";
					int style = SWT.ICON_ERROR;
					Warnings.warnUser(shell, title, message, style);
					
					return;
				}
				
				// if no dataset retrieved for the current report
				if (!log.getDatasetStatus().exists()) {
					
					// warn the user, the ack cannot be retrieved yet
					String title = "Acknowledgment received";
					String message = "Current report state: " + log.getDatasetStatus();
					int style = SWT.ICON_INFORMATION;
					Warnings.warnUser(shell, title, message, style);
					
					return;
				}
				
				updateStatusWithDCF(shell, report, listener);
			}
			else {
				
				// warn the user, the ack cannot be retrieved yet
				String title = "Warning";
				String message = "WARN501: Dataset still in processing.";
				int style = SWT.ICON_INFORMATION;
				Warnings.warnUser(shell, title, message, style);
			}
		}
		else { // otherwise check dcf status
			updateStatusWithDCF(shell, report, listener);
		}
	}
	
	/**
	 * Get an acknowledge of the report
	 * @param shell
	 * @param report
	 * @return
	 */
	public static Ack getAck(Shell shell, EFSAReport report, Listener updateListener) {
		
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

		String title = null;
		String message = null;
		int style = SWT.ERROR;
		
		Ack ack = null;
		
		try {
			
			ack = report.getAck();
			
			if (ack == null) {
				title = "Error";
				message = "ERR803: The current report cannot be refreshed, since it was never sent to the dcf.";
				style = SWT.ICON_ERROR;
			}
			else {
				
				// update the ui accordingly
				updateListener.handleEvent(null);
			}
			
		} catch (MySOAPException e) {
			e.printStackTrace();
			String[] warning = Warnings.getSOAPWarning(e.getError());
			title = warning[0];
			message = warning[1];
			style = SWT.ERROR;
		}
		finally {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
		
		if (message != null)
			Warnings.warnUser(shell, title, message, style);
		
		return ack;
	}
	
	/**
	 * Update the report status with the dataset contained in the DCF
	 * @param shell
	 * @param report
	 * @param updateListener
	 */
	public static void updateStatusWithDCF(Shell shell, EFSAReport report, Listener updateListener) {
		
		String title = null;
		String message = null;
		int style = SWT.ERROR;
		
		IDataset dataset = null;
		try {
			
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
			
			// get the dataset related to the report from the
			// GetDatasetList request
			dataset = report.getDataset();
			
		} catch (MySOAPException e) {
			e.printStackTrace();
			String[] warning = Warnings.getSOAPWarning(e.getError());
			title = warning[0];
			message = warning[1];
			style = SWT.ERROR;
			
		} catch (ReportException e) {
			e.printStackTrace();
			title = "Error";
			message = "ERR700: The dataset of the report cannot be retrieved since the report lacks of datasetSenderId.";
			style = SWT.ERROR;
		}
		finally {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
		
		// if we have an error show it and stop the process
		if (message != null) {
			Warnings.warnUser(shell, title, message, style);
			return;
		}
		
		// if we have the same status then ok stop
		// we have the report updated
		if (dataset.getStatus() == report.getStatus()) {
			Warnings.warnUser(shell, "Acknowledgment received", 
					"Current report state: " + dataset.getStatus(),
					SWT.ICON_INFORMATION);
			return;
		}

		
		// if the report is in status submitted
		// and dcf status ACCEPTED_DWH or REJECTED_EDITABLE
		if (report.getStatus() == DatasetStatus.SUBMITTED) {
			
			// and dataset is accepted dwh or rejected editable
			switch(dataset.getStatus()) {
			case ACCEPTED_DWH:
			case REJECTED_EDITABLE:
				
				// update local report status with the dcf status
				report.setStatus(dataset.getStatus());
				
				// update caller to update ui
				updateListener.handleEvent(null);
				
				Warnings.warnUser(shell, "Information", 
						"The current status of the report is: " 
								+ dataset.getStatus().getStatus(), 
								SWT.ICON_INFORMATION);
				break;
			default:
				break;
			}
			
			return;
		}
		
		
		// otherwise if report is not in status submitted
		// check dcf status
		switch(dataset.getStatus()) {
		case DELETED:
		case REJECTED:
			
			// put the report in draft
			report.makeEditable();
			
			// update caller to update ui
			updateListener.handleEvent(null);
			
			Warnings.warnUser(shell, "Warning", 
					"WARN501: The related dataset in DCF is in status " + dataset.getStatus().getStatus() 
						+ ". Local report status will be changed to " 
						+ DatasetStatus.DRAFT.getStatus(), SWT.ICON_WARNING);
			break;
			
			// otherwise inconsistent status
		default:
			
			Warnings.warnUser(shell, "Error", 
					"ERR501: The related dataset in DCF is in status " 
					+ dataset.getStatus().getStatus() 
					+ ". Status of local report is inconsistent. Please contact zoonoses_support@efsa.europa.eu.", 
					SWT.ICON_ERROR);
			
			break;
		}
	}
	
	/**
	 * Warning based on the required operation and on the status of the dataset
	 * @param shell
	 * @param operation
	 * @return
	 */
	public static int showExportWarning(Shell shell, ReportSendOperation operation) {
		
		String title = null;
		String message = null;
		int style = SWT.ICON_ERROR;
		boolean needConfirmation = false;
		
		if (operation.getStatus() == null)
			return SWT.NO;
		
		String datasetId = operation.getDataset().getId();
		
		switch(operation.getStatus()) {
		case ACCEPTED_DWH:
			title = "Error";
			message = "WARN405: An existing report in DCF with dataset id " 
					+ datasetId 
					+ " was found in status ACCEPTED_DWH. To amend it please download and open it.";
			break;
		case SUBMITTED:
			title = "Error";
			message = "WARN406: An existing report in DCF with dataset id " 
					+ datasetId 
					+ " was found in status SUBMITTED. Please reject it in the validation report if changes are needed.";
			break;
		case PROCESSING:
			title = "Error";
			message = "WARN404: An existing report in DCF with dataset id " 
					+ datasetId 
					+ " was found in status PROCESSING. Please wait the completion of the validation.";
			break;
		case REJECTED_EDITABLE:
		case VALID:
		case VALID_WITH_WARNINGS:
			
			title = "Warning";
			message = "WARN407: An existing report in DCF with dataset id "
					+ datasetId
					+ " was found in status "
					+ operation.getStatus()
					+ " and will be overwritten. Do you want to proceed?.";
			style = SWT.YES | SWT.NO | SWT.ICON_WARNING;
			needConfirmation = true;
			break;
		case REJECTED:
		case DELETED:
			// Do nothing, just avoid the default case
			break;
		default:
			title = "Error";
			message = "ERR400: An error occurred due to a conflicting dataset in DCF. Please contact zoonoses_support@efsa.europa.eu.";
			break;
		}
		
		int val = Warnings.warnUser(shell, title, message, style);
		
		// if the caller need confirmation
		if (needConfirmation)
			return val;
		
		// default answer is no
		return SWT.NO;
	}
}
