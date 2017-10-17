package app_config;

import report.Report;

public class GlobalManager {

	private static GlobalManager self;
	public static Report openedReport;   // the current report
	
	private GlobalManager() {}
	
	public static GlobalManager getInstance() {
		
		if (self == null) {
			self = new GlobalManager();
		}
		
		return self;
	}
	
	public Report getOpenedReport() {
		return openedReport;
	}
	
	public void setOpenedReport(Report openedReport) {
		GlobalManager.openedReport = openedReport;
	}
}
