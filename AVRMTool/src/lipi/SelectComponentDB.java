package lipi;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class SelectComponentDB {

	protected Shell shlSelectComponent;
	private Text txtComponent;

	public int iComponentSelected;
	public boolean bFormResult = false;
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SelectComponentDB window = new SelectComponentDB();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlSelectComponent.open();
		shlSelectComponent.layout();
		while (!shlSelectComponent.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlSelectComponent = new Shell();
		shlSelectComponent.setSize(274, 278);
		shlSelectComponent.setText("Select Component");
		
		Label lblSelectComponent = new Label(shlSelectComponent, SWT.NONE);
		lblSelectComponent.setText("Components-\r\n0) All component\r\n1) Reader\r\n2) Stacker\r\n3) Rejection bin\r\n4) Channel\r\n5) Collection bin\r\n");
		lblSelectComponent.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSelectComponent.setBounds(10, 10, 241, 148);
		
		Label lblSelectComponent_1 = new Label(shlSelectComponent, SWT.NONE);
		lblSelectComponent_1.setText("Select Component");
		lblSelectComponent_1.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSelectComponent_1.setBounds(10, 165, 135, 20);
		
		txtComponent = new Text(shlSelectComponent, SWT.BORDER);
		txtComponent.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtComponent.setBounds(151, 164, 76, 25);
		
		Button btnOK = new Button(shlSelectComponent, SWT.NONE);
		btnOK.setText("OK");
		btnOK.setBounds(47, 204, 75, 25);
		
		Button btnCancel = new Button(shlSelectComponent, SWT.NONE);
		btnCancel.setText("Cancel");
		btnCancel.setBounds(137, 204, 75, 25);

		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					iComponentSelected = Integer.parseInt(txtComponent.getText());
					bFormResult = true;					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				shlSelectComponent.close();
			}
		});
		
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iComponentSelected = -1;
				bFormResult = false;
				shlSelectComponent.close();
			}
		});
		
	}

}
