package acknowledge;

/**
 * We need a different enum for the acks since names are different...
 * @author avonva
 *
 */
public enum AckDatasetStatus {
	
	VALID("VALID"),
	PROCESSING("PROCESSING"),
	VALID_WITH_WARNINGS("VALID_WITH_WARNINGS"),
	REJECTED_EDITABLE("REJECTED_EDITABLE"),
	REJECTED("REJECTED"),
	DELETED("DELETED"),
	SUBMITTED("SUBMITTED"),
	ACCEPTED_DWH("ACCEPTED_DWH"),
	OTHER("OTHER");
	
	private String status;
	AckDatasetStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
	
	/**
	 * Get the enumerator that matches the {@code text}
	 * @param text
	 * @return
	 */
	public static AckDatasetStatus fromString(String text) {
		
		for (AckDatasetStatus b : AckDatasetStatus.values()) {
			if (b.status.equalsIgnoreCase(text)) {
				return b;
			}
		}
		
		return OTHER;
	}
}
