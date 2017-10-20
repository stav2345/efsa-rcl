package progress;

/**
 * Listener used to notify a process of the
 * progress of another thread.
 * @author avonva
 *
 */
public interface ProgressListener {
	
	/**
	 * Called when the process is completed
	 */
	public void progressCompleted();
	
	/**
	 * Called if the progress of the thread is changed
	 */
	public void progressChanged(double progressPercentage);
	
	/**
	 * Called if an exception was thrown in the process
	 * @param exception
	 */
	public void exceptionThrown(Exception exception);
}
