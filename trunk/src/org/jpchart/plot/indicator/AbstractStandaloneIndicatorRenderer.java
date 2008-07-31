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

package org.jpchart.plot.indicator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.jpchart.plot.PlotFrame;

/**
 *
 * @author cfelde
 */
public abstract class AbstractStandaloneIndicatorRenderer implements StandaloneIndicatorRenderer {
    private BigDecimal upper = null;
    private BigDecimal lower = null;
    
    /**
     * This will draw the Y axis of this indicator.
     * 
     * @param g
     * @param plotFrame
     */
    protected void drawAxis(Graphics2D g, PlotFrame plotFrame) {
        int height = g.getClipBounds().height;
        int width = g.getClipBounds().width;
        int yAxisWidth = plotFrame.getYAxisWidth();
        
        g.setColor(Color.BLACK);
        g.drawLine(width-yAxisWidth-1, 0, width-yAxisWidth-1, height);
    }
    
    /**
     * Limits the upper and/or lower axis values. If any of the values are null,
     * getYForValue will return zero.
     * 
     * @param upper
     * @param lower
     */
    protected void setLimits(BigDecimal upper, BigDecimal lower) {
        this.upper = upper;
        this.lower = lower;
    }
    
    /**
     * Returns the Y pixel position for given value.
     * Note that Y = 0 is on the top.
     * 
     * @param value
     * @param height Total plot height
     * @return
     */
    protected int getYForValue(BigDecimal value, int height) {
        if (value == null || upper == null || lower == null || height <= 0)
            return 0;
        
        if (value.compareTo(upper) >= 0)
            return 0;   // Value bigger or equal to upper
        else if (value.compareTo(lower) <= 0)
            return height;  // Value less or equal to lower
        
        BigDecimal valueLower = value.subtract(lower);
        BigDecimal upperLower = upper.subtract(lower);
        BigDecimal aspect = valueLower.divide(upperLower, RoundingMode.HALF_EVEN);
        BigDecimal bigHeight = BigDecimal.valueOf(height);
        
        return bigHeight.subtract(aspect.multiply(bigHeight)).intValue();
    }
    
    /**
     * Draws a line with given color on given Y value. If showValue is true,
     * the value will be marked in the Y axis area. setLimits must have been used
     * with no null arguments prior to usage of this method.
     * 
     * @param g
     * @param plotFrame
     * @param value
     * @param color
     * @param showValue
     */
    protected void drawLine(Graphics2D g, PlotFrame plotFrame, BigDecimal value, Color color, boolean showValue) {
        int height = g.getClipBounds().height;
        int width = g.getClipBounds().width;
        int yAxisWidth = plotFrame.getYAxisWidth();
        
        int y = getYForValue(value, height);
        
        if (y <= 0 || y >= height)
            return; // Out of bounds
        
        // draw line
        g.setColor(color);
        g.drawLine(0, y, width-yAxisWidth-1, y);
        
        if (showValue) {
            int boxHeight = 20;
            
            int boxY = y - (boxHeight/2);
            if (boxY < 0)
                boxY = 0;
            else if (boxY > height - boxHeight)
                boxY = height - boxHeight;
            
            // draw box
            g.setColor(Color.BLACK);
            g.fillRect(width-yAxisWidth, boxY, yAxisWidth, boxHeight);
            
            // draw text
            g.setColor(Color.WHITE);
            g.drawString(value.round(new MathContext(5, RoundingMode.HALF_UP)).toString(), width-yAxisWidth+4, boxY+14);
        }
    }
}
