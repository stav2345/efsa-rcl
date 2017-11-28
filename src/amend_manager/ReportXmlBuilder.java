package amend_manager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import message.MessageConfigBuilder;
import message_creator.MessageXmlBuilder;
import progress.ProgressListener;
import report.EFSAReport;
import report.ReportException;
import table_skeleton.TableRow;

public class ReportXmlBuilder {

	private EFSAReport report;
	private MessageConfigBuilder messageConfig;
	private String rowIdField;
	private ProgressListener progressListener;
	
	/**
	 * Send a report to the DCF
	 * @param report report which will be exported
	 * @param messageConfig configuration used to create the export file
	 * @param rowIdField name of the field which contains the row id of the record
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public ReportXmlBuilder(EFSAReport report, MessageConfigBuilder messageConfig, 
			String rowIdField) {

		this.report = report;
		this.messageConfig = messageConfig;
		this.rowIdField = rowIdField;
	}
	
	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	/**
	 * Set the progress (call the listener if set)
	 * @param progress
	 */
	private void setProgress(double progress) {
		if (this.progressListener != null)
			this.progressListener.progressChanged(progress);
	}
	
	/**
	 * Create an empty report with the desired header/operation
	 * @param messageConfig
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static File createEmptyReport(MessageConfigBuilder messageConfig) 
			throws ParserConfigurationException, SAXException, IOException {
		MessageXmlBuilder creator = new MessageXmlBuilder(messageConfig.getOut(), messageConfig);
		return creator.exportEmpty();
	}
	
	private void clearTable() {
		DatasetComparisonDao dao = new DatasetComparisonDao();
		dao.deleteAll();
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ReportException
	 */
	public File exportReport() throws IOException, ParserConfigurationException, 
		SAXException, ReportException {
		
		clearTable();
		
		// extract the report into the comparisons table
		extractSingleVersion(report);
		
		setProgress(40);
		
		// if baseline, just extract it and export it
		if (report.isBaselineVersion()) {
			setProgress(100);
			return createXmlFile();
		}
		
		// otherwise extract also the previous version

		EFSAReport previousReport = report.getPreviousVersion();
		
		if (previousReport == null) {
			throw new ReportException("Cannot export report " 
					+ report.getVersion() 
					+ " since its previous version cannot be found.");
		}
		
		// extract also the previous report
		extractSingleVersion(previousReport);
		
		setProgress(60);
		
		// solve the amendments of the two versions
		solveDuplications();
		
		setProgress(80);
		
		// export the final xml file with the merged dataset
		File xml = createXmlFile();
		
		setProgress(100);
		
		clearTable();
		
		return xml;
	}
	
	/**
	 * Extract a single version and put it into the database
	 * @param record
	 */
	private void extractSingleVersion(EFSAReport report) {
		
		// for each row
		for (TableRow record : report.getRecords()) {
			
			// update all the record formulas
			record.updateFormulas();

			// get the row id from the record
			String rowId = record.getCode(rowIdField);
			
			// get the version
			String version = report.getVersion();
			
			// create the dataset comparison object
			DatasetComparison comp = new DatasetComparison(rowId, version, record.toXml(false));
			
			// save it into the comparison table
			DatasetComparisonDao dao = new DatasetComparisonDao();
			dao.add(comp);
		}
	}
	
	/**
	 * Solve the duplications and set the amendments
	 */
	private void solveDuplications() {
		
		removeOldRecordVersions();
		setUpdateAmendment();
		setDeleteAmendment();
	}
	
	/**
	 * Create the xml file of the dataset
	 * @return
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private File createXmlFile() throws IOException, ParserConfigurationException, SAXException {
		
		DatasetComparisonDao dao = new DatasetComparisonDao();
		Collection<DatasetComparison> comps = dao.getAll();
		
		// export the xml file
		MessageXmlBuilder creator = new MessageXmlBuilder(messageConfig.getOut(), this.messageConfig);
		return creator.export(comps);
	}
	
	/**
	 * Remove all the records which belong to an old version
	 * and they were replaced by a newer version
	 */
	private void removeOldRecordVersions() {

		StringBuilder query = new StringBuilder();
		query.append("delete from APP.DATASET_COMPARISON ")
			.append("where ROW_ID in ( ")
				.append("select ROW_ID ")
				.append("from APP.DATASET_COMPARISON ")
				.append("group by ROW_ID, XML_RECORD ")
				.append("having COUNT(VERSION) = 2 ")
			.append(") ");
		
		DatasetComparisonDao dao = new DatasetComparisonDao();
		dao.executeQuery(query.toString());
	}
	
	/**
	 * Set the update amendment to the updated row
	 */
	private void setUpdateAmendment() {
		
		StringBuilder query = new StringBuilder();
		
		// set update amendment
		query.append("update APP.DATASET_COMPARISON ")
			.append("set XML_RECORD = XML_RECORD || '<amType>U</amType>',")
			.append("AM_TYPE = 'U' ")
			.append("where VERSION in (")
				.append("select MAX(VERSION) ")
				.append("from APP.DATASET_COMPARISON")
			.append(")")
			.append("and ")
			.append("ROW_ID in (")
				.append("select ROW_ID ")
				.append("from APP.DATASET_COMPARISON ")
				.append("where VERSION in (")
					.append("select MIN(VERSION) ")
					.append("from APP.DATASET_COMPARISON")
				.append(")")
			.append(")");
		
		DatasetComparisonDao dao = new DatasetComparisonDao();
		dao.executeQuery(query.toString());
		
		
		// then delete the old record versions related
		// to the just changed records
		StringBuilder query2 = new StringBuilder();
		query2.append("delete from APP.DATASET_COMPARISON ")
			.append("where AM_TYPE is null ")
			.append("and ")
			.append("ROW_ID in (")
				.append("select ROW_ID ")
				.append("from APP.DATASET_COMPARISON ")
				.append("where AM_TYPE = 'U' ")
			.append(")");
		
		dao.executeQuery(query2.toString());
	}
	
	/**
	 * Set the delete amendment for records that are present
	 * just in the older version (i.e. they were deleted)
	 */
	private void setDeleteAmendment() {

		StringBuilder query = new StringBuilder();
		
		// set update amendment
		query.append("update APP.DATASET_COMPARISON ")
			.append("set XML_RECORD = XML_RECORD || '<amType>D</amType>',")
			.append("AM_TYPE = 'D' ")
			.append("where VERSION in (")
				.append("select MIN(VERSION) ")
				.append("from APP.DATASET_COMPARISON")
			.append(")")
			.append("and ")
			.append("ROW_ID not in (")
				.append("select ROW_ID ")
				.append("from APP.DATASET_COMPARISON ")
				.append("where VERSION in (")
					.append("select MAX(VERSION) ")
					.append("from APP.DATASET_COMPARISON")
				.append(")")
			.append(")");
		
		DatasetComparisonDao dao = new DatasetComparisonDao();
		dao.executeQuery(query.toString());
	}
}
