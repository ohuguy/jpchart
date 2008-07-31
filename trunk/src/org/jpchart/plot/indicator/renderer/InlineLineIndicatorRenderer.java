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

package org.jpchart.plot.indicator.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.jpchart.indicator.SimpleIndicator;
import org.jpchart.plot.PlotFrame;
import org.jpchart.plot.indicator.InlineIndicatorRenderer;
import org.jpchart.market.Market;
import org.jpchart.time.TimeUnit;

/**
 *
 * @author cfelde
 */
public class InlineLineIndicatorRenderer implements InlineIndicatorRenderer {
    private final SimpleIndicator indicator;
    private Map<Long, BigDecimal> valueCache = new HashMap<Long, BigDecimal>();
    
    public InlineLineIndicatorRenderer(SimpleIndicator indicator) {
        this.indicator = indicator;
    }
    
    public void paint(Graphics2D g, PlotFrame plotFrame) {
        g.setColor(Color.BLACK);

        int barWidth = plotFrame.getBarWidth();
        int xEnd = 1;
        int xStart = 1;

        Market lastMarket = plotFrame.getLastMarket();
        Market currentMarket = null;
        TimeUnit lastVisible = plotFrame.getTimeForX(g.getClipBounds().width);
        
        try {
            currentMarket = plotFrame.getDataSource().getOnOrBefore(lastMarket.getTicker(), lastVisible);
        } catch (Exception e) {
            currentMarket = lastMarket;
        }

        while (currentMarket != null && xEnd > 0) {
            xEnd = plotFrame.getXForTime(currentMarket.getMarketTime());

            if (xEnd <= g.getClipBounds().width - barWidth) {
                Market previousMarket = null;
                try {
                    previousMarket = currentMarket.getPrevious();
                    xStart = plotFrame.getXForTime(previousMarket.getMarketTime());
                }
                catch (Exception e) {
                    return;
                }
                
                BigDecimal currentValue = valueCache.get(currentMarket.getMarketTime().getTime().getTimeInMillis());
                BigDecimal previousValue = valueCache.get(previousMarket.getMarketTime().getTime().getTimeInMillis());
                
                if (currentValue == null) {
                    currentValue = indicator.getValue(currentMarket);
                    valueCache.put(currentMarket.getMarketTime().getTime().getTimeInMillis(), currentValue);
                }
                if (previousValue == null) {
                    previousValue = indicator.getValue(previousMarket);
                    valueCache.put(previousMarket.getMarketTime().getTime().getTimeInMillis(), previousValue);
                }
                
                if (currentValue != null && previousValue != null) {
                    int currentY = plotFrame.getYForPrice(currentValue);
                    int previousY = plotFrame.getYForPrice(previousValue);

                    // Draw line
                    g.drawLine(xStart, previousY, xEnd, currentY);
                }
            }

            try {
                currentMarket = currentMarket.getPrevious();
            } catch (Exception e) {
                currentMarket = null;
            }
        }
    }
}
