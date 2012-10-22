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
		if (date == null) {
			return null;
		}
		
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(IsoDate.stringToDate(date,
				IsoDate.DATE_TIME));
		
		return gregorianCalendar;
	}
	
	public static String FromXmlToHumanReadableDate(String date){
		Date dateObj = IsoDate.stringToDate(date, IsoDate.DATE_TIME);
		SimpleDateFormat sdf = new SimpleDateFormat("dd. MM. yyyy");
		
		return sdf.format(dateObj);
	}
}
