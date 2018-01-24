package global_utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import ack.DcfAckLog;
import ack.OpResError;
import app_config.PropertiesReader;
import converter.ExceptionConverter;
import i18n_messages.Messages;
import soap.MySOAPException;
import soap.SOAPError;

public class Warnings {
	
	public static Message create(String title, String message, int style) {
		return new Message(title, message, style);
	}
	
	/**
	 * Warn the user with a message box with custom style
	 * @param title
	 * @param message
	 * @param icon
	 */
	public static int warnUser(Shell shell, String title, String message, int style) {
		return create(title, message, style).open(shell);
	}
	
	/**
	 * Warn the user with an ERROR message box
	 * @param title
	 * @param message
	 */
	public static int warnUser(Shell shell, String title, String message) {
		return warnUser(shell, title, message, SWT.ICON_ERROR);
	}
	
	public static String getStackTrace(Exception e) {
		String trace = ExceptionConverter.getStackTrace(e);
	    return trace;
	}
	
	public static String[] getAckOperationWarning(DcfAckLog log) {

		OpResError error = log.getOpResError();
		
		String title = Messages.get("error.title");
		String message = null;
		
		switch(error) {
		case NOT_EXISTING_DC:
			message = Messages.get("dc.not.valid", 
					log.getDCCode(),
					PropertiesReader.getSupportEmail());
			break;
		case USER_NOT_AUTHORIZED:
			message = Messages.get("account.unauthorized", 
					log.getDCCode(),
					PropertiesReader.getSupportEmail());
			break;
		default:
			message = Messages.get("ack.general.error", 
					error.toString(),
					PropertiesReader.getSupportEmail());
			break;
		}
			
		return new String[] {title, message};
	}
	
	public static String[] getSOAPWarning(MySOAPException e) {
		
		String title = null;
		String message = null;
		SOAPError error = e.getError();
		switch(error) {
		case NO_CONNECTION:
			title = Messages.get("error.title");
			String trace = Warnings.getStackTrace(e);
			message = Messages.get("no.connection", trace);
			break;
		case UNAUTHORIZED:
		case FORBIDDEN:
			title = Messages.get("error.title");
			message = Messages.get("wrong.credentials");
			break;
		case MESSAGE_SEND_FAILED:
			title = Messages.get("error.title");
			message = Messages.get("send.message.failed");
			break;
		}
		
		return new String[] {title, message};
	}
	
	public static Message createSOAPWarning(MySOAPException e) {
		String[] warnings = getSOAPWarning(e);
		return create(warnings[0], warnings[1], SWT.ICON_ERROR);
	}
	
	
	public static void showSOAPWarning(Shell shell, MySOAPException error) {
		String[] warning = Warnings.getSOAPWarning(error);
		Warnings.warnUser(shell, warning[0], warning[1]);
	}
}
