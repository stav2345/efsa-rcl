package report;

import global_utils.Message;
import providers.IReportService;

public class RefreshStatusThread extends Thread {

	private IReportService reportService;
	private ThreadFinishedListener listener;
	private Report report;
	private Message result;
	
	public RefreshStatusThread(Report report, IReportService reportService) {
		this.report = report;
		this.reportService = reportService;
	}
	
	public void setListener(ThreadFinishedListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void run() {
		
		result = reportService.refreshStatus(report);
		
		if (listener != null)
			listener.finished(this);
	}
	
	public Message getLog() {
		return result;
	}
}
