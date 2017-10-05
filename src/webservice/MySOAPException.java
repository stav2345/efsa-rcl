package webservice;

import javax.xml.soap.SOAPException;

/**
 * Custom version of the soap exception which saves
 * also the reason of the exception.
 * @author avonva
 *
 */
public class MySOAPException extends SOAPException {

	private static final long serialVersionUID = 1L;
	private SOAPError error;
	
	public MySOAPException(SOAPException e) {
		super(e);
		
		String message = e.getMessage();
		
		if (message.contains("401"))
			this.setError(SOAPError.UNAUTHORIZED);
		else if (message.contains("403"))
			this.setError(SOAPError.FORBIDDEN);
		else
			this.setError(SOAPError.NO_CONNECTION);
	}
	
	public void setError(SOAPError error) {
		this.error = error;
	}
	
	public SOAPError getError() {
		return error;
	}
}
