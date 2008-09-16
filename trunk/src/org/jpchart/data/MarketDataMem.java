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

import java.util.HashMap;
import java.util.Map;
import org.jpchart.market.Market;
import org.jpchart.time.TimeUnit;

/**
 *
 * @author cfelde
 */
public class MarketDataMem implements MarketData {
    private final MarketData dataSource;
    private final String ticker;
    
    private final Map<Long, Market> cache = new HashMap<Long, Market>();
    private final long firstMarketTime, lastMarketTime;
    
    public MarketDataMem(MarketData dataSource, String ticker) throws Exception {
        this.dataSource = dataSource;
        this.ticker = ticker;
        
        dataSource.setMarketDataSource(this);
        
        // Fill cache with ticker
        Market firstMarket = dataSource.getFirst(ticker);
        Market lastMarket = dataSource.getLast(ticker);
        if (firstMarket == null || lastMarket == null) throw new IllegalArgumentException("Unable to fetch " + ticker + " from data source");
        
        firstMarketTime = firstMarket.getMarketTime().getTime().getTimeInMillis();
        lastMarketTime = lastMarket.getMarketTime().getTime().getTimeInMillis();
        
        Market market = firstMarket;
        System.out.println("Loading data into cache");
        while (market != null) {
            cache.put(market.getMarketTime().getTime().getTimeInMillis(), market);
            market = dataSource.getNext(market);
        }
        System.out.println("Cache loaded");
    }
    
    public void setMarketDataSource(MarketData dataSource) {
        throw new UnsupportedOperationException("This data source can not be wrapped");
    }

    public void close() throws Exception {
        cache.clear();
        dataSource.close();
    }

    public Market get(String ticker, TimeUnit time) throws Exception {
        if (!ticker.equals(this.ticker)) return dataSource.get(ticker, time);
        
        return cache.get(time.getTime().getTimeInMillis());
    }

    public Market getLast(String ticker) throws Exception {
        if (!ticker.equals(this.ticker)) return dataSource.getLast(ticker);
        
        return cache.get(lastMarketTime);
    }

    public Market getFirst(String ticker) throws Exception {
        if (!ticker.equals(this.ticker)) return dataSource.getFirst(ticker);
        
        return cache.get(firstMarketTime);
    }

    public Market getOnOrAfter(String ticker, TimeUnit time) throws Exception {
        if (!ticker.equals(this.ticker)) return dataSource.getOnOrAfter(ticker, time);
        
        long timeValue = time.getTime().getTimeInMillis();
        if (timeValue > lastMarketTime) return null;
        if (timeValue < firstMarketTime) timeValue = firstMarketTime;
        
        Market market = null;
        while (market == null && timeValue <= lastMarketTime) {
            market = cache.get(timeValue);
            if (market == null) {
                time = time.getAddOne();
                timeValue = time.getTime().getTimeInMillis();
            }
        }
        
        return market;
    }

    public Market getOnOrBefore(String ticker, TimeUnit time) throws Exception {
        if (!ticker.equals(this.ticker)) return dataSource.getOnOrBefore(ticker, time);
        
        long timeValue = time.getTime().getTimeInMillis();
        if (timeValue < firstMarketTime) return null;
        if (timeValue > lastMarketTime) timeValue = lastMarketTime;
        
        Market market = null;
        while (market == null && timeValue >= firstMarketTime) {
            market = cache.get(timeValue);
            if (market == null) {
                time = time.getSubOne();
                timeValue = time.getTime().getTimeInMillis();
            }
        }
        
        return market;
    }

    public Market getNext(Market current) throws Exception {
        TimeUnit time = current.getMarketTime().getAddOne();
        String ticker = current.getTicker();
        
        return getOnOrAfter(ticker, time);
    }

    public Market getPrevious(Market current) throws Exception {
        TimeUnit time = current.getMarketTime().getSubOne();
        String ticker = current.getTicker();
        
        return getOnOrBefore(ticker, time);
    }

    public String[] getAvailableTickers() throws Exception {
        return dataSource.getAvailableTickers();
    }

    public int getTicksBetween(String ticker, TimeUnit begin, TimeUnit end) throws Exception {
        return dataSource.getTicksBetween(ticker, begin, end);
    }
}
