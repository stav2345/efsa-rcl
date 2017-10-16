package global_utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import webservice.SOAPError;

public class Warnings {

	/**
	 * Warn the user with a message box with custom style
	 * @param title
	 * @param message
	 * @param icon
	 */
	public static int warnUser(Shell shell, String title, String message, int style) {
		MessageBox mb = new MessageBox(shell, style);
		mb.setText(title);
		mb.setMessage(message);
		return mb.open();
	}
	
	/**
	 * Warn the user with an ERROR message box
	 * @param title
	 * @param message
	 */
	public static int warnUser(Shell shell, String title, String message) {
		MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR);
		mb.setText(title);
		mb.setMessage(message);
		return mb.open();
	}
	
	public static String[] getSOAPWarning(SOAPError error) {
		
		String title = null;
		String message = null;
		switch(error) {
		case NO_CONNECTION:
			title = "Connection error";
			message = "ERR600: It was not possible to connect to the DCF, please check your internet connection.";
			break;
		case UNAUTHORIZED:
		case FORBIDDEN:
			title = "Wrong credentials";
			message = "ERR100: Your credentials are incorrect. Please check them in the Settings.";
			break;
		}
		
		return new String[] {title, message};
	}
	
	
	public static void showSOAPWarning(Shell shell, SOAPError error) {
		String[] warning = Warnings.getSOAPWarning(error);
		Warnings.warnUser(shell, warning[0], warning[1]);
	}
}
