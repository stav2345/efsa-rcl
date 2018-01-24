package report;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import amend_manager.ReportImporter;
import progress_bar.ProgressListener;

/**
 * Thread to import a report
 * @author avonva
 *
 */
public class ReportImporterThread extends Thread {

	private static final Logger LOGGER = LogManager.getLogger(ReportImporterThread.class);
	
	private ReportImporter importer;
	private ProgressListener progressListener;
	
	public ReportImporterThread(ReportImporter importer) {
		this.importer = importer;
	}
	
	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
		this.importer.setProgressListener(progressListener);
	}
	
	@Override
	public void run() {
		
		try {
			
			this.importer.importReport();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			LOGGER.error("Cannot import report", e);
			
			// delete the corrupted versions
			this.importer.abort();
			
			if (progressListener != null)
				this.progressListener.progressStopped(e);
		}
	}
}
