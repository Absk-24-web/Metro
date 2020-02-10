
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import Cris.Currency;
//import org.eclipse.wb.swt.SWTResourceManager;
//import org.eclipse.wb.swt.SWTResourceManager;

//import Cris.SmartCard;

public class MainWindow {

	protected Shell shell;
	private Text txtResponse;

	private String strOSType;
	private Text txtAmount;
	public Currency objCurrency=new Currency();
	private Text textPortCoin;
	private Text txtEnterTimeout;
	private Text txtEnableDenomMask;

	

	
	public static void main(String[] args) {
		try {

			MainWindow window = new MainWindow();
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
		shell.open();
		shell.layout();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
	}

	public static byte GetBCC(byte[] p, int n)
	{
		byte ch;
		int i;
		byte crc = (byte) 0x00;

		for (i = 0; i < n; i++) {
			ch = p[i];
			crc = (byte) (ch ^ crc);
		}
		return crc;
	}

	public static String GetErrorDesc(byte byE1, byte byE0) {
		String strRes = "";

		switch (String.format("%02x%02x", byE1, byE0)) {
		case "0000":
			strRes = "Reception of undefined command";
			break;
		case "0001":
			strRes = "Command parameter error";
			break;
		}

		return strRes;
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(779, 697);
		shell.setText("SWT Application");

		strOSType = System.getProperty("os.name").toLowerCase();
		shell.setLayout(null);

		txtResponse = new Text(shell, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
	   //	txtResponse.setFont(SWTResourceManager.getFont("Times New Roman", 9, SWT.BOLD));
		txtResponse.setBounds(20, 372, 721, 250);
		//txtResponse.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Label lblResponse = new Label(shell, SWT.NONE);
		lblResponse.setBounds(10, 341, 72, 25);
		lblResponse.setText("Response");
		//lblResponse.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Label lblStatus = new Label(shell, SWT.NONE);
		lblStatus.setBounds(10, 628, 44, 18);
		lblStatus.setText("Status-");
		//lblStatus.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Label lblStatusVal = new Label(shell, SWT.NONE);
		lblStatusVal.setBounds(60, 628, 527, 28);
		lblStatusVal.setText("");
		//lblStatusVal.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Button btnClear = new Button(shell, SWT.NONE);
		btnClear.setBounds(623, 336, 118, 30);
		btnClear.setText("Clear");
		//btnClear.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnClear, true, true);

		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setBounds(10, 10, 731, 320);
		//formToolkit.adapt(tabFolder);
		//formToolkit.paintBordersFor(tabFolder);

		TabItem Currency = new TabItem(tabFolder, SWT.NONE);
		Currency.setText("Currency");
	
		
		Currency.setText("Currency");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		Currency.setControl(composite);
		//formToolkit.paintBordersFor(composite);
		// cmbBaudRate.setVisible(false);

		Button btnConnectNote = new Button(composite, SWT.NONE);
		btnConnectNote.setBounds(10, 46, 218, 37);
		//btnConnect.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnConnectNote.setText("ConnectDevice");
		//formToolkit.adapt(btnConnect, true, true);

		Button btnGetNativeLibVersion = new Button(composite, SWT.NONE);
		btnGetNativeLibVersion.setBounds(234, 46, 218, 37);
		btnGetNativeLibVersion.setText("GetNativeLibVersion");
		//btnGetscarddevnativelibversion.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnGetscarddevnativelibversion, true, true);

		Button btnCurrencyFWVersion = new Button(composite, SWT.NONE);
		btnCurrencyFWVersion.setBounds(458, 46, 218, 37);
		btnCurrencyFWVersion.setText("GetCurrencyFWVersion");
		//btnGetscardreaderfwversion.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnGetscardreaderfwversion, true, true);

		Button btnDeviceCoin = new Button(composite, SWT.NONE);
		btnDeviceCoin.setBounds(10, 89, 218, 39);
		btnDeviceCoin.setText("DisconnectDevice");
		//btnGetdevicestatus.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnGetdevicestatus, true, true);

		Button btnAcceptCurrentCurrency = new Button(composite, SWT.NONE);
		btnAcceptCurrentCurrency.setBounds(234, 89, 218, 39);
		btnAcceptCurrentCurrency.setText("AcceptCurrentCurrency");
		//btnEnablecardacceptance.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnEnablecardacceptance, true, true);

		Button btnReturnCurrentCurrency = new Button(composite, SWT.NONE);
		btnReturnCurrentCurrency.setBounds(458, 89, 218, 39);
		btnReturnCurrentCurrency.setText("ReturnCurrentCurrency");
		//btnDisablecardacceptance.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnDisablecardacceptance, true, true);

		Button btnReturnAcceptedCurrency = new Button(composite, SWT.NONE);
		btnReturnAcceptedCurrency.setBounds(458, 134, 218, 39);
		btnReturnAcceptedCurrency.setText("ReturnAcceptedCurrency");
		//btnRejectcard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnRejectcard, true, true);

		Button btnStackAccepted = new Button(composite, SWT.NONE);
		btnStackAccepted.setBounds(234, 134, 218, 39);
		btnStackAccepted.setText("StackedAcceptedCurrency");
		//btnReturncard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnReturncard, true, true);

		Button btnGetValidCurrency = new Button(composite, SWT.NONE);
		btnGetValidCurrency.setBounds(10, 134, 218, 39);
		btnGetValidCurrency.setText("GetValidCurrency");
		//btnDispensecard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnDispensecard, true, true);

		Button btnIscardinchannel = new Button(composite, SWT.NONE);
		btnIscardinchannel.setBounds(10, 179, 218, 39);
		btnIscardinchannel.setText("IsNoteRemoved");
		//btnActivatecard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnActivatecard, true, true);

		Button btnAcceptCurrencies = new Button(composite, SWT.NONE);
		btnAcceptCurrencies.setBounds(234, 226, 218, 44);
		btnAcceptCurrencies.setText("AcceptCurrencies");
		//btnWriteultralightpage.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnWriteultralightpage, true, true);

		Button btnGetAceptedAmount = new Button(composite, SWT.NONE);
		btnGetAceptedAmount.setBounds(458, 226, 218, 44);
		btnGetAceptedAmount.setText("GetAcceptedAmount");
		//btnAcceptcard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnAcceptcard, true, true);

		Button btnCoin = new Button(composite, SWT.RADIO);
		btnCoin.setText("Coin Acceptor");
		//btnCardReaderWriter.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnCoin.setBounds(401, 10, 158, 20);
		//formToolkit.adapt(btnCardReaderWriter, true, true);

		Button btnCashAcceptor = new Button(composite, SWT.RADIO);
		btnCashAcceptor.setText("CashAcceptor");
		//btnCardDispenser.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnCashAcceptor.setBounds(234, 10, 129, 20);
		//formToolkit.adapt(btnCardDispenser, true, true);
		btnCashAcceptor.setSelection(true);
		
		Button btnBoth = new Button(composite, SWT.RADIO);
		btnBoth.setText("Both");
		btnBoth.setBounds(581, 10, 83, 20);
		
		txtAmount = new Text(composite, SWT.BORDER);
		txtAmount.setBounds(119, 224, 109, 20);
		
		Button btnGetdeviceStatus = new Button(composite, SWT.NONE);
		btnGetdeviceStatus.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				byte[] res=null;
				if(btnBoth.getSelection()==true)
				{
					res=objCurrency.DeviceStatus(0, 100);
					txtResponse.append("\nDeviceStatus Response:- ");
					txtResponse.append("\n");
					for (int i = 0; i < res.length; i++)
					{
						txtResponse.append(String.format("%02x ", res[i])+"");
					}
				}
				else if(btnCashAcceptor.getSelection()==true)
				{
					res=objCurrency.DeviceStatus(1, 100);
					txtResponse.append("\nDeviceStatus Response:- ");
					txtResponse.append("\n");
					for (int i = 0; i < res.length; i++)
					{
						txtResponse.append(String.format("%02x ", res[i])+"");
					}
				}
				else if(btnCoin.getSelection()==true)
				{
					res=objCurrency.DeviceStatus(2, 100);
					txtResponse.append("\nDeviceStatus Response:- ");
					txtResponse.append("\n");
					for (int i = 0; i < res.length; i++)
					{
						txtResponse.append(String.format("%02x ", res[i])+"");
					}
				}
				
				/* Device Status Values */
				txtResponse.append("\nByte 0 : Execution status of the API\n");
				switch(res[0])
				{
  					case 0:txtResponse.append(" Operation successful");	break;
					case 1:txtResponse.append(" Operation failed");	break;
					case 18:txtResponse.append(" Operation timeout occurred");	break;
					case 20:txtResponse.append(" Note Accepter not yet connected");	break;
					case 21:txtResponse.append(" Coin Accepter not yet connected");	break;
					case 22:txtResponse.append(" Coin Escrow not yet connected");	break;
					case 28:txtResponse.append(" Communication failure (Note Accepter)");	break;
					case 29:txtResponse.append(" Communication failure (Coin Accepter)");	break;
					case 30:txtResponse.append(" Communication failure (Coin Escrow)");	break;
					case 31:txtResponse.append(" Other error");	break;					
					default:
					break;
				}
				
				txtResponse.append("\nByte 1 : Note Accepter status\n");	
				
				if ((res[1]&0x01) == 0x00)
				{
					txtResponse.append(" Note Accepter Communication status : Ready\n");
				}
				else
				{
					txtResponse.append(" Note Accepter Communication status : Not Ready\n");
				}
				if ((res[1]&0x02) == 0x00)
				{
					txtResponse.append(" Note Accepter Readiness : Ready\n");
				}
				else
				{
					txtResponse.append(" Note Accepter Readiness : Not Ready\n");
				}
				if ((res[1]&0x04) == 0x00)
				{
					txtResponse.append(" Security Door status : Opened\n");
				}
				else
				{
					txtResponse.append(" Security Door status : Closed\n");
				}
				if ((res[1]&0x08) == 0x00)
				{
					txtResponse.append(" Escrow status : Empty\n");
				}
				else
				{
					txtResponse.append(" Escrow status :Not Empty\n");
				}
				if ((res[1]&0x10) == 0x00)
				{
					txtResponse.append(" Collection Box status : Full\n");
				}
				else
				{
					txtResponse.append(" Collection Box status :Not Full\n");
				}				
				
				
				if ((res[1]&0x020) == 0x0)
				{
					txtResponse.append(" Insertion Slot status : Clear\n");
				}
				else
				{
					txtResponse.append(" Insertion Slot status : Blocked\n");
				}	
				if ((res[1]&0x040) == 0x0)
				{
					txtResponse.append(" Transport Channel status : Clear\n");
				}
				else
				{
					txtResponse.append(" Transport Channel status : Blocked\n");
				}	
				
				txtResponse.append("\nByte 2 : Coin Accepter status\n");	
				
				if ((res[2]&0x01) == 0x0)
				{
					txtResponse.append(" Coin Accepter and Escrow Communication status : Ready\n");
				}
				else
				{
					txtResponse.append(" Coin Accepter and Escrow Communication status : Not Ready\n");
				}
				if ((res[2]&0x02) == 0x0)
				{
					txtResponse.append(" Coin Accepter Readyness : Ready\n");
				}
				else
				{
					txtResponse.append(" Coin Accepter Readyness : Not Ready\n");
				}
				if ((res[2]&0x04) == 0x0)
				{
					txtResponse.append(" Security Door status : Opened\n");
				}
				else
				{
					txtResponse.append(" Security Door status : Closed\n");
				}
				if ((res[2]&0x08) == 0x0)
				{
					txtResponse.append(" Escrow status : Empty\n");
				}
				else
				{
					txtResponse.append(" Escrow status :Not Empty\n");
				}
				if ((res[2]&0x10) == 0x0)
				{
					txtResponse.append(" Collection Box status : Full\n");
				}
				else
				{
					txtResponse.append(" Collection Box status :Not Full\n");
				}
				
				if ((res[2]&0x020) == 0x0)
				{
					txtResponse.append(" Insertion Slot status : Clear\n");
				}
				else
				{
					txtResponse.append(" Insertion Slot status : Blocked\n");
				}	
				if ((res[2]&0x040) == 0x0)
				{
					txtResponse.append(" Transport Channel status : Clear\n");
				}
				else
				{
					txtResponse.append(" Transport Channel status : Blocked\n");
				}	
				
				txtResponse.append("Escrowed Notes\n");
				
				txtResponse.append(" No of escrowed INR 5 Notes " + (res[3]&0x0f) +"\n");
				txtResponse.append(" No of escrowed INR 10 Notes " + ((res[3]&0xf0) >> 4) +"\n");
				txtResponse.append(" No of escrowed INR 20 Notes " + (res[4]&0x0f) +"\n");
				txtResponse.append(" No of escrowed INR 50 Notes " + ((res[4]&0xf0) >> 4) +"\n");
				txtResponse.append(" No of escrowed INR 100 Notes " + (res[5]&0x0f) +"\n");
				txtResponse.append(" No of escrowed INR 200 Notes " + ((res[5]&0xf0) >> 4) +"\n");
				txtResponse.append(" No of escrowed INR 500 Notes " + (res[6]&0x0f) +"\n");
				txtResponse.append(" No of escrowed INR 1000 Notes " + ((res[6]&0xf0) >> 4) +"\n");
				txtResponse.append(" No of escrowed INR 2000 Notes " + (res[7]&0x0f) +"\n");
				
				txtResponse.append("Escrowed Coins\n");
				
				txtResponse.append(" No of escrowed INR 5 Coins " + (res[10]&0x0f) +"\n");
				txtResponse.append(" No of escrowed INR 10 Coins " + ((res[10]&0xf0) >> 4) +"\n");
				
				/* End*/
				
				
				
			}
		});
		btnGetdeviceStatus.setText("DeviceStatus");
		btnGetdeviceStatus.setBounds(234, 179, 218, 39);
		
		Button btnClearJammedCurrency = new Button(composite, SWT.NONE);
		btnClearJammedCurrency.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int i=0;
				if(btnBoth.getSelection()==true)
				{
					 txtResponse.append("\nBoth Disabled For this Functions");
				}
				else if (btnCashAcceptor.getSelection() == true)
				{
					 i=objCurrency.ClearJammedCurrencies(1,0 , 100);
					txtResponse.append("\nClearJammedCurrencies Cash Response:- "+i);
				}
				else if (btnCoin.getSelection() == true)
				{
					 i=objCurrency.ClearJammedCurrencies(2,0 , 100);
					txtResponse.append("\nClearJammedCurrencies Coin Response:- "+i);
				}
				
				switch(i)
				{
					case 0:txtResponse.append(" Operation Successfull");
						break;
					case 1:txtResponse.append(" Operation Failed");
					break;
					case 2:txtResponse.append(" Excess amount accepted");
					break;
					case 20:txtResponse.append(" Note Accepter not yet connected");
					break;
					case 21:txtResponse.append("Coin Accepter not yet connected");
					break;
					case 28:txtResponse.append("Communication failure ");
					break;
					case 31:txtResponse.append("Other error");
					break;
				}
			}
		});
		btnClearJammedCurrency.setText("ClearJammedCurrency");
		btnClearJammedCurrency.setBounds(458, 179, 218, 39);
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setBounds(10, 224, 103, 20);
		lblNewLabel_1.setText("Enter Amount:-");
		
		textPortCoin = new Text(composite, SWT.BORDER);
		textPortCoin.setBounds(110, 6, 109, 34);
		
		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		lblNewLabel_2.setBounds(28, 13, 76, 15);
		lblNewLabel_2.setText("Port Coin");
		
		txtEnterTimeout = new Text(composite, SWT.BORDER);
		txtEnterTimeout.setBounds(119, 255, 109, 20);
		
		Label lblEnterTimeout = new Label(composite, SWT.NONE);
		lblEnterTimeout.setText("Enter Timeout");
		lblEnterTimeout.setBounds(10, 250, 103, 20);
		
		Button btnEnabledenomination = new Button(shell, SWT.NONE);
		//btnEnabledenomination.setVisible(false);
		btnEnabledenomination.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if(btnCashAcceptor.getSelection()==true)
				{
					int res=objCurrency.EnableDenomination(1, Integer.parseInt(txtEnableDenomMask.getText().toString()), 100);
				    txtResponse.append("Enable Denomination response:- "+res);
				    switch (res) 
		            {
					case 0: txtResponse.append("\n Operation Succesful");
					 break;
					case 1: txtResponse.append("\n Operation Failed");
					 break;
					case 31: txtResponse.append("\n Other error");
					 break;
					case 20:txtResponse.append("\nNote Accepter not yet connected");
					 break;
					default:
						break;
					}
				}
			}
		});
		btnEnabledenomination.setText("EnableDenomination");
		btnEnabledenomination.setBounds(395, 337, 157, 29);
		
		Button btnExit = new Button(shell, SWT.NONE);
		btnExit.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				System.exit(0);
			}
		});
		btnExit.setText("EXIT");
		btnExit.setBounds(125, 335, 125, 26);
		
		txtEnableDenomMask = new Text(shell, SWT.BORDER);
		txtEnableDenomMask.setBounds(301, 338, 76, 25);
		
		

		// Open button functioning
		btnConnectNote.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					int res = 31;
					if(btnBoth.getSelection()==true)
					{
					    res=objCurrency.ConnectDevice(51, 0, 0,1, 1,100);
						txtResponse.append("\n*****Cash Device******");
						txtResponse.append("\nConnect Cah Response:- "+res);
						
						Thread.sleep(2000);
						
						int PortId=Integer.parseInt("0"+textPortCoin.getText().toString());
						int res1=objCurrency.ConnectDevice(0, PortId, 0, 2, 0, 100);
						txtResponse.append("\n*****Coin Device******");
						txtResponse.append("\nConnect Coin Response:- "+res1);
						
						
					}
					else if (btnCashAcceptor.getSelection() == true)
					{
					    res=objCurrency.ConnectDevice(51, 0, 0,1, 1,100);
						txtResponse.append("\n*****Cash Device******");
						txtResponse.append("\nConnect Cash Response:- "+res);
						
						
					}
					else if (btnCoin.getSelection() == true)
					{
						int PortId=Integer.parseInt(textPortCoin.getText().toString());
					    res=objCurrency.ConnectDevice(0, PortId, 0, 2, 0, 100);
						txtResponse.append("\n*****Coin Device******");
						txtResponse.append("\nConnect Coin Response:- "+res);

					}
					
					switch(res)
					{
						case 0:txtResponse.append(" Connected SuccesFully");
						break;
						case 1:txtResponse.append(" Device connected successfully with few notes/coins left in escrow");
						break;
						case 2:txtResponse.append(" Excess amount accepted");
						break;
						case 20:txtResponse.append(" Note Accepter already connected");
						break;
						case 21:txtResponse.append(" Coin Accepter already connected");
						break;
						case 28:txtResponse.append(" Communication failure ");
						break;
						case 25	:txtResponse.append(" Port Doesn't exists");
						break;
						case 31:txtResponse.append(" Other error");
						break;
					}
				}
				catch (Exception e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
			}
		});

		// Clear button functioning
		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					txtResponse.setText("");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnGetNativeLibVersion.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try
				{
					// if (btnCashAcceptor.getSelection() == true)
					//{
						 String res=objCurrency.GetNativeLibVersion();
						 txtResponse.append("\nGetNativeLibVersion Response:- "+res);
					/*}
					else
					{
						int res=objCurrency.ConnectDevice(51, 0, 0, 1, 0, 100);
					}*/
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		btnCurrencyFWVersion.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
                  {
					String res=objCurrency.GetCurrencyDevFWVersion(2);
					txtResponse.append("\nGetCurrencyDevFWVersion Response:- "+res);
                  }
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		btnDeviceCoin.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int res=31;
				if (btnCashAcceptor.getSelection() == true)
				{
					res=objCurrency.DisConnectDevice(1, 100);
					txtResponse.append("\nDisconnect Cash Response:- "+res);
					
				}
				else if (btnCoin.getSelection() == true)
				{
					res=objCurrency.DisConnectDevice(2, 100);
					txtResponse.append("\nDisconnect Coin Response:- "+res);
				}
				else
				{
					res=objCurrency.DisConnectDevice(1, 100);
					int resCoin=objCurrency.DisConnectDevice(2, 100);
					txtResponse.append("\nDisconnect Cash Response:- "+res);
					txtResponse.append("\nDisconnect Coin Response:- "+resCoin);
				}
				
				
				switch(res)
				{
					case 0:txtResponse.append(" Device Disconnected Suceesfully");
					break;
					case 1:txtResponse.append(" Device connected successfully with few notes/coins left in escrow");
					break;
					case 2:txtResponse.append(" Excess amount accepted");
					break;
					case 20:txtResponse.append(" Note Accepter not yet connected");
					break;
					case 21:txtResponse.append(" Coin Accepter not yet connected");
					break;
					case 28:txtResponse.append(" Communication failure ");
					break;
					case 29:txtResponse.append(" Communication failure ");
					break;
					case 25	:txtResponse.append(" Port Doesn't exists");
					break;
					case 31:txtResponse.append(" Other error");
					break;
				}
			}
		});

		btnAcceptCurrentCurrency.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try 
				{
					int i=0;
					if(btnBoth.getSelection()==true)
					{
						txtResponse.append("\nBoth Disabled For this Function");
					}
					else if (btnCashAcceptor.getSelection() == true )
					{
                        i=objCurrency.AcceptCurrentCurrency(1, 100, 100);
                        txtResponse.append("\nAcceptCurrentCurrency Cash Response:- "+i);
					}
					else if (btnCoin.getSelection()==true)
					{
						i=objCurrency.AcceptCurrentCurrency(2, 100, 100);
						txtResponse.append("\nAcceptCurrentCurrency Coin Response:- "+i);
					}
					else
					{
						txtResponse.append("\nSelect Option From Radio Box First");
					}
					switch(i)
					{
						case 0:txtResponse.append(" Note/Coin of correct denomination accepted");
						break;
						case 1:txtResponse.append(" Device connected successfully with few notes/coins left in escrow");
						break;
						case 2:txtResponse.append(" Excess amount accepted");
						break;
						case 20:txtResponse.append(" Note Accepter not yet connected");
						break;
						case 21:txtResponse.append(" Coin Accepter not yet connected");
						break;
						case 28:txtResponse.append(" Communication failure ");
						break;
						case 29:txtResponse.append(" Communication failure ");
						break;
						case 30:txtResponse.append(" Communication failure ");
						break;
						case 25	:txtResponse.append(" Port Doesn't exists");
						break;
						case 31:txtResponse.append(" Other error");
						break;
					}
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		btnReturnCurrentCurrency.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try
				{
					int i=0;
					if(btnBoth.getSelection()==true)
					{
						 txtResponse.append("\nBoth Disabled For this Functions");
					}
					else if (btnCashAcceptor.getSelection() == true)
					{
						 i=objCurrency.ReturnCurrentCurrency(1, 100);
						 txtResponse.append("\nReturnCurrentCurrency Cash Response:- "+i);
					}
					else if (btnCoin.getSelection()==true)
					{
						 i=objCurrency.ReturnCurrentCurrency(2, 100);
						 txtResponse.append("\nReturnCurrentCurrency Coin Response:- "+i);
					}
					else
					{
						 txtResponse.append("\nSelection Option From Radio Button First");
					}
					
					switch(i)
					{
						case 0:txtResponse.append(" Note/Coin Returned");
						break;
						case 1:txtResponse.append(" Blockage");
						break;
						case 2:txtResponse.append(" Excess amount accepted");
						break;
						case 20:txtResponse.append(" Note Accepter not yet connected");
						break;
						case 21:txtResponse.append(" Coin Accepter not yet connected");
						break;
						case 28:txtResponse.append(" Communication failure ");
						break;
						case 29:txtResponse.append(" Communication failure ");
						break;
						case 30:txtResponse.append(" Communication failure ");
						break;
						case 25	:txtResponse.append(" Port Doesn't exists");
						break;
						case 31:txtResponse.append(" Other error");
						break;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnGetValidCurrency.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					if(btnBoth.getSelection()==true)
					{
						txtResponse.append("\nBoth Disabled For this Function");
					}
					else if (btnCashAcceptor.getSelection() == true)
					{
						int res=objCurrency.GetValidCurrency(1, 100, 100);
						txtResponse.append("\n GetValidCurrency Cash Response:- "+ res);
					}
					else if (btnCoin.getSelection() == true)
					{
						int res=objCurrency.GetValidCurrency(2, 100, 100);
						txtResponse.append("\n GetValidCurrency Coin Response:- "+ res);
					}
					
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnStackAccepted.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					int i=objCurrency.StackAcceptedCurrencies(100);
					txtResponse.append("\nStackAcceptedCurrencies Cash Response:- "+i);
					
					switch(i)
					{
						case 0:txtResponse.append(" Note/Coin Stacked");
						break;
						case 1:txtResponse.append(" Device connected successfully with few notes/coins left in escrow");
						break;
						case 2:txtResponse.append(" Excess amount accepted");
						break;
						case 20:txtResponse.append(" Note Accepter not yet connected");
						break;
						case 21:txtResponse.append(" Coin Accepter not yet connected");
						break;
						case 28:txtResponse.append(" Communication failure ");
						break;
						case 29:txtResponse.append(" Communication failure ");
						break;
						case 30:txtResponse.append(" Communication failure ");
						break;
						case 25	:txtResponse.append(" Port Doesn't exists");
						break;
						case 31:txtResponse.append(" Other error");
						break;
					}
				}
				catch (Exception e1) 
				{
					e1.printStackTrace();
				}
			}
		});

		btnReturnAcceptedCurrency.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					int i=objCurrency.ReturnAcceptedCurrency(100);
					txtResponse.append("\nReturnAcceptedCurrency Cash Response:- "+i);
					switch(i)
					{
						case 0:txtResponse.append(" Note/Coin Returned");
						break;
						case 1:txtResponse.append(" Blockage");
						break;
						case 2:txtResponse.append(" Excess amount accepted");
						break;
						case 20:txtResponse.append(" Note Accepter not yet connected");
						break;
						case 21:txtResponse.append(" Coin Accepter not yet connected");
						break;
						case 28:txtResponse.append(" Communication failure ");
						break;
						case 29:txtResponse.append(" Communication failure ");
						break;
						case 30:txtResponse.append(" Communication failure ");
						break;
						case 25	:txtResponse.append(" Port Doesn't exists");
						break;
						case 31:txtResponse.append(" Other error");
						break;
					}
					
				}
				catch (Exception e1) 
				{
					e1.printStackTrace();
				}
			}
		});

		btnIscardinchannel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try
				{
				    if (btnCashAcceptor.getSelection() == true)
					{
						int i=objCurrency.IsNoteRemoved(100);
						txtResponse.append("\nIsNoteRemoved Cash Response:- "+i);
						
						switch(i)
						{
							case 0:txtResponse.append(" NoteRemoved");
								break;
							case 1:txtResponse.append(" Notes not yet Removed");
							break;
							case 20:txtResponse.append(" Note Accepter not yet connected");
							break;
							case 21:txtResponse.append(" Coin Accepter not yet connected");
							break;
							case 28:txtResponse.append(" Communication failure ");
							break;
							case 31:txtResponse.append(" Other error");
							break;
						}
					}
					else
					{
						txtResponse.append("\nPlease Select The Cash Device ");
					}
				} 
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		btnAcceptCurrencies.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					lblStatusVal.setText("");
                    int Timeout=Integer.parseInt(txtEnterTimeout.getText().toString());
					if (btnBoth.getSelection() == true)
					{
						Boolean i=objCurrency.AcceptCurrencies(0, Integer.parseInt(txtAmount.getText().toString()), Timeout);
						txtResponse.append("\nAcceptCurrencies Both Response:- "+i);
					}
					else if (btnCashAcceptor.getSelection() == true)
					{
						Boolean i=objCurrency.AcceptCurrencies(1, Integer.parseInt(txtAmount.getText().toString()), Timeout);
						txtResponse.append("\nAcceptCurrencies Cash Response:- "+i);
					}
					else if (btnCoin.getSelection() == true)
					{
						Boolean i=objCurrency.AcceptCurrencies(2, Integer.parseInt(txtAmount.getText().toString()), Timeout);
						txtResponse.append("\nAcceptCurrencies Coin Response:- "+i);
					}
					
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		btnGetAceptedAmount.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try
				{
					byte[][] AccpAmt=new byte[25][2];
					int res=objCurrency.GetAcceptedAmount(AccpAmt);
					String Resp="";
				
					txtResponse.append("\nAcceptedAmount Response:- "+res+" ");
					switch(res)
					{
						case 0:txtResponse.append(" Accepting");
						break;
						case 1:txtResponse.append(" Exact amount accepted");
						break;
						case 2:txtResponse.append(" Excess amount accepted");
						break;
						case 20:txtResponse.append(" Note Accepter not yet connected");
						break;
						case 21:txtResponse.append(" Coin Accepter not yet connected");
						break;
						case 28:txtResponse.append(" Communication failure ");
						break;
						case 31:txtResponse.append(" Other error");
						break;
						case 18:txtResponse.append(" Timeout Occured");
						break;
					}
					txtResponse.append("\n");
					AccpAmt=objCurrency.getAcceptedAmount();
					int[] arrDeno=new int[] {0,5,10,20,50,100,200,500,2000};
					for (int i = 1; i < arrDeno.length; i++)
					{
						txtResponse.append("[ "+i+" ] - "+arrDeno[i] +"  , ");
					}
					txtResponse.append("\n");
					int TotalAmount=0;
					for (int i = 0; i < 2; i++)
					{
						for (int j = 0; j < 25; j++)
						{
							txtResponse.append(String.format("%02X ",AccpAmt[j][i]));
							TotalAmount+=arrDeno[AccpAmt[j][i]];
						}
						txtResponse.append("\n");
					}
					
					
					   /*int g_5Notes = 0, g_10Notes = 0, g_20Notes = 0, g_50Notes = 0,g_100Notes = 0,
					       g_200Notes = 0, g_500Notes = 0, g_1000Notes = 0,g_2000Notes = 0,g_5Coin=0,g_10Coin = 0;
					
					for (int i=1;i<arrDeno.length;i++)
					{
						
						for (int j = 0; j < 25; j++)
						{
							switch((int)AccpAmt[j][1])
							{
							  case 1:g_5Notes++;
								  break;
							  case 2:g_10Notes++;
								  break;
							  case 3:g_20Notes++;
								  break;
							  case 4:g_50Notes++;
								  break;
							  case 5:g_100Notes++;
								  break;
							  case 6:g_200Notes++;
								  break;
							  case 7:g_500Notes++;
								  break;
							  case 8:g_1000Notes++;
								  break;
							  case 9:g_2000Notes++;
							      break;
							}
						}
						
					}
					for (int i=1;i<2;i++)
					{
						
						for (int j = 0; j < 25; j++)
						{
							switch((int)AccpAmt[j][1])
							{
							  case 1:g_5Coin++;
								  break;
							  case 2:g_10Coin++;
								  break;
							}
						}
						//txtResponse.append("\nCoin "+arrDeno[i] + " : " +k);
					}		
					
					txtResponse.append("Note 5:- "+g_5Notes+", Note 10:- "+g_10Notes+", Note 20:- "+g_20Notes+"\n");
					txtResponse.append("Note 50:- "+g_50Notes+", Note 100:- "+g_100Notes+", Note 200:- "+g_200Notes+"\n");
					txtResponse.append("Note 500:- "+g_5Notes+", Note 1000:- "+g_1000Notes+", Note 2000:- "+g_2000Notes+"\n");
					txtResponse.append("Coin 5:- "+g_5Coin+", Coin 10:- "+g_10Coin+"\n");*/
					
					
					txtResponse.append("Total Amount Accepted:- "+TotalAmount);
				}
				catch (Exception e1) 
				{
					e1.printStackTrace();
				}
			}
		});
	}

	public static byte[] hexStringToByteArray(String s) {
		try {
			byte[] b = new byte[s.length() / 2];
			for (int i = 0; i < b.length; i++) {
				int index = i * 2;
				int v = Integer.parseInt(s.substring(index, index + 2), 16);
				b[i] = (byte) v;
			}
			return b;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
