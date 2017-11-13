package report;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

import app_config.AppPaths;
import app_config.PropertiesReader;
import global_utils.Warnings;
import message.SendMessageException;
import progress.ProgressBarDialog;
import progress.ProgressListener;
import report.ReportSender.ReportSenderListener;
import webservice.MySOAPException;

/**
 * Bridge between the user interface and the programmatic part.
 * It follows the documentation that can be found in the
 * ToolTSE.vsd file.
 * For downloading reports please use instead {@link ReportDownloader}.
 * For ack use {@link ReportAckManager}.
 * @author avonva
 *
 */
public class ReportActions {
	
	private enum ReportAction {
		REJECT,
		SUBMIT
	}
	
	private Shell shell;
	private Report report;
	
	public ReportActions(Shell shell, Report report) {
		this.shell = shell;
		this.report = report;
	}
	
	/**
	 * Amend a report
	 * @param shell
	 * @param report
	 * @param listener
	 */
	public Report amend() {
		
		int val = Warnings.warnUser(shell, "Warning", 
				"CONF907: Do you confirm you need to apply changes to the report already accepted in the EFSA Data Warehouse?",
				SWT.ICON_WARNING | SWT.YES | SWT.NO);
		
		// go on only if yes
		if (val == SWT.NO)
			return null;
		
		// create a new version of the report in the db
		// it affects directly the current object
		report.amend();
		
		// we can returned the modified object
		return report;
	}
	
	/**
	 * Reject a dataset
	 * @param listener called to update ui
	 */
	public void reject(Listener listener) {
		
		int val = Warnings.warnUser(shell, "Warning", 
				"CONF900: After the rejection of the dataset, to provide again the same report "
				+ "into DCF you need to edit it and send it again. Do you confirm the rejection?",
				SWT.ICON_WARNING | SWT.YES | SWT.NO);
		
		// go on only if yes
		if (val == SWT.NO)
			return;
		
		performAction(ReportAction.REJECT, listener);
	}
	
	/**
	 * Submit a dataset
	 * @param listener called to update ui
	 */
	public void submit(Listener listener) {
		
		int val = Warnings.warnUser(shell, "Warning", 
				"CONF901: After the submission of the dataset, the data will be processed for being inserted into EFSA Data Warehouse. "
				+ "You will be asked to verify data again in the Validation report. Do you confirm the submission?",
				SWT.ICON_WARNING | SWT.YES | SWT.NO);
		
		// go on only if yes
		if (val == SWT.NO)
			return;
		
		performAction(ReportAction.SUBMIT, listener);
	}
	
	/**
	 * Perform a report action that involves the dcf
	 * @param action
	 * @param listener
	 */
	private void performAction(ReportAction action, Listener listener) {
		
		String title;
		String message;
		int style = SWT.ERROR;
		
		try {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
			
			switch(action) {
			case REJECT:
				report.reject();
				title = "Success";
				message = "Reject request sent to DCF. Please wait email notification and then refresh the status.";
				style = SWT.ICON_INFORMATION;
				break;
			case SUBMIT:
				report.submit();
				title = "Success";
				message = "Submit request sent to DCF. Please wait email notification and then refresh the status.";
				style = SWT.ICON_INFORMATION;
				break;
			default:
				title = "Error";
				message = "Action not allowed.";
			}
			

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
			String[] warning = Warnings.getSOAPWarning(e);
			title = warning[0];
			message = warning[1];
		} catch (ReportException e) {
			e.printStackTrace();
			title = "Error";
			message = "ERR405: The dataset cannot be sent since the operation is not supported.";
		}
		catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			for (StackTraceElement ste : e.getStackTrace()) {
		        sb.append("\n\tat ");
		        sb.append(ste);
		    }
		    String trace = sb.toString();
		    
		    message = "XERRX: Generic runtime error. Please contact zoonoses_support@efsa.europa.eu. Error message " 
		    		+ trace;
			
			title = "Generic error";
		}
		finally {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
		
		if (message != null) {
			Warnings.warnUser(shell, title, message, style);
		}
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
	public void send(Listener listener) {
		
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
		if (PropertiesReader.isTestDataCollection(report.getYear())) {
			
			int val2 = Warnings.warnUser(shell, "Warning", 
					"CONF903: The dataset will be sent to " 
					+ PropertiesReader.getDataCollectionCode(report.getYear()) 
					+ " data collection. Do you want to continue?", 
					SWT.ICON_WARNING | SWT.YES | SWT.NO);
			
			if (val2 == SWT.NO)
				return;
		}
		
		ProgressBarDialog progressBarDialog = new ProgressBarDialog(shell, "Sending report");
		progressBarDialog.open();
		
		// start the sender thread
		ReportSender sender = new ReportSender(report);
		
		sender.setReportListener(new ReportSenderListener() {
			
			@Override
			public void confirm(ReportSendOperation opType) {
				
				shell.getDisplay().syncExec(new Runnable() {
					
					@Override
					public void run() {
						
						boolean goOn = showExportWarning(shell, opType);
						
						if (goOn) {
							sender.confirmSend();
						}
						else {
							
							sender.stopSend();
							
							// close the progress bar
							progressBarDialog.close();
						}
					}
				});
			}
		});
		
		
		sender.setProgressListener(new ProgressListener() {
			
			@Override
			public void progressCompleted() {
				
				shell.getDisplay().syncExec(new Runnable() {
					
					@Override
					public void run() {
						
						progressBarDialog.fillToMax();
						
						progressBarDialog.close();

						if (listener != null)
							listener.handleEvent(null);
						
						String title = "Success";
						String message = "Report uploaded to DCF. Please wait email notification and then refresh the status.";
						int icon = SWT.ICON_INFORMATION;
						
						Warnings.warnUser(shell, title, message, icon);
					}
				});
			}
			
			@Override
			public void progressChanged(double progressPercentage) {
				progressBarDialog.addProgress(progressPercentage);
			}
			
			@Override
			public void exceptionThrown(Exception e) {
				
				shell.getDisplay().syncExec(new Runnable() {
					
					@Override
					public void run() {
						
						progressBarDialog.close();
						
						String title = "";
						String message = "";
						int icon = SWT.ICON_ERROR;
						
						if (e instanceof IOException) {
							
							title = "Error";
							message = "ERR402: Errors occurred during the export of the report. Please contact zoonoses_support@efsa.europa.eu and attach this error message: " 
									+ e.getMessage();
							icon = SWT.ICON_ERROR;
						}
						else if (e instanceof MySOAPException) {
							
							String[] warnings = Warnings.getSOAPWarning(((MySOAPException) e));
							title = warnings[0];
							message = warnings[1];
							icon = SWT.ICON_ERROR;
							
						}
						else if (e instanceof SAXException || e instanceof ParserConfigurationException) {
							
							title = "Error";
							message = "ERR403: Errors occurred during the creation of the report. Please check if the " 
									+ AppPaths.MESSAGE_GDE2_XSD 
									+ " file is correct. Please contact urgently zoonoses_support@efsa.europa.eu and attach this error message: " 
									+ e.getMessage();
							icon = SWT.ICON_ERROR;
							
						}
						else if (e instanceof SendMessageException) {
							
							SendMessageException sendE = (SendMessageException) e;
							
							switch(sendE.getResponse().getErrorType()) {
							case NON_DP_USER:
								
								title = "Error";
								message = "ERR103: The data provider profile in DCF is incomplete: please contact zoonoses_support@efsa.europa.eu.";
								icon = SWT.ICON_ERROR;

								break;
								
							case USER_WITHOUT_ORG:
								
								title = "Error";
								message = "ERR102: The user is not correctly profiled in DCF: please contact zoonoses_support@efsa.europa.eu.";
								icon = SWT.ICON_ERROR;
								
								break;
								
							default:
								
								title = "Error";
								message = "ERR404: An unexpected error occurred. Please contact urgently zoonoses_support@efsa.europa.eu and attach this error message: " 
										+ sendE.getErrorMessage();
								icon = SWT.ICON_ERROR;
								break;
							}
							
						}
						else if (e instanceof ReportException) {
							
							title = "Error";
							message = "ERR700: Something went wrong, please check if the report senderDatasetId is set. Please contact zoonoses_support@efsa.europa.eu.";
							icon = SWT.ICON_ERROR;
						}
						else {
							StringBuilder sb = new StringBuilder();
							for (StackTraceElement ste : e.getStackTrace()) {
						        sb.append("\n\tat ");
						        sb.append(ste);
						    }
						    String trace = sb.toString();
						    
						    message = "XERRX: Generic runtime error. Please contact zoonoses_support@efsa.europa.eu. Error message " 
						    		+ trace;
							
							title = "Generic error";
						}
						
						Warnings.warnUser(shell, title, message, icon);
					}
				});
			}
		});
		
		sender.start();
	}
	
	/**
	 * Warning based on the required operation and on the status of the dataset
	 * @param shell
	 * @param operation
	 * @return
	 */
	public boolean showExportWarning(Shell shell, ReportSendOperation operation) {
		
		boolean goOn = true;
		
		String title = null;
		String message = null;
		int style = SWT.ICON_ERROR;
		boolean needConfirmation = false;
		
		// new dataset
		if (operation.getStatus() == null)
			return true;
		
		String datasetId = operation.getDataset().getId();
		
		switch(operation.getStatus()) {
		case ACCEPTED_DWH:
			title = "Error";
			message = "WARN405: An existing report in DCF with dataset id " 
					+ datasetId 
					+ " was found in status ACCEPTED_DWH. To amend it please download and open it.";
			goOn = false;
			break;
		case SUBMITTED:
			title = "Error";
			message = "WARN406: An existing report in DCF with dataset id " 
					+ datasetId 
					+ " was found in status SUBMITTED. Please reject it in the validation report if changes are needed.";
			goOn = false;
			break;
		case PROCESSING:
			title = "Error";
			message = "WARN404: An existing report in DCF with dataset id " 
					+ datasetId 
					+ " was found in status PROCESSING. Please wait the completion of the validation.";
			goOn = false;
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
			goOn = true;

			break;
		default:
			title = "Error";
			message = "ERR400: An error occurred due to a conflicting dataset in DCF. Please contact zoonoses_support@efsa.europa.eu.";
			goOn = false;
			break;
		}
		
		if (title != null && message != null) {
			
			int val = Warnings.warnUser(shell, title, message, style);
			
			// if the caller need confirmation
			if (needConfirmation) {
				
				goOn = val == SWT.YES;
			}
		}
		
		// default answer is no
		return goOn;
	}
}
