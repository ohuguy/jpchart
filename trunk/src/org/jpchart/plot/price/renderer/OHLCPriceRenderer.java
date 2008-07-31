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

package org.jpchart.plot.price.renderer;

import org.jpchart.plot.price.*;
import org.jpchart.data.MarketData;
import org.jpchart.market.Market;
import org.jpchart.plot.PlotFrame;
import org.jpchart.time.TimeUnit;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author cfelde
 */
public class OHLCPriceRenderer implements PricePlotRenderer {

    public static enum PlotTypes { OHLC, OHL, HLC };
    private final PlotTypes plotType;
    private final boolean useColors;

    public OHLCPriceRenderer(PlotTypes plotType, boolean useColors) {
        this.plotType = plotType;
        this.useColors = useColors;
    }

    public void paint(Graphics2D g, PlotFrame plotFrame) {
        g.setColor(Color.BLACK);

        int tapSize = 2;
        int barWidth = plotFrame.getBarWidth();
        
        if (barWidth > 5) {
            tapSize = (barWidth - 1) / 2;
        }
        int lineWidth = 1;
        int barXStart = 1;

        MarketData dataSource = plotFrame.getDataSource();
        Market currentMarket = null;
        Market lastMarket = plotFrame.getLastMarket();
        TimeUnit lastVisible = plotFrame.getTimeForX(g.getClipBounds().width);
        
        try {
            currentMarket = dataSource.getOnOrBefore(lastMarket.getTicker(), lastVisible);
        } catch (Exception e) {
            currentMarket = lastMarket;
        }

        while (currentMarket != null && barXStart > 0) {
            barXStart = plotFrame.getXForTime(currentMarket.getMarketTime());

            if (barXStart <= g.getClipBounds().width - barWidth) {
                int openY = plotFrame.getYForPrice(currentMarket.getOpenPrice());
                int closeY = plotFrame.getYForPrice(currentMarket.getClosePrice());
                int highY = plotFrame.getYForPrice(currentMarket.getHighPrice());
                int lowY = plotFrame.getYForPrice(currentMarket.getLowPrice());
                
                if (useColors) {
                    int rc = currentMarket.getOpenPrice().compareTo(currentMarket.getClosePrice());
                    
                    if (rc < 0)
                        g.setColor(Color.GREEN);
                    else if (rc > 0)
                        g.setColor(Color.RED);
                    else
                        g.setColor(Color.BLACK);
                }
                
                // Open tap
                if (plotType == PlotTypes.OHL || plotType == PlotTypes.OHLC) {
                    g.drawLine(barXStart, openY, barXStart + tapSize - 1, openY);
                }
                // Close tap
                if (plotType == PlotTypes.OHLC || plotType == PlotTypes.HLC) {
                    g.drawLine(barXStart + tapSize + lineWidth, closeY, barXStart + tapSize + tapSize, closeY);
                }
                // Line
                g.fillRect(barXStart + tapSize, highY, lineWidth, lowY - highY + 1);
            }

            try {
                currentMarket = currentMarket.getPrevious();
            } catch (Exception e) {
                currentMarket = null;
            }
        }
    }
}
