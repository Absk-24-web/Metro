package Cris;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import jssc.SerialPort;

public class UPS
{	
	
	private static SerialPort serialPort = null;
	public static Logger UpsLogger = null;
	private static volatile FileHandler fileHandler;
	private static volatile int iPrevDayOfYear = 0;
	public static Boolean isUpsDeviceConnected=false;

	
	public UPS() 
	{
		UpsLogger = Logger.getLogger(UPS.class.getName());
		
		//logger.setLevel(Level.FINE);
		UpsLogger.setLevel(Common.GetLogLevel());
		
		//to show logs on console
		UpsLogger.addHandler(new ConsoleHandler());
	}
	
	public void WriteReqResLog(String strCommandName, byte[] byReqRes)
	{
		String strByteReqRes = "";
		for (int responseLen = 0; responseLen < byReqRes.length; responseLen++)
		{
			strByteReqRes += String.format("%02X ", byReqRes[responseLen]);
		}
		//System.out.println(strCommandName + " - " + strByteReqRes);
		WriteLog(Level.FINEST, strCommandName + " - " + strByteReqRes);
		WriteLog(Level.INFO, strCommandName + " - " + strByteReqRes);
	}
	
	private void WriteLog(Level objLevel, String strLog)
	{
		try
        {
			FileHandler fh = fileHandler;
			if (fh != null) 
			{
				UpsLogger.removeHandler(fh);
		        fh.close(); //Release any file lock.
		    }
			
			//adding custom handler
	        //logger.addHandler(new MyHandler());
	        
			Calendar cal = Calendar.getInstance();
			int iYear = cal.get(Calendar.YEAR);
			int iDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
			
			String strFileName = GlobalMembers.LogDirPath() + File.separator + "AVRMAPI_" + GlobalMembers.strHostName + "Log" + iYear + "." + iDayOfYear;
					
        	File yourFile = new File(strFileName);
        	yourFile.getParentFile().mkdirs();
        	yourFile.createNewFile();
        	
        	//FileHandler file name with max size and number of log files limit
            fileHandler = new FileHandler(strFileName, true);
            
            fileHandler.setFormatter(new MyFormatter());
            
            //setting custom filter for FileHandler
            fileHandler.setFilter(new MyFilter());
            
            //adding custom handler
            UpsLogger.addHandler(fileHandler);
            
            //smartCardlogger.log(objLevel, "prev- " + iPrevDayOfYear);
            //smartCardlogger.log(objLevel, "cur- " + iDayOfYear);
            
            if(iPrevDayOfYear == 0 || iPrevDayOfYear != iDayOfYear)
            {
            	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
            	
            	UpsLogger.log(objLevel, "Date: " + simpleDateFormat.format(new Date()));
            	iPrevDayOfYear = iDayOfYear;
            }
            
            UpsLogger.log(objLevel, strLog);
        }
        catch (SecurityException | IOException e1) 
		{
            e1.printStackTrace();
        }
	}
	
	
	//Connect To the UPS Device
	public int ConnectDevice(int PortId, int Timeout)
	{
		int objresponse=31;
		try 
		{
			if(!isUpsDeviceConnected)
			{
				
				MyFormatter.strSourceMethodName = "ConnectDevice";		
		        //String strComPort ="COM"+PortId;
		        String strComPort ="/dev/ttyUSB"+PortId;
		        WriteLog(Level.INFO, "PortId:" + strComPort + "; Timeout: " + Timeout);
			    serialPort = new SerialPort(strComPort);
		        if(serialPort != null)
		        {
					try
					{
						// opening port
						if(serialPort.openPort() == true)
						{
							WriteLog(Level.FINEST, "Port opened");
						    serialPort.setParams(2400, 	//SerialPort.BAUDRATE_9600,
						                         SerialPort.DATABITS_8,
						                         SerialPort.STOPBITS_1,
						                         SerialPort.PARITY_NONE);		
						}
						isUpsDeviceConnected=true;
						objresponse= 0;
					}
					catch (Exception e) 
					{
						objresponse=20;
					}    
		        }
		        else
				{
		        	    objresponse = 31;
						WriteLog(Level.WARNING, "Serial class object not created");
				}		
			
			}
			else
			{   
				WriteLog(Level.INFO, "Device Already Connected");
				objresponse= 20;
			}
		}
		catch(Exception e2)
		{
			objresponse=28;
			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + objresponse);
		return objresponse;
	
	}
	
	/** To get the Native Library*/
	public String GetNativeLibVersion()
	{
		MyFormatter.strSourceMethodName = "GetNativeLibVersion";		
        WriteLog(Level.INFO, "called");
		String response="";
		try
		{
			WriteLog(Level.FINEST, "Return value- " + response);
            response="01.00.00";//Native lib Version = 1.1.0
		}
		catch(Exception e2)
		{
			response= "00.00.00";
			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + response);
		return response;
	}
	
	
	/** To get the UPS FW Version*/
	public String GetUPSFWVersion()
	{
		MyFormatter.strSourceMethodName = "GetUPSFWVersion";		
        WriteLog(Level.INFO, "GetUPSFWVersion called");
		
		try
		{
			byte[] status = new byte[]{'Q','V','F','W', 0x0d};
			WriteLog(Level.INFO, "Writing Data on Port");
			WriteReqResLog(MyFormatter.strSourceMethodName, status);
            serialPort.writeBytes(status);
            Thread.sleep(100);
            String s=serialPort.readString();
            String[] Res=s.split(":");
        	WriteLog(Level.FINEST, "Return value GetUPSFWVersion- " + Res[1]);
            return Res[1];
		}
		catch(Exception e2)
		{
			//objCRDResponse=28;
			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
		}
		//WriteLog(Level.FINEST, "Return value- " + objCRDResponse);
		WriteLog(Level.FINEST, "Return value- 00.00.00");
		return  "00.00.00";			
	}
	
	/** To get the UPS Battery Version*/
	public int GetBatteryStatus()
	{
		MyFormatter.strSourceMethodName = "GetBatteryStatus";		
        WriteLog(Level.INFO, " GetBatteryStatus called");
		int objCRDResponse;
		try 	
		{
			if(isUpsDeviceConnected)
			{
				byte[] status = new byte[]{0x51, 0x42, 0x56, 0x0d};
				WriteLog(Level.INFO, "Writing Data on Port");
				WriteReqResLog(MyFormatter.strSourceMethodName, status);
	            serialPort.writeBytes(status);
	            Thread.sleep(500);
	            String s=serialPort.readString();
	            String[] Res=s.split(" ");
	            objCRDResponse=Integer.parseInt(Res[3]);//"% Remaining Battery"
			}
			else
			{   
				WriteLog(Level.INFO, "Device Not yet Connected");
				objCRDResponse= 20;
			}
			
		}
		catch(Exception e2)
		{
			objCRDResponse=28;
			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value GetBatteryStatus- " + objCRDResponse);
		return  objCRDResponse;
	}
	
	public int GetUPStatus()
	{
		MyFormatter.strSourceMethodName = "GetUPStatus";		
        WriteLog(Level.INFO, "GetUPStatus called");
		int objCRDResponse=31;
		try
		{
			if(isUpsDeviceConnected)
			{
				byte[] status = new byte[]{0x51, 0x4D, 0x4F, 0x44, 0x0d};
				WriteLog(Level.INFO, "Writing Data on Port");
				WriteReqResLog(MyFormatter.strSourceMethodName, status);
				//byte[] status=new byte[] {(byte) 0x81,0x49,0x13};
		         
	            serialPort.writeBytes(status);
	            Thread.sleep(500);
	            String s=serialPort.readString();
	            if(s.toLowerCase().contains("l"))
	            	 objCRDResponse=0;
	            else if(s.toLowerCase().contains("b"))
	            	    objCRDResponse=1;
	            else
	            	 objCRDResponse=31;
			}
			else
			{   
				WriteLog(Level.INFO, "Device Not yet Connected");
				objCRDResponse= 20;
			}
		}
		catch(Exception e2)
		{
			objCRDResponse=28;
			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
			
		}
		WriteLog(Level.FINEST, "Return value GetUPStatus- " + objCRDResponse);
		return objCRDResponse;
	}

	public int DisConnectDevice(int Timeout) 
	{
		MyFormatter.strSourceMethodName = "DisconnectDevice";		
        WriteLog(Level.INFO, "DisconnectDevice Function Called");
        
		int objResponse=31;
		
		try 
		{
			if(isUpsDeviceConnected)
			{
				isUpsDeviceConnected=false;
				serialPort.closePort();
				serialPort=null;
				WriteLog(Level.INFO, "PortClosed Device Disconnected Success");
				objResponse=0;
	            Thread.sleep(100);
	           
				switch(objResponse)
				{
					case 0:   //UPS Disconnected Successfully
					{
						objResponse=0;
					}
					break;
					
					case 18:		//Operation timeout
					{
						objResponse = 18;
					}
					break;
					
					case 20:		//UPS Not yet connected
					{
						objResponse = 20;
					}
					break;
					default : 
						objResponse = 31;//Other Error	
				}
			}
			else
			{   
				WriteLog(Level.INFO, "Device Not yet Connected");
				objResponse= 20;
			}
		}
		catch(Exception e2)
		{
			objResponse = 28;//Communication Failure
			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
			
		}
		WriteLog(Level.FINEST, "Return value- " + objResponse);
		return objResponse;
	}

}




