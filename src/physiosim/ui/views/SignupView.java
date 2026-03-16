// src/physiosim/ui/views/SignupView.java
package physiosim.ui.views;

import physiosim.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class SignupView extends JPanel {

    private static final int FIELDS_TOP     = 160;
    private static final int FIELD_W        = 300;
    private static final int FIELD_H        = 52;
    private static final int FIELD_GAP      = 12;

    private static final int RADIO_TOP_GAP  = 10;
    private static final int RADIO_GAP_X    = 70;

    private static final int SUBMIT_W       = FIELD_W;
    private static final int SUBMIT_H       = 34;
    private static final int SUBMIT_TOP_GAP = 16;

    private static final int SUB_W          = 110;
    private static final int SUB_H          = 56;
    private static final int SUB_GAP        = 80;
    private static final int SUB_TOP_GAP    = 76;

    private static final int    BTN_RADIUS  = 14;
    private static final Stroke BTN_STROKE  = new BasicStroke(2f);

    private final JTextField     idField    = new JTextField();
    private final JTextField     emailField = new JTextField();
    private final JPasswordField pwField    = new JPasswordField();

    private final JPanel idWrap;
    private final JPanel emailWrap;
    private final JPanel pwWrap;

    private final JRadioButton clinician  = new JRadioButton("CLINICIAN");
    private final JRadioButton researcher = new JRadioButton("RESEARCHER");

    private Rectangle submitRect, loginRect, backRect;

    public static class SignupData {
        public final String id, email, role;
        public final char[] password;

        public SignupData(String id, String email, char[] password, String role) {
            this.id = id;
            this.email = email;
            this.password = password;
            this.role = role;
        }
    }

    private final Runnable onBack;
    private final Runnable onGotoLogin;
    private final Consumer<SignupData> onSignup;

    public SignupView() { this(null, null, null); }

    public SignupView(Runnable onBack, Runnable onGotoLogin, Consumer<SignupData> onSignup) {
        this.onBack      = onBack;
        this.onGotoLogin = onGotoLogin;
        this.onSignup    = onSignup;

        setOpaque(false);
        setLayout(null);

        idWrap    = createLabeledField("ID",       idField);
        emailWrap = createLabeledField("EMAIL",    emailField);
        pwWrap    = createLabeledField("PASSWORD", pwField);

        add(idWrap);
        add(emailWrap);
        add(pwWrap);

        ButtonGroup g = new ButtonGroup();
        g.add(clinician);
        g.add(researcher);
        clinician.setSelected(true);

        styleRadio(clinician);
        styleRadio(researcher);
        add(clinician);
        add(researcher);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (submitRect != null && submitRect.contains(p) && onSignup != null) {
                    onSignup.accept(new SignupData(
                        idField.getText().trim(),
                        emailField.getText().trim(),
                        pwField.getPassword(),
                        clinician.isSelected() ? "CLINICIAN" : "RESEARCHER"
                    ));
                } else if (loginRect != null && loginRect.contains(p) && onGotoLogin != null) {
                    onGotoLogin.run();
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

        int y  = FIELDS_TOP;
        int fx = cx - FIELD_W / 2;

        idWrap.setBounds   (fx, y, FIELD_W, FIELD_H);
        y += FIELD_H + FIELD_GAP;

        emailWrap.setBounds(fx, y, FIELD_W, FIELD_H);
        y += FIELD_H + FIELD_GAP;

        pwWrap.setBounds   (fx, y, FIELD_W, FIELD_H);
        y += FIELD_H + RADIO_TOP_GAP;

        int radioY = y + 2;
        int half   = RADIO_GAP_X / 2;
        clinician.setBounds (cx - half - 105, radioY, 120, 22);
        researcher.setBounds(cx + half,       radioY, 140, 22);

        int submitY = radioY + 30;
        submitRect  = new Rectangle(cx - SUBMIT_W / 2, submitY, SUBMIT_W, SUBMIT_H);

        int bottomY    = submitY + SUBMIT_H + SUB_TOP_GAP;
        int totalSubW  = SUB_W * 2 + SUB_GAP;
        int leftX      = cx - totalSubW / 2;
        int rightX     = leftX + SUB_W + SUB_GAP;

        loginRect = new Rectangle(leftX,  bottomY, SUB_W, SUB_H);
        backRect  = new Rectangle(rightX, bottomY, SUB_W, SUB_H);
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
        drawCenter(g2, "SIGN UP",   cx, 105);

        if (submitRect != null) drawButton(g2, submitRect, "SIGN UP");
        if (loginRect  != null) drawButton(g2, loginRect,  "LOG IN");
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

    private void styleRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setForeground(Theme.FG);
        rb.setFont(Theme.FIELD);
        rb.setFocusPainted(false);
        rb.setBorder(null);
    }
}
