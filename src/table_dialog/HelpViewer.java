package table_dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import i18n_messages.Messages;

/**
 * Panel that displays an help label with an help icon. The
 * help icon can be clicked to trigger the listener set 
 * with the {@link #setListener(MouseListener)} method.
 * It is also possible to set a tooltip text for the help icon
 * by setting {@link #setToolTipText(String)}.
 * @author avonva
 *
 */
public class HelpViewer {

	private Composite parent;
	private String title;
	private Label info;
	private Button helpBtn;
	private boolean addHelpButton;
	
	/**
	 * Panel that displays an help label with an help icon. The
	 * help icon can be clicked to trigger the listener set 
	 * with the {@link #setListener(MouseListener)} method.
	 * It is also possible to set a tooltip text for the help icon
	 * by setting {@link #setToolTipText(String)}.
	 * @author avonva
	 *
	 */
	public HelpViewer(Composite parent, String title, boolean addHelpButton) {
		this.parent = parent;
		this.title = title;
		this.addHelpButton = addHelpButton;
		create();
	}
	
	public HelpViewer(Composite parent, String title) {
		this(parent, title, true);
	}
	
	/**
	 * Add a listener to the help button
	 * @param listener
	 */
	public void setListener(MouseListener listener) {
		if (addHelpButton)
			this.helpBtn.addMouseListener(listener);
	}
	
	/**
	 * Tooltip text for the image
	 * @param text
	 */
	public void setToolTipText(String text) {
		if (addHelpButton)
			this.helpBtn.setToolTipText(text);
	}
	
	private void create() {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2,false));
		
		this.info = new Label(composite, SWT.NONE);
		this.info.setText(title);

		// set the label font to italic and bold
		FontData fontData = Display.getCurrent().getSystemFont().getFontData()[0];

		Font font = new Font(Display.getCurrent(), 
				new FontData(fontData.getName(), fontData.getHeight() + 5, SWT.BOLD));

		this.info.setFont (font);
		
		if (!addHelpButton)
			return;
		
		// help icon
		helpBtn = new Button(composite, SWT.PUSH);
		
		helpBtn.setToolTipText(Messages.get("help.tip"));
		
		Image image = new Image(Display.getCurrent(), 
				this.getClass().getClassLoader().getResourceAsStream("help.png"));
		
		this.helpBtn.setImage(image);
	}
}
