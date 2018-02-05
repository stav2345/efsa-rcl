package message;

public class SendMessageException extends Exception {

	private static final long serialVersionUID = -2425817526913831813L;

	private MessageResponse response;
	
	public SendMessageException(Exception e) {
		super(e);
	}
	
	public SendMessageException(MessageResponse response) {
		super(response.getTrxError());
		this.response = response;
	}
	
	public MessageResponse getResponse() {
		return response;
	}
	
	public String getErrorMessage() {
		
		if (this.response == null)
			return null;
		
		return this.response.getTrxError();
	}
}
