package report;

import dataset.DatasetList;
import dataset.IDataset;
import soap.GetDatasetsList;
import soap.MySOAPException;
import user.IDcfUser;

public class GetDatasetListThread extends Thread {

	private DatasetList datasets;
	
	private ThreadFinishedListener listener;
	private IDcfUser user;
	private String dcCode;
	
	public GetDatasetListThread(IDcfUser user, String dcCode) {
		this.user = user;
		this.dcCode = dcCode;
	}
	
	@Override
	public void run() {
		try {
			datasets = getDatasets();
			if (listener != null)
				listener.finished(this);
		} catch (MySOAPException e) {
			e.printStackTrace();
			if (listener != null)
				listener.terminated(this, e);
		}
	}
	
	public void setListener(ThreadFinishedListener listener) {
		this.listener = listener;
	}
	
	public DatasetList getDatasetsList() {
		return datasets;
	}
	
	/**
	 * Get a list of all the dcf datasets for the selected user,
	 * data collection.
	 * @return
	 * @throws MySOAPException 
	 */
	private DatasetList getDatasets() throws MySOAPException {
		
		DatasetList output = new DatasetList();
		
		GetDatasetsList<IDataset> req = new GetDatasetsList<>(user, dcCode, output);
		req.getList();

		return output;
	}
}
