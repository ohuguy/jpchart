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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jpchart.indicator.SimpleIndicator;
import org.jpchart.market.Market;
import org.jpchart.plot.PlotFrame;
import org.jpchart.plot.indicator.AbstractStandaloneIndicatorRenderer;
import org.jpchart.plot.indicator.StandaloneIndicatorRenderer;
import org.jpchart.time.TimeUnit;

/**
 *
 * @author cfelde
 */
public class StandaloneLineIndicatorRenderer extends AbstractStandaloneIndicatorRenderer implements StandaloneIndicatorRenderer {
    private final SimpleIndicator indicator;
    private Map<Long, BigDecimal> valueCache = new HashMap<Long, BigDecimal>();
    private final boolean autoSetLimits;
    
    public StandaloneLineIndicatorRenderer(SimpleIndicator indicator) {
        this.indicator = indicator;
        autoSetLimits = true;
    }
    
    public StandaloneLineIndicatorRenderer(SimpleIndicator indicator, BigDecimal upper, BigDecimal lower) {
        this.indicator = indicator;
        
        // Switch if needed
        if (upper.compareTo(lower) < 0) {
            BigDecimal tmp = lower;
            lower = upper;
            upper = tmp;
        }
        
        setLimits(upper, lower);
        autoSetLimits = false;
    }

    public void paintStandalone(Graphics2D g, PlotFrame plotFrame) {
        g.setColor(Color.BLACK);

        int yAxisWidth = plotFrame.getYAxisWidth();
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

        ArrayList<BigDecimal> values = new ArrayList<BigDecimal>();
        ArrayList<Market> markets = new ArrayList<Market>();
        BigDecimal maxValue = BigDecimal.valueOf(Long.MIN_VALUE);
        BigDecimal minValue = BigDecimal.valueOf(Long.MAX_VALUE);
        
        while (currentMarket != null && xEnd > 0) {
            xEnd = plotFrame.getXForTime(currentMarket.getMarketTime());

            if (xEnd <= g.getClipBounds().width - yAxisWidth - barWidth) {
                BigDecimal currentValue = valueCache.get(currentMarket.getMarketTime().getTime().getTimeInMillis());
                
                if (currentValue == null) {
                    currentValue = indicator.getValue(currentMarket);
                    valueCache.put(currentMarket.getMarketTime().getTime().getTimeInMillis(), currentValue);
                }
                
                values.add(currentValue);
                markets.add(currentMarket);
                
                if (currentValue != null && currentValue.compareTo(maxValue) > 0)
                    maxValue = currentValue;
                if (currentValue != null && currentValue.compareTo(minValue) < 0)
                    minValue = currentValue;
            }

            try {
                currentMarket = currentMarket.getPrevious();
            } catch (Exception e) {
                currentMarket = null;
            }
        }
        
        if (autoSetLimits)
            setLimits(maxValue, minValue);
        
        for (int x = 0; x < markets.size()-1; x++) {
            currentMarket = markets.get(x);
            BigDecimal currentValue = values.get(x);
            Market prevMarket = markets.get(x+1);
            BigDecimal prevValue = values.get(x+1);
            
            if (currentValue == null || prevValue == null)
                continue;
            
            int curY = getYForValue(currentValue, g.getClipBounds().height);
            int preY = getYForValue(prevValue, g.getClipBounds().height);
            int curX = plotFrame.getXForTime(currentMarket.getMarketTime());
            int preX = plotFrame.getXForTime(prevMarket.getMarketTime());
            
            g.drawLine(preX, preY, curX, curY);
        }
        
        drawAxis(g, plotFrame);
        if (!autoSetLimits) {
            drawLine(g, plotFrame, getUpperLimit().subtract(BigDecimal.valueOf(10)), Color.BLACK, true);
            drawLine(g, plotFrame, getLowerLimit().add(BigDecimal.valueOf(10)), Color.BLACK, true);
        }
    }
}
