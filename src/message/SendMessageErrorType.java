package message;

public enum SendMessageErrorType {
	NONE(""),
	NON_DP_USER("No valid Account found for user with id"),
	USER_WITHOUT_ORG("java.lang.NullPointerException");
	
	private String errorMessage;
	
	/**
	 * Initialize the enumerator with the real 
	 * header name that is present in the xlsx
	 * @param headerName
	 */
	private SendMessageErrorType(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	/**
	 * Get the header name related to the enum field
	 * @return
	 */
	public String getHeaderName() {
		return errorMessage;
	}

	/**
	 * Get the enumerator that matches the {@code text}
	 * @param text
	 * @return
	 */
	public static SendMessageErrorType fromString(String text) {
		
		if (text == null)
			return NONE;
		
		text = text.toLowerCase();
		
		for (SendMessageErrorType b : SendMessageErrorType.values()) {
			if (b.errorMessage.toLowerCase().contains(text)) {
				return b;
			}
		}
		return null;
	}
}