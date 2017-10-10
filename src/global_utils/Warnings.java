package global_utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

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
}
