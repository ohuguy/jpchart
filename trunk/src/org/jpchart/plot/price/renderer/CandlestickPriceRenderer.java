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
import org.jpchart.market.Market;
import org.jpchart.plot.PlotFrame;
import org.jpchart.time.TimeUnit;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author cfelde
 */
public class CandlestickPriceRenderer implements PricePlotRenderer {
    public void paint(Graphics2D g, PlotFrame plotFrame) {
        g.setColor(Color.BLACK);

        int barWidth = plotFrame.getBarWidth();
        int barXStart = 1;

        Market lastMarket = plotFrame.getLastMarket();
        Market currentMarket = null;
        TimeUnit lastVisible = plotFrame.getTimeForX(g.getClipBounds().width);
        
        try {
            currentMarket = plotFrame.getDataSource().getOnOrBefore(lastMarket.getTicker(), lastVisible);
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
                
                if (currentMarket.getOpenPrice().compareTo(currentMarket.getClosePrice()) < 0) {
                    // Up, white
                    g.setColor(Color.GREEN);
                    g.fillRect(barXStart, Math.min(openY, closeY), barWidth - 1, Math.max(openY, closeY) - Math.min(openY, closeY) + 1);
                    int lineX = barXStart + ((barWidth - 1) / 2);
                    g.drawLine(lineX, highY, lineX, lowY);
                } else {
                    // Down, black
                    g.setColor(Color.RED);
                    g.fillRect(barXStart, Math.min(openY, closeY), barWidth - 1, Math.max(openY, closeY) - Math.min(openY, closeY) + 1);
                    int lineX = barXStart + ((barWidth - 1) / 2);
                    g.drawLine(lineX, highY, lineX, lowY);
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
