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

package org.jpchart.market;

import org.jpchart.data.MarketData;
import org.jpchart.time.TimeUnit;
import java.math.BigDecimal;

/**
 *
 * @author cfelde
 */
public class MarketTick implements Market {
    private final MarketData dataSource;
    
    private final String ticker;
    private final TimeUnit marketTime;
    private final BigDecimal openPrice, highPrice, lowPrice, closePrice, volume;

    public MarketTick(String ticker, TimeUnit marketTime, BigDecimal openPrice, BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice, BigDecimal volume, MarketData dataSource) {
        this.ticker = ticker;
        this.marketTime = marketTime;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        
        this.dataSource = dataSource;
    }
    
    public String getTicker() {
        return ticker;
    }

    public TimeUnit getMarketTime() {
        return marketTime;
    }
    
    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public Market getPrevious() throws Exception {
        return dataSource.getPrevious(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MarketTick other = (MarketTick) obj;
        if (this.ticker == null || !this.ticker.equals(other.ticker)) {
            return false;
        }
        if (this.marketTime != other.marketTime && (this.marketTime == null || !this.marketTime.equals(other.marketTime))) {
            return false;
        }
        if (this.openPrice != other.openPrice && (this.openPrice == null || !this.openPrice.equals(other.openPrice))) {
            return false;
        }
        if (this.highPrice != other.highPrice && (this.highPrice == null || !this.highPrice.equals(other.highPrice))) {
            return false;
        }
        if (this.lowPrice != other.lowPrice && (this.lowPrice == null || !this.lowPrice.equals(other.lowPrice))) {
            return false;
        }
        if (this.closePrice != other.closePrice && (this.closePrice == null || !this.closePrice.equals(other.closePrice))) {
            return false;
        }
        if (this.volume != other.volume && (this.volume == null || !this.volume.equals(other.volume))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + (this.ticker != null ? this.ticker.hashCode() : 0);
        hash = 19 * hash + (this.marketTime != null ? this.marketTime.hashCode() : 0);
        hash = 19 * hash + (this.openPrice != null ? this.openPrice.hashCode() : 0);
        hash = 19 * hash + (this.highPrice != null ? this.highPrice.hashCode() : 0);
        hash = 19 * hash + (this.lowPrice != null ? this.lowPrice.hashCode() : 0);
        hash = 19 * hash + (this.closePrice != null ? this.closePrice.hashCode() : 0);
        hash = 19 * hash + (this.volume != null ? this.volume.hashCode() : 0);
        return hash;
    }
}
