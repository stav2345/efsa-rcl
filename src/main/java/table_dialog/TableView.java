package table_dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import i18n_messages.Messages;
import table_skeleton.TableColumn;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import xlsx_reader.TableSchema;
import xlsx_reader.TableSchemaList;

/**
 * This class contains a table which shows all the information related to a
 * summarised information table. In particular, it allows also to change its
 * contents dynamically.
 * 
 * @author avonva
 * @author shahaal
 *
 */
public class TableView {

	private static final Logger LOGGER = LogManager.getLogger(TableView.class);

	private static final String TABLE_COLUMN_DATA_KEY = "schema";

	private Composite parent; // parent widget
	private TableViewer tableViewer; // main table
	private List<TableViewerColumn> columns; // columns of the table
	private TableSchema schema; // defines the table columns
	private TableRowList tableElements; // cache of the table elements to do sorting by column
	private boolean editable; // table is editable or not?
	private Listener inputChangedListener; // called when table data changes
	private List<EditorListener> editorListeners; // called when editor starts/ends
	private TableViewerColumn validator; // data validator, only if needed

	private Collection<TableRow> parents; // parents of the table (tables from which this table was created)

	/**
	 * Create a report table using a predefined schema for the columns
	 * 
	 * @param parent
	 * @param schema schema which specifies the columns
	 */
	public TableView(Composite parent, String schemaSheetName, boolean editable) {

		this.parent = parent;
		this.editable = editable;
		this.parents = new ArrayList<>();
		this.columns = new ArrayList<>();
		this.editorListeners = new ArrayList<>();

		this.schema = TableSchemaList.getByName(schemaSheetName);
		this.tableElements = new TableRowList(schema);
	}

	public void addParentTable(TableRow parent) {
		parents.add(parent);
	}

	public void clearParents() {
		parents.clear();
	}

	/**
	 * Create the interface into the composite
	 */
	public void create() {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// create the table
		this.tableViewer = new TableViewer(composite,
				SWT.VIRTUAL | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.NONE);

		this.tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		this.tableViewer.getTable().setHeaderVisible(true);
		this.tableViewer.setContentProvider(new TableContentProvider());
		this.tableViewer.setUseHashlookup(true);
		this.tableViewer.getTable().setLinesVisible(true);

		/* TODO to fix:
		 * ENTER trigger double click listener openening second level
		 * TAB doesnt move inside the table if not editing firstly a cell
		 * 
		// add ability to move inside the cells
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(this.tableViewer,
				new FocusCellOwnerDrawHighlighter(this.tableViewer));

		ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(
				this.tableViewer) {
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				// if right click disable edit
				if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION) {
					EventObject source = event.sourceEvent;
					if (source instanceof MouseEvent && ((MouseEvent) source).button == 3)
						return false;
				}

				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TableViewerEditor.create(this.tableViewer, focusCellManager, activationSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
		*/
		
		// create the columns based on the schema
		createColumns();
	}

	/**
	 * Create all the columns which are related to the schema Only visible columns
	 * are added
	 */
	private void createColumns() {

		// Add the validator column if editable table
		if (editable) {
			this.validator = new TableViewerColumn(this.tableViewer, SWT.NONE);
			validator.getColumn().setWidth(140);
			validator.getColumn().setText(Messages.get("data.check.header"));
		}

		for (TableColumn col : schema) {

			// skip non visible columns
			if (!col.isVisible(schema, parents))
				continue;

			if (col.getLabel() == null || col.getLabel().isEmpty()) {
				LOGGER.warn("Column " + col + " is set as visible but it has not a label set.");
				col.setLabel("MISSING_" + col.getId());
			}

			// create the column
			TableViewerColumn columnViewer = createColumn(col);

			// save the column
			columns.add(columnViewer);
		}

		setEditable(editable);
	}

	/**
	 * Create a table column given the column properties
	 * 
	 * @param col
	 */
	private TableViewerColumn createColumn(TableColumn col) {

		// Add the column to the parent table
		TableViewerColumn columnViewer = new TableViewerColumn(this.tableViewer, SWT.NONE);

		// set the label provider for column
		columnViewer.setLabelProvider(new TableLabelProvider(col.getId()));

		// set width according to type and label
		columnViewer.getColumn().setWidth(getColumnWidth(col));

		// set text
		if (col.getLabel() != null) {

			String label = col.getLabel();

			// if the mandatory depends on the row (and it is not a formula)
			// if (col.isConditionallyMandatory() && !col.isComposite()) {
			if (col.isMandatory())// ) && !col.isComposite())
				label = label + Messages.get("mandatory.column.marker");

			// if conditional mandatory then put the tilde aside the label
			if (col.isConditionallyMandatory())
				label = label + Messages.get("conditionally.mandatory.column.marker");

			columnViewer.getColumn().setText(label);
		}

		// set tool tip
		if (col.getTip() != null)
			columnViewer.getColumn().setToolTipText(col.getTip());

		// add possibility to sort record by clicking
		// in the column name
		addColumnSorter(col, columnViewer);

		// add the column schema to the column viewer
		columnViewer.getColumn().setData(TABLE_COLUMN_DATA_KEY, col);

		return columnViewer;
	}

	/**
	 * Get the width of a column
	 * 
	 * @param column
	 * @return
	 */
	private int getColumnWidth(TableColumn column) {

		// decide the width of the column
		int size = 80;
		switch (column.getType()) {
		case INTEGER:
		case U_INTEGER:
			size = 80;
			break;
		default:
			size = 115 + column.getLabel().length() * 4;
			break;
		}

		return size;
	}

	/**
	 * Add a click to the column header that sorts the table by the clicked field
	 * 
	 * @param columnViewer
	 */
	private void addColumnSorter(TableColumn column, TableViewerColumn columnViewer) {

		// set default sort direction (false will do an ascending sorting)
		columnViewer.getColumn().setData("sortingDir", false);

		// when a column is pressed order by the selected variable
		columnViewer.getColumn().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				// get old direction
				boolean oldSortDirection = (boolean) columnViewer.getColumn().getData("sortingDir");

				// save the new direction
				columnViewer.getColumn().setData("sortingDir", !oldSortDirection);

				// invert the direction in the table
				orderRowsBy(column, !oldSortDirection);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 * Sort the table elements by a column
	 * 
	 * @param column
	 * @param ascendant if true ascendant order, otherwise descendant
	 */
	private void orderRowsBy(TableColumn column, boolean ascendant) {

		// sort elements
		Collections.sort(tableElements, new TableRowComparator(column, ascendant));

		// reset input with ordered elements
		this.tableViewer.setInput(tableElements);
	}

	/**
	 * Get all the table rows
	 * 
	 * @return
	 */
	public TableRowList getTableElements() {
		return tableElements;
	}

	/**
	 * Get the table viewer
	 * 
	 * @return
	 */
	public TableViewer getViewer() {
		return tableViewer;
	}

	/**
	 * Get the table
	 * 
	 * @return
	 */
	public Table getTable() {
		return tableViewer.getTable();
	}

	/**
	 * Get the selected element if present
	 * 
	 * @return
	 */
	public TableRow getSelection() {

		IStructuredSelection selection = (IStructuredSelection) this.tableViewer.getSelection();

		if (selection.isEmpty())
			return null;

		TableRow lightRow = (TableRow) selection.getFirstElement();

		return this.tableElements.getElementById(lightRow.getDatabaseId());
	}

	public TableRow getCompleteRow(int id) {
		TableRow completeRow = this.tableElements.getElementById(id);
		return completeRow;
	}

	/**
	 * Get the selected elements if present
	 * 
	 * @return
	 */
	public TableRowList getAllSelectedRows() {

		IStructuredSelection selection = (IStructuredSelection) this.tableViewer.getSelection();

		if (selection.isEmpty())
			return null;

		TableRowList list = new TableRowList(schema);

		Iterator<?> iter = selection.iterator();

		while (iter.hasNext()) {

			TableRow lightRow = (TableRow) iter.next();

			TableRow completeRow = getCompleteRow(lightRow.getDatabaseId());

			if (completeRow != null)
				list.add(completeRow);
		}

		return list;
	}

	/**
	 * Get the number of elements contained in the table
	 * 
	 * @return
	 */
	public int getItemCount() {
		return this.tableViewer.getTable().getItemCount();
	}

	/**
	 * Check if the table is empty or not
	 */
	public boolean isEmpty() {
		return getItemCount() == 0;
	}

	/**
	 * Check if the table is editable or not
	 * 
	 * @return
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * Show/hide a password field
	 * 
	 * @param colId
	 * @param visible
	 */
	public void setPasswordVisibility(String colId, boolean visible) {

		for (TableViewerColumn colViewer : columns) {

			TableColumn column = (TableColumn) colViewer.getColumn().getData(TABLE_COLUMN_DATA_KEY);

			if (column.getId().equals(colId)) {
				TableLabelProvider labelProvider = new TableLabelProvider(colId);
				labelProvider.setPasswordVisibility(visible);
				colViewer.setLabelProvider(labelProvider);
				tableViewer.refresh();
				break;
			}
		}
	}

	/**
	 * Set if the table can be edited or not
	 * 
	 * @param editable
	 */
	public void setEditable(boolean editable) {

		this.editable = editable;

		if (tableViewer == null)
			return;

		// if disable editing remove editors
		for (TableViewerColumn column : columns) {

			TableEditor editor = null;

			if (editable) {

				TableColumn columnSchema = (TableColumn) column.getColumn().getData(TABLE_COLUMN_DATA_KEY);

				editor = new TableEditor(this, columnSchema);

				for (EditorListener listener : editorListeners)
					editor.addListener(listener);
			}

			// remove editor if editable is false
			column.setEditingSupport(editor);
		}

	}

	/**
	 * Get the table schema
	 * 
	 * @return
	 */
	public TableSchema getSchema() {
		return schema;
	}

	/**
	 * Check if the whole table is correct
	 * 
	 * @return
	 */
	public boolean areMandatoryFilled() {
		for (TableRow row : tableElements) {
			if (!row.areMandatoryFilled())
				return false;
		}
		return true;
	}

	/**
	 * Add an element to the table viewer
	 * 
	 * @param row
	 */
	public void addRow(TableRow row) {
		this.tableViewer.add(row);
		this.tableElements.add(row);
		moveToBottom();
	}

	/**
	 * Move the table to the bottom
	 */
	public void moveToBottom() {
		this.tableViewer.getTable().deselectAll();
		this.tableViewer.getTable().select(tableViewer.getTable().getItemCount() - 1);
		this.tableViewer.getTable().showSelection();
		this.tableViewer.getTable().deselectAll();
	}

	/**
	 * Add an element to the table viewer
	 * 
	 * @param row
	 */
	public void addAll(Collection<TableRow> rows) {
		for (TableRow r : rows) {
			this.tableViewer.add(r);
		}
		this.tableElements.addAll(rows);
	}

	/**
	 * Remove an element from the table viewer
	 * 
	 * @param row
	 */
	public void removeRow(TableRow row) {
		this.tableViewer.remove(row);
		this.tableElements.remove(row);
		row.delete();
	}

	public void removeRows(TableRowList rows) {

		this.tableViewer.getTable().setRedraw(false);

		for (TableRow row : rows)
			this.tableViewer.remove(row);

		this.tableViewer.getTable().setRedraw(true);
		this.tableElements.removeAll(rows);
		rows.deleteAll();
	}

	/**
	 * Remove the selected rows
	 */
	public void removeSelectedRows() {

		TableRowList list = getAllSelectedRows();

		if (list == null || list.isEmpty())
			return;

		// remove all the rows from the table
		removeRows(list);
	}

	/**
	 * Clear all the elements of the table
	 */
	public void clear() {
		this.tableElements.clear();
		this.tableViewer.getTable().removeAll();
	}

	/**
	 * Set the input of the table
	 * 
	 * @param elements
	 */
	public void setInput(TableRowList elements) {
		this.tableViewer.setInput(elements);
		this.tableViewer.setItemCount(elements.size());
		this.tableElements = new TableRowList(elements);
	}

	/**
	 * Set menu to the table
	 * 
	 * @param menu
	 */
	public void setMenu(Menu menu) {
		this.tableViewer.getTable().setMenu(menu);
	}

	/**
	 * Set the validator label provider
	 * 
	 * @param validatorLabelProvider
	 */
	public void setValidatorLabelProvider(RowValidatorLabelProvider validatorLabelProvider) {

		if (this.validator == null)
			return;

		this.validator.setLabelProvider(validatorLabelProvider);
	}

	/**
	 * Refresh a single row of the table
	 * 
	 * @param row
	 */
	public void refreshAndSave(TableRow row, boolean saveInDb) {

		TableRow oldRow = this.tableElements.getElementById(row.getDatabaseId());

		if (oldRow == null) {
			LOGGER.warn("Cannot refresh row " + row.getDatabaseId() + " since it is not present in the table.");
			return;
		}

		// update the edited values
		oldRow.copyValues(row);
		row.updateFormulas();

		if (saveInDb) {
			// update also the formulas using the new values
			oldRow.updateFormulas();

			// save in db the changed values
			oldRow.update();
		}

		this.tableViewer.refresh(row);

		// call listener
		if (inputChangedListener != null) {
			Event event = new Event();
			event.data = row;
			inputChangedListener.handleEvent(event);
		}
	}

	/**
	 * Select a row of the table
	 * 
	 * @param index
	 */
	public void select(int index) {

		if (tableViewer.getTable().getItemCount() <= index)
			return;

		tableViewer.setSelection(new StructuredSelection(tableViewer.getElementAt(index)), true);
	}

	public void refresh(TableRow row) {
		this.tableViewer.refresh(row);
	}

	public void replaceRow(TableRow row) {
		int index = this.tableElements.indexOf(row);
		this.tableViewer.replace(row.getVisibleFields(), index);
	}

	public void refreshValidator(TableRow row) {
		this.validator.getViewer().refresh(row);
	}

	/**
	 * Refresh all the elements
	 */
	public void refresh() {
		this.tableViewer.setInput(tableElements);
	}

	/**
	 * Add a listener which is called when the table changes the highlighted element
	 * 
	 * @param listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.tableViewer.addSelectionChangedListener(listener);
	}

	/**
	 * Add a listener which is called when the table changes the highlighted element
	 * 
	 * @param listener
	 */
	public void addDoubleClickListener(IDoubleClickListener listener) {
		this.tableViewer.addDoubleClickListener(listener);
	}

	/**
	 * Called when the input of the table changes
	 * 
	 * @param inputChangedListener
	 */
	public void setInputChangedListener(Listener inputChangedListener) {
		this.inputChangedListener = inputChangedListener;
	}

	/**
	 * Set listener called when edit starts/ends
	 * 
	 * @param editorListener
	 */
	public void addEditorListener(EditorListener editorListener) {
		this.editorListeners.add(editorListener);

		// refresh editor to add the listener
		this.setEditable(this.editable);
	}
}
