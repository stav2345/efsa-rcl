package report;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import amend_manager.ReportImporter;
import dataset.Dataset;
import dataset.DatasetList;
import dataset.NoAttachmentException;
import formula.FormulaException;
import global_utils.Warnings;
import progress.ProgressBarDialog;
import progress.ProgressListener;
import webservice.MySOAPException;

/**
 * Download a report into the database
 * @author avonva
 *
 */
public abstract class ReportDownloader {

	private Shell shell;
	
	public ReportDownloader(Shell shell) {
		this.shell = shell;
	}
	
	/**
	 * Download a dataset from the dcf
	 * @param validSenderId
	 */
	public void download() {
		
		DownloadReportDialog dialog = getDialog();
		
		dialog.open();
		
		// get the chosen dataset
		Dataset selectedDataset = dialog.getSelectedDataset();
		
		if (selectedDataset == null)  // user pressed cancel
			return;
		
		// extract the senderId from the composed field (senderId.version)
		String senderId = selectedDataset.getDecomposedSenderId();
		
		// if the report already exists locally, warn that it will be overwritten
		if (Report.isLocallyPresent(senderId)) {
			
			int val = Warnings.warnUser(shell, "Warning", 
					"WARN700: This report already exists locally. Do you want to overwrite it?", 
					SWT.YES | SWT.NO | SWT.ICON_WARNING);
			
			if (val == SWT.NO)  // user pressed cancel
				return;
		}
		
		// import report
		
		// get all the versions of the dataset that are present in the DCF
		DatasetList<Dataset> allVersions = dialog.getSelectedDatasetVersions();

		// download and import the dataset
		ReportImporter downloader = this.getImporter(allVersions);
		ReportImporterThread thread = new ReportImporterThread(downloader);
		
		ProgressBarDialog progressBarDialog = new ProgressBarDialog(shell, "Downloading report");
		progressBarDialog.open();
		
		thread.setProgressListener(new ProgressListener() {

			@Override
			public void progressCompleted() {
				
				// show warning
				shell.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						
						progressBarDialog.fillToMax();
						progressBarDialog.close();
						
						String title = "Success";
						String message = "Report successfully downloaded. The downloaded report is not automatically opened. Please open it to see the content.";
						int style = SWT.ICON_INFORMATION;
						Warnings.warnUser(shell, title, message, style);
					}
				});
			}

			@Override
			public void progressChanged(double progressPercentage) {
				progressBarDialog.setProgress(progressPercentage);
			}

			@Override
			public void exceptionThrown(Exception e) {

				// show warning
				shell.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						
						progressBarDialog.close();
						
						String title = null;
						String message = null;

						if (e instanceof MySOAPException) {
							String[] warnings = Warnings.getSOAPWarning(((MySOAPException) e));
							title = warnings[0];
							message = warnings[1];
						}
						else if (e instanceof XMLStreamException
								|| e instanceof IOException) {
							title = "Error";
							message = "ERR702: The downloaded report is badly formatted. Please contact technical assistance.";
						}
						else if (e instanceof FormulaException) { 
							title = "Error";
							message = "ERR701: Parsing error, the dataset contents are not well formed. Please contact zoonoses_support@efsa.europa.eu.";
						}
						else if (e instanceof NoAttachmentException) {
							title = "Error";
							message = "ERR703: No attachment was found for the selected dataset in the DCF response.";
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
						
						
						Warnings.warnUser(shell, title, message);
					}
				});
			}
		});
		
		thread.start();
	}
	
	/**
	 * Get the class encharged of importing the report into the database.
	 * The importer must be an extension of
	 * the {@link ReportImporter} class (it already processes
	 * amendments using all the report versions)
	 * @param allVersions
	 */
	public abstract ReportImporter getImporter(DatasetList<Dataset> allVersions);
	
	/**
	 * Get the dialog which should be shown to select the dataset to download.
	 * Here it is possible to customize the columns that should be shown.
	 * @return
	 */
	public abstract DownloadReportDialog getDialog();
}
