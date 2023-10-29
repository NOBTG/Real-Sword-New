package com.nobtg.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class GradientText extends JPanel {
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 46);
    private static final String TEXT = "Real Sword";
    private static final int COLOR_COUNT = 30;
    private Paint myPaint;
    private Color startColor = Color.GRAY;
    private Color endColor = Color.GRAY;
    public static float hue = 0;
    public int Width = 360;
    public int Height = 200;

    public GradientText() {
        setBackground(new Color(0, 0, 0, 0));
        setPreferredSize(new Dimension(Width, Height));
        Timer timer = new Timer(10, (e) -> SwingUtilities.invokeLater(() -> {
            hue += 0.01F;
            startColor = Color.getHSBColor(hue, 1, 1);
            endColor = Color.getHSBColor(hue + 16 * 0.01F, 1, 1);
            updatePaint();
        }));
        timer.start();
        updatePaint();
    }

    private void updatePaint() {
        float[] fractions = new float[COLOR_COUNT];
        Color[] colors = new Color[COLOR_COUNT];
        for (int i = 0; i < colors.length; i++) {
            fractions[i] = ((float) i) / COLOR_COUNT;
            float ratio = fractions[i];
            colors[i] = interpolateColor(startColor, endColor, ratio);
        }
        myPaint = new LinearGradientPaint(0, 0, Width, 0, fractions, colors);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Map<?, ?> desktopHints =
                (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        Graphics2D g2d = (Graphics2D) g;
        if (desktopHints != null)
            g2d.setRenderingHints(desktopHints);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setPaint(myPaint);
        g2.setFont(FONT);
        g2.drawString(TEXT, 20, 100);
    }

    private Color interpolateColor(Color startColor, Color endColor, float ratio) {
        int red = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * ratio);
        int green = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * ratio);
        int blue = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * ratio);
        return new Color(red, green, blue);
    }

    public static void createAndShowGui() {
        GradientText mainPanel = new GradientText();
        JFrame frame = new JFrame("");
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setType(JFrame.Type.UTILITY);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - frame.getWidth();
        int y = 0;
        frame.setLocation(x, y);
        frame.setVisible(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0);
        frame.setIconImage(image);
        frame.setAlwaysOnTop(true);
    }
}