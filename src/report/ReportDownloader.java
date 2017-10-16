package report;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import amend_manager.ReportImporter;
import dataset.Dataset;
import dataset.DatasetList;
import global_utils.Warnings;
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
	 * @param shell
	 */
	public void download(String validSenderId) {
		
		// show only the datasets that can be downloaded (valid status, valid senderId)
		DownloadReportDialog dialog = new DownloadReportDialog(shell, validSenderId);
		
		// get the chosen dataset
		Dataset selectedDataset = dialog.getSelectedDataset();
		
		if (selectedDataset == null)  // user pressed cancel
			return;
		
		// extract the senderId from the composed field (senderId.version)
		String senderId = selectedDataset.getDecomposedSenderId();
		
		// if the report already exists locally, warn that it will be overwritten
		if (Report.isLocallyPresent(senderId)) {
			
			int val = Warnings.warnUser(shell, "Warning", 
					"This report already exists locally. Do you want to overwrite it?", 
					SWT.YES | SWT.NO | SWT.ICON_WARNING);
			
			if (val == SWT.NO)  // user pressed cancel
				return;
			
			Report.deleteAllVersions(senderId);
		}
		
		// import report
		
		// get all the versions of the dataset that are present in the DCF
		DatasetList<Dataset> allVersions = dialog.getSelectedDatasetVersions();
		
		// download and import the dataset
		ReportImporter downloader = this.getImporter(allVersions);
		
		String title = null;
		String message = null;
		int style = SWT.ICON_ERROR;
		
		try {
			
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
			
			// import the dataset
			downloader.importReport();
			
			title = "Success";
			message = "The report was successfully downloaded. Open it to check its content.";
			style = SWT.ICON_INFORMATION;
			
		} catch (MySOAPException e) {
			e.printStackTrace();
			
			String[] warnings = Warnings.getSOAPWarning(e.getError());
			title = warnings[0];
			message = warnings[1];
			
		} catch (XMLStreamException | IOException e) {
			e.printStackTrace();
			
			title = "Error";
			message = "The downloaded report is badly formatted. Please contact technical assistance.";
		}
		finally {
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
		
		if (message != null) {
			Warnings.warnUser(shell, title, message, style);
		}
	}
	
	/**
	 * Get the class encharged of importing the report into the database.
	 * The importer must be an extension of
	 * the {@link ReportImporter} class (it already processes
	 * amendments using all the report versions)
	 * @param allVersions
	 */
	public abstract ReportImporter getImporter(DatasetList<Dataset> allVersions);
}
