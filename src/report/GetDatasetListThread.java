package report;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import dataset.DatasetList;
import dataset.IDataset;
import soap.GetDatasetsList;
import soap.MySOAPException;
import user.IDcfUser;

public class GetDatasetListThread extends Thread {

	private static final Logger LOGGER = LogManager.getLogger(GetDatasetListThread.class);
	
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
			LOGGER.error("GetDatasetList failed", e);
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
