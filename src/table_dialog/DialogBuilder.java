package table_dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import table_dialog.RowCreatorViewer.CatalogChangedListener;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import xlsx_reader.TableSchema;

/**
 * Class that allows creating a custom dialog by calling the
 * methods {@link #addHelp(String)} {@link #addLabel(String)}
 * {@link #addRowCreator(String)} and others.
 * @author avonva
 *
 */
public class DialogBuilder {

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
	
	/**
	 * Create an empty table
	 * @param parent
	 */
	public DialogBuilder(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
		this.composite.setLayout(new GridLayout(1,false));
		this.composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
		
		Stack<Control> stack = new Stack<>();
		
		stack.add(composite);
		
		Control found = null;
		
		while (!stack.isEmpty()) {
			
			Control widget = stack.pop();

			Object widgetCode = widget.getData("code");
			
			if (widgetCode != null && widgetCode.equals(code)) {
				found = widget;
				break;
			}
			
			// add all the children
			if (widget instanceof Composite) {
				
				for (Control c : ((Composite) widget).getChildren()) {
					stack.add(c);
				}
			}
		}
		
		return found;
	}
	
	/**
	 * Add a text box to the dialog
	 * @param text
	 * @param editable
	 * @return
	 */
	public DialogBuilder addText(String text, boolean editable) {
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
	public DialogBuilder addLabel(String code, String text) {
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
	public DialogBuilder addLabel(String code) {
		return addLabelToComposite(code, null);
	}
	
	/**
	 * Add an hidden label (it will be shown by calling {@link #setLabelText(String, String)})
	 * @param code
	 * @return
	 */
	public DialogBuilder addLabelToComposite(String code, String compositeCode) {
		
		GridData gd = new GridData();
		gd.exclude = true;
		
		Composite parent;
		if (compositeCode != null)
			parent = (Composite) getWidget(compositeCode);
		else
			parent = composite;
		
		Label label = new Label(parent, SWT.NONE);
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
	
	
	public DialogBuilder addComposite(String code, Layout layout, LayoutData data) {
		return addCompositeToComposite(code, null, layout, data);
	}
	
	/**
	 * 
	 * @param code
	 * @param compositeCode
	 * @param layout
	 * @param data
	 * @return
	 */
	public DialogBuilder addCompositeToComposite(String code, String compositeCode, Layout layout, LayoutData data) {
		
		Composite parent;
		if (compositeCode != null)
			parent = (Composite) getWidget(compositeCode);
		else
			parent = composite;
		
		Composite subComposite = new Composite(parent, SWT.NONE);
		subComposite.setLayout(layout);
		subComposite.setData("code", code);
		if (data != null)
			subComposite.setLayoutData(data);
		return this;
	}
	
	public DialogBuilder addGroup(String code, String groupTitle, Layout layout, LayoutData data) {
		return addGroupToComposite(code, null, groupTitle, layout, data);
	}
	
	public DialogBuilder addGroupToComposite(String code, String compositeCode, 
			String groupTitle, Layout layout, LayoutData data) {
		
		Composite parent;
		if (compositeCode != null)
			parent = (Composite) getWidget(compositeCode);
		else
			parent = composite;
		
		Group subComposite = new Group(parent, SWT.NONE);
		subComposite.setLayout(layout);
		subComposite.setData("code", code);
		subComposite.setText(groupTitle);
		
		if (data != null)
			subComposite.setLayoutData(data);

		return this;
	}
	
	/**
	 * Add a button to the dialog
	 * @param text
	 * @param editable
	 * @return
	 */
	public DialogBuilder addButton(String code, String text, SelectionListener listener) {
		return addButtonToComposite(code, null, text, listener, null);
	}
	
	public DialogBuilder addButton(String code, String text, MouseListener listener) {
		return addButtonToComposite(code, null, text, null, listener);
	}
	
	/**
	 * 
	 * @param code
	 * @param compositeCode
	 * @param text
	 * @param listener
	 * @return
	 */
	public DialogBuilder addButtonToComposite(String code, String compositeCode, String text, 
			SelectionListener listener) {
		return addButtonToComposite(code, compositeCode, text, listener, null);
	}
	
	/**
	 * 
	 * @param code
	 * @param compositeCode
	 * @param text
	 * @param listener
	 * @return
	 */
	public DialogBuilder addButtonToComposite(String code, String compositeCode, String text, 
			MouseListener listener) {
		return addButtonToComposite(code, compositeCode, text, null, listener);
	}
	
	/**
	 * Add a button to the dialog
	 * @param text
	 * @param editable
	 * @return
	 */
	public DialogBuilder addButtonToComposite(String code, String compositeCode, String text, 
			SelectionListener listener, MouseListener mouseListener) {
		
		Composite parent;
		if (compositeCode != null)
			parent = (Composite) getWidget(compositeCode);
		else
			parent = composite;

		Button button = new Button(parent, SWT.PUSH);
		button.setData("code", code);
		button.setText(text);

		if (listener != null)
			button.addSelectionListener(listener);
		
		if (mouseListener != null)
			button.addMouseListener(mouseListener);
		
		return this;
	}
	
	/**
	 * Add an image to a button
	 * @param code
	 * @param image
	 */
	public DialogBuilder addButtonImage(String code, Image image) {
		Button button = (Button) getWidget(code);
		button.setImage(image);
		return this;
	}
	
	/**
	 * Enable/disable a widget
	 * @param code
	 * @param enabled
	 */
	public void setEnabled(String code, boolean enabled) {
		
		Control widget = getWidget(code);
		
		if (widget == null)
			return;
		
		widget.setEnabled(enabled);
	}
	
	/**
	 * Add the help viewer to the parent
	 * @param helpMessage
	 */
	public DialogBuilder addHelp(String helpMessage, boolean addHelpBtn) {
		this.helpViewer = new HelpViewer(composite, helpMessage, addHelpBtn);
		return this;
	}
	
	/**
	 * Add the help viewer to the parent
	 * @param helpMessage
	 */
	public DialogBuilder addHelp(String helpMessage) {
		this.helpViewer = new HelpViewer(composite, helpMessage);
		return this;
	}
	
	/**
	 * Add a simple row creator button
	 * @param label label showed at the left of the row creator
	 * @return
	 */
	public DialogBuilder addRowCreator(String label) {
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
	public DialogBuilder addRowCreator(String label, String selectionListCode) {
		this.catalogSelector = new RowCreatorViewer(composite, RowCreationMode.SELECTOR);
		this.catalogSelector.setLabelText(label);
		this.catalogSelector.setList(selectionListCode);
		return addRowCreatorToComposite(null, label, selectionListCode);
	}
	
	
	/**
	 * Add a row creator with selector.
	 * {@code selectionListCode} identifies an xml list for the combo box. 
	 * All the values in the list will be picked up.
	 * @param selectionListCode
	 */
	public DialogBuilder addRowCreatorToComposite(String compositeCode, 
			String label, String selectionListCode) {
		
		Composite parent;
		if (compositeCode != null)
			parent = (Composite) getWidget(compositeCode);
		else
			parent = composite;
		
		this.catalogSelector = new RowCreatorViewer(parent, RowCreationMode.SELECTOR);
		this.catalogSelector.setLabelText(label);
		
		if (selectionListCode != null)
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
	public DialogBuilder addRowCreator(String label, String selectionListCode, String selectionId) {
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
	public DialogBuilder addTable(String schemaSheetName, boolean editable, TableRow... parents) {
		
		this.table = new TableView(composite, schemaSheetName, editable);
		
		for (TableRow parent : parents)
			table.addParentTable(parent);
		
		table.create();
		
		return this;
	}
	
	/**
	 * Select a row of the table
	 * @param index
	 * @return
	 */
	public DialogBuilder selectRow(int index) {
		
		if (this.table == null)
			return this;
		
		this.table.select(index);
		
		return this;
	}
	
	/**
	 * Show/hide a password column
	 * @param colId id of the column to show/hide
	 * @param visible true to show the password in plain text, false to show it as dots
	 * @return
	 */
	public DialogBuilder setPasswordVisibility(String colId, boolean visible) {
		
		if (this.table == null)
			return this;
		
		this.table.setPasswordVisibility(colId, visible);
		
		return this;
	}
	
	/**
	 * Add a listener to the editor of the table
	 * @param editorListener
	 */
	public void addTableEditorListener(EditorListener editorListener) {
		if (this.table == null)
			return;
		
		this.table.addEditorListener(editorListener);
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
	public void refreshAndSave(TableRow row) {
		
		if (table == null)
			return;
		
		this.table.refreshAndSave(row);
	}
	
	public void replace(TableRow row) {
		if (table == null)
			return;
		
		this.table.replaceRow(row);
	}
	
	public void refresh(TableRow row) {
		
		if (table == null)
			return;
		
		this.table.refresh(row);
	}
	
	public void refreshValidator(TableRow row) {
		if (table == null)
			return;
		
		this.table.refreshValidator(row);
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
	public void setInput(TableRowList row) {
		
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
		
		table.clear();
	}
	
	/**
	 * Delete the selected row
	 */
	public void removeSelectedRow() {
		
		if (table == null)
			return;
		
		table.removeSelectedRows();
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
	
	public TableView getTable() {
		return table;
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
