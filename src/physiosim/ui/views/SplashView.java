// src/physiosim/ui/views/SplashView.java
package physiosim.ui.views;

import physiosim.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SplashView extends JFrame {
    private final Image img = new ImageIcon(
        getClass().getResource("/images/dissection.png")
    ).getImage();

    public SplashView(Runnable onFinish) {
        setUndecorated(true);
        setSize(440, 620);
        setLocationRelativeTo(null);
        
        setBackground(new Color(0,0,0,0));
        setShape(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),Theme.RADIUS, Theme.RADIUS));

        setContentPane(new JPanel() {
            @Override protected void paintComponent(Graphics g) {
            	super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(Theme.BG);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),48,48);

                g2.setColor(Theme.FG);
                g2.setFont(Theme.TITLE);
                drawCenter(g2,"HUMAN",getWidth()/2,90);
                drawCenter(g2,"PHYSIOLOGY",getWidth()/2,140);

                g2.setFont(Theme.FIELD);
                drawCenter(g2,"PHYSIOSIM",getWidth()/2,174);

                // 이미지
                int iw = img.getWidth(this), ih = img.getHeight(this);
                if (iw > 0 && ih > 0) {
                    int availW = getWidth() - 60;
                    int availH = getHeight() - 220;

                    double s = Math.min(availW / (double) iw, availH / (double) ih);
                    int w = (int)(iw * s), h = (int)(ih * s);
                    int x = (getWidth() - w) / 2;
                    int y = getHeight() - h;
                    g2.drawImage(img,x,y,w,h,this);
                }
                g2.dispose();
            }
            private void drawCenter(Graphics2D g2,String text,int cx,int y){
                FontMetrics fm = g2.getFontMetrics();
                int x = cx - fm.stringWidth(text) / 2;
                g2.drawString(text, x, y);
            }
        });

        // 2초 후 전환
        new Timer(2000, e -> {
            ((Timer)e.getSource()).stop();
            if (onFinish != null) onFinish.run();
            dispose();
        }).start();
    }
}
