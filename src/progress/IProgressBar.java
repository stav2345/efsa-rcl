package progress;

public interface IProgressBar {

	/**
	 * Add progress to the bar
	 * @param progress
	 */
	public void addProgress(double progress);

	/**
	 * Fill the bar to the maximum
	 */
	public void fillToMax();
}
