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

public class ReadUltraLightDB {

	protected Shell shlReadultralight;
	private Text txtAddress;

	public int iAddress;
	public boolean bFormResult = false;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ReadUltraLightDB window = new ReadUltraLightDB();
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
		shlReadultralight.open();
		shlReadultralight.layout();
		while (!shlReadultralight.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlReadultralight = new Shell();
		shlReadultralight.setSize(250, 124);
		shlReadultralight.setText("ReadUltraLight");
		
		Label lblReadAddress = new Label(shlReadultralight, SWT.NONE);
		lblReadAddress.setText("Read Address");
		lblReadAddress.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblReadAddress.setBounds(10, 13, 119, 20);
		
		txtAddress = new Text(shlReadultralight, SWT.BORDER);
		txtAddress.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtAddress.setBounds(144, 10, 76, 25);
		
		Button btnOK = new Button(shlReadultralight, SWT.NONE);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					iAddress = Integer.parseInt(txtAddress.getText());					
					bFormResult = true;					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				shlReadultralight.close();
			}
		});
		btnOK.setText("OK");
		btnOK.setBounds(37, 49, 75, 25);
		
		Button btnCancel = new Button(shlReadultralight, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iAddress = -1;
				bFormResult = false;
				shlReadultralight.close();
			}
		});
		btnCancel.setText("Cancel");
		btnCancel.setBounds(127, 49, 75, 25);

	}

}
