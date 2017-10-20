package progress;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class CustomProgressBar implements IProgressBar {

	private Composite parent;
	private ProgressBar progressBar;
	
	private int progressDone;
	private double progressDoneFract;  // used to manage fractional progresses
	private int progressLimit;  // set this to limit the bar progress
	
	/**
	 * Create a progress bar
	 * @param parent
	 */
	public CustomProgressBar(Composite parent) {
		
		this.parent = parent;
		
		this.progressLimit = 100;
		this.progressDone = 0;
		this.progressDoneFract = 0;

		initializeGraphics();
	}
	
	/**
	 * Creates all the graphics for the progress bar
	 * @param parentShell
	 */
	public void initializeGraphics() {

		// progress bar
		progressBar = new ProgressBar(parent, SWT.SMOOTH);
		progressBar.setMaximum(100);

		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		progressBar.setLayoutData(gridData);
	}
	
	/**
	 * Get the inner progress bar
	 */
	public ProgressBar getProgressBar() {
		return progressBar;
	}
	
	/**
	 * Add a progress to the progress bar. The current progress
	 * is added to the last progress. If a double < 1 is passed
	 * we accumulate the progresses until we reach an integer, in order
	 * to set the progress bar progresses
	 * @param progress
	 */
	public void addProgress(double progress) {

		// add to the done fract the double progress
		progressDoneFract = progressDoneFract + progress;

		// when we reach the 1 with the done progress
		// we can refresh the progress bar adding
		// the integer part of the doneFract
		if (progressDoneFract >= 1) {
			
			setProgress (progressDone + (int) progressDoneFract);

			// reset the doneFract double
			progressDoneFract = 0;
		}
	}
	
	/**
	 * Set the progress of the progress bar
	 * @param percent
	 */
	public void setProgress(double percent) {
		
		if (percent >= 100) {
			progressDone = 100;
		}
		else if (percent < 0) {
			progressDone = 0;
		}
		else {
			progressDone = (int) percent;
		}
		
		// limit progress if required
		if (progressDone > progressLimit) {
			progressDone = progressLimit;
		}
		
		refreshProgressBar(progressDone);
	}
	
	/**
	 * Refresh the progress bar state
	 */
	public void refreshProgressBar(final int done) {

		if (progressBar.isDisposed())
			return;

		Display disp = progressBar.getDisplay();

		if (disp.isDisposed())
			return;

		disp.asyncExec(new Runnable() {
			public void run() {

				if (progressBar.isDisposed())
					return;

				progressBar.setSelection(done);
				progressBar.update();
			}
		} );

		try {
			// set a small value! Otherwise the bar will slow all the Tool if often called
			Thread.sleep(11);  
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if the progress bar is filled at 100%
	 * @return
	 */
	public boolean isCompleted() {
		return progressDone >= progressLimit;
	}
	
	public boolean isDisposed() {
		return progressBar.isDisposed();
	}
	
	public Display getDisplay() {
		return progressBar.getDisplay();
	}

	@Override
	public void fillToMax() {
		setProgress(progressLimit);
	}
}
