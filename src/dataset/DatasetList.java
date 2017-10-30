package dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import report.VersionComparator;
import webservice.GetDatasetList;

/**
 * List of dataset received by calling {@link GetDatasetList}
 * @author avonva
 *
 */
public class DatasetList<T extends IDataset> extends ArrayList<T> {

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
	public DatasetList<T> filterByDatasetId(String regex) {
		
		DatasetList<T> filteredList = new DatasetList<T>();
		
		for (T dataset : this) {
			
			String datasetId = dataset.getDatasetId();
			
			// avoid null senderId
			if (datasetId == null)
				continue;
			
			if (datasetId.matches(regex))
				filteredList.add(dataset);
		}
		
		return filteredList;
	}
	
	/**
	 * Filter the datasets by their sender id (regex)
	 * @param regex
	 * @return
	 */
	public DatasetList<T> filterBySenderId(String regex) {
		
		DatasetList<T> filteredList = new DatasetList<T>();
		
		for (T dataset : this) {
			
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
	public DatasetList<T> filterByDecomposedSenderId(String regex) {
		
		DatasetList<T> filteredList = new DatasetList<>();
		
		for (T dataset : this) {
			
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
	 * Filter by inclusion
	 * @param statusFilter
	 * @return
	 */
	public DatasetList<T> filterByStatus(Collection<DatasetStatus> statusFilter) {
		return filterByStatus(statusFilter, false);
	}
	
	/**
	 * Filter the datasets by their status
	 * @param statusFilter
	 * @return
	 */
	public DatasetList<T> filterByStatus(Collection<DatasetStatus> statusFilter, boolean exclude) {
		
		DatasetList<T> filteredList = new DatasetList<>();
		for (T d: this) {
			
			boolean contained = statusFilter.contains(d.getStatus());
			
			// if contained and we filter by inclusion
			// of if not contained and we filter by exclusion add it
			boolean addIt = (contained && !exclude) || (!contained && exclude);
			
			if (addIt) {
				filteredList.add(d);
			}
		}
		
		return filteredList;
	}
	
	public DatasetList<T> filterByStatus(DatasetStatus statusFilter, boolean exclude) {
		
		Collection<DatasetStatus> status = new ArrayList<>();
		status.add(statusFilter);
		
		return filterByStatus(status, exclude);
	}
	
	public DatasetList<T> filterByStatus(DatasetStatus statusFilter) {		
		return filterByStatus(statusFilter, false);
	}
	
	/**
	 * Get the last version in status {@link DatasetStatus#ACCEPTED_DWH}
	 * @return
	 */
	public T getLastAcceptedVersion(String senderId) {
		
		// get only accepted datasets
		DatasetList<T> datasets = this.filterByStatus(DatasetStatus.ACCEPTED_DWH);
		
		// get the last
		return datasets.getLastVersion(senderId);
	}
	
	/**
	 * Get the last version of a dataset with status
	 * which is not {@link DatasetStatus#DELETED}
	 * or {@link DatasetStatus#REJECTED}
	 * @param senderId
	 * @return
	 */
	public T getLastExistingVersion(String senderId) {
		
		Collection<DatasetStatus> statusFilter = new ArrayList<>();
		statusFilter.add(DatasetStatus.DELETED);
		statusFilter.add(DatasetStatus.REJECTED);
		
		// get only datasets that exist in DCF
		DatasetList<T> datasets = this.filterByStatus(statusFilter, true);
		
		// get the last
		return datasets.getLastVersion(senderId);
	}
	
	/**
	 * Get the last version of the dataset with the specified
	 * senderId in the list
	 * @param senderId
	 * @return
	 */
	public T getLastVersion(String senderId) {
		
		// get only related datasets
		DatasetList<T> datasets = this.filterByDecomposedSenderId(senderId);
		
		if (datasets.isEmpty())
			return null;
		
		T last = datasets.get(0);
		
		for (T d: datasets) {
			
			VersionComparator comparator = new VersionComparator();
			int compare = comparator.compare(d, last);

			// if the new has a greater version
			// then save it as last
			if (compare < 0)
				last = d;
		}
		
		return last;
	}
	
	/**
	 * Sort the collection by sender id and version
	 */
	public void sort() {
		Collections.sort(this, new VersionComparator());
	}
	
	/**
	 * Filter all the old versions of datasets
	 * @return
	 */
	public DatasetList<T> filterOldVersions() {
		
		DatasetList<T> lasts = new DatasetList<>();
		for (T d: this) {
			
			String senderId = d.getDecomposedSenderId();
			
			if (senderId == null)
				continue;
			
			// get last version of the dataset
			T last = this.getLastVersion(senderId);

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
	public DatasetList<T> getDownloadableDatasets(String validSenderIdPattern) {
		
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
				.filterBySenderId(validSenderIdPattern)
				.filterOldVersions();
	}
}
