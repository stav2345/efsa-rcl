package report;

import global_utils.Message;
import providers.IReportService;

public class DisplayAckThread extends Thread {
	
	private Message result;
	private String messageId;
	private IReportService reportService;
	private ThreadFinishedListener listener;
	
	public DisplayAckThread(String messageId, IReportService reportService) {
		this.messageId = messageId;
		this.reportService = reportService;
	}
	
	@Override
	public void run() {
		
		this.result = reportService.displayAck(messageId);
		
		if (listener != null)
			listener.finished(this);
	}
	
	public void setListener(ThreadFinishedListener listener) {
		this.listener = listener;
	}
	
	public Message getLog() {
		return result;
	}
}
