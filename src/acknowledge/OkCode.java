package acknowledge;

public enum OkCode {
	OK("OK"),
	KO("KO"),
	OTHER("OTHER");
	
	private String headerName;
	
	/**
	 * Initialize the enumerator with the real 
	 * header name that is present in the xlsx
	 * @param headerName
	 */
	private OkCode(String headerName) {
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
	public static OkCode fromString(String text) {
		
		for (OkCode b : OkCode.values()) {
			if (b.headerName.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return OTHER;
	}
}
