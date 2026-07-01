package util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsChart extends JComponent {
    public static final int TYPE_BAR = 0;
    public static final int TYPE_LINE = 1;
    public static final int TYPE_PIE = 2;

    private final int chartType;
    
    // Data structures
    private Map<String, Double> doubleData = new HashMap<>();
    private Map<String, Integer> intData = new HashMap<>();
    private String title = "";

    // Design details
    private static final Color[] PIE_COLORS = {
            new Color(59, 130, 246),   // Blue
            new Color(16, 185, 129),   // Emerald
            new Color(245, 158, 11),   // Amber
            new Color(139, 92, 246),   // Violet
            new Color(236, 72, 153),   // Pink
            new Color(239, 68, 68)     // Red
    };

    public AnalyticsChart(int type) {
        this.chartType = type;
        setPreferredSize(new Dimension(350, 220));
    }

    public void setData(Map<String, Double> data) {
        this.doubleData = data != null ? data : new HashMap<>();
        repaint();
    }

    public void setIntData(Map<String, Integer> data) {
        this.intData = data != null ? data : new HashMap<>();
        repaint();
    }

    public void setTitle(String title) {
        this.title = title;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background card outline
        g2.setColor(UIComponents.COLOR_CARD);
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
        
        // Draw title
        if (title != null && !title.isEmpty()) {
            g2.setColor(UIComponents.COLOR_TEXT_MAIN);
            g2.setFont(UIComponents.FONT_BOLD);
            g2.drawString(title, 20, 25);
        }

        switch (chartType) {
            case TYPE_BAR:
                paintBarChart(g2, w, h);
                break;
            case TYPE_LINE:
                paintLineChart(g2, w, h);
                break;
            case TYPE_PIE:
                paintPieChart(g2, w, h);
                break;
        }
    }

    private void paintBarChart(Graphics2D g2, int w, int h) {
        if (doubleData.isEmpty()) {
            drawNoData(g2, w, h);
            return;
        }

        int paddingLeft = 45;
        int paddingBottom = 40;
        int paddingTop = 40;
        int paddingRight = 20;

        int graphW = w - paddingLeft - paddingRight;
        int graphH = h - paddingTop - paddingBottom;

        // Find max value to scale chart (cap at 100 max)
        double maxVal = 100.0;
        for (double val : doubleData.values()) {
            if (val > maxVal) maxVal = val;
        }

        // Draw horizontal grid lines and labels
        g2.setFont(UIComponents.FONT_SMALL);
        g2.setStroke(new BasicStroke(1.0f));
        int divisions = 4;
        for (int i = 0; i <= divisions; i++) {
            double gridVal = maxVal * i / divisions;
            int y = paddingTop + graphH - (int) (gridVal / maxVal * graphH);
            
            // Gridline
            g2.setColor(new Color(255, 255, 255, 10)); // very subtle gridline
            g2.drawLine(paddingLeft, y, w - paddingRight, y);

            // Text tag
            g2.setColor(UIComponents.COLOR_TEXT_MUTED);
            g2.drawString(String.format("%.0f%%", gridVal), 10, y + 4);
        }

        // Draw bars
        List<String> keys = new ArrayList<>(doubleData.keySet());
        int numBars = keys.size();
        if (numBars == 0) return;

        int barSpace = graphW / numBars;
        int barW = (int) (barSpace * 0.55); // Width of bar relative to space

        for (int i = 0; i < numBars; i++) {
            String key = keys.get(i);
            double val = doubleData.get(key);

            int x = paddingLeft + (i * barSpace) + (barSpace - barW) / 2;
            int barHeight = (int) (val / maxVal * graphH);
            int y = paddingTop + graphH - barHeight;

            // Draw bar with beautiful gradient
            GradientPaint gp = new GradientPaint(x, y, UIComponents.COLOR_ACCENT, x, y + barHeight, new Color(30, 58, 138));
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(x, y, barW, barHeight, 6, 6));

            // Draw value above bar
            g2.setColor(UIComponents.COLOR_TEXT_SEC);
            g2.drawString(String.format("%.1f", val), x + (barW - g2.getFontMetrics().stringWidth(String.format("%.1f", val))) / 2, y - 6);

            // Draw label below bar (clipped/shortened if too long)
            g2.setColor(UIComponents.COLOR_TEXT_SEC);
            String displayKey = key.length() > 6 ? key.substring(0, 5) + "." : key;
            int textX = x + (barW - g2.getFontMetrics().stringWidth(displayKey)) / 2;
            g2.drawString(displayKey, textX, paddingTop + graphH + 18);
        }
    }

    private void paintLineChart(Graphics2D g2, int w, int h) {
        if (doubleData.isEmpty()) {
            drawNoData(g2, w, h);
            return;
        }

        int paddingLeft = 45;
        int paddingBottom = 40;
        int paddingTop = 40;
        int paddingRight = 20;

        int graphW = w - paddingLeft - paddingRight;
        int graphH = h - paddingTop - paddingBottom;

        double maxVal = 100.0;
        for (double val : doubleData.values()) {
            if (val > maxVal) maxVal = val;
        }

        // Draw horizontal grid lines
        g2.setFont(UIComponents.FONT_SMALL);
        for (int i = 0; i <= 4; i++) {
            double gridVal = maxVal * i / 4.0;
            int y = paddingTop + graphH - (int) (gridVal / maxVal * graphH);
            
            g2.setColor(new Color(255, 255, 255, 10));
            g2.drawLine(paddingLeft, y, w - paddingRight, y);

            g2.setColor(UIComponents.COLOR_TEXT_MUTED);
            g2.drawString(String.format("%.0f%%", gridVal), 10, y + 4);
        }

        List<String> keys = new ArrayList<>(doubleData.keySet());
        int numPoints = keys.size();
        if (numPoints < 2) {
            // Draw a single point or simple bar
            paintBarChart(g2, w, h);
            return;
        }

        int stepX = graphW / (numPoints - 1);
        List<Point> points = new ArrayList<>();

        for (int i = 0; i < numPoints; i++) {
            String key = keys.get(i);
            double val = doubleData.get(key);
            int x = paddingLeft + (i * stepX);
            int y = paddingTop + graphH - (int) (val / maxVal * graphH);
            points.add(new Point(x, y));

            // Draw label below
            g2.setColor(UIComponents.COLOR_TEXT_SEC);
            int textX = x - g2.getFontMetrics().stringWidth(key) / 2;
            g2.drawString(key, textX, paddingTop + graphH + 20);
        }

        // Draw connecting line
        g2.setColor(UIComponents.COLOR_SUCCESS);
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < points.size() - 1; i++) {
            g2.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
        }

        // Draw dots at vertices
        g2.setStroke(new BasicStroke(2.0f));
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            
            // Outer glowing dot
            g2.setColor(UIComponents.COLOR_CARD);
            g2.fillOval(p.x - 6, p.y - 6, 12, 12);
            
            g2.setColor(UIComponents.COLOR_SUCCESS);
            g2.drawOval(p.x - 6, p.y - 6, 12, 12);
            g2.fillOval(p.x - 3, p.y - 3, 6, 6);

            // Display value
            g2.setColor(UIComponents.COLOR_TEXT_MAIN);
            g2.setFont(UIComponents.FONT_SMALL);
            String valStr = String.format("%.0f", doubleData.get(keys.get(i)));
            g2.drawString(valStr, p.x - g2.getFontMetrics().stringWidth(valStr) / 2, p.y - 10);
        }
    }

    private void paintPieChart(Graphics2D g2, int w, int h) {
        if (intData.isEmpty()) {
            drawNoData(g2, w, h);
            return;
        }

        // Sum values
        double total = 0;
        for (int val : intData.values()) {
            total += val;
        }

        if (total == 0) {
            drawNoData(g2, w, h);
            return;
        }

        // Draw pie slices (Donut Chart)
        int size = Math.min(w, h) - 70;
        int pieX = 25;
        int pieY = 45;

        double curAngle = 90.0; // Start at top
        int colorIdx = 0;

        List<String> keys = new ArrayList<>(intData.keySet());
        
        // Draw Legend
        int legendX = pieX + size + 20;
        int legendY = 55;
        g2.setFont(UIComponents.FONT_SMALL);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            int val = intData.get(key);
            double angle = (val / total) * 360.0;

            Color c = PIE_COLORS[colorIdx % PIE_COLORS.length];
            g2.setColor(c);
            
            // Draw slice
            g2.fill(new Arc2D.Double(pieX, pieY, size, size, curAngle, -angle, Arc2D.PIE));
            
            // Draw Legend dot and text
            g2.fillOval(legendX, legendY + (i * 22), 8, 8);
            
            g2.setColor(UIComponents.COLOR_TEXT_SEC);
            String label = String.format("%s: %d (%.0f%%)", key, val, (val / total) * 100);
            g2.drawString(label, legendX + 15, legendY + (i * 22) + 8);

            curAngle -= angle;
            colorIdx++;
        }

        // Draw center circle to convert into a Donut Chart
        int donutSize = (int) (size * 0.55);
        int donutOffset = (size - donutSize) / 2;
        g2.setColor(UIComponents.COLOR_CARD);
        g2.fillOval(pieX + donutOffset, pieY + donutOffset, donutSize, donutSize);

        // Draw total text in center
        g2.setColor(UIComponents.COLOR_TEXT_MUTED);
        g2.setFont(UIComponents.FONT_SMALL);
        g2.drawString("TOTAL", pieX + size/2 - g2.getFontMetrics().stringWidth("TOTAL")/2, pieY + size/2 - 5);
        
        g2.setColor(UIComponents.COLOR_TEXT_MAIN);
        g2.setFont(UIComponents.FONT_BOLD);
        String totalStr = String.valueOf((int)total);
        g2.drawString(totalStr, pieX + size/2 - g2.getFontMetrics().stringWidth(totalStr)/2, pieY + size/2 + 14);
    }

    private void drawNoData(Graphics2D g2, int w, int h) {
        g2.setColor(UIComponents.COLOR_TEXT_MUTED);
        g2.setFont(UIComponents.FONT_BODY);
        String txt = "No data entries available";
        g2.drawString(txt, (w - g2.getFontMetrics().stringWidth(txt)) / 2, h / 2 + 10);
    }
}
