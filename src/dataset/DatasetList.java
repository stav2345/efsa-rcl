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
	 * Check if all the dataset in the list are editable or not
	 * @return
	 */
	public boolean isEditableAll() {
		
		for (Dataset d: this) {
			
			DatasetStatus status = d.getStatus();
			
			if (status == null)
				return false;
			
			if (!status.isEditable())
				return false;
		}
		
		return true;
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
		
		// filter also by the sender id, which should 
		// be in the format country year(2) month(2) with
		// an optional version number
		// examples: IT1704 FR1411.1 SP1512.01 GR1109.14
		final String country = "[a-zA-Z][a-zA-Z]";
		final String year = "\\d\\d";
		final String month = "\\d\\d";
		final String version = "(\\.\\d+)?";
		
		final String validSenderIdPattern = country + year + month + version;
		
		return filterByStatus(statusFilter).filterBySenderId(validSenderIdPattern);
	}
}
