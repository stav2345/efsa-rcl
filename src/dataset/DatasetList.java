package dataset;

import java.util.ArrayList;
import java.util.Collection;

import webservice.GetDatasetList;

/**
 * List of dataset received by calling {@link GetDatasetList}
 * @author avonva
 *
 */
public class DatasetList extends ArrayList<Dataset> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Check if the datasets list contains a 
	 * report with the chosen senderId
	 * @param senderId
	 * @return
	 */
	public boolean contains(String senderId) {
		return !filterBySenderId(senderId).isEmpty();
	}
	
	/**
	 * Filter the datasets by their sender id (regex)
	 * @param regex
	 * @return
	 */
	public DatasetList filterBySenderId(String regex) {
		
		DatasetList filteredList = new DatasetList();
		
		for (Dataset dataset : this) {
			
			String senderId = dataset.getSenderId();
			
			// avoid null senderId
			if (senderId == null)
				continue;
			
			if (senderId.matches(regex))
				filteredList.add(dataset);
		}
		
		return filteredList;
	}
	
	
	/**
	 * Filter the datasets by their decomposed sender id (regex)
	 * @param regex
	 * @return
	 */
	public DatasetList filterByDecomposedSenderId(String regex) {
		
		DatasetList filteredList = new DatasetList();
		
		for (Dataset dataset : this) {
			
			String senderId = dataset.getDecomposedSenderId();
			
			// avoid null senderId
			if (senderId == null)
				continue;
			
			if (senderId.matches(regex))
				filteredList.add(dataset);
		}
		
		return filteredList;
	}
	
	/**
	 * Filter the datasets by their status
	 * @param statusFilter
	 * @return
	 */
	public DatasetList filterByStatus(Collection<DatasetStatus> statusFilter) {
		
		DatasetList filteredList = new DatasetList();
		for (Dataset d: this) {
			if (statusFilter.contains(d.getStatus())) {
				filteredList.add(d);
			}
		}
		
		return filteredList;
	}
	
	/**
	 * Get the last version of the dataset with the specified
	 * senderId in the list
	 * @param senderId
	 * @return
	 */
	public Dataset getLastVersion(String senderId) {
		
		// get only related datasets
		DatasetList datasets = this.filterByDecomposedSenderId(senderId);
		
		if (datasets.isEmpty())
			return null;
		
		Dataset last = datasets.get(0);
		
		for (Dataset d: datasets) {
			
			// if the new has a greater version
			// then save it as last
			if (d.compareTo(last) > 0)
				last = d;
		}
		
		return last;
	}
	
	/**
	 * Filter all the old versions of datasets
	 * @return
	 */
	public DatasetList filterOldVersions() {
		
		DatasetList lasts = new DatasetList();
		for (Dataset d: this) {
			
			String senderId = d.getDecomposedSenderId();
			
			if (senderId == null)
				continue;
			
			// get last version of the dataset
			Dataset last = this.getLastVersion(senderId);
			
			// if not already added, put it in the list of lasts
			if (!lasts.contains(last)) {
				lasts.add(last);
			}
		}
		
		return lasts;
	}
	
	/**
	 * Get the list of dataset that can be downloaded from the tool
	 * @return
	 */
	public DatasetList getDownloadableDatasets() {
		
		Collection<DatasetStatus> statusFilter = new ArrayList<>();
		statusFilter.add(DatasetStatus.REJECTED_EDITABLE);
		statusFilter.add(DatasetStatus.ACCEPTED_DWH);
		statusFilter.add(DatasetStatus.VALID);
		statusFilter.add(DatasetStatus.VALID_WITH_WARNINGS);
		statusFilter.add(DatasetStatus.SUBMITTED);
		
		// filter also by the sender id, which should 
		// be in the format country year(2) month(2) with
		// an optional version number
		// examples: IT1704 FR1411.1 SP1512.01 GR1109.14
		
		return filterByStatus(statusFilter)
				.filterBySenderId(Dataset.VALID_SENDER_ID_PATTERN)
				.filterOldVersions();
	}
}
