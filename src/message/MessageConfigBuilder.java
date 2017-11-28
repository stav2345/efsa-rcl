package message;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import app_config.AppPaths;
import global_utils.TimeUtils;
import message_creator.MessageXmlBuilder;
import message_creator.OperationType;
import table_relations.Relation;
import table_skeleton.TableRow;
import xlsx_reader.TableSchema;
import xlsx_reader.TableSchemaList;

public class MessageConfigBuilder {

	private Collection<TableRow> messageParents;
	private OperationType opType;
	private File out;
	
	/**
	 * Create a configuration for a message that will be created
	 * in {@link MessageXmlBuilder}
	 * @param messageParents parents that will be injected into the message
	 * put here all the tables that are required (foreignKey) in the message schema
	 * @param opType required operation type that will be put in the message
	 * ({@link AppPaths#MESSAGE_CONFIG_SHEET}).
	 */
	public MessageConfigBuilder(Collection<TableRow> messageParents, OperationType opType) {
		this(messageParents, opType, generateTempFile());
	}
	
	public MessageConfigBuilder(Collection<TableRow> messageParents, OperationType opType, File out) {
		this.messageParents = messageParents;
		this.opType = opType;
		this.out = out;
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
	
	public OperationType getOpType() {
		return opType;
	}
	
	public boolean needEmptyDataset() {
		return opType.needEmptyDataset();
	}
	
	/**
	 * Get the file where the export will be created
	 * @return
	 */
	public File getOut() {
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
		row.initialize();

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
		
		row.updateFormulas();
		
		return row;
	}
}
