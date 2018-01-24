package report;

public interface ThreadFinishedListener {
	/**
	 * 
	 * @param thread
	 */
	public void finished(Runnable thread);
	public void terminated(Runnable thread, Exception e);
}
