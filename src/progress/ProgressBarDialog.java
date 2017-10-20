package progress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;


public class ProgressBarDialog implements IProgressBar {

	private Shell parent;
	private Shell dialog;
	private String title;
	private CustomProgressBar progressBar;
	private boolean opened;        // if the bar is opened or not

	/**
	 * Constructor, initialize the progress bar
	 * @param parent the shell where to create the progress bar
	 */
	public ProgressBarDialog(Shell parent, String title) {
		this.parent = parent;
		this.title = title;
		this.opened = false;
		this.initializeGraphics();
	}

	public ProgressBar getProgressBar() {
		return progressBar.getProgressBar();
	}
	
	/**
	 * Creates all the graphics for the progress bar
	 */
	public void initializeGraphics() {
		
		// create pop up
		this.dialog = new Shell(parent, SWT.TITLE | SWT.APPLICATION_MODAL);
		dialog.setSize(300, 130);
		dialog.setText(title);
		dialog.setLayout(new GridLayout(1, false));

		// progress bar
		this.progressBar = new CustomProgressBar(dialog);

		setLocationAtCenter();
	}
	
	/**
	 * Move dialog at the center of the screen
	 */
	public void setLocationAtCenter() {
		Monitor primary = parent.getMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle pict = dialog.getBounds();
		int x = bounds.x + (bounds.width - pict.width) / 2;
		int y = bounds.y + (bounds.height - pict.height) / 2;
		dialog.setLocation(x, y);
	}

	/**
	 * Set the location of the progress bar
	 * @param x
	 * @param y
	 */
	public void setLocation(int x, int y) {
		dialog.setLocation(x, y);
	}
	
	/**
	 * Get the location of the progress bar
	 */
	public Point getLocation() {
		return dialog.getLocation();
	}
	
	/**
	 * Open the dialog
	 */
	public void open() {
		
		opened = true;

		parent.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				dialog.open();
			}
		});
	}

	
	/**
	 * Close the progress bar
	 */
	public void close() {

		// set the opened state accordingly
		opened = false;

		if (progressBar.isDisposed())
			return;

		Display disp = progressBar.getDisplay();
		
		if (disp.isDisposed())
			return;
		
		disp.asyncExec(new Runnable() {
			
			public void run () {
				
				if (dialog.isDisposed())
					return;
				
				dialog.close();
			}
		});
	}

	/**
	 * Add a progress to the progress bar. The current progress
	 * is added to the last progress. If a double < 1 is passed
	 * we accumulate the progresses until we reach an integer, in order
	 * to set the progress bar progresses
	 * @param progress
	 */
	public void addProgress(double progress) {
		progressBar.addProgress(progress);
	}

	/**
	 * Set the progress of the progress bar
	 * @param percent
	 */
	public void setProgress(double percent) {
		progressBar.setProgress(percent);
	}

	/**
	 * Set the bar to 100%
	 */
	public void fillToMax() {
		progressBar.fillToMax();
	}

	/**
	 * Get if the progress bar is open or not
	 * @return
	 */
	public boolean isOpened() {
		return opened;
	}

	/**
	 * Check if the progress bar is filled at 100%
	 * @return
	 */
	public boolean isCompleted() {
		return progressBar.isCompleted();
	}
}
