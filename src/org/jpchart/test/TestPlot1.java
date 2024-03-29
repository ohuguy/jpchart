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

package org.jpchart.test;

import org.jpchart.plot.*;
import org.jpchart.data.MarketData;
import org.jpchart.market.Market;
import org.jpchart.plot.price.renderer.CandlestickPriceRenderer;
import org.jpchart.plot.price.renderer.LinePriceRenderer;
import org.jpchart.plot.price.renderer.MultiPriceRenderer;
import org.jpchart.plot.price.renderer.OHLCPriceRenderer;
import java.math.BigDecimal;
import javax.swing.UIManager;
import org.jpchart.data.MarketDataMem;
import org.jpchart.data.MarketDataYahoo;
import org.jpchart.indicator.CutlerRSI;
import org.jpchart.indicator.SimpleDeltaMovingAverage;
import org.jpchart.indicator.SimpleMovingAverage;
import org.jpchart.indicator.Volume;
import org.jpchart.plot.indicator.renderer.InlineLineIndicatorRenderer;
import org.jpchart.plot.indicator.renderer.StandaloneLineIndicatorRenderer;

/**
 *
 * @author  cfelde
 */
public class TestPlot1 extends javax.swing.JFrame {

    /** Creates new form TestPlot1 */
    public TestPlot1() {
        try {
            // Set look and feel to the OS we are running on..
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            initComponents();
            PlotFrame plot = new PlotFrameComponent();
            setContentPane((PlotFrameComponent)plot);
            setTitle("JPChart demo - www.jpchart.org");
            validate();
            pack();
            setLocationRelativeTo(null);
            
            String ticker = "goog";
            
            // Use MarketDataMem as a wrapped around cache, since it's faster for selected ticker.
            MarketData dataSource = new MarketDataMem(new MarketDataYahoo(), ticker);
            Market lastMarket = dataSource.getLast(ticker);
            plot.setMarketData(dataSource, lastMarket);
            
            //plot.setPricePlotRenderer(new OHLCPriceRenderer(OHLCPriceRenderer.PlotTypes.OHLC, true));
            //plot.setPricePlotRenderer(new CandlestickPriceRenderer());
            MultiPriceRenderer multiRenderer = new MultiPriceRenderer();
            multiRenderer.addRenderer(new CandlestickPriceRenderer());
            //multiRenderer.addRenderer(new OHLCPriceRenderer(OHLCPriceRenderer.PlotTypes.OHLC, false));
            //multiRenderer.addRenderer(new LinePriceRenderer(LinePriceRenderer.PlotTypes.CLOSE, false));
            //multiRenderer.addRenderer(new LinePriceRenderer(LinePriceRenderer.PlotTypes.HIGH, false));
            //multiRenderer.addRenderer(new LinePriceRenderer(LinePriceRenderer.PlotTypes.LOW, false));
            plot.setPricePlotRenderer(multiRenderer);
            
            // Indicators
            plot.addInlineIndicator(
                new InlineLineIndicatorRenderer(
                    new SimpleMovingAverage(14, SimpleMovingAverage.UsePrice.CLOSE)
                ), false
            );
            
            plot.addStandalineIndicator(
                new StandaloneLineIndicatorRenderer(
                    //new SimpleMovingAverage(14, SimpleMovingAverage.UsePrice.CLOSE)
                    new CutlerRSI(14)
                    //new SimpleDeltaMovingAverage(14, SimpleMovingAverage.UsePrice.CLOSE)
                )
            );
            
            // Test priceForY
            System.out.println("Last market info:");
            System.out.println("\topen:\t" + lastMarket.getOpenPrice());
            System.out.println("\tclose:\t" + lastMarket.getClosePrice());
            System.out.println();
            //for (int x = 0; x < plot.getPricePlotHeight(); x++) {
            //    System.out.println("Price for " + x + ":\t" + plot.getPriceForY(x));
            //}
            System.out.println("Y for price:");
            for (int x = 10; x <= 100; x += 10) {
                System.out.println("Y for " + x + " = " + plot.getYForPrice(BigDecimal.valueOf(x)));
                System.out.println("\tand back to price: " + plot.getPriceForY(plot.getYForPrice(BigDecimal.valueOf(x))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 611, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 405, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TestPlot1().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
