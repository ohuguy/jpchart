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

package org.jpchart.plot;

import org.jpchart.plot.price.PricePlotRenderer;
import org.jpchart.data.MarketData;
import org.jpchart.market.Market;
import org.jpchart.time.TimeUnit;
import java.math.BigDecimal;
import org.jpchart.plot.indicator.InlineIndicatorRenderer;
import org.jpchart.plot.indicator.StandaloneIndicatorRenderer;

/**
 *
 * @author cfelde
 */
public interface PlotFrame {
    /**
     * Gets the aproximate price at given Y, where Y = 0 is the top
     * 
     * @param y coordinate
     * @return Price for Y
     */
    BigDecimal getPriceForY(int y);
    
    /**
     * Returns the height of the price plot.
     * 
     * @return price plot height
     */
    int getPricePlotHeight();

    /**
     * Returns the time at given X. If X is not within the visible area,
     * null is returned.
     *
     * @param x
     * @return Time or null if not available
     */
    TimeUnit getTimeForX(int x);

    /**
     * Returns the X pixel position where X = 0 is to the left.
     *
     * @param time
     * @return X pixel position
     */
    int getXForTime(TimeUnit time);

    /**
     * Returns the width of the Y axis
     * 
     * @return Y axis width
     */
    int getYAxisWidth();

    /**
     * Returns the Y pixel position where Y = 0 is the top
     * and Y = pricePlotHeight is the bottom.
     *
     * @param price
     * @return Y pixel position
     */
    int getYForPrice(BigDecimal price);
    
    /**
     * Returns the width of the price bar
     * 
     * @return bar width
     */
    int getBarWidth();

    /**
     * Returns true if mouse is clicked
     * 
     * @return
     */
    boolean isMouseClicked();

    /**
     * Returns true if mouse is dragged
     * 
     * @return
     */
    boolean isMouseDragged();

    /**
     * Returns true if mouse has entered the area
     * 
     * @return
     */
    boolean isMouseEntered();

    /**
     * Returns true if the mouse button is pressed
     * 
     * @return
     */
    boolean isMousePressed();

    /**
     * Returns true if the mouse button is released
     * 
     * @return
     */
    boolean isMouseReleased();

    /**
     * Sets the data source and last market data for this plot frame.
     * 
     * @param dataSource
     * @param lastMarket
     * @throws java.lang.Exception
     */
    void setMarketData(MarketData dataSource, Market lastMarket) throws Exception;
    
    /**
     * Returns the data source used in this plot frame.
     * 
     * @return Data source
     */
    MarketData getDataSource();
    
    /**
     * Returns the last market data used in this plot frame.
     * 
     * @return Last market
     */
    Market getLastMarket();
    
    /**
     * Sets the price plot renderer used in this plot frame.
     * 
     * @param renderer
     */
    void setPricePlotRenderer(PricePlotRenderer renderer);
    
    /**
     * Add an inline rendered indicator.
     * 
     * @param indicator Indicator to add
     * @param prePrice If this is true, the indicator will be renderered
     *          prior to the price rendering (in the background)
     */
    void addInlineIndicator(InlineIndicatorRenderer indicator, boolean prePrice);
    
    /**
     * Add a standalone rendered indicator.
     * 
     * @param indicator Indicator to add
     */
    void addStandalineIndicator(StandaloneIndicatorRenderer indicator);
}
