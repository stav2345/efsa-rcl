package report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.Config;
import dataset.DatasetList;
import dataset.IDataset;
import soap.GetDatasetsList;
import soap.DetailedSOAPException;
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
		} catch (DetailedSOAPException e) {
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
	 * @throws DetailedSOAPException 
	 */
	private DatasetList getDatasets() throws DetailedSOAPException {
		
		DatasetList output = new DatasetList();
		
		Config config = new Config();
		GetDatasetsList<IDataset> req = new GetDatasetsList<>();
		req.getList(config.getEnvironment(), user, dcCode, output);

		return output;
	}
}
