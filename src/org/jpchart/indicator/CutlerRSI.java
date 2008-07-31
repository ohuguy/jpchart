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
import java.math.MathContext;
import java.math.RoundingMode;
import org.jpchart.market.Market;

/**
 *
 * @author cfelde
 */
public class CutlerRSI implements SimpleIndicator {
    private final int periode;
    
    public CutlerRSI(int periode) {
        this.periode = periode;
    }

    public BigDecimal getValue(Market market) {
        BigDecimal up = new BigDecimal(0);
        BigDecimal down = new BigDecimal(0);
        
        for (int x = 0; x < periode; x++) {
            try {
                Market prevMarket = market.getPrevious();
                if (prevMarket == null) return null;
                
                if (market.getClosePrice().compareTo(prevMarket.getClosePrice()) > 0) {
                    up = up.add(market.getClosePrice().subtract(prevMarket.getClosePrice()));
                }
                else if (market.getClosePrice().compareTo(prevMarket.getClosePrice()) < 0) {
                    down = down.add(prevMarket.getClosePrice().subtract(market.getClosePrice()));
                }
                
                market = prevMarket;
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        
        // Calculate average
        BigDecimal avgUp = up.divide(BigDecimal.valueOf(periode), RoundingMode.HALF_EVEN);
        BigDecimal avgDown = down.divide(BigDecimal.valueOf(periode), RoundingMode.HALF_EVEN);
        
        // If avg down is zero, RSI = 100
        if (avgDown.compareTo(BigDecimal.valueOf(0)) == 0)
            return BigDecimal.valueOf(100);
        
        // RS
        BigDecimal rs = avgUp.divide(avgDown, RoundingMode.HALF_EVEN);
        
        // RSI = 100 - 100*(1/(1+RS))
        BigDecimal rsi = BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(1).add(rs), MathContext.DECIMAL128)));
        
        return rsi;
    }
}
