package report;

import amend_manager.ReportImporter;
import progress_bar.ProgressListener;

/**
 * Thread to import a report
 * @author avonva
 *
 */
public class ReportImporterThread extends Thread {

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
			
			// delete the corrupted versions
			this.importer.abort();
			
			if (progressListener != null)
				this.progressListener.progressStopped(e);
		}
	}
}
