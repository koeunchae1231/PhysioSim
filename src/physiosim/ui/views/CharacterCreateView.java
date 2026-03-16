// src/physiosim/ui/views/CharacterCreateView.java
package physiosim.ui.views;

import physiosim.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class CharacterCreateView extends JPanel {

    private static final int FIELDS_TOP     = 120;
    private static final int FIELD_W        = 300;
    private static final int FIELD_H        = 52;
    private static final int FIELD_GAP      = 12;

    private static final int RADIO_TOP_GAP  = 10;
    private static final int RADIO_GAP_X    = 100;

    private static final int BTN_W          = FIELD_W;
    private static final int BTN_H          = 34;
    private static final int BTN_TOP_GAP    = 80;
    private static final int BTN_GAP        = 16;

    private static final int    BTN_RADIUS  = 14;
    private static final Stroke BTN_STROKE  = new BasicStroke(2f);

    private final JTextField nameField   = new JTextField();
    private final JTextField birthField  = new JTextField();
    private final JTextField heightField = new JTextField();
    private final JTextField weightField = new JTextField();

    private final JPanel nameWrap;
    private final JPanel birthWrap;
    private final JPanel heightWrap;
    private final JPanel weightWrap;

    private final JRadioButton male   = new JRadioButton("MALE");
    private final JRadioButton female = new JRadioButton("FEMALE");

    private Rectangle createRect, backRect;

    public static class CharacterData {
        public final String name;
        public final String birth;
        public final String height;
        public final String weight;
        public final String gender;

        public CharacterData(String name,
                             String birth,
                             String height,
                             String weight,
                             String gender) {
            this.name   = name;
            this.birth  = birth;
            this.height = height;
            this.weight = weight;
            this.gender = gender;
        }
    }

    private final Runnable onBack;
    private final Consumer<CharacterData> onCreate;

    public CharacterCreateView() {
        this(null, null);
    }

    public CharacterCreateView(Runnable onBack,
                               Consumer<CharacterData> onCreate) {
        this.onBack   = onBack;
        this.onCreate = onCreate;

        setOpaque(false);
        setLayout(null);

        nameWrap   = createLabeledField("CHARACTER NAME",              nameField);
        birthWrap  = createLabeledField("CHARACTER BIRTH (YYYYMMDD)",  birthField);
        heightWrap = createLabeledField("CHARACTER HEIGHT",            heightField);
        weightWrap = createLabeledField("CHARACTER WEIGHT",            weightField);

        add(nameWrap);
        add(birthWrap);
        add(heightWrap);
        add(weightWrap);

        styleInput(nameField);
        styleInput(birthField);
        styleInput(heightField);
        styleInput(weightField);

        PlainDocument birthDoc = (PlainDocument) birthField.getDocument();
        birthDoc.setDocumentFilter(new DigitLimitFilter(8));
        birthField.setToolTipText("8자리 숫자로 입력 바랍니다.");

        ButtonGroup g = new ButtonGroup();
        g.add(male);
        g.add(female);
        male.setSelected(true);

        styleRadio(male);
        styleRadio(female);
        add(male);
        add(female);

        MouseAdapter m = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (runIfHit(createRect, p) && onCreate != null) {
                    onCreate.accept(new CharacterData(
                        nameField.getText().trim(),
                        birthField.getText().trim(),
                        heightField.getText().trim(),
                        weightField.getText().trim(),
                        male.isSelected() ? "MALE" : "FEMALE"
                    ));
                } else if (runIfHit(backRect, p) && onBack != null) {
                    onBack.run();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean hover =
                    (createRect != null && createRect.contains(p)) ||
                    (backRect   != null && backRect.contains(p));
                setCursor(hover
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    private boolean runIfHit(Rectangle r, Point p) {
        return r != null && r.contains(p);
    }

    private JPanel createLabeledField(String label, JTextComponent input) {
        JPanel wrap = new JPanel(new BorderLayout(0, Theme.GAP_SM));
        wrap.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(Theme.LABEL);
        l.setForeground(Theme.FG);
        wrap.add(l, BorderLayout.NORTH);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(Theme.PANEL);
        holder.setBorder(new EmptyBorder(10, 12, 10, 12));

        styleInput(input);
        holder.add(input, BorderLayout.CENTER);
        wrap.add(holder, BorderLayout.CENTER);

        holder.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                input.requestFocusInWindow();
            }
        });

        return wrap;
    }

    private void styleInput(JTextComponent input) {
        input.setOpaque(false);
        input.setBorder(BorderFactory.createEmptyBorder());
        input.setFont(Theme.FIELD);
        input.setForeground(Theme.FG);
        input.setCaretColor(Theme.FG);
    }

    private void styleRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setForeground(Theme.FG);
        rb.setFont(Theme.FIELD);
        rb.setFocusPainted(false);
        rb.setBorder(null);
    }

    @Override
    public void doLayout() {
        int w  = getWidth();
        int cx = w / 2;

        int y  = FIELDS_TOP;
        int fx = cx - FIELD_W / 2;

        nameWrap.setBounds  (fx, y, FIELD_W, FIELD_H);
        y += FIELD_H + FIELD_GAP;

        birthWrap.setBounds (fx, y, FIELD_W, FIELD_H);
        y += FIELD_H + FIELD_GAP;

        heightWrap.setBounds(fx, y, FIELD_W, FIELD_H);
        y += FIELD_H + FIELD_GAP;

        weightWrap.setBounds(fx, y, FIELD_W, FIELD_H);
        y += FIELD_H + RADIO_TOP_GAP;

        int radioY = y + 2;
        int half   = RADIO_GAP_X / 2;
        male.setBounds  (cx - half - 60, radioY, 80, 22);
        female.setBounds(cx + half - 40, radioY, 80, 22);

        int btnY1 = radioY + BTN_TOP_GAP;
        createRect = new Rectangle(cx - BTN_W / 2, btnY1, BTN_W, BTN_H);

        int btnY2 = btnY1 + BTN_H + BTN_GAP;
        backRect  = new Rectangle(cx - BTN_W / 2, btnY2, BTN_W, BTN_H);
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
        drawCenter(g2, "PHYSIOSIM", cx, 80);

        if (createRect != null) drawButton(g2, createRect, "CREATE");
        if (backRect   != null) drawButton(g2, backRect,   "BACK");

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

    private static class DigitLimitFilter extends DocumentFilter {
        private final int maxLen;

        DigitLimitFilter(int maxLen) {
            this.maxLen = maxLen;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;
            String filtered = onlyDigits(string);
            if (filtered.isEmpty()) return;

            int curLen = fb.getDocument().getLength();
            int newLen = curLen + filtered.length();
            if (newLen > maxLen) {
                filtered = filtered.substring(0, maxLen - curLen);
            }
            if (!filtered.isEmpty()) {
                super.insertString(fb, offset, filtered, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) {
                super.replace(fb, offset, length, null, attrs);
                return;
            }
            String filtered = onlyDigits(text);
            int curLen = fb.getDocument().getLength();
            int newLen = curLen - length + filtered.length();
            if (newLen > maxLen) {
                filtered = filtered.substring(0, maxLen - (curLen - length));
            }
            super.replace(fb, offset, length, filtered, attrs);
        }

        private String onlyDigits(String s) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (Character.isDigit(c)) sb.append(c);
            }
            return sb.toString();
        }
    }
}
