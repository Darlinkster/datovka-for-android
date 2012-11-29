package cz.nic.datovka.tinyDB;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.kobjects.isodate.IsoDate;

/**
 * These methods were put here from ISDSCommon. ISDS cannot contain them, since
 * some platforms (Android) do not support XMLGregorianCalendar
 * 
 * @author b00lean
 * 
 */
public class AndroidUtils {
	

	public static String toXmlDate(Date date) {
		String out = IsoDate.dateToString(date, IsoDate.DATE_TIME);
		return out;
	}

	public static GregorianCalendar toGregorianCalendar(String date) {
		if (date == null || date.length() == 0) {
			return null;
		}
		
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(IsoDate.stringToDate(date,
				IsoDate.DATE_TIME));
		
		return gregorianCalendar;
	}
	
	public static String FromXmlToHumanReadableDate(String date){
		Date dateObj = IsoDate.stringToDate(date, IsoDate.DATE_TIME);
		SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy");
		
		return sdf.format(dateObj);
	}
	
	public static String FromXmlToHumanReadableDateWithTime(String date){
		Date dateObj = IsoDate.stringToDate(date, IsoDate.DATE_TIME);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss    d.M.yyyy");
		
		return sdf.format(dateObj);
	}
	
	public static String FromXmlToHumanReadableTime(String date){
		Date dateObj = IsoDate.stringToDate(date, IsoDate.DATE_TIME);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		
		return sdf.format(dateObj);
	}
	
	public static String FromEpochTimeToHumanReadableDateWithTime(long time){
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(time);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss    d.M.yyyy");
		
		return sdf.format(gc.getTime());
	}
	
	public static boolean stringToBoolean(String param) {
		if((param == null) || param.equalsIgnoreCase("")) {
			return false;
		}
		else if(param.equalsIgnoreCase("yes") || param.equalsIgnoreCase("true")){
			return true;
		}
		else if(param.equalsIgnoreCase("no") || param.equalsIgnoreCase("false")) {
			return false;
		}
		
		return false;
	}
}
