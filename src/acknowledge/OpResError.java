package acknowledge;

public enum OpResError {

	USER_NOT_AUTHORIZED("Account not authorized for the Data Collection"),
	NOT_EXISTING_DC("The specified dcCode value is not a valid code registered in the system"),
	NONE("None");
	
	String errorSubstring;
	OpResError(String errorSubstring) {
		this.errorSubstring = errorSubstring;
	}
	
	public static OpResError fromString(String error) {
		
		String normError = error.toLowerCase();
		
		for (OpResError err : OpResError.values()) {
			if (err.errorSubstring.toLowerCase().contains(normError)) {
				return err;
			}
		}
		
		return NONE;
	}
}
