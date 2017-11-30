package report_validator;

import java.util.Collection;

public interface ReportError {
	
	enum ErrorType {
		
		ERROR("Error"),
		WARNING("Warning");
		
		private String text;
		private ErrorType(String text) {
			this.text = text;
		}
		
		public String getText() {
			return text;
		}
	}
	
	public ErrorType getTypeOfError();
	public String getErrorMessage();
	public Collection<String> getInvolvedRowsIdsMessage();
	public String getCorrectExample();
	public Collection<String> getErroneousValues();
}
