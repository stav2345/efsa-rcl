package table_relations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.poi.ss.usermodel.Row;

import app_config.AppPaths;
import app_config.BooleanValue;
import xlsx_reader.XlsxReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Parser for the special sheet "Relations" which
 * identifies all the table relationships
 * @author avonva
 *
 */
public class RelationParser extends XlsxReader {
	
	private static final Logger LOGGER = LogManager.getLogger(RelationParser.class);
	
	private Collection<Relation> relations;
	private String parentTable;
	private String childTable;
	private boolean directRelation;
	
	public RelationParser(String filename) throws IOException {
		super(filename);
		this.relations = new ArrayList<>();	
	}
	
	/**
	 * Read all the relations and returns it
	 * @return
	 * @throws IOException
	 */
	public Collection<Relation> read() throws IOException {
		super.read(AppPaths.RELATIONS_SHEET);
		return relations;
	}
	
	public static boolean isRelationsSheet(String sheetName) {
		return AppPaths.RELATIONS_SHEET.equals(sheetName);
	}

	@Override
	public void processCell(String header, String value) {
		
		
		RelationHeader h = null;
		try {
			h = RelationHeader.fromString(header);  // get enum from string
		}
		catch(IllegalArgumentException e) {
			LOGGER.error("Error in processing cell ", e);
			return;
		}

		if(h == null)
			return;
		
		switch(h) {
		case PARENTTABLE:
			this.parentTable = value;
			break;
		case CHILDTABLE:
			this.childTable = value;
			break;
		case DIRECT_RELATION:
			this.directRelation = BooleanValue.isTrue(value);
			break;		
		}
	}

	@Override
	public void startRow(Row row) {}

	@Override
	public void endRow(Row row) {
		
		if(parentTable == null || childTable == null)
			return;
		
		// create a new table relation and put it into the collection
		Relation r = new Relation(parentTable, childTable, directRelation);
		relations.add(r);
	}
}
