package report;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import message.SendMessageException;
import progress_bar.ProgressListener;
import soap.MySOAPException;

public class ReportSender extends Thread {

	private enum Status {
		CONTINUE,
		STOP,
		WAIT
	}
	
	private Status status;
	
	private Report report;
	private ProgressListener progressListener;
	private ReportSenderListener reportListener;
	
	public ReportSender(Report report) {
		this.report = report;
		this.status = Status.WAIT;
	}
	
	public void setReportListener(ReportSenderListener reportListener) {
		this.reportListener = reportListener;
	}
	
	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	@Override
	public void run() {
		
		if (this.reportListener == null) {
			System.err.println("Cannot start ReportSender without reportListener set.");
			return;
		}
		
		try {
			
			send();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			if (progressListener != null)
				this.progressListener.progressStopped(e);
		}
	}
	
	/**
	 * Send the report
	 * @throws MySOAPException
	 * @throws ReportException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws SendMessageException
	 * @throws InterruptedException 
	 */
	private void send() throws MySOAPException, ReportException, 
		IOException, ParserConfigurationException, SAXException, SendMessageException, 
		InterruptedException {
		
		if (progressListener != null)
			progressListener.progressChanged(5);
		
		ReportSendOperation opType = report.getSendOperation();
		
		if (progressListener != null)
			progressListener.progressChanged(20);
		
		// wait for confirmation
		reportListener.confirm(opType);
		
		while (status == Status.WAIT) {
			Thread.sleep(500);
		}
		
		if (status == Status.STOP)
			return;
		
		// Update the report dataset id if it was found in the DCF
		// (Required if we are overwriting an existing report)
		if (opType.getDataset() != null) {

			System.out.println("Overwriting dataset id: " + report.getDatasetId() 
				+ " with " + opType.getDataset().getId());
			
			report.setDatasetId(opType.getDataset().getId());
			report.update();
		}
		
		report.exportAndSend(opType.getOpType(), progressListener);
	}
	
	public void confirmSend() {
		this.status = Status.CONTINUE;
	}
	
	public void stopSend() {
		this.status = Status.STOP;
	}
	
	/**
	 * To be called to stop or to continue the thread
	 * @param status
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public interface ReportSenderListener {
		/**
		 * Confirm or not the send operation, use {@link ReportSender#setStatus(Status)}
		 * to confirm or not it.
		 * @param opType
		 */
		public void confirm(ReportSendOperation opType);
	}
}
