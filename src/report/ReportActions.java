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
import i18n_messages.Messages;
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
		
		int val = Warnings.warnUser(shell, Messages.get("warning.title"), 
				Messages.get("amend.confirm"), 
				SWT.ICON_WARNING | SWT.YES | SWT.NO);
		
		// go on only if yes
		if (val == SWT.NO)
			return null;
		
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		
		// create a new version of the report in the db
		// it affects directly the current object
		report.amend();
		
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		
		// we can returned the modified object
		return report;
	}
	
	/**
	 * Reject a dataset
	 * @param listener called to update ui
	 */
	public void reject(Listener listener) {
		
		int val = Warnings.warnUser(shell, Messages.get("warning.title"), Messages.get("reject.confirm"),
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
		
		int val = Warnings.warnUser(shell, Messages.get("warning.title"), 
				Messages.get("submit.confirm"),
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
				title = Messages.get("success.title");
				message = Messages.get("reject.success");
				style = SWT.ICON_INFORMATION;
				break;
			case SUBMIT:
				report.submit();
				title = Messages.get("success.title");
				message = Messages.get("submit.success");
				style = SWT.ICON_INFORMATION;
				break;
			default:
				title = Messages.get("error.title");
				message = Messages.get("report.unsupported.action");
			}
			

			listener.handleEvent(null);
			
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			title = Messages.get("error.title");
			message = Messages.get("report.io.error");
		} catch (SendMessageException e) {
			e.printStackTrace();
			title = Messages.get("error.title");
			message = Messages.get("send.message.failed", e.getErrorMessage());
		} catch (MySOAPException e) {
			e.printStackTrace();
			String[] warning = Warnings.getSOAPWarning(e);
			title = warning[0];
			message = warning[1];
		} catch (ReportException e) {
			e.printStackTrace();
			title = Messages.get("error.title");
			message = Messages.get("report.unsupported.action");
		}
		catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			for (StackTraceElement ste : e.getStackTrace()) {
		        sb.append("\n\tat ");
		        sb.append(ste);
		    }
		    String trace = sb.toString();
		    message = Messages.get("generic.error", trace);
			
			title = Messages.get("error.title");
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
		
		int val = Warnings.warnUser(shell, Messages.get("warning.title"), Messages.get("send.confirm"), 
				SWT.ICON_WARNING | SWT.YES | SWT.NO);
		
		if (val == SWT.NO)
			return;
		
		// if invalid report
		if (!report.isValid()) {
			Warnings.warnUser(shell, Messages.get("error.title"), 
					Messages.get("send.report.check"));
			return;
		}
		
		// if test data collection ask confirmation
		if (PropertiesReader.isTestDataCollection(report.getYear())) {
			
			String dc = PropertiesReader.getDataCollectionCode(report.getYear());
			int val2 = Warnings.warnUser(shell, Messages.get("warning.title"), 
					Messages.get("send.confirm.dc", dc),
					SWT.ICON_WARNING | SWT.YES | SWT.NO);
			
			if (val2 == SWT.NO)
				return;
		}
		
		ProgressBarDialog progressBarDialog = new ProgressBarDialog(shell, Messages.get("send.progress.title"));
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
						
						String title = Messages.get("success.title");
						String message = Messages.get("send.success");
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
							
							title = Messages.get("error.title");
							message = Messages.get("report.io.error", e.getMessage());
							icon = SWT.ICON_ERROR;
						}
						else if (e instanceof MySOAPException) {
							
							String[] warnings = Warnings.getSOAPWarning(((MySOAPException) e));
							title = warnings[0];
							message = warnings[1];
							icon = SWT.ICON_ERROR;
							
						}
						else if (e instanceof SAXException || e instanceof ParserConfigurationException) {
							
							title = Messages.get("error.title");
							message = Messages.get("gde2.missing", AppPaths.MESSAGE_GDE2_XSD, e.getMessage());
							icon = SWT.ICON_ERROR;
							
						}
						else if (e instanceof SendMessageException) {
							
							SendMessageException sendE = (SendMessageException) e;
							
							switch(sendE.getResponse().getErrorType()) {
							case NON_DP_USER:
								
								title = Messages.get("error.title");
								message = Messages.get("account.incomplete");
								icon = SWT.ICON_ERROR;

								break;
								
							case USER_WITHOUT_ORG:
								
								title = Messages.get("error.title");
								message = Messages.get("account.incorrect");
								icon = SWT.ICON_ERROR;
								
								break;
								
							default:
								
								title = Messages.get("error.title");
								message = Messages.get("send.message.failed", sendE.getErrorMessage());
								icon = SWT.ICON_ERROR;
								break;
							}
							
						}
						else if (e instanceof ReportException) {
							
							title = Messages.get("error.title");
							message = Messages.get("send.failed.no.senderId", e.getMessage());
							icon = SWT.ICON_ERROR;
						}
						else {
							StringBuilder sb = new StringBuilder();
							for (StackTraceElement ste : e.getStackTrace()) {
						        sb.append("\n\tat ");
						        sb.append(ste);
						    }
						    String trace = sb.toString();
						    
						    message = Messages.get("generic.error", trace);
							
							title = Messages.get("error.title");
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
			title = Messages.get("error.title");
			message = Messages.get("send.warning.acc.dwh", datasetId);
			goOn = false;
			break;
		case SUBMITTED:
			title = Messages.get("error.title");
			message = Messages.get("send.warning.submitted", datasetId);
			goOn = false;
			break;
		case PROCESSING:
			title = Messages.get("error.title");
			message = Messages.get("send.warning.processing", datasetId);
			goOn = false;
			break;
		case REJECTED_EDITABLE:
		case VALID:
		case VALID_WITH_WARNINGS:
			
			title = Messages.get("warning.title");
			message = Messages.get("send.warning.replace", datasetId, operation.getStatus().getLabel());
			style = SWT.YES | SWT.NO | SWT.ICON_WARNING;
			needConfirmation = true;
			break;
		case REJECTED:
		case DELETED:
			// Do nothing, just avoid the default case
			goOn = true;

			break;
		default:
			title = Messages.get("error.title");
			message = Messages.get("send.error.acc.dcf");
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
