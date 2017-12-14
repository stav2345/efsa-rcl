package test_case;

import java.util.EnumSet;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

/**
 * Allow selecting an operation type
 * @author avonva
 *
 */
public class EnumPicker<E extends Enum<E>> {

	private Shell parent;
	private Shell dialog;
	private Enum<E> selectedEnum;
	private Enum<E> defaultValue;
	private Class<E> enumerator;
	
	public EnumPicker(Shell parent, Class<E> enumerator) {
		this.parent = parent;
		this.enumerator = enumerator;
	}
	
	public void setDefaultValue(Enum<E> defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	private void create() {

		this.dialog = new Shell(parent);
		dialog.setText("Select value");
		dialog.setLayout(new GridLayout(1,false));
		
		final ComboViewer c = new ComboViewer(dialog, SWT.READ_ONLY);
		c.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}
			
			@Override
			public void dispose() {}
			
			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(Object arg0) {
				return ((EnumSet<E>) arg0).toArray();
			}
		});
		
		c.setLabelProvider(new LabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				Enum<E> op = (Enum<E>) element;
				return op.toString();
			}
		});
		
	    c.setInput(java.util.EnumSet.allOf(enumerator));
	    
	    // select the default value if set
	    if (defaultValue != null)
	    	c.setSelection(new StructuredSelection(defaultValue));
	    else
	    	c.getCombo().select(0);
	    
	    Button button = new Button(dialog, SWT.PUSH);
	    button.setText("OK");
	    
	    button.addSelectionListener(new SelectionListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				if (c.getSelection().isEmpty())
					return;
				
				// get the operation type selected
				selectedEnum = (Enum<E>) ((IStructuredSelection) c.getSelection()).getFirstElement();
				
				dialog.close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
	    
	    dialog.pack();
	}
	
	public void open() {
		
		create();
		
		dialog.open();
		
		// Event loop
		while (!dialog.isDisposed()) {
			if (!dialog.getDisplay().readAndDispatch())
				dialog.getDisplay().sleep();
		}
	}
	
	public Enum<E> getSelection() {
		return this.selectedEnum;
	}
}
