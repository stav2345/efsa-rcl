package report_validator;

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
	public String getInvolvedRowsIdsMessage();
	public String getCorrectExample();
	public String getErroneousValue();
}
