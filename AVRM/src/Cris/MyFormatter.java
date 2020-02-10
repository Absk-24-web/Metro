package Cris;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MyFormatter extends Formatter 
{
	public static String strSourceMethodName;
	
    @Override
    public String format(LogRecord record) {
        //return record.getThreadID()+"::" + record.getSourceClassName() + "::" + record.getSourceMethodName() + "::" + new Date(record.getMillis()) + "::" + record.getMessage()+"\n";
    	
    	// get event time and convert it into date 
        DateFormat simple = new SimpleDateFormat("HH:mm:ss:SSS"); 
  
        Date result = new Date(record.getMillis()); 
  
        //System.out.println("Event Time " + simple.format(result));
        
    	return "[" + simple.format(result) + " " + record.getLevel() + " " + record.getSourceClassName() + "] \t" + strSourceMethodName + ":" + record.getMessage()+"\n";
        
    }
}