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

public class ActivateCardDB {

	protected Shell shlActivateCard;
	private Text txtCardType;
	private Text txtSAMSlot;

	public int iSAMSlot;
	public int iCardType;
	public boolean bFormResult = false;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ActivateCardDB window = new ActivateCardDB();
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
		shlActivateCard.open();
		shlActivateCard.layout();
		while (!shlActivateCard.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlActivateCard = new Shell();
		shlActivateCard.setSize(251, 304);
		shlActivateCard.setText("Activate Card");
		
		Label lblCardTechType = new Label(shlActivateCard, SWT.NONE);
		lblCardTechType.setText("Card Tech Type-\r\n0) Contact less card\r\n1) Contact card\r\n");
		lblCardTechType.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblCardTechType.setBounds(10, 10, 210, 66);
		
		Label lblSelectCardType = new Label(shlActivateCard, SWT.NONE);
		lblSelectCardType.setText("Select Card Type");
		lblSelectCardType.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSelectCardType.setBounds(10, 83, 119, 20);
		
		Label lblSamSlot = new Label(shlActivateCard, SWT.NONE);
		lblSamSlot.setText("SAM Slot-\r\n1) First SAM slot\r\n2) Second SAM slot\r\n");
		lblSamSlot.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSamSlot.setBounds(10, 112, 210, 66);
		
		txtCardType = new Text(shlActivateCard, SWT.BORDER);
		txtCardType.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtCardType.setBounds(144, 80, 76, 25);
		
		Label lblSelectSam = new Label(shlActivateCard, SWT.NONE);
		lblSelectSam.setText("Select SAM Slot");
		lblSelectSam.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSelectSam.setBounds(10, 185, 132, 20);
		
		txtSAMSlot = new Text(shlActivateCard, SWT.BORDER);
		txtSAMSlot.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtSAMSlot.setBounds(144, 182, 76, 25);
		
		Button btnOK = new Button(shlActivateCard, SWT.NONE);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					iSAMSlot = Integer.parseInt(txtSAMSlot.getText());
					iCardType = Integer.parseInt(txtCardType.getText());
					bFormResult = true;					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				shlActivateCard.close();
			}
		});
		btnOK.setText("OK");
		btnOK.setBounds(39, 226, 75, 25);
		
		Button btnCancel = new Button(shlActivateCard, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iSAMSlot = -1;
				iCardType = -1;
				bFormResult = false;
				shlActivateCard.close();
			}
		});
		btnCancel.setText("Cancel");
		btnCancel.setBounds(129, 226, 75, 25);

	}

}
