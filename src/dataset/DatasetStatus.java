package dataset;

/**
 * Enumerator that identifies the status of a {@link Dataset}
 * @author avonva
 *
 */
public enum DatasetStatus {
	
	DRAFT("DRAFT", "Draft"),  // local status, if message was never sent
	UPLOAD_FAILED("UPLOAD_FAILED", "Upload failed"),  // local status, used if send message fails
	VALID("VALID", "Valid"),
	UPLOADED("UPLOADED", "Uploaded"),  // dataset sent but no response received yet 
	PROCESSING("PROCESSING", "Processing"),
	VALID_WITH_WARNINGS("VALID_WITH_WARNINGS", "Valid with warnings"),
	REJECTED_EDITABLE("REJECTED EDITABLE", "Rejected editable"),
	REJECTED("REJECTED", "Rejected"),
	REJECTION_SENT("REJECTION_SENT", "Rejection sent"),
	DELETED("DELETED", "Deleted"),
	SUBMITTED("SUBMITTED", "Submitted"),
	SUBMISSION_SENT("SUBMISSION_SENT", "Submission sent"),
	ACCEPTED_DWH("ACCEPTED DWH", "Accepted DWH"),
	UPDATED_BY_DATA_RECEIVER("UPLOADED_BY_DATA_RECEIVER", "Uploaded by data receiver"),
	OTHER("OTHER", "Other");  // error state
	
	private String status;
	private String label;
	private String step;
	
	private DatasetStatus(String status, String label) {
		this.status = status;
		this.label = label;
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
	 * Get the status label
	 * @return
	 */
	public String getLabel() {
		return label;
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
		return this == DRAFT || this == UPLOAD_FAILED 
				|| this == REJECTED;
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
		return this == VALID || this == VALID_WITH_WARNINGS || this == REJECTION_SENT
				|| this == REJECTED_EDITABLE || this == UPLOADED
				|| this == SUBMISSION_SENT || this == SUBMITTED;
	}
	
	/**
	 * Check if the status of the dataset can be refreshed or not
	 * @return
	 */
	public boolean canDisplayAck() {
		return this == UPLOAD_FAILED || this == DRAFT 
				|| this == REJECTED || this == VALID 
				|| this == VALID_WITH_WARNINGS || this == REJECTED_EDITABLE;
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
	 * Check if the current dataset exists or not in the DCF
	 */
	public boolean existsInDCF() {
		return this == VALID 
				|| this == VALID_WITH_WARNINGS
				|| this == REJECTED_EDITABLE
				|| this == SUBMITTED
				|| this == ACCEPTED_DWH
				|| this == UPDATED_BY_DATA_RECEIVER
				|| this == SUBMISSION_SENT
				|| this == REJECTION_SENT;
	}

	/**
	 * Get the enumerator that matches the {@code text}
	 * @param text
	 * @return
	 */
	public static DatasetStatus fromString(String text) {
		
		String myStatus = text.toLowerCase().replaceAll(" ", "").replaceAll("_", "");
		
		for (DatasetStatus b : DatasetStatus.values()) {
			
			String otherStatus = b.status.toLowerCase().replaceAll(" ", "").replaceAll("_", "");
			
			if (otherStatus.equalsIgnoreCase(myStatus)) {
				return b;
			}
		}
		
		return OTHER;
	}
	
	@Override
	public String toString() {
		return this.getLabel();
	}
}
