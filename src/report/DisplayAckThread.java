package report;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;

import ack.DcfAck;
import app_config.AppPaths;
import global_utils.Message;
import global_utils.Warnings;
import html_viewer.HtmlViewer;
import i18n_messages.Messages;
import soap.DetailedSOAPException;

public class DisplayAckThread extends Thread {

	private static final Logger LOGGER = LogManager.getLogger(DisplayAckThread.class);
	
	private Message result;
	private EFSAReport report;
	private ThreadFinishedListener listener;
	
	public DisplayAckThread(EFSAReport report) {
		this.report = report;
	}
	
	@Override
	public void run() {
		
		this.result = displayAck();
		
		if (listener != null)
			listener.finished(this);
	}
	
	public void setListener(ThreadFinishedListener listener) {
		this.listener = listener;
	}
	
	public Message getLog() {
		return result;
	}
	
	/**
	 * Display an ack in the browser
	 * @param shell
	 * @param report
	 */
	public Message displayAck() {
		
		String messageId = report.getMessageId();
		
		// if no message id found
		if (messageId == null || messageId.isEmpty()) {
			return Warnings.create(Messages.get("error.title"), 
					Messages.get("ack.no.message.id"), SWT.ICON_ERROR);
		}
		
		// if no connection return
		DcfAck ack = null;
		try {
			ack = report.getAck();
		} catch (DetailedSOAPException e) {
			e.printStackTrace();
			LOGGER.error("Cannot get ack for report=" + report.getSenderId(), e);
			return Warnings.createSOAPWarning(e);
		}

		// if no ack return
		if (ack == null) {
			String message = Messages.get("ack.not.available");
			return Warnings.create(Messages.get("error.title"), message, SWT.ICON_ERROR);
		}
		
		// get the raw log to send the .xml to the browser
		InputStream rawLog = ack.getLog().getRawLog();
		
		// write it into a file in the temporary folder
		// in order to be able to open it in the browser
		String filename = AppPaths.TEMP_FOLDER + "ack_" + System.currentTimeMillis() + ".xml";
		File targetFile = new File(filename);
		
		try {
			
			Files.copy(rawLog, targetFile.toPath());
			
			// close input stream
			rawLog.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("Cannot copy the ack into local disk (for displaying the html with the browser)", e);
			return Warnings.create(Messages.get("error.title"), 
					Messages.get("ack.file.not.found"), SWT.ICON_ERROR);
		}
		
		// open the ack in the browser to see it formatted
		HtmlViewer viewer = new HtmlViewer();
		viewer.open(targetFile);
		
		return null;
	}
}
