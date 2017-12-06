package report_validator;

import java.util.Collection;

import i18n_messages.Messages;

public interface ReportError {
	
	enum ErrorType {
		
		ERROR(Messages.get("table.type.error")),
		WARNING(Messages.get("table.type.warning"));
		
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
	public String getSuggestions();
	public Collection<String> getErroneousValues();
}
