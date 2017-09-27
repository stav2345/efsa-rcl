package warn_user;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class Warnings {

	/**
	 * Warn the user with an ERROR message box
	 * @param title
	 * @param message
	 * @param icon
	 */
	public static int warnUser(Shell shell, String title, String message, int icon) {
		MessageBox mb = new MessageBox(shell, icon);
		mb.setText(title);
		mb.setMessage(message);
		return mb.open();
	}
	
	/**
	 * Warn the user with a message box with custom icon
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
