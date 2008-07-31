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

package org.jpchart.indicator;

import java.math.BigDecimal;
import org.jpchart.market.Market;

/**
 *
 * @author cfelde
 */
public class SimpleDeltaMovingAverage implements SimpleIndicator {
    private final int periode;
    private final SimpleMovingAverage.UsePrice price;
    private final SimpleMovingAverage sma;
    
    public SimpleDeltaMovingAverage(int periode, SimpleMovingAverage.UsePrice price) {
        this.periode = periode;
        this.price = price;
        this.sma = new SimpleMovingAverage(periode, price);
    }
    
    public BigDecimal getValue(Market market) {
        try {
            BigDecimal current = sma.getValue(market);
            BigDecimal previous = sma.getValue(market.getPrevious());
            
            if (current == null || previous == null) return null;
            
            return current.subtract(previous);
        }
        catch (Exception e) {
            return null;
        }
    }
}
