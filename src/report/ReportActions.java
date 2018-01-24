package report;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

import i18n_messages.Messages;
import message.SendMessageException;
import progress_bar.FormProgressBar;
import progress_bar.ProgressListener;
import report.ReportSender.ReportSenderListener;
import soap.MySOAPException;

/**
 * Bridge between the user interface and the programmatic part.
 * It follows the documentation that can be found in the
 * ToolTSE.vsd file.
 * For downloading reports please use instead {@link ReportDownloader}.
 * For ack use {@link ReportAckManager}.
 * @author avonva
 *
 */
public abstract class ReportActions {	
	
	private static final Logger LOGGER = LogManager.getLogger(ReportActions.class);
	
	public enum ReportAction {
		SEND,
		AMEND,
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
		
		
		boolean confirm = askConfirmation(ReportAction.AMEND);
		
		if (!confirm)
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
		
		boolean confirm = askConfirmation(ReportAction.REJECT);
		
		if (!confirm)
			return;
		
		performAction(ReportAction.REJECT, listener);
	}
	
	/**
	 * Submit a dataset
	 * @param listener called to update ui
	 */
	public void submit(Listener listener) {
		
		
		boolean confirm = askConfirmation(ReportAction.SUBMIT);
		
		if (!confirm)
			return;
		
		performAction(ReportAction.SUBMIT, listener);
	}
	
	/**
	 * Perform a report action that involves the dcf
	 * @param action
	 * @param listener
	 */
	private void performAction(ReportAction action, Listener listener) {
		
		Exception exceptionOccurred = null;
		
		try {
			
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
			
			switch(action) {
			case REJECT:
				report.reject();
				break;
			case SUBMIT:
				report.submit();
				break;
			default:  // unsupported
				break;
			}

			listener.handleEvent(null);
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Cannot perform operation=" + action + " for report=" + report.getSenderId(), e);
			exceptionOccurred = e;
		}
		finally {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
		
		// if an exception occurred call manage exception
		if (exceptionOccurred == null) {
			end(action);
		}
		else {
			manageException(exceptionOccurred, action);
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
		
		boolean confirm = askConfirmation(ReportAction.SEND);
		
		if (!confirm)
			return;
		
		// data collection ask confirmation
		boolean confirm2 = askDataCollectionConfirmation(report);
		
		if (!confirm2)
			return;
		
		FormProgressBar progressBarDialog = new FormProgressBar(shell, Messages.get("send.progress.title"));
		progressBarDialog.open();
		
		// start the sender thread
		ReportSender sender = new ReportSender(report);
		
		sender.setReportListener(new ReportSenderListener() {
			
			@Override
			public void confirm(ReportSendOperation opType) {
				
				shell.getDisplay().syncExec(new Runnable() {
					
					@Override
					public void run() {
						
						try {

							boolean goOn = showSendWarning(shell, opType);
							
							if (goOn) {
								sender.confirmSend();
							}
							else {
								
								sender.stopSend();
								
								// close the progress bar
								progressBarDialog.close();
							}
							
						} catch (UnsupportedReportActionException e) {
							e.printStackTrace();
							LOGGER.error("Cannot send report=" + report.getSenderId(), e);
							manageException(e, ReportAction.SEND);
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
						
						end(ReportAction.SEND);
					}
				});
			}
			
			@Override
			public void progressChanged(double progressPercentage) {
				progressBarDialog.addProgress(progressPercentage);
			}

			@Override
			public void progressChanged(double currentProgress, double maxProgress) {}

			@Override
			public void progressStopped(Exception e) {
				shell.getDisplay().syncExec(new Runnable() {
					
					@Override
					public void run() {
						
						progressBarDialog.close();
						
						manageException(e, ReportAction.SEND);
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
	 * @throws UnsupportedReportActionException 
	 */
	public boolean showSendWarning(Shell shell, ReportSendOperation operation) 
			throws UnsupportedReportActionException {
		
		boolean goOn = true;

		// new dataset
		if (operation.getStatus() == null)
			return true;

		switch(operation.getStatus()) {
		case ACCEPTED_DWH:
		case SUBMITTED:
		case PROCESSING:
			throw new UnsupportedReportActionException(operation);

		case REJECTED_EDITABLE:
		case VALID:
		case VALID_WITH_WARNINGS:
			// replace
			goOn = askReplaceConfirmation(operation);
			break;
		case REJECTED:
		case DELETED:
			// Do nothing, just avoid the default case
			goOn = true;

			break;
		default:
			throw new UnsupportedReportActionException(operation);
		}
		
		// default answer is no
		return goOn;
	}
	
	/**
	 * Ask to the user confirmation for a generic action.
	 * @param action
	 * @return
	 */
	public abstract boolean askConfirmation(ReportAction action);
	
	/**
	 * Ask confirmation for sending to the test data collection
	 * @return
	 */
	public abstract boolean askDataCollectionConfirmation(Report report);
	
	/**
	 * Ask confirmation for replacing an existing dataset with a send operation
	 * @param sendOp
	 * @return
	 */
	public abstract boolean askReplaceConfirmation(ReportSendOperation sendOp);
	
	/**
	 * Called if an exception occurred
	 * @param e
	 * @param action
	 */
	public abstract void manageException(Exception e, ReportAction action);
	
	/**
	 * Called if a process is finished
	 * @param opType
	 */
	public abstract void end(ReportAction action);
}
