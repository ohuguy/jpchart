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

package org.jpchart.data;

import org.jpchart.market.Market;
import org.jpchart.time.TimeUnit;

/**
 *
 * @author cfelde
 */
public interface MarketData {
    /**
     * Use this to set the data source given to new Market objects generated
     * by this data source. Normally that would be the data source it self,
     * but that would change if that data source is wrapped within another
     * data source.
     */
    void setMarketDataSource(MarketData dataSource);
    
    /**
     * Use this to close any connections and such (like a database connection)
     */
    void close() throws Exception;

    /**
     * Returns market data for given ticker on given time, or null if not available.
     *
     * @param ticker
     * @param time
     * @return Market data, or null
     * @throws Exception on errors
     */
    Market get(String ticker, TimeUnit time) throws Exception;

    /**
     * Get last available market data for given ticker, or null if not available
     *
     * @param ticker
     * @return Market data, or null
     * @throws Exception on errors
     */
    Market getLast(String ticker) throws Exception;

    /**
     * Get first available market data for given ticker, or null if not available
     * 
     * @param ticker
     * @return Market data, or null
     * @throws Exception on errors
     */
    Market getFirst(String ticker) throws Exception;
    
    /**
     * Get first available market data for ticker,
     * on or after given time. Returns null if nothing available.
     * 
     * @param ticker
     * @param time
     * @return First available market data on or after given time, or null
     * @throws Exception on errors
     */
    Market getOnOrAfter(String ticker, TimeUnit time) throws Exception;
    
    /**
     * Get first available market data for ticker,
     * on or before given time. Returns null if nothing available.
     * 
     * @param ticker
     * @param time
     * @return First available market data on or after given time, or null
     * @throws Exception on errors
     */
    Market getOnOrBefore(String ticker, TimeUnit time) throws Exception;
    
    /**
     * Get next available market data.
     * Returns null if not available.
     * 
     * @param current Current market data
     * @return Next market data, or null
     * @throws Exception on errors
     */
    Market getNext(Market current) throws Exception;
    
    /**
     * Get previous available market data.
     * Returns null if not available.
     * 
     * @param current Current market data
     * @return Previous market data, or null
     * @throws Exception on errors
     */
    Market getPrevious(Market current) throws Exception;
    
    /**
     * Get all available tickers. This might just include tickers that already
     * have been downloaded from an external source, resulting in other tickers
     * being available if requested.
     * 
     * If, for some reason, no tickers are available, and empty array will be
     * returned.
     * 
     * @return Array list of available tickers
     */
    String[] getAvailableTickers() throws Exception;
    
    /**
     * This is used by the plotting engine to calculate X positions.
     * It should be as fast as possible (as any data access method.)
     * 
     * Method returns the number of ticks between the given times, inclusive.
     * 
     * @param ticker Ticker
     * @param begin First time stamp
     * @param end Last time stamp
     * @throws Exception on errors
     * @return Number of ticks
     */
    int getTicksBetween(String ticker, TimeUnit begin, TimeUnit end) throws Exception;
}
