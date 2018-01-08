package dataset;

import i18n_messages.Messages;

/**
 * Enumerator that identifies the status of a {@link Dataset}
 * @author avonva
 *
 */
public enum RCLDatasetStatus {
	
	DRAFT("DRAFT", Messages.get("draft")),  // local status, if message was never sent
	LOCALLY_VALIDATED("LOCALLY_VALIDATED", Messages.get("validated")),
	UPLOAD_FAILED("UPLOAD_FAILED", Messages.get("upload.failed")),  // local status, used if send message fails
	VALID("VALID", Messages.get("valid")),
	UPLOADED("UPLOADED", Messages.get("uploaded")),  // dataset sent but no response received yet 
	PROCESSING("PROCESSING", Messages.get("processing")),
	VALID_WITH_WARNINGS("VALID_WITH_WARNINGS", Messages.get("valid.warnings")),
	REJECTED_EDITABLE("REJECTED EDITABLE", Messages.get("rejected.editable")),
	REJECTED("REJECTED", Messages.get("rejected")),
	REJECTION_SENT("REJECTION_SENT", Messages.get("rejection.sent")),
	DELETED("DELETED", Messages.get("deleted")),
	SUBMITTED("SUBMITTED", Messages.get("submitted")),
	SUBMISSION_SENT("SUBMISSION_SENT", Messages.get("submission.sent")),
	ACCEPTED_DWH("ACCEPTED DWH", Messages.get("accepted.dwh")),
	UPDATED_BY_DATA_RECEIVER("UPLOADED_BY_DATA_RECEIVER", Messages.get("uploaded.receiver")),
	OTHER("OTHER", Messages.get("other"));  // error state
	
	private String status;
	private String label;
	private String step;
	
	private RCLDatasetStatus(String status, String label) {
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
		return this == LOCALLY_VALIDATED || this == UPLOAD_FAILED 
				|| this == REJECTED;
	}
	
	/**
	 * Check if the dataset is valid or not
	 * @return
	 */
	public boolean isValid() {
		return this == LOCALLY_VALIDATED;
	}
	
	/**
	 * Check if the dataset can be made editable or not
	 * @return
	 */
	public boolean canBeMadeEditable() {
		return this == VALID || this == VALID_WITH_WARNINGS 
				|| this == REJECTED_EDITABLE || this == REJECTED 
				|| this == UPLOAD_FAILED || this == DELETED || this == LOCALLY_VALIDATED;
	}
	
	public boolean canBeChecked() {
		return this == DRAFT;
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
		return this == RCLDatasetStatus.UPLOADED || this == SUBMISSION_SENT
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
	public static RCLDatasetStatus fromString(String text) {
		
		String myStatus = text.toLowerCase().replaceAll(" ", "").replaceAll("_", "");
		
		for (RCLDatasetStatus b : RCLDatasetStatus.values()) {
			
			String otherStatus = b.status.toLowerCase().replaceAll(" ", "").replaceAll("_", "");
			
			if (otherStatus.equalsIgnoreCase(myStatus)) {
				return b;
			}
		}
		
		return OTHER;
	}
	
	public static RCLDatasetStatus fromDcfStatus(DcfDatasetStatus status) {
		
		RCLDatasetStatus newStatus;
		
		switch(status) {
		case ACCEPTED_DWH:
			newStatus = ACCEPTED_DWH;
			break;
		case DELETED:
			newStatus = DELETED;
			break;
		case PROCESSING:
			newStatus = PROCESSING;
			break;
		case SUBMITTED:
			newStatus = SUBMITTED;
			break;
		case REJECTED:
			newStatus = REJECTED;
			break;
		case REJECTED_EDITABLE:
			newStatus = REJECTED_EDITABLE;
			break;
		case VALID:
			newStatus = VALID;
			break;
		case VALID_WITH_WARNINGS:
			newStatus = VALID_WITH_WARNINGS;
			break;
		case UPDATED_BY_DATA_RECEIVER:
			newStatus = UPDATED_BY_DATA_RECEIVER;
			break;
		case OTHER:
		default:
			newStatus = OTHER;
			break;
		}
		
		return newStatus;
	}
	
	@Override
	public String toString() {
		return this.getLabel();
	}
}
