package global_utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import app_config.PropertiesReader;

public class Message {
	
	private String title;
	private String message;
	private int style;
	private boolean fatal;
	
	public Message(String title, String message, int style) {
		this(title, message, style, false);
	}
	
	public Message(String title, String message, int style, boolean fatal) {
		this.title = title;
		this.message = message;
		this.style = style;
		this.fatal = fatal;
	}
	
	public int open(Shell shell) {
		MessageBox mb = new MessageBox(shell, style);
		mb.setText(title);
		mb.setMessage(message);
		
		if (fatal)
			PropertiesReader.openMailPanel();
		
		return mb.open();
	}
}