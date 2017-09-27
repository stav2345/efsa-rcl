package message_creator;

/**
 * Operation types that are used to create a report
 * @author avonva
 *
 */
public enum OperationType {
	
	INSERT("Insert"),
	REPLACE("Replace"),
	REJECT("Reject"),
	SUBMIT("Submit"),
	NOT_SUPPORTED("NotSupported");
	
	private String code;

	private OperationType(String code) {
		this.code = code;
	}
	
	/**
	 * Get the code of the operation
	 * @return
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Get the enumerator that matches the {@code text}
	 * @param text
	 * @return
	 */
	public static OperationType fromString(String text) {
		
		for (OperationType b : OperationType.values()) {
			if (b.code.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
