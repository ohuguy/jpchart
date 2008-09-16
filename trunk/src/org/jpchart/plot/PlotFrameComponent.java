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

import org.jpchart.plot.indicator.InlineIndicatorRenderer;
import org.jpchart.plot.indicator.StandaloneIndicatorRenderer;
import org.jpchart.plot.price.PricePlotRenderer;
import org.jpchart.data.MarketData;
import org.jpchart.market.Market;
import org.jpchart.time.TimeUnit;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;

/**
 *
 * @author cfelde
 */
public class PlotFrameComponent extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener, PlotFrame {
    private final static int TIME_LINE_HEIGHT = 20;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    private Dimension dim = new Dimension(600, 400);
    private int lastHeight = dim.height;
    private int lastWidth = dim.width;
    private BufferedImage renderCache = null;
    
    private boolean mouseClickedState = false;
    private boolean mousePressedState = false;
    private boolean mouseEnteredState = false;
    private boolean mouseDraggedState = false;

    private int currentMouseX = 0;
    private int currentMouseY = 0;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    
    // Number of pixels used by the y-axis area (to the right of the graph)
    private int yAxisWidth = 60;
    
    // Height of price plot
    private int pricePlotHeight = (int) (dim.height * 0.75);
    
    private static int HEIGHT_RESIZE_NONE = -2;
    private static int HEIGHT_RESIZE_PRICEPLOT = -1;
    private int currentHeightResize = HEIGHT_RESIZE_NONE;
    
    // Price plot renderer
    private PricePlotRenderer pricePlotRenderer = null;
    
    // Inline pre price indictors
    private ArrayList<InlineIndicatorRenderer> inlinePrePriceIndicators = new ArrayList<InlineIndicatorRenderer>();
    // Inline post price indicators
    private ArrayList<InlineIndicatorRenderer> inlinePostPriceIndicators = new ArrayList<InlineIndicatorRenderer>();
    // Standalone indicators
    private ArrayList<StandaloneIndicatorRenderer> standaloneIndicators = new ArrayList<StandaloneIndicatorRenderer>();
    
    // Mouse drag state
    private static int DRAG_NONE = 0;
    private static int DRAG_HEIGHT = 1;
    private static int DRAG_PRICE_PLOT = 2;
    private static int DRAG_PRICE_Y = 3;
    private int currentMouseDrag = DRAG_NONE;
    
    // Market data and view
    public static int BAR_TYPE_OHLC = 0;
    public static int BAR_TYPE_OHL = 1;
    public static int BAR_TYPE_HLC = 2;
    public static int BAR_TYPE_CLOSE = 3;
    public static int BAR_TYPE_CANDLESTICK = 4;
    
    private MarketData dataSource = null;
    private Market firstMarket = null, lastMarket = null;
    private Map<Long, Integer> xCacheForTime = new HashMap<Long, Integer>();
    private Map<Integer, TimeUnit> timeCacheForX = new HashMap<Integer, TimeUnit>();
    
    // Position of the last bar, relative to open price
    private int lastBarOpenX = dim.width - yAxisWidth - 10;
    private int lastBarOpenY = pricePlotHeight/2;
    private BigDecimal pricePixel = null;
    private int barWidth = 5; // 5
    
    private int barType = BAR_TYPE_OHLC;
    
    public PlotFrameComponent() {
        setPreferredSize(dim);
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }
    
    public PlotFrameComponent(int width, int height) {
        dim.setSize(width, height);
        setPreferredSize(dim);
        lastHeight = dim.height;
        lastWidth = dim.width;
        pricePlotHeight = (int) (dim.height * 0.75);
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }
    
    public PlotFrameComponent(Dimension dim) {
        this.dim = dim;
        setPreferredSize(dim);
        lastHeight = dim.height;
        lastWidth = dim.width;
        pricePlotHeight = (int) (dim.height * 0.75);
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }
    
    public void setMarketData(MarketData dataSource, Market lastMarket) throws Exception {
        this.dataSource = dataSource;
        this.lastMarket = lastMarket;
        this.firstMarket = dataSource.getFirst(lastMarket.getTicker());
        
        // Initialize plot/price valiables
        try {
            // Get number of visible bars
            int visibleBars = (dim.width-yAxisWidth)/barWidth;
            
            // Get max and min price for visible bars, for initial y axis range
            BigDecimal max = new BigDecimal(Long.MIN_VALUE);
            BigDecimal min = new BigDecimal(Long.MAX_VALUE);
            Market currentMarket = lastMarket;
            for (int x = 0; x < visibleBars && currentMarket != null; x++) {
                if (max.compareTo(currentMarket.getHighPrice()) < 0)
                    max = currentMarket.getHighPrice();
                if (min.compareTo(currentMarket.getLowPrice()) > 0)
                    min = currentMarket.getLowPrice();
                currentMarket = currentMarket.getPrevious();
            }
            // Calculate pixelPrice
            pricePixel = max.subtract(min).divide(new BigDecimal(pricePlotHeight), RoundingMode.HALF_UP);
            
            // Calculate lastBarOpenY and lastBarOpenX
            BigDecimal openMinDiff = lastMarket.getOpenPrice().subtract(min);
            lastBarOpenY = openMinDiff.divide(pricePixel, RoundingMode.HALF_DOWN).intValue()+10;
            lastBarOpenX = dim.width-getYAxisWidth()-1-barWidth-10;
        }
        catch (Exception e) {
            this.dataSource = null;
            this.lastMarket = null;
        }
        finally {
            repaint();
        }
    }

    public void mouseClicked(MouseEvent e) {
        mouseClickedState = true;
        mouseEventHandler(e);
    }

    public void mousePressed(MouseEvent e) {
        mousePressedState = true;
        mouseEventHandler(e);
    }

    public void mouseReleased(MouseEvent e) {
        mouseClickedState = false;
        mouseDraggedState = false;
        mousePressedState = false;
        mouseEventHandler(e);
    }

    public void mouseEntered(MouseEvent e) {
        mouseEnteredState = true;
        mouseEventHandler(e);
    }

    public void mouseExited(MouseEvent e) {
        mouseEnteredState = false;
        mouseEventHandler(e);
    }

    public void mouseDragged(MouseEvent e) {
        mouseDraggedState = true;
        mouseEventHandler(e);
    }

    public void mouseMoved(MouseEvent e) {
        mouseEventHandler(e);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        boolean doRepaint = false;
        
        if (isOverPricePlot(e)) {
            if (e.getUnitsToScroll()/Math.abs(e.getUnitsToScroll()) > 0) {
                barWidth++;
                
                try {
                    TimeUnit timeOnX = getTimeForX(e.getX());
                    int between = dataSource.getTicksBetween(lastMarket.getTicker(), timeOnX, lastMarket.getMarketTime());
                    lastBarOpenX += between;
                }
                catch (Exception ex) {}
            }
            else if (barWidth > 3) {
                barWidth--;
                try {
                    TimeUnit timeOnX = getTimeForX(e.getX());
                    int between = dataSource.getTicksBetween(lastMarket.getTicker(), timeOnX, lastMarket.getMarketTime());
                    lastBarOpenX -= between;
                }
                catch (Exception ex) {}
            }
            
            
            doRepaint = true;
        }
        else if (isOverPricePlotYAxis(e)) {
            double adjust = pricePixel.doubleValue()*0.05*(e.getUnitsToScroll()/Math.abs(e.getUnitsToScroll()));
            // NOTE dirty hack, make this better..
            if (Math.abs(adjust) < 0.005)
                adjust = 0.005*(adjust/Math.abs(adjust));
            pricePixel = pricePixel.multiply(new BigDecimal(1+adjust), MathContext.DECIMAL32);
            
            doRepaint = true;
        }
        
        if (doRepaint) {
            renderCache = null;
            repaint();
        }
    }
    
    public boolean isMouseClicked() {
        return mouseClickedState;
    }

    public boolean isMouseDragged() {
        return mouseDraggedState;
    }

    public boolean isMouseEntered() {
        return mouseEnteredState;
    }

    public boolean isMousePressed() {
        return mousePressedState;
    }

    public boolean isMouseReleased() {
        return !(mouseClickedState || mouseDraggedState || mousePressedState);
    }
    
    public int getYAxisWidth() {
        return yAxisWidth;
    }
    
    /**
     * Returns the X pixel position where X = 0 is to the left.
     * 
     * @param time
     * @return X pixel position
     */
    public int getXForTime(TimeUnit time) {
        // Check cache
        if (xCacheForTime.containsKey(time.getTime().getTimeInMillis()))
            return lastBarOpenX - (barWidth*xCacheForTime.get(time.getTime().getTimeInMillis()));
        
        String ticker = lastMarket.getTicker();
        TimeUnit end = lastMarket.getMarketTime();
        
        try {
            int x = dataSource.getTicksBetween(ticker, time, end);
            
            // Add to/update cache
            xCacheForTime.put(time.getTime().getTimeInMillis(), x);
            
            return lastBarOpenX - (barWidth*x);
        }
        catch (Exception e) {
            return 0;
        }
    }

    /**
     * Returns the time at given X. If X is not within the visible area,
     * null is returned.
     * 
     * @param x
     * @return Time or null if not available
     */
    public TimeUnit getTimeForX(int x) {
        if (x < 0 || x > dim.width - getYAxisWidth() - 1)
            return null;
        
        int between = (x - lastBarOpenX)/-barWidth;
        
        if (timeCacheForX.containsKey(between))
            return timeCacheForX.get(between);
        
        
        TimeUnit timeForX = lastMarket.getMarketTime();
        TimeUnit useThisTime = timeForX;
        int lastX = Integer.MIN_VALUE;
        long firstTime = firstMarket.getMarketTime().getTime().getTimeInMillis();
        while (x < getXForTime(timeForX) && timeForX.getTime().getTimeInMillis() > firstTime) {
            timeForX = timeForX.getSubOne();
            if (getXForTime(timeForX) != lastX) {
                lastX = getXForTime(timeForX);
                useThisTime = timeForX;
            }
        }
        
        timeCacheForX.put(between, useThisTime);
        return useThisTime;
    }
    
    /**
     * Returns the Y pixel position where Y = 0 is the top
     * and Y = pricePlotHeight is the bottom.
     * 
     * @param price
     * @return Y pixel position
     */
    public int getYForPrice(BigDecimal price) {
        BigDecimal diff = price.subtract(lastMarket.getOpenPrice());
        return pricePlotHeight - diff.divide(pricePixel, MathContext.DECIMAL128).intValue() - lastBarOpenY;
    }
    
    public BigDecimal getPriceForY(int y) {
        BigDecimal negative_pPy = pricePixel.multiply(BigDecimal.valueOf(y), MathContext.DECIMAL128).multiply(BigDecimal.valueOf(-1), MathContext.DECIMAL128);
        BigDecimal pPpPH = pricePixel.multiply(BigDecimal.valueOf(pricePlotHeight), MathContext.DECIMAL128);
        BigDecimal negative_pPlBOY = pricePixel.multiply(BigDecimal.valueOf(lastBarOpenY), MathContext.DECIMAL128).multiply(BigDecimal.valueOf(-1), MathContext.DECIMAL128);
        
        return negative_pPy.add(pPpPH).add(negative_pPlBOY).add(lastMarket.getOpenPrice());
    }
    
    public int getPricePlotHeight() {
        return pricePlotHeight;
    }

    public int getBarWidth() {
        return barWidth;
    }
    
    private void mouseEventHandler(MouseEvent e) {
        boolean doRepaint = false;
        
        if (isMouseReleased()) {
            currentHeightResize = HEIGHT_RESIZE_NONE;
            
            if (isOverResizeLine(e)) {
                currentMouseDrag = DRAG_HEIGHT;
                setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            }
            else if (isOverPricePlotYAxis(e)) {
                currentMouseDrag = DRAG_PRICE_Y;
                setCursor(Cursor.getDefaultCursor());
            }
            else if (isOverPricePlot(e)) {
                currentMouseDrag = DRAG_PRICE_PLOT;
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }
            else {
                currentMouseDrag = DRAG_NONE;
                setCursor(Cursor.getDefaultCursor());
            }
        }
        else if (isMouseDragged()) {
            if (currentMouseDrag == DRAG_HEIGHT) {
                if (currentHeightResize == HEIGHT_RESIZE_PRICEPLOT) {
                    int lastPricePlotHeight = pricePlotHeight;
                    pricePlotHeight = e.getY() - TIME_LINE_HEIGHT;
                    
                    // Update pricePixel
                    double aspect = ((double)pricePlotHeight)/(double)lastPricePlotHeight;
                    pricePixel = pricePixel.divide(new BigDecimal(aspect), RoundingMode.HALF_EVEN);
                    
                    doRepaint = true;
                }
            }
            else if (currentMouseDrag == DRAG_PRICE_PLOT) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                
                lastBarOpenX += e.getX()-lastMouseX;
                lastBarOpenY += lastMouseY-e.getY();
                
                doRepaint = true;
            }
            else if (currentMouseDrag == DRAG_PRICE_Y) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                
                lastBarOpenY += lastMouseY-e.getY();
                
                doRepaint = true;
            }
        }
        
        lastMouseX = currentMouseX;
        lastMouseY = currentMouseY;
        currentMouseX = e.getX();
        currentMouseY = e.getY();
        mouseClickedState = false;
        
        if (doRepaint || true) repaint();
    }

    private boolean isOverResizeLine(MouseEvent e) {
        if (e.getY() >= pricePlotHeight + TIME_LINE_HEIGHT - 1 && e.getY() <= pricePlotHeight + TIME_LINE_HEIGHT + 1) {
            currentHeightResize = HEIGHT_RESIZE_PRICEPLOT;
            return true;
        }
        else {
            currentHeightResize = HEIGHT_RESIZE_NONE;
            return false;
        }
    }
    
    private boolean isOverPricePlot(MouseEvent e) {
        return isOverPricePlot(e.getX(), e.getY());
    }
    
    private boolean isOverPricePlot(int x, int y) {
        if (x > 0 && x < dim.width-yAxisWidth && y > 0 && y < pricePlotHeight)
            return true;
        else
            return false;
    }
    
    private boolean isOverPricePlotYAxis(MouseEvent e) {
        if (e.getX() > dim.width-yAxisWidth && e.getX() <= dim.width && e.getY() > 0 && e.getY() < pricePlotHeight)
            return true;
        else
            return false;
    }
    
    @Override
    public void paint(Graphics g) {
        getSize(dim);
        
        if (updatePositions() || renderCache == null || !isMouseReleased()) {
            // Update cache
            renderCache = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = renderCache.createGraphics();
            
            // Fill with background color
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, dim.width, dim.height);

            // Draw pre price inline indicators
            drawPrePriceIndicators(g2d);

            // Draw price plot area
            drawPricePlot(g2d);

            // Draw post price inline indicators
            drawPostPriceIndicators(g2d);

            drawPriceYAxis(g2d);

            drawPriceXAxis(g2d);

            // Draw standalone indicators
            drawStandaloneIndicators(g2d);
        }
        
        Graphics2D g2d = (Graphics2D) g;
        
        g.drawImage(renderCache, 0, 0, null);
        
        if (isMouseReleased() && isOverPricePlot(currentMouseX, currentMouseY))
            drawPointerCross(g2d);
        
        // Draw resize lines
        drawResizeLines(g2d);
    }
    
    /**
     * This will update all positions, which might have changed due to
     * resizing and such.
     * 
     * Returns true if update was needed
     * 
     * @return True if update was needed
     */
    private boolean updatePositions() {
        boolean updated = false;
        
        if (dim.height != lastHeight) {
            // Hight has changed
            // Update price plot height
            double aspect = ((double)pricePlotHeight)/(double)lastHeight;
            int lastPricePlotHeight = pricePlotHeight;
            pricePlotHeight = (int) (dim.height * aspect);
            
            // Update pricePixel
            aspect = ((double)pricePlotHeight)/(double)lastPricePlotHeight;
            pricePixel = pricePixel.divide(new BigDecimal(aspect), RoundingMode.HALF_EVEN);
            
            updated = true;
        }
        
        if (dim.width != lastWidth) {
            lastBarOpenX += dim.width - lastWidth;
            
            updated = true;
        }
        
        lastHeight = dim.height;
        lastWidth = dim.width;
        
        return updated;
    }
    
    private void drawResizeLines(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.drawLine(0, pricePlotHeight + TIME_LINE_HEIGHT, dim.width, pricePlotHeight + TIME_LINE_HEIGHT);
    }
    
    private void drawPrePriceIndicators(Graphics2D g) {
        if (dataSource == null || lastMarket == null || inlinePrePriceIndicators.size() == 0)
            return; // Nothing to do
        
        int plotHeight = pricePlotHeight-1;
        int plotWidth = dim.width - getYAxisWidth() - 1;
        
        Graphics2D gArea = (Graphics2D) g.create(0, 0, plotWidth, plotHeight);
        
        for (InlineIndicatorRenderer indicatorRenderer : inlinePrePriceIndicators)
            indicatorRenderer.paintInline(gArea, this);
    }
    
    private void drawPricePlot(Graphics2D g) {
        int plotHeight = pricePlotHeight-1;
        int plotWidth = dim.width - getYAxisWidth() - 1;
        
        Graphics2D gArea = (Graphics2D) g.create(0, 0, plotWidth, plotHeight);
        
        if (dataSource == null || lastMarket == null) {
            if (plotHeight > 50 && plotWidth > 150)
                g.drawString("No price data available", 10, 40);
        }
        else if (pricePlotRenderer == null) {
            if (plotHeight > 50 && plotWidth > 150)
                g.drawString("No price plot renderer available", 10, 40);
        }
        else {
            pricePlotRenderer.paint(gArea, this);
            
            if (plotHeight > 30 && plotWidth > 150) {
                g.setColor(Color.BLACK);
                g.drawString(lastMarket.getTicker().toUpperCase() + " (" + lastMarket.getMarketTime().getResolutionDescription().toLowerCase() + ")", 5, 15);
            }
        }
    }
    
    private void drawPostPriceIndicators(Graphics2D g) {
        if (dataSource == null || lastMarket == null || inlinePostPriceIndicators.size() == 0)
            return; // Nothing to do
        
        int plotHeight = pricePlotHeight-1;
        int plotWidth = dim.width - getYAxisWidth() - 1;
        
        Graphics2D gArea = (Graphics2D) g.create(0, 0, plotWidth, plotHeight);
        
        for (InlineIndicatorRenderer indicatorRenderer : inlinePostPriceIndicators)
            indicatorRenderer.paintInline(gArea, this);
    }
    
    private void drawPointerCross(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.drawLine(0, currentMouseY, dim.width - getYAxisWidth() - 1, currentMouseY);
        //g.drawLine(currentMouseX, 0, currentMouseX, pricePlotHeight);
        g.drawLine(currentMouseX, 0, currentMouseX, dim.height);
        
        int priceBoxHeight = 20;
        int priceBoxWidth = 120;
        int currentPriceBoxY = currentMouseY - (priceBoxHeight/2);
        int currentPriceBoxX = currentMouseX - (priceBoxWidth/2);
        
        if (currentPriceBoxY < 0)
            currentPriceBoxY = 0;
        else if (currentPriceBoxY > pricePlotHeight - priceBoxHeight)
            currentPriceBoxY = pricePlotHeight - priceBoxHeight;
        
        if (currentPriceBoxX < 0)
            currentPriceBoxX = 0;
        else if (currentPriceBoxX > dim.width - getYAxisWidth() - 1 - priceBoxWidth)
            currentPriceBoxX = dim.width - getYAxisWidth() - 1 - priceBoxWidth;
        
        // Y
        g.fillRect(dim.width-getYAxisWidth(),  currentPriceBoxY, getYAxisWidth(), priceBoxHeight);
        // X
        g.fillRect(currentPriceBoxX, pricePlotHeight, priceBoxWidth, TIME_LINE_HEIGHT);
        
        int priceBoxTextY = currentPriceBoxY + 14;
        g.setColor(Color.WHITE);
        g.drawString(getPriceForY(currentMouseY).round(new MathContext(5, RoundingMode.HALF_UP)).toString(), dim.width - getYAxisWidth() + 4, priceBoxTextY);
        
        int priceBoxTextX = currentPriceBoxX + 1;
        if (getTimeForX(currentMouseX) != null)
            g.drawString(dateFormat.format(getTimeForX(currentMouseX).getTime().getTime()), priceBoxTextX, pricePlotHeight + 16);
    }
    
    private void drawPriceYAxis(Graphics2D g) {
        int plotHeight = pricePlotHeight-1;
        int plotWidth = dim.width - getYAxisWidth() - 1;
        
        g.setColor(Color.BLACK);
        g.drawLine(plotWidth, 0, plotWidth, plotHeight);
        
        if (pricePlotHeight < 40) return;
        
        g.drawLine(plotWidth, 10, plotWidth+3, 10);
        g.drawLine(plotWidth, pricePlotHeight-10, plotWidth+3, pricePlotHeight-10);
        g.drawString(getPriceForY(10).round(new MathContext(5, RoundingMode.HALF_UP)).toString(), plotWidth + 4, 10+4);
        g.drawString(getPriceForY(pricePlotHeight-10).round(new MathContext(5, RoundingMode.HALF_UP)).toString(), plotWidth + 4, pricePlotHeight-10+4);
    }
    
    private void drawPriceXAxis(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.drawLine(0, pricePlotHeight, dim.width - getYAxisWidth() - 1, pricePlotHeight);
        
        // Draw taps
        g.drawLine(10, pricePlotHeight, 10, pricePlotHeight + 4);
        g.drawLine(dim.width - getYAxisWidth() - 1 - 10, pricePlotHeight, dim.width - getYAxisWidth() - 1 - 10, pricePlotHeight + 4);
        
        // Draw first and last visible time
        TimeUnit firstTime = getTimeForX(10);
        TimeUnit lastTime = getTimeForX(dim.width - getYAxisWidth() - 1 - 10);
        if (firstTime == null || lastTime == null)
            return;
        
        // Format time
        String firstFormat = dateFormat.format(firstTime.getTime().getTime());
        String lastFormat = dateFormat.format(lastTime.getTime().getTime());
        g.drawString(firstFormat, 10, pricePlotHeight + 16);
        g.drawString(lastFormat, dim.width - getYAxisWidth() - 1 - 126, pricePlotHeight + 16);
    }
    

    private void drawStandaloneIndicators(Graphics2D g2d) {
        // TODO Add suppoert for several renderes with resize lines between..
        
        int top = pricePlotHeight + TIME_LINE_HEIGHT + 1;
        Graphics2D gArea = (Graphics2D) g2d.create(0, top, dim.width, dim.height - top);
        
        if (standaloneIndicators.size() > 0) {
            StandaloneIndicatorRenderer renderer = standaloneIndicators.get(0);
            renderer.paintStandalone(gArea, this);
        }
    }

    public void setPricePlotRenderer(PricePlotRenderer renderer) {
        this.pricePlotRenderer = renderer;
        
        repaint();
    }

    public MarketData getDataSource() {
        return dataSource;
    }

    public Market getLastMarket() {
        return lastMarket;
    }

    public void addInlineIndicator(InlineIndicatorRenderer indicator, boolean prePrice) {
        if (prePrice)
            inlinePrePriceIndicators.add(indicator);
        else
            inlinePostPriceIndicators.add(indicator);
        
        repaint();
    }

    public void addStandalineIndicator(StandaloneIndicatorRenderer indicator) {
        standaloneIndicators.add(indicator);
        
        repaint();
    }
}
