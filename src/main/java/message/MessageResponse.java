package message;

public class MessageResponse {
	
	private String messageId;
	private TrxCode trxState;
	private String trxError;
	
	public MessageResponse(String messageId, TrxCode trxState, String trxError) {
		this.messageId = messageId;
		this.trxState = trxState;
		this.trxError = trxError;
	}
	
	public String getMessageId() {
		return messageId;
	}
	public TrxCode getTrxState() {
		return trxState;
	}
	public String getTrxError() {
		return trxError;
	}
	
	public SendMessageErrorType getErrorType() {
		return SendMessageErrorType.fromString(trxError);
	}
	
	/**
	 * Check if the response was valid or not
	 * @return
	 */
	public boolean isCorrect() {
		return trxState == TrxCode.TRXOK;
	}
	
	@Override
	public String toString() {
		return "MessageId=" + messageId + "; trxCode=" + trxState;
	}
}
