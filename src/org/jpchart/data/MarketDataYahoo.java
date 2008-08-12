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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import org.jpchart.market.Market;
import org.jpchart.market.MarketTick;
import org.jpchart.time.Day;
import org.jpchart.time.TimeUnit;

/**
 *
 * @author cfelde
 */
public class MarketDataYahoo implements MarketData {
    private ArrayList<Market> marketCache = new ArrayList<Market>();
    private String ticker = null;
    
    private void prepareTicker(String ticker) {
        ticker = ticker.toUpperCase();
        if (this.ticker != null && this.ticker.equals(ticker)) return;
        
        this.ticker = null;
        marketCache.clear();
            
        try {
            URL yahooUrl = new URL("http://ichart.finance.yahoo.com/table.csv?s=" + ticker + "&a=00&b=1&c=1930&d=11&e=31&f=2050&g=d&ignore=.csv");
            URLConnection conn = yahooUrl.openConnection();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            String input = reader.readLine();
            
            // First line is header, use that as data check
            if (!input.equals("Date,Open,High,Low,Close,Volume,Adj Close")) throw new IllegalArgumentException("Unknown header: " + input);
            
            System.out.print("Loading data from Yahoo..");
            System.out.flush();
            while ((input = reader.readLine()) != null) {
                String[] parts = input.split(",");
                if (parts.length != 7) throw new IllegalArgumentException("Incorrect data format: " + input);
                
                Day day = new Day(parts[0]);
                BigDecimal open = new BigDecimal(parts[1]);
                BigDecimal high = new BigDecimal(parts[2]);
                BigDecimal low = new BigDecimal(parts[3]);
                BigDecimal close = new BigDecimal(parts[4]);
                BigDecimal volume = new BigDecimal(parts[5]);
                
                Market market = new MarketTick(ticker, day, open, high, low, close, volume, this);
                
                // Note: Yahoo orders with newest first
                marketCache.add(market);
                System.out.print(".");
                System.out.flush();
            }
            
            reader.close();
            
            this.ticker = ticker;
            
            System.out.println("\nData loaded..");
        }
        catch (Exception e) {
            System.err.println(e.toString());
            this.ticker = null;
            marketCache.clear();
        }
    }
    
    public void close() throws Exception {
    }

    public Market get(String ticker, TimeUnit time) throws Exception {
        prepareTicker(ticker);
        
        for (Market market : marketCache)
            if (market.getMarketTime().equals(time)) return market;
        
        return null;
    }

    public Market getLast(String ticker) throws Exception {
        prepareTicker(ticker);
        
        if (marketCache.isEmpty()) return null;
        else return marketCache.get(0);
    }

    public Market getFirst(String ticker) throws Exception {
        prepareTicker(ticker);
        
        if (marketCache.isEmpty()) return null;
        else return marketCache.get(marketCache.size()-1);
    }

    public Market getOnOrAfter(String ticker, TimeUnit time) throws Exception {
        prepareTicker(ticker);
        
        for (int x = marketCache.size()-1; x >= 0; x--) {
            Market current = marketCache.get(x);
            
            if (current.getMarketTime().getTime().getTimeInMillis() >= time.getTime().getTimeInMillis())
                return current;
        }
        
        return null;
    }

    public Market getOnOrBefore(String ticker, TimeUnit time) throws Exception {
        prepareTicker(ticker);
        
        for (int x = 0; x < marketCache.size(); x++) {
            Market current = marketCache.get(x);
            
            if (current.getMarketTime().getTime().getTimeInMillis() <= time.getTime().getTimeInMillis())
                return current;
        }
        
        return null;
    }

    public Market getNext(Market current) throws Exception {
        prepareTicker(current.getTicker());
        
        for (int x = marketCache.size()-1; x >= 0; x--) {
            Market market = marketCache.get(x);
            
            if (market.equals(current)) {
                if (x > 0) return marketCache.get(x-1);
            }
        }
        
        return null;
    }

    public Market getPrevious(Market current) throws Exception {
        prepareTicker(ticker);
        
        for (int x = 0; x < marketCache.size(); x++) {
            Market market = marketCache.get(x);
            
            if (market.equals(current)) {
                if (x < marketCache.size()-1) return marketCache.get(x+1);
            }
        }
        
        return null;
    }

    public String[] getAvailableTickers() throws Exception {
        if (ticker != null) return new String[] { ticker };
        else return new String[0];
    }

    public int getTicksBetween(String ticker, TimeUnit begin, TimeUnit end) throws Exception {
        prepareTicker(ticker);
        
        // Swap if needed
        if (begin.getTime().getTimeInMillis() > end.getTime().getTimeInMillis()) {
            System.err.println("Warning: Needed to swap begin and end in getTicksBetween!");
            TimeUnit tmp = begin;
            begin = end;
            end = tmp;
        }
        
        for (int x = 0; x < marketCache.size(); x++) {
            Market endMarket = marketCache.get(x);
            
            if (endMarket.getMarketTime().getTime().getTimeInMillis() >= end.getTime().getTimeInMillis()) {
                for (int y = x; y < marketCache.size(); y++) {
                    Market beginMarket = marketCache.get(y);
                    
                    if (beginMarket.getMarketTime().getTime().getTimeInMillis() <= begin.getTime().getTimeInMillis())
                        return y-x;
                }
            }
        }
        
        return marketCache.size();
    }
}
