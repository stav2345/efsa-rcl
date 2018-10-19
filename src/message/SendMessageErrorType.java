package message;

public enum SendMessageErrorType {
	NONE("------NONE------"),  // not put an empty string, otherwise the contains will match every string with this
	NON_DP_USER("No valid Account found for user with id"),
	USER_WRONG_ORG("The user is not granted to transmit data on behalf of the specified senderOrgCode:"),
	USER_WRONG_PROFILE("java.lang.NullPointerException");
	
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
		
		String loweredText = text.toLowerCase();
		
		for (SendMessageErrorType b : SendMessageErrorType.values()) {
			if (loweredText.contains(b.errorMessage.toLowerCase())) {
				return b;
			}
		}
		return NONE;
	}
}