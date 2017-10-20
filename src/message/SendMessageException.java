package message;

public class SendMessageException extends Exception {

	private static final long serialVersionUID = -2425817526913831813L;

	private MessageResponse response;
	
	public SendMessageException(MessageResponse response) {
		super(response.getTrxError());
		this.response = response;
	}
	
	public MessageResponse getResponse() {
		return response;
	}
	
	public String getErrorMessage() {
		return this.response.getTrxError();
	}
}
