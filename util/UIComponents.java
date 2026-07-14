package util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class UIComponents {
    // Premium Color System (Tailwind Slate / Emerald / Blue theme)
    public static final Color COLOR_BG = new Color(15, 23, 42);         // #0f172a - Slate 900
    public static final Color COLOR_CARD = new Color(30, 41, 59);       // #1e293b - Slate 800
    public static final Color COLOR_CARD_LIGHT = new Color(51, 65, 85); // #334155 - Slate 700
    
    public static final Color COLOR_ACCENT = new Color(59, 130, 246);   // #3b82f6 - Blue 500
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);  // #10b981 - Emerald 500
    public static final Color COLOR_DANGER = new Color(239, 68, 68);    // #ef4444 - Red 500
    
    public static final Color COLOR_TEXT_MAIN = new Color(248, 250, 252);   // #f8fafc - Slate 50
    public static final Color COLOR_TEXT_SEC = new Color(203, 213, 225);    // #cbd5e1 - Slate 300
    public static final Color COLOR_TEXT_MUTED = new Color(148, 163, 184);  // #94a3b8 - Slate 400
    public static final Color COLOR_BORDER = new Color(51, 65, 85);     // #334155 - Slate 700

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    /**
     * Custom JPanel with rounded corners and anti-aliased graphics
     */
    public static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (backgroundColor != null) {
                graphics.setColor(backgroundColor);
            } else {
                graphics.setColor(getBackground());
            }
            
            graphics.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
        }
    }

    /**
     * Custom text field with rounded corners, padding, placeholder text, and dark theme colors
     */
    public static class CustomTextField extends JTextField {
        private final String placeholder;
        private boolean isShowingPlaceholder;

        public CustomTextField(String placeholder) {
            this.placeholder = placeholder;
            this.isShowingPlaceholder = true;
            
            setBackground(COLOR_CARD_LIGHT);
            setForeground(COLOR_TEXT_MUTED);
            setText(placeholder);
            setCaretColor(COLOR_TEXT_MAIN);
            setFont(FONT_BODY);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setOpaque(false);

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (isShowingPlaceholder) {
                        setText("");
                        setForeground(COLOR_TEXT_MAIN);
                        isShowingPlaceholder = false;
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().trim().isEmpty()) {
                        setText(placeholder);
                        setForeground(COLOR_TEXT_MUTED);
                        isShowingPlaceholder = true;
                    }
                }
            });
        }

        @Override
        public String getText() {
            return isShowingPlaceholder ? "" : super.getText();
        }

        @Override
        public void setText(String t) {
            if (t == null || t.isEmpty() || t.equals(placeholder)) {
                super.setText(placeholder);
                setForeground(COLOR_TEXT_MUTED);
                isShowingPlaceholder = true;
            } else {
                super.setText(t);
                setForeground(COLOR_TEXT_MAIN);
                isShowingPlaceholder = false;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            super.paintComponent(g);
        }
    }

    /**
     * Custom password field with placeholder and rounded borders
     */
    public static class CustomPasswordField extends JPasswordField {
        private final String placeholder;
        private boolean isShowingPlaceholder;

        public CustomPasswordField(String placeholder) {
            this.placeholder = placeholder;
            this.isShowingPlaceholder = true;
            
            setBackground(COLOR_CARD_LIGHT);
            setForeground(COLOR_TEXT_MUTED);
            setText(placeholder);
            setEchoChar((char) 0); // Hide echo character to show placeholder
            setCaretColor(COLOR_TEXT_MAIN);
            setFont(FONT_BODY);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setOpaque(false);

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (isShowingPlaceholder) {
                        setText("");
                        setEchoChar('•');
                        setForeground(COLOR_TEXT_MAIN);
                        isShowingPlaceholder = false;
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (new String(getPassword()).trim().isEmpty()) {
                        setText(placeholder);
                        setEchoChar((char) 0);
                        setForeground(COLOR_TEXT_MUTED);
                        isShowingPlaceholder = true;
                    }
                }
            });
        }

        public String getActualPassword() {
            return isShowingPlaceholder ? "" : new String(getPassword());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            super.paintComponent(g);
        }
    }

    /**
     * Custom high-fidelity button with hover effects and animations
     */
    public static class CustomButton extends JButton {
        private final Color baseColor;
        private final Color hoverColor;
        private boolean isHovered = false;

        public CustomButton(String text) {
            this(text, COLOR_ACCENT, COLOR_ACCENT.brighter());
        }

        public CustomButton(String text, Color baseColor, Color hoverColor) {
            super(text);
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;

            setFont(FONT_BOLD);
            setForeground(COLOR_TEXT_MAIN);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(10, 20, 10, 20));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (isHovered) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(baseColor);
            }
            
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            super.paintComponent(g);
        }
    }

    /**
     * Sidebar tab/navigation button
     */
    public static class SidebarButton extends JButton {
        private boolean active = false;

        public SidebarButton(String text) {
            super(text);
            setFont(FONT_BOLD);
            setForeground(COLOR_TEXT_SEC);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(12, 20, 12, 20));
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!active) {
                        setForeground(COLOR_TEXT_MAIN);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!active) {
                        setForeground(COLOR_TEXT_SEC);
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? COLOR_TEXT_MAIN : COLOR_TEXT_SEC);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (active) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 25)); // Slight highlight background
                g2.fill(new RoundRectangle2D.Double(10, 4, getWidth() - 20, getHeight() - 8, 8, 8));
                
                // Left marker accent
                g2.setColor(COLOR_ACCENT);
                g2.fill(new RoundRectangle2D.Double(12, 10, 4, getHeight() - 20, 2, 2));
            }
            super.paintComponent(g);
        }
    }

    /**
     * Styles a JTable in high-fidelity dark-mode layout
     */
    public static void styleTable(JTable table) {
        table.setBackground(COLOR_CARD);
        table.setForeground(COLOR_TEXT_MAIN);
        table.setFont(FONT_BODY);
        table.setRowHeight(38);
        table.setGridColor(COLOR_BORDER);
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(59, 130, 246, 50));
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setBorder(null);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(COLOR_BG);
        header.setForeground(COLOR_TEXT_MAIN);
        header.setFont(FONT_BOLD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        header.setReorderingAllowed(false);

        // Header Renderer to override basic borders
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(COLOR_BG);
                setForeground(COLOR_TEXT_MAIN);
                setFont(FONT_BOLD);
                setHorizontalAlignment(JLabel.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                return this;
            }
        };
        header.setDefaultRenderer(headerRenderer);

        // Cell Renderer for padding and alternating colors
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
                
                if (isSelected) {
                    setBackground(new Color(59, 130, 246, 80));
                } else {
                    if (row % 2 == 0) {
                        setBackground(COLOR_CARD);
                    } else {
                        setBackground(new Color(23, 33, 48)); // Slightly darker slate
                    }
                }
                
                // Customize alignment for GPA/Grade/ID numbers
                if (column == 0 || value instanceof Number || (value instanceof String && ((String)value).startsWith("STU"))) {
                    setHorizontalAlignment(JLabel.LEFT);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, cellRenderer);
    }
}
