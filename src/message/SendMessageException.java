package message;

public class SendMessageException extends Exception {

	private static final long serialVersionUID = -2425817526913831813L;

	public SendMessageException(MessageResponse response) {
		super(response.getTrxError());
	}
}
