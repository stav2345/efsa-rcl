package global_utils;

import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import app_config.PropertiesReader;
import dataset.IDataset;

public class Message {
	
	private String code;
	private String title;
	private String message;
	private int style;
	private boolean fatal;
	private IDataset[] reports;
	
	public Message(String title, String message, int style) {
		this(title, message, style, false);
	}
	
	public Message(String title, String message, int style, boolean fatal) {
		this.title = title;
		this.message = message;
		this.style = style;
		this.fatal = fatal;
		this.reports = null;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setReports(IDataset... reports) {
		this.reports = reports;
	}

	public String getTitle() {
		return title;
	}
	
	public String getMessage() {
		return message;
	}
	
	public int getStyle() {
		return style;
	}
	
	public boolean isFatal() {
		return fatal;
	}
	
	public int open(Shell shell) {
		MessageBox mb = new MessageBox(shell, style);
		mb.setText(title);
		mb.setMessage(message);
		
		if (fatal)
			PropertiesReader.openMailPanel(reports);
		
		return mb.open();
	}
	
	@Override
	public String toString() {
		return code + " " + message;
	}
}