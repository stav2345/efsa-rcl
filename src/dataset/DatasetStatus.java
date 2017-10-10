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
	REJECTION_SENT("REJECTION_SENT"),
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
		return this == DRAFT;
	}
	
	
	/**
	 * Check if the dataset can be sent to the dcf or not
	 * @return
	 */
	public boolean canBeSent() {
		return this == DRAFT || this == DatasetStatus.UPLOAD_FAILED 
				|| this == DatasetStatus.REJECTED;
	}
	
	/**
	 * Check if the dataset can be made editable or not
	 * @return
	 */
	public boolean canBeMadeEditable() {
		return this == VALID || this == VALID_WITH_WARNINGS 
				|| this == REJECTED_EDITABLE;
	}
	
	/**
	 * Check if the dataset can be rejected or not
	 * @return
	 */
	public boolean canBeRejected() {
		return this == VALID || this == VALID_WITH_WARNINGS; 
	}
	
	/**
	 * Check if the status of the dataset can be refreshed or not
	 * @return
	 */
	public boolean canBeRefreshed() {
		return this == VALID || this == DatasetStatus.VALID_WITH_WARNINGS || this == REJECTION_SENT
				|| this == DatasetStatus.REJECTED_EDITABLE || this == DatasetStatus.UPLOADED
				|| this == DatasetStatus.SUBMISSION_SENT || this == DatasetStatus.SUBMITTED;
	}
	
	/**
	 * Check if the dataset can be submitted or not
	 * @return
	 */
	public boolean canBeSubmitted() {
		return this == VALID || this == VALID_WITH_WARNINGS; 
	}
	
	/**
	 * Check if the dataset can be amended or not
	 * @return
	 */
	public boolean canBeAmended() {
		return this == ACCEPTED_DWH; 
	}
	
	/**
	 * Check if an ack for the chosen dataset can be picked up or not
	 * @return
	 */
	public boolean canGetAck() {
		return this == DatasetStatus.UPLOADED || this == SUBMISSION_SENT
				|| this == REJECTION_SENT;
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
