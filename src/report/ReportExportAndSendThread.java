package report;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import dataset.Dataset;
import message.MessageConfigBuilder;
import message.SendMessageException;
import progress_bar.ProgressListener;
import providers.IReportService;
import soap.DetailedSOAPException;

public class ReportExportAndSendThread extends Thread {

	private static final Logger LOGGER = LogManager.getLogger(ReportExportAndSendThread.class);

	private IReportService reportService;

	private Report report;
	private MessageConfigBuilder messageConfig;
	private Dataset dcfDataset;
	private ProgressListener progressListener;

	public ReportExportAndSendThread(Report report, Dataset dcfDataset, 
			MessageConfigBuilder messageConfig, IReportService reportService) {
		this.report = report;
		this.messageConfig = messageConfig;
		this.dcfDataset = dcfDataset;
		this.reportService = reportService;
	}

	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	@Override
	public void run() {

		try {
			reportService.send(report, dcfDataset, messageConfig, progressListener);
		} catch (DetailedSOAPException | IOException | ParserConfigurationException | SAXException
				| SendMessageException | ReportException e) {

			e.printStackTrace();

			LOGGER.error("Cannot send report=" + report.getSenderId(), e);

			if (progressListener != null)
				this.progressListener.progressStopped(e);
		}
	}
}
