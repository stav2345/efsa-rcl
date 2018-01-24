package report;

import dataset.Dataset;
import dataset.DatasetList;

public interface IDownloadReportDialog {

	/**
	 * Get the selected dataset
	 * @return
	 */
	public Dataset getSelectedDataset();
	
	/**
	 * Get the selected dataset and all its versions
	 * @return
	 */
	public DatasetList getSelectedDatasetVersions();
	
	/**
	 * Set the list of datasets retrieved with a get datasets list
	 * web service request (all versions!)
	 * @param list
	 */
	public void setDatasetsList(DatasetList list);
	
	/**
	 * Open the dialog
	 */
	public void open();
}
