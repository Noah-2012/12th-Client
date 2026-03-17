/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 *
 * This file is part of the 12th Client project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details.
 */

package com.noadsch12.macro;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MACROWindow {

    public static void show(MACROReport report) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("12th Client – Crash Management");
            frame.setSize(900, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            Color SIDEBAR_ACTIVE = new Color(255, 90, 95);
            Color SIDEBAR_IDLE = new Color(30, 30, 35);
            Color EVERYTHING_IS_GOOD = new Color(43, 179, 79);

            BufferedImage logo = loadLogo();
            if (logo != null) frame.setIconImage(logo);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(15, 15, 18));

            // ---------------- SIDEBAR ----------------
            JPanel sidebar = new JPanel();
            sidebar.setMinimumSize(new Dimension(200, 0));
            sidebar.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
            sidebar.setPreferredSize(new Dimension(200, 0));
            sidebar.setBackground(new Color(22, 22, 26));
            sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
            sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(45, 45, 50)));

            // ---------------- CENTER (CARDS) ----------------
            CardLayout cardLayout = new CardLayout();
            JPanel cardPanel = new JPanel(cardLayout);
            cardPanel.setOpaque(false);

            // Summary
            JTextArea summaryArea = createLogArea(buildSummaryWithCause(report));
            cardPanel.add(createModernScroll(summaryArea), "SUMMARY");

            // Stacktrace
            JTextArea stackArea = createLogArea(report.stacktrace);
            cardPanel.add(createModernScroll(stackArea), "STACK");

            // Environment
            JTextArea envArea = createLogArea(getDetailedEnvironment());
            cardPanel.add(createModernScroll(envArea), "ENV");

            // ---------------- SIDEBAR BUTTONS ----------------
            List<JButton> navButtons = new ArrayList<>();
            sidebar.add(Box.createVerticalStrut(20));

            JButton summaryBtn = createNavButton(
                    "Summary",
                    cardPanel, cardLayout, "SUMMARY",
                    navButtons, SIDEBAR_ACTIVE, SIDEBAR_IDLE
            );

            JButton stackBtn = createNavButton(
                    "Stacktrace",
                    cardPanel, cardLayout, "STACK",
                    navButtons, SIDEBAR_ACTIVE, SIDEBAR_IDLE
            );

            JButton envBtn = createNavButton(
                    "Stats",
                    cardPanel, cardLayout, "ENV",
                    navButtons, SIDEBAR_ACTIVE, SIDEBAR_IDLE
            );

            navButtons.add(summaryBtn);
            navButtons.add(stackBtn);
            navButtons.add(envBtn);

            sidebar.add(summaryBtn);
            sidebar.add(stackBtn);
            sidebar.add(envBtn);

            // ---------------- HEADER ----------------
            JPanel header = new JPanel(new BorderLayout(15, 0));
            header.setBackground(new Color(25, 25, 30));
            header.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

            JLabel title;
            if (!report.stacktrace.contains("12th Client managed Crash")) {
                title = new JLabel("12th Client Crash Report");
                title.setForeground(SIDEBAR_ACTIVE);
            } else {
                title = new JLabel("12th Client Crash Report (Triggered)");
                title.setForeground(EVERYTHING_IS_GOOD);
            }
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            header.add(title, BorderLayout.WEST);

            // ---------------- FOOTER ----------------
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
            footer.setBackground(new Color(20, 20, 24));

            JButton aiButton = createStyledButton(
                    "Report to ChatGPT",
                    new Color(16, 163, 127),
                    new Color(25, 195, 125)
            );
            aiButton.addActionListener(e -> openChatGPT(report.stacktrace));

            JButton copyButton = createStyledButton(
                    "Copy Error",
                    new Color(60, 60, 70),
                    new Color(70, 70, 80)
            );
            copyButton.addActionListener(e -> {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new java.awt.datatransfer.StringSelection(report.stacktrace),
                        null
                );
                copyButton.setText("Copied!");
                new Timer(1500, ev -> copyButton.setText("Copy Error")).start();
            });

            JButton closeButton = createStyledButton(
                    "Close",
                    new Color(220, 50, 60),
                    new Color(240, 70, 80)
            );
            closeButton.addActionListener(e -> System.exit(0));

            footer.add(aiButton);
            footer.add(copyButton);
            footer.add(closeButton);

            // ---------------- LAYOUT ----------------
            mainPanel.add(header, BorderLayout.NORTH);
            mainPanel.add(sidebar, BorderLayout.WEST);
            mainPanel.add(cardPanel, BorderLayout.CENTER);
            mainPanel.add(footer, BorderLayout.SOUTH);

            frame.setContentPane(mainPanel);
            frame.setVisible(true);

            // Default tab
            summaryBtn.doClick();
        });
    }

    // ================= LOGIC =================

    private static String buildSummaryWithCause(MACROReport report) {
        return buildCrashSummary(report)
                + "\n--- POSSIBLE CAUSE ---\n\n"
                + detectPossibleCause(report.stacktrace);
    }

    private static String buildCrashSummary(MACROReport report) {
        String stack = report.stacktrace;
        String exception = "Unknown";
        String message = "No message";
        String location = "Unknown";

        if (stack != null && !stack.isEmpty()) {
            String[] lines = stack.split("\n");

            if (lines.length > 0) {
                exception = lines[0];
                int idx = exception.indexOf(":");
                if (idx != -1) {
                    message = exception.substring(idx + 1).trim();
                    exception = exception.substring(0, idx).trim();
                }
            }

            for (String line : lines) {
                if (line.trim().startsWith("at ") && line.contains(".java")) {
                    location = line.trim().replace("at ", "");
                    break;
                }
            }
        }

        return """
        --- CRASH SUMMARY ---

        Exception:
        %s

        Message:
        %s

        First Occurrence:
        %s

        Timestamp:
        %s
        """.formatted(
                exception,
                message,
                location,
                LocalDateTime.now()
        );
    }

    private static String detectPossibleCause(String stacktrace) {
        if (stacktrace == null) return "Unknown cause.";

        if (stacktrace.contains("NullPointerException"))
            return "A required object was not initialized before use.";

        if (stacktrace.contains("OutOfMemoryError"))
            return "The client ran out of memory. Increase the JVM RAM limit.";

        if (stacktrace.contains("ClassNotFoundException"))
            return "A required class is missing or incompatible.";

        if (stacktrace.contains("NoSuchMethodError"))
            return "Version mismatch between client components.";

        if (stacktrace.contains("12th Client managed Crash - THIS IS NOT A REAL CRASH -"))
            return "This Crash got triggered controlled. You are good!";

        if (stacktrace.contains("OpenGL") || stacktrace.contains("LWJGL"))
            return "Graphics initialization failed (driver or GPU issue).";

        return "The exact cause could not be determined automatically.";
    }

    private static String getDetailedEnvironment() {
        Runtime rt = Runtime.getRuntime();
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        return """
        --- 12th CLIENT DIAGNOSTICS ---

        [SYSTEM]
        OS: %s (%s)
        Arch: %s
        Cores: %d

        [JAVA]
        VM: %s
        Version: %s
        Vendor: %s

        [MEMORY]
        Allocated: %d MB
        Max: %d MB
        Free: %d MB

        [GRAPHICS]
        Device: %s
        Resolution: %dx%d
        Refresh Rate: %d Hz
        """.formatted(
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"),
                rt.availableProcessors(),
                System.getProperty("java.vm.name"),
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                rt.totalMemory() / 1024 / 1024,
                rt.maxMemory() / 1024 / 1024,
                rt.freeMemory() / 1024 / 1024,
                gd.getIDstring(),
                gd.getDisplayMode().getWidth(),
                gd.getDisplayMode().getHeight(),
                gd.getDisplayMode().getRefreshRate()
        );
    }

    // ================= UI HELPERS =================

    private static JButton createNavButton(
            String text,
            JPanel cardPanel,
            CardLayout layout,
            String cardName,
            List<JButton> allButtons,
            Color activeColor,
            Color idleColor
    ) {
        JButton btn = createStyledButton(text, idleColor, new Color(45, 45, 50));
        btn.setMaximumSize(new Dimension(180, 45)); // größer für bessere Sichtbarkeit
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); // fetter
        btn.setHorizontalAlignment(SwingConstants.LEFT); // Text links

        btn.addActionListener(e -> {
            layout.show(cardPanel, cardName);

            for (JButton b : allButtons) {
                b.setBackground(idleColor);
                b.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
            }

            btn.setBackground(activeColor);
            btn.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, activeColor));
        });

        return btn;
    }

    private static JTextArea createLogArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setBackground(new Color(22, 22, 26));
        area.setForeground(new Color(200, 200, 210));
        area.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        area.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return area;
    }

    private static JScrollPane createModernScroll(JComponent comp) {
        JScrollPane scroll = new JScrollPane(comp);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        return scroll;
    }

    private static JButton createStyledButton(String text, Color bg, Color hoverBg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverBg);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bg);
            }
        });

        return button;
    }

    private static BufferedImage loadLogo() {
        try (InputStream is = MACROWindow.class.getResourceAsStream("/assets/12th-client/logo.png")) {
            return is != null ? ImageIO.read(is) : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static void openChatGPT(String stacktrace) {
        try {
            String prompt = "12th Client crash, what does the error say: " + stacktrace;
            String url = "https://chatgpt.com/?q=" +
                    URLEncoder.encode(prompt, StandardCharsets.UTF_8);
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ignored) {}
    }

    // ================= SCROLLBAR =================

    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(80, 80, 90);
            trackColor = new Color(30, 30, 36);
        }

        @Override
        protected JButton createDecreaseButton(int o) { return invisible(); }

        @Override
        protected JButton createIncreaseButton(int o) { return invisible(); }

        private JButton invisible() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
            g2.dispose();
        }
    }
}
