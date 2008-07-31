/*
 * JPChart, Java Price Chart, for plotting price information and more.
 * Copyright (C) 2008  CodeConsult AS (mail@codeconsult.no)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.jpchart.time;

import java.io.File;
import java.util.Calendar;

/**
 *
 * @author cfelde
 */
public class TimeUtils {
    /**
     * Get a file reference to the file containing minute data for given time
     * and ticker. Returns null if no such file exists.
     * 
     * @param base Base data dir
     * @param time Data time
     * @param ticker Ticker
     * @return File reference or null
     */
    public static File getMinuteDataPath(File base, Minute time, String ticker) {
        int year = time.getTime().get(Calendar.YEAR);
        int month = time.getTime().get(Calendar.MONTH)+1;
        int date = time.getTime().get(Calendar.DAY_OF_MONTH);
        
        File dataFile = new File(base, year + "/" + month + "/" + date + "/" + ticker.toLowerCase() + ".txt");
        if (dataFile.exists() && dataFile.isFile()) {
            return dataFile;
        }
        else {
            return null;
        }
    }
    
    /**
     * Returns the HH:MM time as a String from given time.
     * 
     * @param time
     * @return HH:MM string part of given time
     */
    public static String getMinuteTimeString(Minute time) {
        int hour = time.getTime().get(Calendar.HOUR_OF_DAY);
        int minute = time.getTime().get(Calendar.MINUTE);
        
        String hourString = Integer.toString(hour);
        String minuteString = Integer.toString(minute);
        if (hour < 10) {
            hourString = "0" + hourString;
        }
        if (minute < 10) {
            minuteString = "0" + minuteString;
        }
        
        return hourString + ":" + minuteString;
    }
    
    /**
     * Subtract given number of time units from given time unit.
     * 
     * @param time Given time unit
     * @param count Given time units to subtract
     * @return New time unit
     */
    public static TimeUnit getSubtracted(TimeUnit time, int count) {
        while (count-- > 0) {
            time = time.getSubOne();
        }
        return time;
    }
}
