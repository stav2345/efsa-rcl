package table_dialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
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

import table_skeleton.TableColumn;
import table_skeleton.TableRow;
import xlsx_reader.TableSchema;
import xlsx_reader.TableSchemaList;

/**
 * This class contains a table which shows all the information
 * related to a summarized information table. In particular,
 * it allows also to change its contents dynamically.
 * @author avonva
 *
 */
public class TableView {

	private Composite parent;                    // parent widget
	private TableViewer tableViewer;             // main table
	private TableSchema schema;                  // defines the table columns
	private ArrayList<TableRow> tableElements;   // cache of the table elements to do sorting by column
	private boolean editable;                    // table is editable or not?
	private Listener inputChangedListener;       // called when table data changes
	private TableViewerColumn validator;         // data validator, only if needed
	
	/**
	 * Create a report table using a predefined schema for the columns
	 * @param parent
	 * @param schema schema which specifies the columns 
	 */
	public TableView(Composite parent, String schemaSheetName, boolean editable) {
		
		this.parent = parent;
		this.editable = editable;
		try {
			this.schema = TableSchemaList.getByName(schemaSheetName);
			this.tableElements = new ArrayList<>();
			this.create();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the interface into the composite 
	 */
	private void create() {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// create the table
		this.tableViewer = new TableViewer(composite, SWT.VIRTUAL | SWT.BORDER | SWT.SINGLE
				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.NONE);
		
		this.tableViewer.getTable().setHeaderVisible(true);
		this.tableViewer.setContentProvider(new TableContentProvider());
		this.tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// create the columns based on the schema
		createColumns();
	}
	
	/**
	 * Create all the columns which are related to the schema
	 * Only visible columns are added
	 */
	private void createColumns() {
		
		// Add the validator column if editable table
		if (editable) {
			this.validator = new TableViewerColumn(this.tableViewer, SWT.NONE);
			validator.getColumn().setWidth(140);
			validator.getColumn().setText("Data check");
		}

		for (TableColumn col : schema) {

			// skip non visible columns
			if (!col.isVisible())
				continue;
			
			if (col.getLabel() == null || col.getLabel().isEmpty()) {
				System.err.println("WARNING: column " 
						+ col 
						+ " is set as visible but it has not a label set.");
				col.setLabel("MISSING_" + col.getId());
			}
			
			// Add the column to the parent table
			TableViewerColumn columnViewer = new TableViewerColumn(this.tableViewer, SWT.NONE);

			// set the label provider for column
			columnViewer.setLabelProvider(new TableLabelProvider(col.getId()));

			// set width according to type and label
			columnViewer.getColumn().setWidth(getColumnWidth(col));
			
			// set text
			if (col.getLabel() != null)
				columnViewer.getColumn().setText(col.getLabel());
			
			// set tool tip
			if(col.getTip() != null)
				columnViewer.getColumn().setToolTipText(col.getTip());
			
			// add editor if editable flag is true
			if (editable)
				columnViewer.setEditingSupport(new TableEditor(this, col));

			// add possibility to sort record by clicking
			// in the column name
			addColumnSorter(col, columnViewer);
		}
	}

	/**
	 * Get the width of a column
	 * @param column
	 * @return
	 */
	private int getColumnWidth(TableColumn column) {
		
		// decide the width of the column
		int size = 80;
		switch (column.getType()) {
		case INTEGER:
			size = 80;
			break;
		default:
			size = 80 + column.getLabel().length() * 4;
			break;
		}
		
		return size;
	}
	
	/**
	 * Add a click to the column header that sorts
	 * the table by the clicked field
	 * @param columnViewer
	 */
	private void addColumnSorter(TableColumn column, TableViewerColumn columnViewer) {
		
		// set default sort direction (false will do an ascending sorting)
		columnViewer.getColumn().setData(false);

		// when a column is pressed order by the selected variable
		columnViewer.getColumn().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				// get old direction
				boolean oldSortDirection = (boolean) columnViewer.getColumn().getData();

				// save the new direction
				columnViewer.getColumn().setData(!oldSortDirection);

				// invert the direction in the table
				orderRowsBy(column, !oldSortDirection);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
	}

	/**
	 * Sort the table elements by a column
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
	 * @return
	 */
	public ArrayList<TableRow> getTableElements() {
		return tableElements;
	}
	
	/**
	 * Get the table viewer
	 * @return
	 */
	public TableViewer getViewer() {
		return tableViewer;
	}
	
	/**
	 * Get the table
	 * @return
	 */
	public Table getTable() {
		return tableViewer.getTable();
	}
	
	/**
	 * Get the selected element if present
	 * @return
	 */
	public TableRow getSelection() {
		
		IStructuredSelection selection = (IStructuredSelection) this.tableViewer.getSelection();
		
		if (selection.isEmpty())
			return null;
		
		return (TableRow) selection.getFirstElement();
	}
	
	
	/**
	 * Get the number of elements contained in the table
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
	 * @return
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * Get the table schema
	 * @return
	 */
	public TableSchema getSchema() {
		return schema;
	}
	
	/**
	 * Check if the whole table is correct
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
	 * @param row
	 */
	public void addRow(TableRow row) {
		this.tableViewer.add(row);
		this.tableElements.add(row);
	}
	
	/**
	 * Add an element to the table viewer
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
	 * @param row
	 */
	public void removeRow(TableRow row) {
		this.tableViewer.remove(row);
		this.tableElements.remove(row);
		row.delete();
	}
	
	/**
	 * Remove the selected row
	 */
	public void removeSelectedRow() {
		TableRow row = getSelection();
		
		if(row == null)
			return;
		
		removeRow(row);
	}
	
	/**
	 * Clear all the elements of the table
	 */
	public void removeAll() {
		this.tableElements.clear();
		this.tableViewer.getTable().removeAll();
	}

	/**
	 * Set the input of the table
	 * @param elements
	 */
	public void setInput(Collection<TableRow> elements) {
		this.tableViewer.setInput(elements);
		this.tableElements = new ArrayList<>(elements);
	}
	
	/**
	 * Set menu to the table
	 * @param menu
	 */
	public void setMenu(Menu menu) {
		this.tableViewer.getTable().setMenu(menu);
	}
	
	/**
	 * Set the validator label provider
	 * @param validatorLabelProvider
	 */
	public void setValidatorLabelProvider(RowValidatorLabelProvider validatorLabelProvider) {
		
		if (this.validator == null)
			return;
		
		this.validator.setLabelProvider(validatorLabelProvider);
	}
	
	/**
	 * Refresh a single row of the table
	 * @param row
	 */
	public void refresh(TableRow row) {

		this.tableViewer.refresh(row);
		
		// call listener
		if (inputChangedListener != null) {
			Event event = new Event();
			event.data = row;
			inputChangedListener.handleEvent(event);
		}
	}
	
	/**
	 * Refresh all the elements
	 */
	public void refresh() {
		this.tableViewer.setInput(tableElements);
	}
	
	/**
	 * Add a listener which is called when the table changes the highlighted element
	 * @param listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.tableViewer.addSelectionChangedListener(listener);
	}
	
	/**
	 * Add a listener which is called when the table changes the highlighted element
	 * @param listener
	 */
	public void addDoubleClickListener(IDoubleClickListener listener) {
		this.tableViewer.addDoubleClickListener(listener);
	}
	
	/**
	 * Called when the input of the table changes
	 * @param inputChangedListener
	 */
	public void setInputChangedListener(Listener inputChangedListener) {
		this.inputChangedListener = inputChangedListener;
	}
}
