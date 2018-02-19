package report;

import dataset.Dataset;

public class NotOverwritableDcfDatasetException extends Exception {
	
	private static final long serialVersionUID = 8112874740274540347L;
	private Dataset status;
	
	public NotOverwritableDcfDatasetException() {
		super();
	}
	
	public NotOverwritableDcfDatasetException(Dataset status) {
		super();
		this.status = status;
	}
	
	public Dataset getDataset() {
		return status;
	}
}
