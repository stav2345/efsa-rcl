package report;

import org.eclipse.swt.widgets.Shell;

import amend_manager.ReportImporter;
import dataset.Dataset;
import dataset.DatasetList;
import i18n_messages.Messages;
import progress_bar.FormProgressBar;
import progress_bar.ProgressListener;

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

		// get all the versions of the dataset that are present in the DCF
		DatasetList allVersions = dialog.getSelectedDatasetVersions();
		
		if (allVersions == null)
			return;
		
		// extract the senderId from the composed field (senderId.version)
		String senderId = selectedDataset.getDecomposedSenderId();
		
		// if the report already exists locally, warn that it will be overwritten
		if (Report.isLocallyPresent(senderId)) {
			
			// ask confirmation to the user
			boolean confirm = askConfirmation();
			
			if (!confirm)
				return;
		}

		// download and import the dataset
		ReportImporter downloader = this.getImporter(allVersions);
		ReportImporterThread thread = new ReportImporterThread(downloader);
		
		FormProgressBar progressBarDialog = new FormProgressBar(shell, 
				Messages.get("download.progress.title"));
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
						
						end();
					}
				});
			}

			@Override
			public void progressChanged(double progressPercentage) {
				progressBarDialog.setProgress(progressPercentage);
			}

			@Override
			public void progressChanged(double currentProgress, double maxProgress) {}

			@Override
			public void progressStopped(Exception e) {

				// show warning
				shell.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						
						progressBarDialog.close();
						
						// manage the exception
						manageException(e);
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
	public abstract ReportImporter getImporter(DatasetList allVersions);
	
	/**
	 * Get the dialog which should be shown to select the dataset to download.
	 * Here it is possible to customize the columns that should be shown.
	 * @return
	 */
	public abstract DownloadReportDialog getDialog();
	
	
	public abstract boolean askConfirmation();
	
	/**
	 * Manage an exception that was thrown during the download process
	 * @param e
	 */
	public abstract void manageException(Exception e);
	
	/**
	 * Called at the end of the process
	 */
	public abstract void end();
}
