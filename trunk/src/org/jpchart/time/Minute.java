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

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author cfelde
 */
public class Minute implements TimeUnit {
    private final Calendar time;
    
    /**
     * Return a new minute unit using todays date.
     */
    public Minute() {
        // Get cal and set secound and millis to zero
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        this.time = cal;
    }
    
    /**
     * Return a new minute unit using given date, rounded down to closest minute.
     */
    public Minute(Calendar cal) {
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        this.time = (Calendar) cal.clone();
    }
    
    /**
     * Return a new minute unit using given date, rounded down to closest minute.
     */
    public Minute(Date time) {
        this(time.getTime());
    }
    
    /**
     * Return a new minute unit using given date, rounded down to closest minute.
     */
    public Minute(long time) {
        // Get cal and set hour, minute, secound and millis to zero
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeInMillis(time);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        this.time = cal;
    }
    
    /**
     * Return a new minute unit based on the given time unit, rounded down to closest minute.
     */
    public Minute(TimeUnit timeUnit) {
        this((Calendar)timeUnit.getTime().clone());
    }
    
    /**
     * Return a new minute unit based on a date string given as YYYY-MM-DD, and
     * time string given as HH:MM
     */
    public Minute(String dateString, String timeString) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.clear();
        
        String yearString = dateString.split("-")[0];
        String monthString = dateString.split("-")[1];
        String dayString = dateString.split("-")[2];
        
        int year = Integer.parseInt(yearString);
        int month = Integer.parseInt(monthString) - 1;
        int day = Integer.parseInt(dayString);
        
        String hourString = timeString.split(":")[0];
        String minuteString = timeString.split(":")[1];
        
        int hour = Integer.parseInt(hourString);
        int minute = Integer.parseInt(minuteString);
        
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        this.time = cal;
    }

    public Calendar getTime() {
        return time;
    }

    public TimeUnit getAddOne() {
        Calendar cal = (Calendar) time.clone();
        cal.add(Calendar.MINUTE, 1);
        return new Minute(cal);
    }

    public TimeUnit getSubOne() {
        Calendar cal = (Calendar) time.clone();
        cal.add(Calendar.MINUTE, -1);
        return new Minute(cal);
    }

    public String getResolutionDescription() {
        return "Minute";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Minute other = (Minute) obj;
        if (this.time != other.time && (this.time == null || !this.time.equals(other.time))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.time != null ? this.time.hashCode() : 0);
        return hash;
    }
}
