package message;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import app_config.AppPaths;
import global_utils.TimeUtils;
import message_creator.OperationType;
import providers.IFormulaService;
import table_relations.Relation;
import table_skeleton.TableRow;
import xlsx_reader.TableSchema;
import xlsx_reader.TableSchemaList;

public class MessageConfigBuilder {

	private Collection<TableRow> messageParents;
	private OperationType opType;
	private File out;
	
	private IFormulaService formulaService;
	
	public MessageConfigBuilder(IFormulaService formulaService, 
			Collection<TableRow> messageParents) {
		this.messageParents = messageParents;
		this.formulaService = formulaService;
	}
	
	/**
	 * Generate a temporary .xml file to export the dataset
	 * @return
	 */
	private static File generateTempFile() {
		String filename = AppPaths.TEMP_FOLDER + "report-" + TimeUtils.getTodayTimestamp() + ".xml";
		return new File(filename);
	}
	
	public Collection<TableRow> getMessageParents() {
		return messageParents;
	}
	
	public void setOpType(OperationType opType) {
		this.opType = opType;
	}
	
	public OperationType getOpType() {
		return opType;
	}
	
	public boolean needEmptyDataset() {
		return opType.needEmptyDataset();
	}
	
	public void setOut(File out) {
		this.out = out;
	}
	
	/**
	 * Get the file where the export will be created
	 * @return
	 */
	public File getOut() {
		
		if (out == null)
			return generateTempFile();
		
		return out;
	}
	
	/**
	 * Get the configuration for the export
	 * @return
	 * @throws IOException 
	 */
	public TableRow getMessageConfig() throws IOException {
		
		TableSchema schema = TableSchemaList.getByName(AppPaths.MESSAGE_CONFIG_SHEET);

		// create the header row
		TableRow row = new TableRow(schema);
		row.Initialise();
		
		// add the op type to the row
		row.put(AppPaths.MESSAGE_CONFIG_OP_TYPE, opType.getOpType());
		row.put(AppPaths.MESSAGE_CONFIG_INTERNAL_OP_TYPE, opType.getInternalOpType());

		if (messageParents != null) {
			
			// inject the parents
			for (TableRow parent : messageParents) {
				Relation.injectParent(parent, row);
			}
		}
		
		Relation.emptyCache();
		
		formulaService.updateFormulas(row);
		
		return row;
	}
}
