package test_case;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import message_creator.OperationType;

/**
 * Allow selecting an operation type
 * @author avonva
 *
 */
public class ExportTypeDialog {

	private Shell parent;
	private Shell dialog;
	private OperationType opType;
	
	public ExportTypeDialog(Shell parent) {
		this.parent = parent;
		create();
	}
	
	private void create() {

		this.dialog = new Shell(parent);
		dialog.setText("Select operation type");
		dialog.setLayout(new GridLayout(1,false));
		
		final ComboViewer c = new ComboViewer(dialog, SWT.READ_ONLY);
		c.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}
			
			@Override
			public void dispose() {}
			
			@Override
			public Object[] getElements(Object arg0) {
				return (OperationType[]) arg0;
			}
		});
		
		c.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				OperationType op = (OperationType) element;
				return super.getText(op.getInternalOpType());
			}
		});
		
	    c.setInput(OperationType.values());
	    c.getCombo().select(0);
	    
	    Button button = new Button(dialog, SWT.PUSH);
	    button.setText("Export");
	    
	    button.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				if (c.getSelection().isEmpty())
					return;
				
				// get the operation type selected
				opType = (OperationType) ((IStructuredSelection) c.getSelection()).getFirstElement();
				
				dialog.close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
	    
	    dialog.pack();
	}
	
	public void open() {
		
		dialog.open();
		
		// Event loop
		while (!dialog.isDisposed()) {
			if (!dialog.getDisplay().readAndDispatch())
				dialog.getDisplay().sleep();
		}
	}
	
	public OperationType getSelectedOp() {
		return this.opType;
	}
}
