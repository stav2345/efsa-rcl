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

	public MySOAPException(SOAPException e) {
		super(e);
		e.printStackTrace();
	}
	
	public SOAPError getError() {
		
		SOAPError error;
		
		String message = this.getMessage();
		
		/*if (message.contains("401"))
			error = SOAPError.UNAUTHORIZED;
		else if (message.contains("403"))
			error = SOAPError.FORBIDDEN;
		else
			error = SOAPError.NO_CONNECTION;*/
		
		error = SOAPError.NO_CONNECTION;
		
		return error;
	}
}
