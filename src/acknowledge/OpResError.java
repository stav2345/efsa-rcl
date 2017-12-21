package acknowledge;

public enum OpResError {

	USER_NOT_AUTHORIZED(1, "Account not authorized for the Data Collection"),
	NOT_EXISTING_DC(2, "The specified dcCode value is not a valid code registered in the system"),
	OTHER(3, "General error for op res error"),
	NONE(0, "None");
	
	private int priority;
	private String errorSubstring;
	OpResError(int priority, String errorSubstring) {
		this.errorSubstring = errorSubstring;
		this.priority = priority;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public boolean priorTo(OpResError other) {
		return this.priority >= other.priority;
	}
	
	public static OpResError fromString(String error) {

		String normError = error.toLowerCase();
		
		for (OpResError err : OpResError.values()) {
			if (normError.contains(err.errorSubstring.toLowerCase())) {
				return err;
			}
		}
		
		return OTHER;
	}
}
