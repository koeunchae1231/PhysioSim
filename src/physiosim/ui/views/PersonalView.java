// src/physiosim/ui/views/PersonalView.java
package physiosim.ui.views;

import physiosim.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PersonalView extends JPanel {

    private static final int BTN_W       = 300;
    private static final int BTN_H       = 34;
    private static final int BTN_GAP     = 30;
    private static final int FIRST_BTN_Y = 180;

    private static final int    BTN_RADIUS = 14;
    private static final Stroke BTN_STROKE = new BasicStroke(2f);

    private Rectangle newRect, listRect, accountRect, exitRect;

    private final Runnable onNewCharacter;
    private final Runnable onCharacterList;
    private final Runnable onAccountSetting;

    public PersonalView() { this(null, null, null); }

    public PersonalView(Runnable onNewCharacter, Runnable onCharacterList, Runnable onAccountSetting) {
        this.onNewCharacter   = onNewCharacter;
        this.onCharacterList  = onCharacterList;
        this.onAccountSetting = onAccountSetting;

        setOpaque(false);

        MouseAdapter m = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (runIfHit(newRect, p, onNewCharacter)) return;
                if (runIfHit(listRect, p, onCharacterList)) return;
                if (runIfHit(accountRect, p, onAccountSetting)) return;
                if (exitRect != null && exitRect.contains(p)) {
                    System.exit(0);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean hover =
                    (newRect     != null && newRect.contains(p)) ||
                    (listRect    != null && listRect.contains(p)) ||
                    (accountRect != null && accountRect.contains(p)) ||
                    (exitRect    != null && exitRect.contains(p));
                setCursor(hover
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    private boolean runIfHit(Rectangle r, Point p, Runnable action) {
        if (r != null && r.contains(p) && action != null) {
            action.run();
            return true;
        }
        return false;
    }

    @Override
    public void doLayout() {
        int cx = getWidth() / 2;

        int y1 = FIRST_BTN_Y;
        int y2 = y1 + BTN_H + BTN_GAP;
        int y3 = y2 + BTN_H + BTN_GAP;
        int y4 = y3 + BTN_H + BTN_GAP * 2;

        int x = cx - BTN_W / 2;

        newRect     = new Rectangle(x, y1, BTN_W, BTN_H);
        listRect    = new Rectangle(x, y2, BTN_W, BTN_H);
        accountRect = new Rectangle(x, y3, BTN_W, BTN_H);
        exitRect    = new Rectangle(x, y4, BTN_W, BTN_H);
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

        if (newRect     != null) drawButton(g2, newRect,     "NEW CHARACTER",   false);
        if (listRect    != null) drawButton(g2, listRect,    "CHARACTER LIST",  false);
        if (accountRect != null) drawButton(g2, accountRect, "ACCOUNT SETTING", false);
        if (exitRect    != null) drawButton(g2, exitRect,    "EXIT",            true);

        g2.dispose();
    }

    private void drawButton(Graphics2D g2, Rectangle r, String label, boolean isExit) {
        g2.setColor(Theme.BTNBACK);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, BTN_RADIUS, BTN_RADIUS);

        g2.setStroke(BTN_STROKE);
        g2.setColor(isExit ? Theme.ERROR : Theme.FG);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, BTN_RADIUS, BTN_RADIUS);

        g2.setFont(Theme.BTN1);
        g2.setColor(isExit ? Theme.ERROR : Theme.FG);
        FontMetrics fm = g2.getFontMetrics();
        int baseY = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
        drawCenter(g2, label, r.x + r.width / 2, baseY);
    }

    private void drawCenter(Graphics g, String text, int cx, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, baselineY);
    }
}
