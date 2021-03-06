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

public class Security
{	
	private static SerialPort serialPort = null;	
	public static Thread thCoinAccept = null;	
	public static Logger SecurityLogger = null;
	private static volatile FileHandler fileHandler;
	private static volatile int iPrevDayOfYear = 0;
	public static Boolean isSecurityDeviceConnected=false;
	
	 public Security()
	 {
		 SecurityLogger = Logger.getLogger(Security.class.getName());
		
		//logger.setLevel(Level.FINE);
		 SecurityLogger.setLevel(Common.GetLogLevel());
		
		//to show logs on console
		 SecurityLogger.addHandler(new ConsoleHandler());
	}	
	
	private void WriteLog(Level objLevel, String strLog)
	{
		try
        {
			FileHandler fh = fileHandler;
			if (fh != null) 
			{
				SecurityLogger.removeHandler(fh);
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
            SecurityLogger.addHandler(fileHandler);
            
            //smartCardlogger.log(objLevel, "prev- " + iPrevDayOfYear);
            //smartCardlogger.log(objLevel, "cur- " + iDayOfYear);
            
            if(iPrevDayOfYear == 0 || iPrevDayOfYear != iDayOfYear)
            {
            	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
            	
            	SecurityLogger.log(objLevel, "Date: " + simpleDateFormat.format(new Date()));
            	iPrevDayOfYear = iDayOfYear;
            }
            
            SecurityLogger.log(objLevel, strLog);
        }
        catch (SecurityException | IOException e1) 
		{
            e1.printStackTrace();
        }
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
	
	
	//Connect To the Security Device
	public int ConnectDevice(int PortId, int Timeout)
	{
		int objresponse=31;
		try 
		{
			if(!isSecurityDeviceConnected)
			{
				MyFormatter.strSourceMethodName = "ConnectDevice";		
				
				String  strComPort="";
				if(PortId==5)
		          strComPort ="/dev/ttyACM0";
				
				if(PortId==6 || PortId==7)
			          strComPort ="/dev/ttyACM1";
				
				WriteLog(Level.INFO, "PortId:" + strComPort + "; Timeout: " + Timeout);
				serialPort = new SerialPort(strComPort);
				if (Timeout < 100) {
					Timeout = 100;
					WriteLog(Level.FINE, "Default timeout set");
				}

		        
		        if(serialPort != null)
		        {
					// opening port
		            if(serialPort.openPort() == true)
		            {
		            	WriteLog(Level.FINEST, "Port opened");
			            serialPort.setParams(SerialPort.BAUDRATE_19200, 	//SerialPort.BAUDRATE_9600,
			                                 SerialPort.DATABITS_8,
			                                 SerialPort.STOPBITS_1,
			                                 SerialPort.PARITY_NONE);		
		            }
		            isSecurityDeviceConnected=true;
				    objresponse= 0;                
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
		String response="";
		try
		{
		
			MyFormatter.strSourceMethodName = "GetNativeLibVersion";		
	        WriteLog(Level.INFO, "Called");
            response="01.00.00";//Native lib Version = 1.1.0	
		
			return response;
		}
		catch(Exception e2)
		{
			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
			response= "00.00.00";
			//e2.printStackTrace();
		}
		WriteLog(Level.FINEST, "Return value- " + response);
		return response;
	}
	
	
	/** To get the UPS FW Version*/
	public String GetSecurityDevFWVersion()
	{
		try
		{
			MyFormatter.strSourceMethodName = "GetSecurityDevFWVersion";		
	        WriteLog(Level.INFO, "Called");
			String strControllerVersion = "00.00.00";
			
			if(serialPort != null)
			{
				byte[] byResponse = null;
				
			    byte[] byCmd = new byte [] {0x1B, 0x1D, 0x49, 0x01, 0x02 };  //Send Version of Controller
			    WriteLog(Level.INFO, "Writing Data on Port");
				WriteReqResLog(MyFormatter.strSourceMethodName, byCmd);
								
				if(serialPort.writeBytes(byCmd))
				{
					byte[] byRes1 = serialPort.readBytes(1, 5000);
					
					int iPendingDataLen = byRes1[0];
					
					byte[] byRes2 = serialPort.readBytes(iPendingDataLen, 5000);
					
					byResponse = new byte[byRes1.length + byRes2.length];				
										
					System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
					System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
					
					byte[] byRes3 = new byte[byResponse.length - 2];
					
					System.arraycopy(byResponse, 1, byRes3, 0, byResponse.length - 2);
					strControllerVersion = new String(byRes3, "UTF-8");
				    //System.out.println("Controller Version - " + strControllerVersion);
				 }				
			}
			else
			{
					WriteLog(Level.WARNING, "Serial class object not created");
					return "00.00.00";
			}	
			 //Controller Version LIPI CDC
			 WriteLog(Level.FINEST, "Return value- 01.00.00");
			 return strControllerVersion;
		} 
		catch (Exception e)
		{          
            WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
			WriteLog(Level.FINEST, "Return value- 00.00.00");
			return "00.00.00";
		}	
	}
	
	/** To get the Door Status Version*/
	public int GetDoorStatus(int DoorType)
	{
		int objCRDResponse=31;
		MyFormatter.strSourceMethodName = "GetDoorStatus";		
        WriteLog(Level.INFO, "Called");
		try
		{
			if(isSecurityDeviceConnected)
			{	
				if(DoorType==0)
				{
					byte[] status = new byte[]{ 0x1B, 0x42, 0x44, 0x46, 0x48 };
					WriteLog(Level.INFO, "Writing Data on Port");
					WriteReqResLog(MyFormatter.strSourceMethodName, status);
		            serialPort.writeBytes(status);
		            Thread.sleep(500);
		            String Lower=serialPort.readHexString();  //Lower

		            byte[] status1 = new byte[]{ 0x1B, 0x41, 0x43, 0x45, 0x47 };
		        	WriteReqResLog(MyFormatter.strSourceMethodName, status1);
		            serialPort.writeBytes(status1);
		            Thread.sleep(500);
		            String Upper=serialPort.readHexString(); //Upper	            
					System.out.print("GetDoorStatus : ");
					System.out.println(Lower + " # " +Upper);		            
		            if(Lower.contains("01 4F") && Upper.contains("01 4F"))
		            	objCRDResponse=0;
					else if(Lower.contains("01 43") ||Upper.contains("01 43"))
						objCRDResponse=1;
					else
						objCRDResponse=28;
				}
				else if(DoorType==1)
				{
					byte[] status = new byte[]{ 0x1B, 0x44, 0x64, (byte) 0x84, (byte) 0x94 };
					WriteLog(Level.INFO, "Writing Data on Port");
					WriteReqResLog(MyFormatter.strSourceMethodName, status);
		            serialPort.writeBytes(status);
		            Thread.sleep(200);
		            String ResponseRecieved=serialPort.readHexString();
	            
		            if(ResponseRecieved.contains("01 4F"))
		            	objCRDResponse=0;
					else if(ResponseRecieved.contains("01 43"))
						objCRDResponse=1;
					else
						objCRDResponse=31;
				}            
			
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
		WriteLog(Level.FINEST, "GetDoorStatus Return value- " + objCRDResponse);
		return  objCRDResponse;
	}
	
	public int DisableAlarm(int DoorType,int Time)
	{
		MyFormatter.strSourceMethodName = "DisableAlarm";		
        WriteLog(Level.INFO, "DisableAlarm Function called");
		int objCRDResponse=31;
		try
		{
			if(isSecurityDeviceConnected)
			{
				//byte[] status = new byte[]{ 0x1B, 0x38, 0x48, 0x58, 0x68};
				byte[] status = new byte[]{0x1b, 0x39, 0x79, (byte)0x89, 0x00};
				
				status[4] = (byte)( (Time+59)/60 ); // Time in Minutes
				WriteLog(Level.INFO, "Writing Data on Port");
	            serialPort.writeBytes(status);
	        	WriteReqResLog(MyFormatter.strSourceMethodName, status);
	            Thread.sleep(300);
	            String ResponseRecieved=serialPort.readHexString();
		        objCRDResponse=0;
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
		WriteLog(Level.FINEST, "Disable Alarm Return value- " + objCRDResponse);
		return objCRDResponse;
	}
	
	public int LedTokenOn() //Start Blinking The LED for Token Device
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x45, 0x06, 0x08, 0x02 };      
            serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;  //Success
		}
		catch(Exception e2)
		{
			return 1;  //Failure
		}	
	}
	
	
	public int LedTokenOff()  //Stop Blinking The LED for Token Device
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x46, 0x03, 0x05, 0x02 };
			serialPort.writeBytes(status);
	        Thread.sleep(200);
	        return 0;   //Success
		}
	    catch(Exception e2)
		{
			return 1; //Failure
		}
		
	}
	
	public int LedCardOn()  //Start Blinking The LED above Card Device
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x45, 0x06, 0x08, 0x04 };
            serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;  //Success

		}
		catch(Exception e2)
		{
			return 1;  //Failure
		}

	}
	
	
	public int LedCardOff()  //Stop Blinking The LED above Card Device
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x46, 0x03, 0x05, 0x04 };
            serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;  //Success
		}
		catch(Exception e2)
		{ 
            return 1;   //Failure
		}
	}
	
	
	public int LedCashOn()  //Start Blinking The LED above Cash(BNR) Device
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x45, 0x06, 0x08, 0x08 };
			serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;  //Success
		}
		catch(Exception e2)
		{ 
            return 1;  //Failure
		}
	}
	
	
	public int LedcashOff()  //Stop Blinking The LED above Cash(BNR) Device
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x46, 0x03, 0x05, 0x08 };
            serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;  //Success
		}
		catch(Exception e2)
		{ 
            return 1;   //Failure
		}
	}
	
	
	public int LedCoinOn()   //Start Blinking The LED above Coin Device
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x45, 0x06, 0x08, 0x10 };
			serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;   //Success
		}
		catch(Exception e2)
		{ 
            return 1;   //Failure
		}
	}

	
	
	public int LedCoinOff() //Stop Blinking The LED above Coin Device
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x46, 0x03, 0x05, 0x10 };
			serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;   //Success
		}
		catch(Exception e2)
		{ 
            return 1; //Failure
		}
	}
	
	
	public int LedRpOn()    //Start Blinking The LED above Receipt Printer
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x45, 0x06, 0x08, 0x20 };
			serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;  //Success
		}
		catch(Exception e2)
		{ 
            return 1;    //Failure
		}
	}
	
	
	public int LedRpOff() //Stop Blinking The LED above Receipt Printer
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x46, 0x03, 0x05, 0x20 };
			serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;   //Success
		}
		catch(Exception e2)
		{ 
            return 1;
		}
	}
	
	public int DuraceOn()  //Enable The Durace Lock
	{

		try
		{
			byte[] status = new byte[]{ 0x1B, 0x35, 0x40, 0x45, 0x50 };
			serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;  //Success
		}
		catch(Exception e2)
		{ 
            return 1;  //Failure
		}
	}
	
	
	public int DuraceOff()   //Disable The Durace Lock 
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x34, 0x37, 0x38, 0x39 };
			serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;  //Success
		}
		catch(Exception e2)
		{ 
            return 1;  //Failure
		}
	}

	
	public int LedLightTokenCoinOn() //Start Blinking The LED Where the coin Token and Coin are Dispensed
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x45, 0x07, 0x09, 0x10 };
			serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;   //Success
		}
		catch(Exception e2)
		{ 
            return 1;  //Failure
		}
	}
	
	public int LedLightTokenCoinOff() //Stop Blinking The LED Where the coin Token and Coin are Dispensed
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x46, 0x05, 0x07, 0x10 };
			serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;  //Success
			}
			catch(Exception e2)
			{ 
	            return 1;  //Failure
			}
	}
	
	
	public int LedAllOff() //Stop Blinking All The LED that are enabled
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x47, 0x50, 0x53, 0x56 };
			serialPort.writeBytes(status);
            Thread.sleep(200);
            return 0;  //Success
		}
		catch(Exception e2)
		{ 
            return 1; //Failure
		}
	}
	
	
	public int EnableAlarm()   //Enable The Alarm or Buzzer
	{
		try
		{
			byte[] status = new byte[]{ 0x1B, 0x36, 0x46, 0x56, 0x66 };
			WriteReqResLog(MyFormatter.strSourceMethodName, status);
			serialPort.writeBytes(status);
	        Thread.sleep(200);
	        return 0;  //Success
		}
		catch(Exception e2)
		{ 
	        return 1; //Failure
		}
    }	

	public int DisConnectDevice(int Timeout) 
	{
		int objResponse=31;
		MyFormatter.strSourceMethodName = "DisconnectDevice";		
        WriteLog(Level.INFO, "DisconnectDevice Function Called");
		try 
		{
			if(isSecurityDeviceConnected)
			{
				isSecurityDeviceConnected=false;
				serialPort.closePort();
				serialPort=null;
				WriteLog(Level.INFO, "PortClosed Device Disconnected Success");
				objResponse=0;
	            Thread.sleep(100);
	           
				switch(objResponse)
				{
					case 0:   // Disconnected Successfully
					{
						objResponse=0;
					}
					break;				
					case 18:		//Operation timeout
					{
						objResponse = 18;
					}
					break;				
					case 20:		// Not yet connected
					{
						objResponse = 20;
					}
					break;
					default: 
						objResponse = 31;//Operation Timeout Occured
					break;
				}
			}
			else
			{
				WriteLog(Level.INFO, "Device Not Yet Connected");
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
		
		WriteLog(Level.FINEST, "Disconnect Device Return value- " + objResponse);
		return objResponse;	
	}
}




