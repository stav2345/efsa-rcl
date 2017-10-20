package report;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import amend_manager.ReportImporter;
import progress.ProgressListener;
import webservice.MySOAPException;

/**
 * Thread to import a tse report
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
			
		} catch (MySOAPException | XMLStreamException | IOException e) {
			e.printStackTrace();
			
			if (progressListener != null)
				this.progressListener.exceptionThrown(e);
		}
	}
}
