package report;

import java.util.Collection;

import dataset.IDataset;
import dataset.RCLDatasetStatus;
import table_skeleton.TableRow;

/**
 * Generic EFSA report which contains the basic functionalities
 * that are needed.
 * @author avonva
 *
 */
public interface EFSAReport extends IDataset {

	/**
	 * Get all the report records (all the records, not only the
	 * direct children of the report). Note that the records
	 * defined here are the records which will be exported in
	 * a .xml file.
	 * @return
	 */
	public Collection<TableRow> getRecords();

	/**
	 * Force the report to be editable
	 */
	public void makeEditable();
	
	/**
	 * Get the message id related to the report if present
	 * @return
	 */
	public String getMessageId();
	
	/**
	 * Get the dataset id related to the report if present
	 * @return
	 */
	public String getId();
	
	/**
	 * Set the dataset id of the dataset related to the report
	 * @param id
	 */
	public void setId(String id);

	/**
	 * Get the current version of the report
	 * @return
	 */
	public String getVersion();
	
	/**
	 * Get all the report versions that are present locally
	 * @return
	 */
	public ReportList getAllVersions();
	
	/**
	 * Get the previous local version of the report if present
	 * @return
	 */
	public EFSAReport getPreviousVersion();
	
	/**
	 * Check if the version of the report is the first one
	 * @return
	 */
	public boolean isBaselineVersion();
	
	/**
	 * Delete all the report versions from
	 * the database
	 * @return true if ok
	 */
	public boolean deleteAllVersions();
	
	/**
	 * Get the current local status of the report
	 * @return
	 */
	public RCLDatasetStatus getRCLStatus();
	
	/**
	 * Update the dataset status
	 * @param status
	 */
	public void setStatus(String status);
	
	/**
	 * Update the dataset status
	 * @param status
	 */
	public void setStatus(RCLDatasetStatus status);
	
	/**
	 * Get the sender dataset id related to the report.
	 * This information should be always present in the report.
	 * @return
	 */
	public String getSenderId();
	
	/**
	 * Get the year related to the report
	 * @return
	 */
	public String getYear();
	
	/**
	 * Get the month related to the report
	 * @return
	 */
	public String getMonth();
}
