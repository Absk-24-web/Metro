package Cris;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenDispenserJava
{		
	public static Boolean isTokenTeuConnected=false;
	public static Logger TokenDispenserLogger = null;
	private static volatile FileHandler fileHandler;
	private static volatile int iPrevDayOfYear = 0;
	public static Boolean isPrinterDeviceConnected=false;

	
	public TokenDispenserJava()
	{
		
		  try
		  {
		        System.setProperty("java.library.path","/var/lipi/Metro/TokenDispenser/Lib/");
		        Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		        fieldSysPath.setAccessible(true);
		        fieldSysPath.set(null, null);	        
		  }
		  catch (Exception ex)
		  {
		        //System.out.println("Failed to set Java Library Path: " + ex.getMessage);
		  }		
		

		
		TokenDispenserLogger = Logger.getLogger(TokenDispenserJava.class.getName());
		
		//logger.setLevel(Level.FINE);
		TokenDispenserLogger.setLevel(Common.GetLogLevel());
		
		//to show logs on console
		TokenDispenserLogger.addHandler(new ConsoleHandler());
	}
	
	private static String bytesToHex(byte[] bytes)
	{
		char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) 
	    {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	private static byte[] hexStringToByteArray(String s)
	{
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) 
        {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
	
	private void WriteLog(Level objLevel, String strLog)
	{
		try
        {
			FileHandler fh = fileHandler;
			if (fh != null) 
			{
				TokenDispenserLogger.removeHandler(fh);
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
            TokenDispenserLogger.addHandler(fileHandler);
            
            //smartCardlogger.log(objLevel, "prev- " + iPrevDayOfYear);
            //smartCardlogger.log(objLevel, "cur- " + iDayOfYear);
            
            if(iPrevDayOfYear == 0 || iPrevDayOfYear != iDayOfYear)
            {
            	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
            	
            	TokenDispenserLogger.log(objLevel, "Date: " + simpleDateFormat.format(new Date()));
            	iPrevDayOfYear = iDayOfYear;
            }
            
            TokenDispenserLogger.log(objLevel, strLog);
        }
        catch (SecurityException | IOException e1) 
		{
            e1.printStackTrace();
        }
	}

	public  int ConnectDevice(int PortId,int ChannelClearanceMode,int Timeout)
    {
	  int ret=0;	
	  try 
	  {
		  if(!isTokenTeuConnected)
		  {
				 String command="",line,response = "";
		         command="./ReferenceMain 1";// 1 20";
				 Runtime run  = Runtime.getRuntime(); 
			     Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
			     
			     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
			     while ((line = input.readLine()) != null) 
			     {  
			       response =line;
			     }  
			     System.out.println("ConnectDevice TEU "+response);
			     input.close();  
			     ret=Integer.parseInt(response);
			     if(ret==0)
			    	 isTokenTeuConnected=true;
			     
			     return ret;  
		  }
		  else
		  {
			  return 20;
		  }
	  }
	  catch (Exception e)
	  { 
		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());   
		return 28;
	  }
    }
	
	public  int ConnectDeviceReader(int PortId,int ChannelClearanceMode,int Timeout)
    {
	  int ret=0;	
	  try 
	  {
		  if(isTokenTeuConnected)
		  {
				 String command="",line,response = "";
		         command="./ReferenceMain 2";// 1 20";
				 Runtime run  = Runtime.getRuntime(); 
				 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
			     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
			     while ((line = input.readLine()) != null) 
			     {  
			       response =line;
			     }  
			     input.close();  
			     ret=Integer.parseInt(response);
			     System.out.println("ConnectDeviceReader "+response);
			     if(ret==0)
			    	 isTokenTeuConnected=true;
			     
			     return ret;
		  }
		  else
		  {
			  return 20;
		  }
	  }
	  catch (Exception e)
	  { 
		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());   
		return 28;
	  }
    }
	
    public  String GetNativeLibVersion()   
    {
  	  String response="00.00.00";	
  	  try 
  	  {
  		 String command="",line;
         command="./ReferenceMain 5";// 1 20";
  		 Runtime run  = Runtime.getRuntime(); 
  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	     while ((line = input.readLine()) != null) 
  	     {  
  	       response =line;
  	     }  
  	     //System.out.println(response);
  	     input.close();  
  	     return response;
  	  }
  	  catch (Exception e)
  	  { 
  		return "00.00.00";
  	  }
    }

    public  String GetTokenDispenserReaderFWVersion()
    {
  	  String response="00.00.00";	
  	  try 
  	  {
  		 String command="",line;
         command="./ReferenceMain 7";// 1 20";
  		 Runtime run  = Runtime.getRuntime(); 
  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	     while ((line = input.readLine()) != null) 
  	     {  
  	       response =line;
  	     }  
  	     //System.out.println(response);
  	     input.close();  
  	     return response;
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString()); 
  		return "00.00.00";
  	  }
    }
    
    
    public  String GetTokenDispenserFWVersion()
    {
      String response="00.00.00";
  	  try 
  	  {
  		 String command="",line;
         command="./ReferenceMain 6";// 1 20";
  		 Runtime run  = Runtime.getRuntime(); 
  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	     while ((line = input.readLine()) != null) 
  	     {  
  	       response =line;
  	     }  
  	     input.close();  
  	     return response;
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString()); 
  		return "00.00.00";
  	  }
    }
    
    
    public  byte[] GetDeviceStatus(int ComponentId,int Timeout)
    {
  	  byte[] ret=new byte[8];	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {
  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 4 "+ComponentId;// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     System.out.println("GetDeviceStatus"+response);
  	  	     input.close();  
  	  	     ret=response.getBytes();
  	  	     return ret;
  	  	  
  		  }
  		  else
  		  {
  			 ret[0]=20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());   
  		ret[0]=28;
  	  }
      
  	  return ret;
     }
    
    
    public  int DispenseTokenPhase1(int BoxNo,int Timeout)
    {
  	  int ret=0;	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {
  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 8 "+BoxNo;// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     input.close();  
  	  	     ret=Integer.parseInt(response);
  	  	     return ret;
  	  	  
  		  }
  		  else
  		  {
  			  return 20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());   
  		return 28;
  	  }
    }
    
    
    public  int DispenseTokenPhase2(int BoxNo,int TokenDest,int Timeout)
    {
  	  int ret=0;	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {
  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 11 "+BoxNo+" "+TokenDest;// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     //System.out.println(response);
  	  	     input.close();  
  	  	     ret=Integer.parseInt(response);
  		     return ret;
  		  }
  		  else
  		  {
  			  
  			  return 20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());
  		return 28;
  	  }
    }
    
    
    public  int EmptyTokenBox(int BoxNo,int TokenDest,int Timeout)
    {
  	  int ret=0;	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {
  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 12 "+BoxNo+" "+TokenDest;// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     input.close(); 
  	  	     ret=Integer.parseInt(response);
  		     return ret;
  		  }
  		  else
  		  {
  			  return 20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());
  		return 28;
  	  }
    }
    
    
    public  int ClearJammedToken(int BoxNo,int TokenDest,int Timeout)
    {
  	  int ret=0;	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {
  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 3 "+BoxNo + " "+TokenDest;// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     input.close();  
  	  	     ret=Integer.parseInt(response);
  		     return ret;
  	  	  
  		  }
  		  else
  		  {
  			return 20;  
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());  
  		return 28;
  	  }
    }
    
    
    public  int DisConnectDevice(int Timeout)
    {
  	  int ret=0;	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {
  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 18";// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     input.close();  
  	  	     ret=Integer.parseInt(response);
  		     return ret;
  		  }
  		  else
  		  {
  			  return 20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());  
  		return 28;
  	  }
     }
    
    
    public  int DisConnectDeviceReader(int Timeout)
    {
  	  int ret=0;	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {
  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 17";// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     input.close();  
  	  	     ret=Integer.parseInt(response);
  		     return ret;
  		  }
  		  else
  		  {
  			  return 20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());  
  		return 29;
  	  }
    }
    
    
    
    //public native int ConnectDevice(int DeviceId,int PortId,int Timeout);
    public  int SAMSlotPowerOnOff(int DeviceId,int SAMSlotId,int PowerOnOffState,int Timeout)
    {
  	  int ret=0;	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {
  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 15";// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     input.close();  
  	  	     ret=Integer.parseInt(response);
  		     return ret;
  	  	  
  		  }
  		  else
  		  {
  			  return 20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());  
  		return 29;
  	  }
    }
    
    
    public  byte[] ResetSAM(int DeviceId,int SAMSlotId,int ResetType,int Timeout)
    {
  	  byte[] ret=new byte[8];	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {

  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 16";// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     input.close();  
  	  	     ret=response.getBytes();
  		     return ret;
  	  	  
  		  }
  		  else
  		  {
  			  ret[0]=20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());  
  		ret[0]=29;
  	  }
      
  	  return ret;
    }
    
    
    public  byte[] ActivateCard(int DeviceId ,int CardTechType,int SAMSlotId,int Timeout)
    {
  	  byte[] ret=new byte[10];	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {
  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 13";// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     input.close();  
 	  	     WriteLog(Level.INFO, " Data Read (HEX)-> "+response); 
  	  	     ret = hexStringToByteArray(response);  	  	  	  	     
  	  	     //ret=response.getBytes();
  		     return ret;
  	  	  
  		  }
  		  else
  		  {
  			  ret[0]=20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());  
  		ret[0]=29;
  	  }
      
  	  return ret;
    }
    
    
    public  int DeactivateCard(int DeviceId ,int CardTechType,int SAMSlotId,int Timeout)
    {
  	  int ret=0;	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {

  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 14";// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     input.close();  
  	  	     ret=Integer.parseInt(response);
  		     return ret;
  	  	  
  		  }
  		  else
  		  {
  			  ret=20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());  
  		ret= 29;
  	  }
  	  return ret;
    }
    
    public  byte[] ReadUltraLightBlock(int DeviceId,int Addr,int Timeout)
    {
  	  byte[] ret=new byte[17];	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {

  	  		 String command="",line,response = "";
  	         command="./ReferenceMain 10 "+Addr;
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     input.close();  
  	  	     WriteLog(Level.INFO, " Data Read (HEX)-> "+response); 
  	  	     ret = hexStringToByteArray(response);    	  	    
  	  	     //ret=response.getBytes();
  		     return ret;
  		  }
  		  else
  		  {
  			  ret[0]=20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());  
  		ret[0]=29;
  	  }
      
  	  return ret;
    }
    
    
    public  int WriteUltralightPage(int DeviceId,int Addr,byte[] Data,int Timeout)
    {
  	  int ret=0;	
  	  try 
  	  {
  		  if(isTokenTeuConnected)
  		  {
  			 String string = "";
  	  		 String command="",line,response = "";
  	  		string = bytesToHex(Data);
  	  		WriteLog(Level.INFO, " Data Written (HEX)-> "+string); 
  	         command="./ReferenceMain 9 "+Addr+" "+string;// 1 20";
  	  		 Runtime run  = Runtime.getRuntime(); 
  	  		 Process proc = run.exec(command,null,new File("/var/lipi/Metro/TokenDispenser/Build/"));
  	  	     BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
  	  	     while ((line = input.readLine()) != null) 
  	  	     {  
  	  	       response =line;
  	  	     }  
  	  	     input.close();  
  	  	     WriteLog(Level.INFO, " Data Response -> "+response);  
  	  	     ret=Integer.parseInt(response);
  		     return ret;
  		  }
  		  else
  		  {
  			  ret=20;
  		  }
  	  }
  	  catch (Exception e)
  	  { 
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		WriteLog(Level.FINEST, errors.toString());  
  		ret= 29;
  	  }
  	  return ret;
     }
}

