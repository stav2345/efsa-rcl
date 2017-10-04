package table_dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import table_dialog.RowCreatorViewer.CatalogChangedListener;
import table_skeleton.TableRow;
import xlsx_reader.TableSchema;

/**
 * Class that allows creating a custom dialog by calling the
 * methods {@link #addHelp(String)} {@link #addLabel(String)}
 * {@link #addRowCreator(String)} and others.
 * @author avonva
 *
 */
public class PanelBuilder {

	/**
	 * How a row should be created.
	 * @author avonva
	 *
	 */
	public enum RowCreationMode {
		SELECTOR,  // selector + button to add rows
		STANDARD,  // just a button to add rows
		NONE       // adding not supported
	}
	
	private Composite composite;
	private String helpMessage;
	
	private HelpViewer helpViewer;
	private RowCreatorViewer catalogSelector;
	private TableView table;
	
	private HashMap<String, Control> widgets;
	
	/**
	 * Create an empty table
	 * @param parent
	 */
	public PanelBuilder(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
		this.composite.setLayout(new GridLayout(1,false));
		this.composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.widgets = new HashMap<>();
	}
	
	/**
	 * Set a label provider for the validator column (only available if
	 * the table is editable)
	 * @param validator
	 */
	public void setValidatorLabelProvider(RowValidatorLabelProvider validator) {
		this.table.setValidatorLabelProvider(validator);
	}
	
	/**
	 * Get the composite of the builder in order to add
	 * custom elements to it
	 * @return
	 */
	public Composite getComposite() {
		return composite;
	}
	
	/**
	 * Get a widget of the panel by its code
	 * @param code
	 * @return
	 */
	public Control getWidget(String code) {
		
		for (Control widget : composite.getChildren()) {
			
			Object widgetCode = widget.getData("code");
			
			if (widgetCode == null)
				continue;
			
			if (widgetCode.equals(code))
				return widget;
		}
		
		return null;
	}
	
	/**
	 * Add a text box to the dialog
	 * @param text
	 * @param editable
	 * @return
	 */
	public PanelBuilder addText(String text, boolean editable) {
		Text textBox = new Text(composite, SWT.NONE);
		textBox.setEditable(editable);
		textBox.setText(text);
		return this;
	}
	
	/**
	 * Add a label to the dialog
	 * @param code code to identify the label
	 * @param text
	 * @return
	 */
	public PanelBuilder addLabel(String code, String text) {
		Label label = new Label(composite, SWT.NONE);
		label.setText(text);
		label.setData("code", code);
		return this;
	}
	
	/**
	 * Add an hidden label (it will be shown by calling {@link #setLabelText(String, String)})
	 * @param code
	 * @return
	 */
	public PanelBuilder addLabel(String code) {
		
		GridData gd = new GridData();
		gd.exclude = true;
		
		Label label = new Label(composite, SWT.NONE);
		label.setData("code", code);
		
		label.setLayoutData(gd);
		label.setVisible(false);
		
		return this;
	}
	
	/**
	 * Set the label text
	 * @param code
	 * @param text
	 */
	public void setLabelText(String code, String text) {
		Label label = (Label) this.getWidget(code);
		label.setText(text);
		label.setVisible(true);
		((GridData) label.getLayoutData()).exclude = false;
		label.getParent().layout();
	}
	
	/**
	 * Add a button to the dialog
	 * @param text
	 * @param editable
	 * @return
	 */
	public PanelBuilder addButton(String code, String text, SelectionListener listener) {
		Button button = new Button(composite, SWT.PUSH);
		button.setData("code", code);
		button.setText(text);
		button.addSelectionListener(listener);
		return this;
	}
	
	/**
	 * Enable/disable a widget
	 * @param code
	 * @param enabled
	 */
	public void setEnabled(String code, boolean enabled) {
		getWidget(code).setEnabled(enabled);
	}
	
	/**
	 * Add the help viewer to the parent
	 * @param helpMessage
	 */
	public PanelBuilder addHelp(String helpMessage) {
		this.helpViewer = new HelpViewer(composite, helpMessage);
		return this;
	}
	
	/**
	 * Add a simple row creator button
	 * @param label label showed at the left of the row creator
	 * @return
	 */
	public PanelBuilder addRowCreator(String label) {
		this.catalogSelector = new RowCreatorViewer(composite, RowCreationMode.STANDARD);
		this.catalogSelector.setLabelText(label);
		return this;
	}
	
	/**
	 * Add a row creator with selector.
	 * {@code selectionListCode} identifies an xml list for the combo box. 
	 * All the values in the list will be picked up.
	 * @param selectionListCode
	 */
	public PanelBuilder addRowCreator(String label, String selectionListCode) {
		this.catalogSelector = new RowCreatorViewer(composite, RowCreationMode.SELECTOR);
		this.catalogSelector.setLabelText(label);
		this.catalogSelector.setList(selectionListCode);
		return this;
	}
	
	/**
	 * Add a row creator with selector.
	 * {@code selectionListCode} identifies an xml list for the combo box.
	 * {@code selectionId} identifies a sub node of the xml list and allows taking just the values
	 * under the matched node.
	 * @param selectionListCode
	 * @param selectionId
	 */
	public PanelBuilder addRowCreator(String label, String selectionListCode, String selectionId) {
		this.catalogSelector = new RowCreatorViewer(composite, RowCreationMode.SELECTOR);
		this.catalogSelector.setLabelText(label);
		this.catalogSelector.setList(selectionListCode, selectionId);
		return this;
	}
	
	/**
	 * Enable/disable row creator
	 * @param enabled
	 */
	public void setRowCreatorEnabled(boolean enabled) {
		
		if (catalogSelector == null)
			return;
		
		this.catalogSelector.setEnabled(enabled);
	}
	
	/**
	 * Add the table to the parent
	 */
	public PanelBuilder addTable(String schemaSheetName, boolean editable) {
		Collection<TableRow> parents = new ArrayList<>();
		return this.addTable(schemaSheetName, editable, parents);
	}
	
	/**
	 * Add the table to the parent
	 */
	public PanelBuilder addTable(String schemaSheetName, boolean editable, TableRow parent) {
		
		Collection<TableRow> parents = new ArrayList<>();
		parents.add(parent);
		return this.addTable(schemaSheetName, editable, parents);
	}
	
	/**
	 * Add the table to the parent
	 */
	public PanelBuilder addTable(String schemaSheetName, boolean editable, Collection<TableRow> parents) {
		
		this.table = new TableView(composite, schemaSheetName, editable);
		
		for (TableRow parent : parents)
			table.addParentTable(parent);
		
		table.create();
		
		return this;
	}
	
	/**
	 * Change editability of the table
	 * @param editable
	 */
	public void setTableEditable(boolean editable) {
		
		if (table == null)
			return;
		
		table.setEditable(editable);
	}
	
	/**
	 * Check if the table is editable
	 * @return
	 */
	public boolean isTableEditable() {
		
		if (table == null)
			return false;
		
		return table.isEditable();
	}

	/**
	 * Refresh a row of the table
	 * @param row
	 */
	public void refresh(TableRow row) {
		
		if (table == null)
			return;
		
		this.table.refresh(row);
	}
	
	/**
	 * Refresh the entire table
	 */
	public void refresh() {
		
		if (table == null)
			return;
		
		this.table.refresh();
	}
	
	/**
	 * Add listener to the help image
	 * @param listener
	 */
	public void addHelpListener(MouseListener listener) {
		
		if (helpViewer == null)
			return;
		
		this.helpViewer.setListener(listener);
	}
	
	/**
	 * Enable/disable the creation of new records
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		if (catalogSelector != null)
			this.catalogSelector.setEnabled(enabled);
	}
	
	/**
	 * Set a menu to the table
	 * @param menu
	 */
	public void setMenu(Menu menu) {
		
		if (table == null)
			return;
		
		table.setMenu(menu);
	}
	
	/**
	 * Add a row to the table
	 * @param row
	 */
	public void add(TableRow row) {
		
		if (table == null)
			return;
		
		table.addRow(row);
	}
	
	/**
	 * Add a list of rows to the table
	 * @param row
	 */
	public void addAll(Collection<TableRow> rows) {
		
		if (table == null)
			return;
		
		table.addAll(rows);
	}
	
	/**
	 * Set the table input
	 * @param row
	 */
	public void setInput(Collection<TableRow> row) {
		
		if (table == null)
			return;
		
		table.setInput(row);
	}
	
	/**
	 * Clear all the records
	 */
	public void clearTable() {
		
		if (table == null)
			return;
		
		table.removeAll();
	}
	
	/**
	 * Delete the selected row
	 */
	public void removeSelectedRow() {
		
		if (table == null)
			return;
		
		table.removeSelectedRow();
	}
	
	/**
	 * Listener called when the input of the table
	 * changes
	 * @param inputChangedListener
	 */
	public void setInputChangedListener(Listener inputChangedListener) {
		
		if (table == null)
			return;
		
		table.setInputChangedListener(inputChangedListener);
	}
	
	/**
	 * Listener called when the selection in the table changes
	 * @param listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		
		if (table == null)
			return;
		
		this.table.addSelectionChangedListener(listener);
	}
	
	/**
	 * Listener called when the selection in the table changes
	 * @param listener
	 */
	public void addDoubleClickListener(IDoubleClickListener listener) {
		
		if (table == null)
			return;
		
		this.table.addDoubleClickListener(listener);
	}
	
	/**
	 * Check if all the mandatory fields of the
	 * table are fullified
	 * @return
	 */
	public boolean areMandatoryFilled() {
		
		if (table == null)
			return true;
		
		return table.areMandatoryFilled();
	}
	
	/**
	 * Check if the table was defined
	 * @return
	 */
	public boolean isTableDefined() {
		return table != null;
	}
	
	public TableRow getSelection() {
		
		if (table == null)
			return null;
		
		return table.getSelection();
	}
	
	public ArrayList<TableRow> getTableElements() {
		
		if (table == null)
			return null;
		
		return table.getTableElements();
	}
	
	public TableSchema getSchema() {
		
		if (table == null)
			return null;
		
		return table.getSchema();
	}
	
	public String getHelpMessage() {
		
		if (helpViewer == null)
			return null;
		
		return helpMessage;
	}
	
	public RowCreatorViewer getTypeSelector() {
		return catalogSelector;
	}
	
	public boolean isTableEmpty() {
		
		if (table == null)
			return true;
		
		return this.table.isEmpty();
	}
	
	/**
	 * Listener called when the {@link #catalogSelector}
	 * changes selection
	 * @param listener
	 */
	public void addSelectionListener(CatalogChangedListener listener) {
		if (this.catalogSelector != null) {
			this.catalogSelector.addSelectionListener(listener);
		}
	}
	
	public HelpViewer getHelpViewer() {
		return helpViewer;
	}
}
