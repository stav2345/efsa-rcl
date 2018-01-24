package global_utils;

import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class Message {
	private String title;
	private String message;
	private int style;
	
	public Message(String title, String message, int style) {
		this.title = title;
		this.message = message;
		this.style = style;
	}
	
	public int open(Shell shell) {
		MessageBox mb = new MessageBox(shell, style);
		mb.setText(title);
		mb.setMessage(message);
		return mb.open();
	}
}