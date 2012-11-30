/*
 *  Datove schranky (http://github.com/b00lean/datoveschranky)
 *  Copyright (C) 2010  Karel Kyovsky <karel.kyovsky at apksoft.eu>
 *  Modification: 09/2012 CZ NIC z.s.p.o. <podpora at nic dot cz>
 *
 *  This file is part of Datove schranky (http://github.com/b00lean/datoveschranky).
 *
 *  Datove schranky is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License
 *  
 *  Datove schranky is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Datove schranky.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
