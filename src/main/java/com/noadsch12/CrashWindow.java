package com.noadsch12;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class CrashWindow {

    public static void show(CrashReport report) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("12th Client – Crash");
            frame.setSize(750, 500);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            // Load and set the logo as window icon
            BufferedImage logo = loadLogo();
            if (logo != null) {
                frame.setIconImage(logo);
            }

            // Main panel with gradient background
            JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
            mainPanel.setBackground(new Color(15, 15, 18));

            // Header panel with logo and title
            JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
            headerPanel.setBackground(new Color(25, 25, 30));
            headerPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 70)),
                    BorderFactory.createEmptyBorder(20, 25, 20, 25)
            ));

            // Logo label
            JLabel logoLabel = new JLabel();
            if (logo != null) {
                Image scaledLogo = logo.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaledLogo));
            }

            // Title and subtitle panel
            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
            titlePanel.setOpaque(false);

            JLabel title = new JLabel("12th Client has crashed");
            title.setForeground(new Color(255, 90, 95));
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));

            JLabel subtitle = new JLabel("An unexpected error occurred");
            subtitle.setForeground(new Color(150, 150, 160));
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subtitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            titlePanel.add(title);
            titlePanel.add(subtitle);

            headerPanel.add(logoLabel, BorderLayout.WEST);
            headerPanel.add(titlePanel, BorderLayout.CENTER);

            // Content panel for stack trace
            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBackground(new Color(15, 15, 18));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

            JLabel stackLabel = new JLabel("Stack Trace:");
            stackLabel.setForeground(new Color(180, 180, 190));
            stackLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

            JTextArea area = new JTextArea(report.stacktrace);
            area.setEditable(false);
            area.setBackground(new Color(22, 22, 26));
            area.setForeground(new Color(220, 220, 230));
            area.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
            area.setCaretColor(new Color(255, 255, 255));
            area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            area.setLineWrap(false);

            JScrollPane scroll = new JScrollPane(area);
            scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 48), 1));
            scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
            scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

            contentPanel.add(stackLabel, BorderLayout.NORTH);
            contentPanel.add(scroll, BorderLayout.CENTER);

            // Footer panel with buttons
            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
            footerPanel.setBackground(new Color(20, 20, 24));
            footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 40, 48)));

            JButton copyButton = createStyledButton("Copy Error", new Color(60, 60, 70), new Color(70, 70, 80));
            copyButton.addActionListener(e -> {
                java.awt.datatransfer.StringSelection selection =
                        new java.awt.datatransfer.StringSelection(report.stacktrace);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                copyButton.setText("Copied!");
                Timer timer = new Timer(1500, evt -> copyButton.setText("Copy Error"));
                timer.setRepeats(false);
                timer.start();
            });

            JButton closeButton = createStyledButton("Close", new Color(220, 50, 60), new Color(240, 70, 80));
            closeButton.addActionListener(e -> System.exit(0));

            footerPanel.add(copyButton);
            footerPanel.add(closeButton);

            // Assemble the frame
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(contentPanel, BorderLayout.CENTER);
            mainPanel.add(footerPanel, BorderLayout.SOUTH);

            frame.setContentPane(mainPanel);
            frame.setVisible(true);
        });
    }

    private static BufferedImage loadLogo() {
        try {
            InputStream logoStream = CrashWindow.class.getResourceAsStream("/assets/12th-client/logo.png");
            if (logoStream != null) {
                return ImageIO.read(logoStream);
            } else {
                System.err.println("Logo not found at /assets/12th-client/logo.png");
            }
        } catch (IOException e) {
            System.err.println("Failed to load logo: " + e.getMessage());
        }
        return null;
    }

    private static JButton createStyledButton(String text, Color bg, Color hoverBg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverBg);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
            }
        });

        return button;
    }

    // Custom ScrollBar UI for modern look
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(80, 80, 90);
            this.trackColor = new Color(30, 30, 36);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }

        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                    thumbBounds.width - 4, thumbBounds.height - 4, 6, 6);
            g2.dispose();
        }
    }
}