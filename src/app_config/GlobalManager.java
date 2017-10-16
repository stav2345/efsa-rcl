package app_config;

import report.Report;

public class GlobalManager {

	private static GlobalManager self;
	public static Report openedReport;   // the current report
	public static boolean dcTest;        // if we are using the data collection of test
	
	private GlobalManager() {}
	
	public static GlobalManager getInstance() {
		
		if (self == null) {
			self = new GlobalManager();
		}
		
		return self;
	}
	
	public boolean isDcTest() {
		return dcTest;
	}
	
	public Report getOpenedReport() {
		return openedReport;
	}
	
	public void setOpenedReport(Report openedReport) {
		GlobalManager.openedReport = openedReport;
	}
	public void setDcTest(boolean dcTest) {
		GlobalManager.dcTest = dcTest;
	}
}
