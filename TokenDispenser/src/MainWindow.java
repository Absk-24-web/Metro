
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;


//import org.eclipse.wb.swt.SWTResourceManager;

import Cris.TokenDispenser;



public class MainWindow {

	protected Shell shell;
	private Text txtResponse;

	private String strOSType;
	private Text textDispensePath;
	public TokenDispenser objToken=new TokenDispenser();
	private Text txtWriteData;
	public Text textComponetID;
	
	public static void main(String[] args)
	{
		try
		{
			MainWindow window = new MainWindow();
			window.open();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open()
	{
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

	private static String bytesToHex(byte[] bytes) {
		char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	private static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
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
		shell.setSize(779, 795);
		shell.setText("SWT Application");

		strOSType = System.getProperty("os.name").toLowerCase();
		shell.setLayout(null);

		txtResponse = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		txtResponse.setBounds(10, 425, 721, 250);
		//txtResponse.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Label lblResponse = new Label(shell, SWT.NONE);
		lblResponse.setBounds(10, 394, 72, 25);
		lblResponse.setText("Response");
		//lblResponse.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Label lblStatus = new Label(shell, SWT.NONE);
		lblStatus.setBounds(10, 681, 44, 18);
		lblStatus.setText("Status-");
		//lblStatus.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Label lblStatusVal = new Label(shell, SWT.NONE);
		lblStatusVal.setBounds(125, 711, 527, 28);
		lblStatusVal.setText("");
		//lblStatusVal.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Button btnClear = new Button(shell, SWT.NONE);
		btnClear.setBounds(618, 389, 118, 30);
		btnClear.setText("Clear");
		//btnClear.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnClear, true, true);

		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setBounds(10, 10, 731, 366);
		//formToolkit.adapt(tabFolder);
		//formToolkit.paintBordersFor(tabFolder);

		TabItem TokenDispenser = new TabItem(tabFolder, SWT.NONE);
		TokenDispenser.setText("TokenDispenser");
	
		
		TokenDispenser.setText("TokenDispenser");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		TokenDispenser.setControl(composite);
		
		 // Group dipsensePath = new Group(shell, SWT.NONE);
	     // dipsensePath.setLayout(new RowLayout(SWT.HORIZONTAL));
		//formToolkit.paintBordersFor(composite);
		// cmbBaudRate.setVisible(false);

		Button btnConnectTokenTeu = new Button(composite, SWT.NONE);
		btnConnectTokenTeu.setBounds(10, 46, 218, 37);
		//btnConnect.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnConnectTokenTeu.setText("ConnectDevice");
		//formToolkit.adapt(btnConnect, true, true);

		Button btnGetNativeLibVersion = new Button(composite, SWT.NONE);
		btnGetNativeLibVersion.setBounds(234, 135, 218, 37);
		btnGetNativeLibVersion.setText("GetNativeLibVersion");
		//btnGetscarddevnativelibversion.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnGetscarddevnativelibversion, true, true);

		Button btnTokenFWVersion = new Button(composite, SWT.NONE);
		btnTokenFWVersion.setBounds(458, 91, 218, 37);
		btnTokenFWVersion.setText("GetTokenFWVersion");
		//btnGetscardreaderfwversion.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnGetscardreaderfwversion, true, true);

		Button btnDeviceTokenTeu = new Button(composite, SWT.NONE);
		btnDeviceTokenTeu.setBounds(234, 45, 218, 39);
		btnDeviceTokenTeu.setText("DisconnectDevice");
		//btnGetdevicestatus.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnGetdevicestatus, true, true);

		Button btnTokenReaderFWVersion = new Button(composite, SWT.NONE);
		btnTokenReaderFWVersion.setBounds(10, 134, 218, 39);
		btnTokenReaderFWVersion.setText("GetTokenReaderFWVersion");
		//btnEnablecardacceptance.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnEnablecardacceptance, true, true);

		Button btnEmptyTokenBox = new Button(composite, SWT.NONE);
		btnEmptyTokenBox.setBounds(458, 134, 218, 39);
		btnEmptyTokenBox.setText("EmptyTokenBox");
		//btnDisablecardacceptance.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnDisablecardacceptance, true, true);

		Button btnClearJammedToken = new Button(composite, SWT.NONE);
		btnClearJammedToken.setBounds(458, 179, 218, 39);
		btnClearJammedToken.setText("ClearJammedToken");
		//btnRejectcard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnRejectcard, true, true);

		Button btnDispenseTokenPhase2 = new Button(composite, SWT.NONE);
		btnDispenseTokenPhase2.setBounds(234, 179, 218, 39);
		btnDispenseTokenPhase2.setText("DispenseToken 2");
		//btnReturncard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnReturncard, true, true);

		Button btnDispenseTokenPhase1 = new Button(composite, SWT.NONE);
		btnDispenseTokenPhase1.setBounds(10, 182, 218, 39);
		btnDispenseTokenPhase1.setText("DispenseToken 1");
		//btnDispensecard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnDispensecard, true, true);

		Button btnSamSlot = new Button(composite, SWT.NONE);
		btnSamSlot.setBounds(10, 227, 218, 39);
		btnSamSlot.setText("SamSlotPowerOn/Off");
		//btnActivatecard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnActivatecard, true, true);

		
		textComponetID = new Text(composite, SWT.BORDER);
		textComponetID.setText("0");
		textComponetID.setBounds(490, 7, 45, 35);
		
		
		Button btndeviceStatus = new Button(composite, SWT.NONE);
		btndeviceStatus.setBounds(458, 42, 218, 44);
		btndeviceStatus.setText("DeviceStatus");
		
		Button btnResetSam = new Button(composite, SWT.NONE);
		btnResetSam.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				
			}
		});
		btnResetSam.setText("ResetSam");
		btnResetSam.setBounds(234, 227, 218, 39);
		
		Button btnActivatecard = new Button(composite, SWT.NONE);
		btnActivatecard.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				byte[] ret=objToken.ActivateCard(0, 0, 0,0);
				txtResponse.append("\nActivateCard Response:- " + bytesToHex(ret));
				//bytesToHex(ret)
				for (int i = 0; i < ret.length; i++)
				{
					//txtResponse.append(" "+(char)ret[i]);
				}
			}
		});
		btnActivatecard.setText("ActivateCard");
		btnActivatecard.setBounds(458, 227, 218, 39);
		
		Button btnBox1 = new Button(composite, SWT.RADIO);
		btnBox1.setText("Box 1");
		btnBox1.setSelection(true);
		//btnBox1.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnBox1.setBounds(10, 10, 77, 23);
		
		Button btnBox2 = new Button(composite, SWT.RADIO);
		btnBox2.setText("Box 2");
		btnBox2.setSelection(false);
		//btnBox2.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnBox2.setBounds(113, 10, 77, 21);
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setBounds(205, 10, 95, 26);
		lblNewLabel.setText("Dispense Path");
		
		textDispensePath = new Text(composite, SWT.BORDER);
		textDispensePath.setText("1");
		textDispensePath.setBounds(310, 4, 45, 35);
		
		Button btnConnectReader = new Button(composite, SWT.NONE);
		btnConnectReader.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				txtResponse.append("\nConnect Reader Response:- "+objToken.ConnectDeviceReader(0,0,0));
			}
		});
		btnConnectReader.setText("ConnectDeviceReader");
		btnConnectReader.setBounds(10, 89, 218, 37);
		
		Button btnDisconnectdevicereader = new Button(composite, SWT.NONE);
		btnDisconnectdevicereader.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				txtResponse.append("\nDisconnect Reader Response:- "+objToken.DisConnectDeviceReader(0));
			}
		});
		btnDisconnectdevicereader.setText("DisconnectDeviceReader");
		btnDisconnectdevicereader.setBounds(234, 90, 218, 39);
		
		Button btnReadultralightblock = new Button(composite, SWT.NONE);
		btnReadultralightblock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				
				txtResponse.append("\nReadUltralightBlock Response:- \n");	
				byte[] ret=objToken.ReadUltraLightBlock(0, 0, 0);
				txtResponse.append("Address : 00 " +bytesToHex(ret));
				ret=objToken.ReadUltraLightBlock(0, 4, 0);
				txtResponse.append("\nAddress : 04 " +bytesToHex(ret));
				ret=objToken.ReadUltraLightBlock(0, 8, 0);
				txtResponse.append("\nAddress : 08 " +bytesToHex(ret));
				ret=objToken.ReadUltraLightBlock(0, 12, 0);
				txtResponse.append("\nAddress : 0c " +bytesToHex(ret));
				//txtResponse.append("\nReadUltralightBlock Response:- \n");
				
				for (int i = 0; i < ret.length; i++)
					
				{
					//txtResponse.append(" "+(char)ret[i]);
				}
			}
		});
		btnReadultralightblock.setText("ReadUltralightBlock");
		btnReadultralightblock.setBounds(458, 274, 218, 39);
		
		Button btnWriteultralightblock = new Button(composite, SWT.NONE);
		btnWriteultralightblock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String Data=txtWriteData.getText().toString();
				
				byte[] data = HexStringToByteArray(Data);
				//byte[] data=Data.getBytes();
				txtResponse.append("\nWriteUltralightPage Response:- "+objToken.WriteUltralightPage(0,4,data,0));
			}
		});
		btnWriteultralightblock.setText("WriteUltralightBlock");
		btnWriteultralightblock.setBounds(234, 274, 218, 39);
		
		txtWriteData = new Text(composite, SWT.BORDER);
		txtWriteData.setBounds(10, 276, 218, 37);
		
		Label lblComponentId = new Label(composite, SWT.NONE);
		lblComponentId.setText("Component ID");
		lblComponentId.setBounds(380, 10, 104, 26);
		
		
		
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
		btnExit.setBounds(125, 393, 125, 26);
		
		

		// Open button functioning
		btnConnectTokenTeu.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					txtResponse.append("\nConnect TEU Response:- "+objToken.ConnectDevice(0,0,0));
				}
				catch (Exception e1) 
				{
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
			{/*
				String S=objToken.GetNativeLibVersion();
				System.out.println("Version:- "+S);
				
			  */
				txtResponse.append("\nGetnativeLibVersion Response:- "+objToken.GetNativeLibVersion());
				}
		});

		btnTokenFWVersion.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
                  {
					txtResponse.append("\nGetTokenDispenserFWVersion Response:- "+objToken.GetTokenDispenserFWVersion());
                  }
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		btnDeviceTokenTeu.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				txtResponse.append("\nDisconnect TEU Device:- "+objToken.DisConnectDevice(0));
			}
		});

		btnTokenReaderFWVersion.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try 
				{
					txtResponse.append("\nGetTokenDispenserReaderFWVersion Response:- "+objToken.GetTokenDispenserReaderFWVersion());
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		btnEmptyTokenBox.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				int res = 0;
				try
				{
					
					if(btnBox1.getSelection()==true)
					{
						res=objToken.EmptyTokenBox(1,Integer.parseInt(textDispensePath.getText().toString()), 0);
					}
					else if(btnBox2.getSelection()==true)
					{
						res=objToken.EmptyTokenBox(2,Integer.parseInt(textDispensePath.getText().toString()), 0);
					}
					
					txtResponse.append("\nEmptyTokenBox Response:- "+res);
					//objToken.EmptyTokenBox(0, 0, 0);
					
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnDispenseTokenPhase1.addSelectionListener(new SelectionAdapter() 
		{
			int res;
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if(btnBox1.getSelection()==true)
				{
					res=objToken.DispenseTokenPhase1(1, 100);
				}
				else if(btnBox2.getSelection()==true)
				{
					res=objToken.DispenseTokenPhase1(2, 100);
				}
				txtResponse.append("\nDispenseTokenPhase 1 Response:-"+res);
			}
		});

		btnDispenseTokenPhase2.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					int dis=Integer.parseInt(textDispensePath.getText().toString());
					txtResponse.append("\nDispenseTokenPhase 2 Response:- "+objToken.DispenseTokenPhase2(2, dis, 0));
				}
				catch (Exception e1) 
				{
					e1.printStackTrace();
				}
			}
		});

		btnClearJammedToken.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int res = 0;
				try
				{
					if(btnBox1.getSelection()==true)
					{
						res=objToken.ClearJammedToken(1,Integer.parseInt(textDispensePath.getText().toString()), 0);
					}
					else if(btnBox2.getSelection()==true)
					{
						res=objToken.ClearJammedToken(2,Integer.parseInt(textDispensePath.getText().toString()), 0);
					}
					txtResponse.append("\nClearJammedToken Response:- "+res);
					//objToken.EmptyTokenBox(0, 0, 0);
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		btnSamSlot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try
				{
					
				} 
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		btndeviceStatus.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
			    int id=Integer.parseInt(textComponetID.getText().toString());
				byte[] ret=objToken.GetDeviceStatus(id, 0);
				txtResponse.append("\nGetDeviceStatus Response:- \n");
				for (int i = 0; i < ret.length; i++)
				{
					txtResponse.append(" "+(char)ret[i]);
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