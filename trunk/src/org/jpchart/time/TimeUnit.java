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

/**
 *
 * @author cfelde
 */
public interface TimeUnit {
    /**
     * Get this time unit as a Calendar object
     * 
     * @return Calendar object set to this time unit value
     */
    public Calendar getTime();
    
    /**
     * Return this time unit + one unit
     * 
     * @return This time unit + one
     */
    public TimeUnit getAddOne();
    
    /**
     * Return this time unit - one unit
     * 
     * @return This time unit - one
     */
    public TimeUnit getSubOne();
    
    /**
     * Returns a human readable one-word description of the time units
     * resolution. Example: Minute, Hour, Day, Week, etc.
     * 
     * @return Time unit resolution one-word description
     */
    public String getResolutionDescription();
}
