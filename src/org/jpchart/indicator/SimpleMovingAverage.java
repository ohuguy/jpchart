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
import java.math.RoundingMode;
import org.jpchart.market.Market;

/**
 *
 * @author cfelde
 */
public class SimpleMovingAverage implements SimpleIndicator {
    public static enum UsePrice { OPEN, HIGH, LOW, CLOSE };
    
    private final int periode;
    private final UsePrice price;
    
    public SimpleMovingAverage(int periode, UsePrice price) {
        this.periode = periode;
        this.price = price;
    }
    
    public BigDecimal getValue(Market market) {
        BigDecimal sum = new BigDecimal(0);
        
        for (int x = 0; x < periode && market != null; x++) {
            if (price == UsePrice.OPEN)
                sum = sum.add(market.getOpenPrice());
            else if (price == UsePrice.HIGH)
                sum = sum.add(market.getHighPrice());
            else if (price == UsePrice.LOW)
                sum = sum.add(market.getLowPrice());
            else if (price == UsePrice.CLOSE)
                sum = sum.add(market.getClosePrice());
            else
                market = null; // Undef
            
            try {
                market = market.getPrevious();
            }
            catch (Exception e) {
                market = null;
            }
        }
        
        if (market == null) return null;
        
        return sum.divide(BigDecimal.valueOf(periode), RoundingMode.HALF_EVEN);
    }
}
