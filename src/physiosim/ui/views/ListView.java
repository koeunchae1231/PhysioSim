// src/physiosim/ui/views/ListView.java
package physiosim.ui.views;

import physiosim.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;

public class ListView extends JPanel {

    private static final int BTN_W      = 300;
    private static final int BTN_H      = 34;
    private static final int BTN_RADIUS = 14;
    private static final Stroke BTN_STROKE = new BasicStroke(2f);

    private static final int LIST_LEFT       = 60;
    private static final int LIST_TOP        = 130;
    private static final int LIST_BOTTOM_GAP = 110;

    private static final Color LIST_BG   = Color.WHITE;
    private static final Color LIST_FG   = new Color(40, 40, 40);
    private static final Color SEL_BG    = Theme.BTNBACK;
    private static final Color SEL_FG    = Theme.FG;
    private static final Color SEL_BD    = Theme.FG;
    private static final int   CELL_ARC  = 10;
    private static final int   ROW_H     = 32;

    private static final int   SCROLL_W  = 12;
    private static final Color TRACK_COL = new Color(26, 63, 84);
    private static final Color THUMB_COL = new Color(62, 98, 122);
    private static final Color THUMB_HOV = new Color(76, 118, 147);
    private static final Color THUMB_DRG = new Color(50, 90, 112);

    private Rectangle backRect;
    private final Runnable onBack;

    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list             = new JList<>(model);
    private final JScrollPane scroll             = new JScrollPane(list);

    private final Consumer<String> onSelect;

    public ListView() {
        this(null, null, null);
    }

    public ListView(Runnable onBack, List<String> initialItems, Consumer<String> onSelect) {
        this.onBack   = onBack;
        this.onSelect = onSelect;

        setOpaque(false);
        setLayout(null);

        list.setFont(new Font("Arial", Font.PLAIN, 18));
        list.setForeground(LIST_FG);
        list.setBackground(LIST_BG);
        list.setFixedCellHeight(ROW_H);
        list.setSelectionBackground(new Color(0, 0, 0, 0));
        list.setSelectionForeground(LIST_FG);
        list.setBorder(new EmptyBorder(6, 10, 6, 10));
        list.setCellRenderer(new RoundedCellRenderer());

        scroll.setBorder(BorderFactory.createLineBorder(Theme.FG));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(LIST_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getVerticalScrollBar().setUI(new ThemedScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ThemedScrollBarUI());
        scroll.setCorner(JScrollPane.LOWER_RIGHT_CORNER, corner(TRACK_COL));
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner(TRACK_COL));
        add(scroll);

        if (initialItems != null && !initialItems.isEmpty()) {
            initialItems.forEach(model::addElement);
        }

        MouseAdapter m = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (runIfHit(backRect, p) && onBack != null) {
                    onBack.run();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean hover = backRect != null && backRect.contains(p);
                setCursor(hover
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = list.locationToIndex(e.getPoint());
                    fireSelect(idx);
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    fireSelect(list.getSelectedIndex());
                }
            }
        });
    }

    private boolean runIfHit(Rectangle r, Point p) {
        return r != null && r.contains(p);
    }

    public void setItems(List<String> items) {
        model.clear();
        if (items != null) {
            items.forEach(model::addElement);
        }
    }

    public void addItem(String item) {
        if (item != null && !item.isBlank()) {
            model.addElement(item);
        }
    }

    private void fireSelect(int idx) {
        if (idx >= 0 && idx < model.size() && onSelect != null) {
            onSelect.accept(model.getElementAt(idx));
        }
    }

    private static JComponent corner(Color c) {
        JPanel p = new JPanel();
        p.setBackground(c);
        return p;
    }

    @Override
    public void doLayout() {
        int w     = getWidth();
        int h     = getHeight();
        int listW = w - LIST_LEFT * 2;
        int listH = h - LIST_TOP - LIST_BOTTOM_GAP;

        scroll.setBounds(LIST_LEFT, LIST_TOP, listW, listH);

        int cx    = w / 2;
        int backY = LIST_TOP + listH + 20;
        backRect  = new Rectangle(cx - BTN_W / 2, backY, BTN_W, BTN_H);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w  = getWidth();
        int h  = getHeight();
        int cx = w / 2;

        g2.setColor(Theme.BG);
        g2.fillRoundRect(0, 0, w, h, Theme.RADIUS, Theme.RADIUS);

        g2.setColor(Theme.FG);
        g2.setFont(Theme.TITLE);
        drawCenter(g2, "PHYSIOSIM", cx, 90);

        if (backRect != null) {
            drawButton(g2, backRect, "BACK");
        }

        g2.dispose();
    }

    private void drawButton(Graphics2D g2, Rectangle r, String label) {
        g2.setColor(Theme.BTNBACK);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, BTN_RADIUS, BTN_RADIUS);

        g2.setColor(Theme.FG);
        g2.setStroke(BTN_STROKE);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, BTN_RADIUS, BTN_RADIUS);

        g2.setFont(Theme.BTN1);
        g2.setColor(Theme.FG);
        FontMetrics fm = g2.getFontMetrics();
        int baseY = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
        drawCenter(g2, label, r.x + r.width / 2, baseY);
    }

    private void drawCenter(Graphics g, String text, int cx, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, baselineY);
    }

    private static class RoundedCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel l = (JLabel) super.getListCellRendererComponent(
                    list, value, index, false, false);
            l.setOpaque(false);
            l.setFont(new Font("Arial", Font.PLAIN, 18));
            l.setBorder(new EmptyBorder(6, 8, 6, 8));

            if (isSelected) {
                l.setForeground(SEL_FG);
                l.putClientProperty("selected", Boolean.TRUE);
            } else {
                l.setForeground(LIST_FG);
                l.putClientProperty("selected", Boolean.FALSE);
            }
            return l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            boolean selected = Boolean.TRUE.equals(getClientProperty("selected"));
            if (selected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(SEL_BG);
                g2.fillRoundRect(2, 2, w - 4, h - 4, CELL_ARC, CELL_ARC);
                g2.setColor(SEL_BD);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(2, 2, w - 4, h - 4, CELL_ARC, CELL_ARC);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private static class ThemedScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = THUMB_COL;
            trackColor = TRACK_COL;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return zero(); }

        @Override
        protected JButton createIncreaseButton(int orientation) { return zero(); }

        private JButton zero() {
            JButton b = new JButton();
            b.setFocusable(false);
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            b.setOpaque(false);
            b.setBorder(null);
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (!scrollbar.isEnabled() || r.width <= 0 || r.height <= 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color col = isDragging
                    ? THUMB_DRG
                    : (isThumbRollover() ? THUMB_HOV : THUMB_COL);

            int arc = 10;
            g2.setColor(col);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, arc, arc);
            g2.setColor(Theme.FG);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, arc, arc);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(TRACK_COL);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return (scrollbar.getOrientation() == Adjustable.VERTICAL)
                    ? new Dimension(SCROLL_W, super.getPreferredSize(c).height)
                    : new Dimension(super.getPreferredSize(c).width, SCROLL_W);
        }
    }
}
