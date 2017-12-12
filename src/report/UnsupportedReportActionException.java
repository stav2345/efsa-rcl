package report;

public class UnsupportedReportActionException extends Exception {
	
	private static final long serialVersionUID = 8112874740274540347L;
	private ReportSendOperation op;
	
	public UnsupportedReportActionException() {
		super();
	}
	
	public UnsupportedReportActionException(ReportSendOperation op) {
		super();
		this.op = op;
	}
	
	public ReportSendOperation getOperation() {
		return op;
	}
}
