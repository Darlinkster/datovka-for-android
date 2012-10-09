package cz.nic.datovka.tinyDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * These methods were put here from ISDSCommon. 
 * ISDS cannot contain them, since some platforms (Android) do not support XMLGregorianCalendar 
 * @author b00lean
 *
 */
public class AndroidUtils {

	public static String toXmlDate(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
		String xmldate = sdf.format(date).replaceAll("GMT", "");

		return xmldate;
	}

    public static GregorianCalendar toGregorianCalendar(String date) {
    	if (date == null) {
    		return null;
    	}
    	
    	String fullDate;
    	if (date.indexOf("+") != -1){
    		fullDate = date.replaceAll("[+]", "GMT+");
    	}
    	else {
    		fullDate = date.replaceAll("[-]", "GMT-");
    	}
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
    	GregorianCalendar gregorianCalendar = new GregorianCalendar();
		try {
			Date dateObject = sdf.parse(fullDate);
	    	gregorianCalendar.setTime(dateObject);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return gregorianCalendar;
    }
}
