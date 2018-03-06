package report;

import providers.IReportService;

public class DisplayAckThread extends Thread {
	
	private DisplayAckResult result;
	private EFSAReport report;
	private IReportService reportService;
	private ThreadFinishedListener listener;
	
	public DisplayAckThread(EFSAReport report, IReportService reportService) {
		this.report = report;
		this.reportService = reportService;
	}
	
	@Override
	public void run() {
		
		this.result = reportService.displayAck(report);
		
		if (listener != null)
			listener.finished(this);
	}
	
	public void setListener(ThreadFinishedListener listener) {
		this.listener = listener;
	}
	
	public DisplayAckResult getDisplayAckResult() {
		return result;
	}
}
