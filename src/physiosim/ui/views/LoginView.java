// src/physiosim/ui/views/LoginView.java
package physiosim.ui.views;

import physiosim.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

public class LoginView extends JPanel {

    private static final int FIELDS_TOP    = 190;
    private static final int FIELD_W       = 300;
    private static final int FIELD_H       = 52;
    private static final int FIELD_GAP     = 12;

    private static final int LOGIN_W       = FIELD_W;
    private static final int LOGIN_H       = 34;
    private static final int LOGIN_TOP_GAP = 16;

    private static final int SUB_W         = 110;
    private static final int SUB_H         = 56;
    private static final int SUB_GAP       = 80;
    private static final int SUB_TOP_GAP   = 120;

    private static final int    BTN_RADIUS = 14;
    private static final Stroke BTN_STROKE = new BasicStroke(2f);

    private final JTextField     idField = new JTextField();
    private final JPasswordField pwField = new JPasswordField();

    private final JPanel idWrap;
    private final JPanel pwWrap;

    private Rectangle loginRect, signupRect, backRect;

    private final Runnable onBack;
    private final Runnable onGotoSignup;
    private final BiConsumer<String, char[]> onLogin;

    public LoginView() {this(null, null, null);}

    public LoginView(Runnable onBack, Runnable onGotoSignup, BiConsumer<String, char[]> onLogin) {
        this.onBack       = onBack;
        this.onGotoSignup = onGotoSignup;
        this.onLogin      = onLogin;

        setOpaque(false);
        setLayout(null);

        idWrap = createLabeledField("ID",        idField);
        pwWrap = createLabeledField("PASSWORD", pwField);

        add(idWrap);
        add(pwWrap);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (loginRect != null && loginRect.contains(p) && onLogin != null) {
                    onLogin.accept(idField.getText().trim(), pwField.getPassword());
                } else if (signupRect != null && signupRect.contains(p) && onGotoSignup != null) {
                    onGotoSignup.run();
                } else if (backRect != null && backRect.contains(p) && onBack != null) {
                    onBack.run();
                }
            }
        });
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
        if (input instanceof JTextField tf) {
            tf.setCaretColor(Theme.FG);
        }
    }

    @Override
    public void doLayout() {
        int w  = getWidth();
        int cx = w / 2;

        int idX = cx - FIELD_W / 2;
        int idY = FIELDS_TOP;
        idWrap.setBounds(idX, idY, FIELD_W, FIELD_H);

        int pwY = idY + FIELD_H + FIELD_GAP;
        pwWrap.setBounds(idX, pwY, FIELD_W, FIELD_H);

        int loginY = pwY + FIELD_H + LOGIN_TOP_GAP;
        loginRect  = new Rectangle(cx - LOGIN_W / 2, loginY, LOGIN_W, LOGIN_H);

        int bottomY   = loginY + LOGIN_H + SUB_TOP_GAP;
        int totalSubW = SUB_W * 2 + SUB_GAP;
        int leftX     = cx - totalSubW / 2;
        int rightX    = leftX + SUB_W + SUB_GAP;

        signupRect = new Rectangle(leftX,  bottomY, SUB_W, SUB_H);
        backRect   = new Rectangle(rightX, bottomY, SUB_W, SUB_H);
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
        drawCenter(g2, "LOG IN",    cx, 105);

        if (loginRect  != null) drawButton(g2, loginRect,  "LOG IN");
        if (signupRect != null) drawButton(g2, signupRect, "SIGN UP");
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
        FontMetrics fm = g2.getFontMetrics();
        int baseY = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
        drawCenter(g2, label, r.x + r.width / 2, baseY);
    }

    private void drawCenter(Graphics g, String text, int cx, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, baselineY);
    }
}
