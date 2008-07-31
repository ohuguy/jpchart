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

import org.jpchart.time.TimeUnit;
import java.math.BigDecimal;

/**
 *
 * @author cfelde
 */
public interface Market {
    public String getTicker();
    
    public TimeUnit getMarketTime();
    
    public BigDecimal getOpenPrice();
    public BigDecimal getHighPrice();
    public BigDecimal getLowPrice();
    public BigDecimal getClosePrice();
    
    public BigDecimal getVolume();
    
    /**
     * Get previous market data of same type, or null if not available.
     * 
     * @return Previous market data of same type
     */
    public Market getPrevious() throws Exception;
}
