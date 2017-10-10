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
	DEBUG_TEST("Debug TEST (Only for debugging purposes)"),
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
	 * Check if the operation needs an empty dataset
	 * to be sent to the dcf
	 * @return
	 */
	public boolean needEmptyDataset() {
		return this == REJECT || this == SUBMIT;
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
