package me.sirtyler.pokedex.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class DrawPanel extends JPanel {
    private AnimatedGIF ag;

    public DrawPanel(URL url) throws IOException {
        //URLConnection connect = url.openConnection();
        //connect.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        //connect.connect();
        ag = new AnimatedGIF(this, url);
        ag.play();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        BufferedImage currentFrame = ag.getCurrentFrame();
        if (currentFrame != null) {
            int x = (getWidth() - currentFrame.getWidth()) / 2;
            int y = (getHeight() - currentFrame.getHeight()) / 2;
            g2d.drawImage(currentFrame, x, y, this);
        }
        g2d.dispose();
    }
}