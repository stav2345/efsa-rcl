package dataset;

/**
 * Enumerator that identifies the status of a {@link Dataset}
 * @author avonva
 *
 */
public enum DatasetStatus {
	
	DRAFT("DRAFT"),  // local status, if message was never sent
	UPLOAD_FAILED("UPLOAD_FAILED"),  // local status, used if send message fails
	VALID("VALID"),
	UPLOADED("UPLOADED"),  // dataset sent but no response received yet 
	PROCESSING("PROCESSING"),
	VALID_WITH_WARNINGS("VALID_WITH_WARNINGS"),
	REJECTED_EDITABLE("REJECTED EDITABLE"),
	REJECTED("REJECTED"),
	DELETED("DELETED"),
	SUBMITTED("SUBMITTED"),
	SUBMISSION_SENT("SUBMISSION_SENT"),
	ACCEPTED_DWH("ACCEPTED DWH"),
	UPDATED_BY_DATA_RECEIVER("Uploaded by data receiver"),
	OTHER("OTHER");  // error state
	
	private String status;
	private String step;
	
	private DatasetStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Set the status step
	 * @param step
	 */
	public void setStep(String step) {
		this.step = step;
	}
	
	/**
	 * Get the raw status
	 * @return
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Get the status step
	 * @return
	 */
	public String getStep() {
		return step;
	}

	/**
	 * Check if the dataset is editable or not
	 * @return
	 */
	public boolean isEditable() {
		return this == DRAFT || this == REJECTED_EDITABLE || this == REJECTED
				|| this == VALID || this == VALID_WITH_WARNINGS;
	}
	
	/**
	 * Check if the dataset can be rejected or not
	 * @return
	 */
	public boolean canBeRejected() {
		return this == VALID || this == VALID_WITH_WARNINGS; 
	}
	
	/**
	 * Check if the dataset can be submitted or not
	 * @return
	 */
	public boolean canBeSubmitted() {
		return this == VALID || this == VALID_WITH_WARNINGS; 
	}


	/**
	 * Get the enumerator that matches the {@code text}
	 * @param text
	 * @return
	 */
	public static DatasetStatus fromString(String text) {
		
		for (DatasetStatus b : DatasetStatus.values()) {
			if (b.status.equalsIgnoreCase(text)) {
				return b;
			}
		}
		
		return OTHER;
	}
}
