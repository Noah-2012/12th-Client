package com.noadsch12.render;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;

public class MotionBlurOverlay extends JFrame {

    private static final int FRAME_DELAY_MS = 8;
    private static final int MAX_TRAIL_FRAMES = 3;
    private static final float[] ALPHA_VALUES = {0.20f, 0.13f, 0.07f};

    private ArrayDeque<BufferedImage> frameQueue;
    private Robot robot;
    private Timer captureTimer;
    private GraphicsDevice gd;
    private volatile boolean isCapturing = false;
    private Canvas canvas;

    public MotionBlurOverlay() throws AWTException {
        robot = new Robot();
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        frameQueue = new ArrayDeque<>(MAX_TRAIL_FRAMES);

        // Undecorated und transparent
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        setFocusable(false);
        setType(Window.Type.UTILITY);

        // Canvas statt JPanel für bessere Performance
        canvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

                synchronized (frameQueue) {
                    int idx = 0;
                    for (BufferedImage frame : frameQueue) {
                        if (frame != null && idx < ALPHA_VALUES.length) {
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ALPHA_VALUES[idx]));
                            g2d.drawImage(frame, 0, 0, getWidth(), getHeight(), null);
                            idx++;
                        }
                    }
                }
            }
        };

        canvas.setFocusable(false);
        add(canvas);
    }

    public void start() {
        System.out.println("Motion Blur gestartet: " + FRAME_DELAY_MS + "ms");

        Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();
        setBounds(screenBounds);

        // Wichtig: Erst visible, dann opacity
        setVisible(true);
        setOpacity(0.01f); // Fast unsichtbar = Click-through

        System.out.println("Overlay Position: " + screenBounds);

        captureTimer = new Timer(FRAME_DELAY_MS, e -> captureFrame());
        captureTimer.start();
    }

    private void captureFrame() {
        if (isCapturing) return;

        isCapturing = true;

        SwingUtilities.invokeLater(() -> {
            try {
                // Mache Fenster komplett transparent für Capture
                float oldOpacity = getOpacity();
                setOpacity(0.0f);

                // Mini-Pause
                try { Thread.sleep(1); } catch (InterruptedException ex) {}

                // Screenshot
                Rectangle bounds = getBounds();
                BufferedImage screenshot = robot.createScreenCapture(bounds);

                // Stelle Opacity wieder her
                setOpacity(oldOpacity);

                // Frame zur Queue
                synchronized (frameQueue) {
                    frameQueue.addFirst(screenshot);

                    while (frameQueue.size() > MAX_TRAIL_FRAMES) {
                        BufferedImage old = frameQueue.removeLast();
                        if (old != null) old.flush();
                    }
                }

                // Repaint
                canvas.repaint();

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                isCapturing = false;
            }
        });
    }

    public void stop() {
        if (captureTimer != null) {
            captureTimer.stop();
        }

        synchronized (frameQueue) {
            frameQueue.clear();
        }

        setVisible(false);
        dispose();
        System.out.println("Motion Blur gestoppt");
    }
}