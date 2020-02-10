
public class Common
{	
	public static int logInfo=1;
	
	/** To get the vendor id of the vendor supplying the API*/
	public int GetVendorId()
	{
		try 
		{
			return 2; //Lipi Data Systems Ltd Fixed value
		}
		catch(Exception ex) 
		{
			ex.printStackTrace();
			return 0;
		}
		
	}
	
	
	/** To get the AVRM Api Version*/
	public String GetAVRMApiVersion()
	{
		try
		{
			return "01.00.00";  //valid AVRM 
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return "00.00.00";  //Invalid String
		}
		
	}	
	
	
	/** To activate required logging level*/
	public int SetLoggingLevel(int LogLevel)
	{
		int response=1;
		try
        {
				/*case 41:{firstStatus=41;}break;//TRACE(41): This level specifies finer-grained informational message than the DEBUG.
				case 42:{firstStatus=42;}break;//DEBUG(42): This level specifies fine-grained informational message that are most useful to debug an application.
				case 43:{firstStatus=43;}break;//INFO(43): This level specifies informational messages that highlight the progress of the application at coarse-grained level. 
				case 44:{firstStatus=44;}break;//WARN(44): This level specifies potentially harmful situation.
				case 45:{firstStatus=45;}break;//ERROR(45): This level specifies error messages that might still allow the application to continue running.
				case 46:{firstStatus=46;}break;//TRACE(46): This level specifies very severe error messages that will presumably lead the application to abort.
				case 47:{firstStatus=47;}break;//ALL(47): The ALL has the lowest possible rank and is intended to turn on all logging.
				case 40:{firstStatus=40;}break;//OFF(40): The OFF has the highest possible rank and is intended to turn on all logging.
				default :firstStatus = 1;*/
				logInfo=LogLevel;
				response=0;
		}
		catch(Exception ex)
		{
			response=1;
		}
		
		return response;
	}	
	
	
	/** To get the currently activated logging level*/
	public int GetLoggingLevel()
	{
		try 
		{
			/** This value is global and all class can access it.*/
			return logInfo;
		} 
		catch (Exception e)
		{
			return 1;
		}
	}
}