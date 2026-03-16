// src/physiosim/ui/views/HomeView.java
package physiosim.ui.views;

import physiosim.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HomeView extends JPanel {

    private static final Font   TITLE_FONT = Theme.TITLE;
    private static final Font   BTN_FONT   = Theme.BTN1;
    private static final Stroke BTN_STROKE = new BasicStroke(2f);

    private static final int BTN_WIDTH  = 110;
    private static final int BTN_HEIGHT = 56;
    private static final int BTN_GAP    = 40;
    private static final int BTN_Y      = 240;
    private static final int BTN_RADIUS = 14;

    private Rectangle loginRect, signupRect;
    private final Runnable onLogin, onSignup;

    public HomeView() { this(null, null); }

    public HomeView(Runnable onLogin, Runnable onSignup) {
        this.onLogin = onLogin;
        this.onSignup = onSignup;
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (loginRect != null && loginRect.contains(p) && onLogin != null) {
                    onLogin.run();
                } else if (signupRect != null && signupRect.contains(p) && onSignup != null) {
                    onSignup.run();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w  = getWidth();
        int cx = w / 2;

        g2.setColor(Theme.BG);
        g2.fillRoundRect(0, 0, w, getHeight(), Theme.RADIUS, Theme.RADIUS);

        g2.setColor(Theme.FG);
        g2.setFont(TITLE_FONT);
        drawCenter(g2, "PHYSIOSIM", cx, 80);

        int bxLogin  = cx - BTN_WIDTH - BTN_GAP / 2;
        int bxSignup = cx + BTN_GAP / 2;

        loginRect  = new Rectangle(bxLogin,  BTN_Y, BTN_WIDTH, BTN_HEIGHT);
        signupRect = new Rectangle(bxSignup, BTN_Y, BTN_WIDTH, BTN_HEIGHT);

        drawButton(g2, loginRect,  "LOG IN");
        drawButton(g2, signupRect, "SIGN UP");

        g2.dispose();
    }

    private void drawButton(Graphics2D g2, Rectangle r, String label) {
        g2.setColor(Theme.BTNBACK);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, BTN_RADIUS, BTN_RADIUS);

        g2.setStroke(BTN_STROKE);
        g2.setColor(Theme.FG);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, BTN_RADIUS, BTN_RADIUS);

        g2.setFont(BTN_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int baseY = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(Theme.FG);
        drawCenter(g2, label, r.x + r.width / 2, baseY);
    }

    private void drawCenter(Graphics g, String text, int cx, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, baselineY);
    }
}
