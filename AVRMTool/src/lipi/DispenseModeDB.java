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

public class DispenseModeDB {

	protected Shell shlDispenseMode;
	private Text txtDispenseMode;

	public int iDispenseModeSelected;
	public boolean bFormResult = false;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DispenseModeDB window = new DispenseModeDB();
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
		shlDispenseMode.open();
		shlDispenseMode.layout();
		while (!shlDispenseMode.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlDispenseMode = new Shell();
		shlDispenseMode.setSize(282, 206);
		shlDispenseMode.setText("Dispense Mode");
		
		Label lblDispenseMode = new Label(shlDispenseMode, SWT.NONE);
		lblDispenseMode.setText("Dispense Mode-\r\n1) Hold at mouth\r\n2) Dispense immediately\r\n");
		lblDispenseMode.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblDispenseMode.setBounds(10, 10, 241, 66);
		
		Label lblSelectDispenseMode = new Label(shlDispenseMode, SWT.NONE);
		lblSelectDispenseMode.setText("Select Dispense Mode");
		lblSelectDispenseMode.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSelectDispenseMode.setBounds(10, 83, 159, 20);
		
		txtDispenseMode = new Text(shlDispenseMode, SWT.BORDER);
		txtDispenseMode.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtDispenseMode.setBounds(175, 80, 76, 25);
		
		Button btnOK = new Button(shlDispenseMode, SWT.NONE);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					iDispenseModeSelected = Integer.parseInt(txtDispenseMode.getText());
					bFormResult = true;					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				shlDispenseMode.close();
			}
		});
		btnOK.setText("OK");
		btnOK.setBounds(47, 122, 75, 25);
		
		Button btnCancel = new Button(shlDispenseMode, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iDispenseModeSelected = -1;
				bFormResult = false;
				shlDispenseMode.close();
			}
		});
		btnCancel.setText("Cancel");
		btnCancel.setBounds(137, 122, 75, 25);

	}

}
