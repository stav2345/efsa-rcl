package acknowledge;

public enum MessageValResCode {
	DELIVERED("DELIVERED"),
	DISCARDED("DISCARDED"),
	OTHER("OTHER");
	
	private String headerName;
	
	/**
	 * Initialize the enumerator with the real 
	 * header name that is present in the xlsx
	 * @param headerName
	 */
	private MessageValResCode(String headerName) {
		this.headerName = headerName;
	}
	
	/**
	 * Get the header name related to the enum field
	 * @return
	 */
	public String getHeaderName() {
		return headerName;
	}

	/**
	 * Get the enumerator that matches the {@code text}
	 * @param text
	 * @return
	 */
	public static MessageValResCode fromString(String text) {
		
		for (MessageValResCode b : MessageValResCode.values()) {
			if (b.headerName.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return OTHER;
	}
}
