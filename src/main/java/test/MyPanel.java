package test;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;


public class MyPanel extends JPanel implements ActionListener {

    BufferedImage backGround;
    final int PANEL_WIDTH = 640;
    final int PANEL_HEIGHT = 480;
    Timer timer;
    long lastTickTime;
    Sprite p;
    Player player;
    Input input;
    boolean showGallery = true; // G toggles between the animation gallery and the game
    double galleryTime = 0;



    MyPanel(){
        p = new Sprite();
        this.setPreferredSize(new Dimension(PANEL_WIDTH,PANEL_HEIGHT));

        backGround = createImage("Sprites/Background/testBackground.png");

        int[] frameCounts = new int[p.animation.length];
        for (int a = 0; a < frameCounts.length; a++) {
            frameCounts[a] = p.animation[a].length;
        }
        BufferedImage idle = p.animation[Player.State.IDLE.animationIndex][0];
        player = new Player(0, 0, idle.getWidth(), idle.getHeight(), frameCounts);
        input = new Input();

        lastTickTime = System.nanoTime();
        timer = new Timer(16, this); // about 60 updates a second
        timer.start();
    }

    // the window calls this once it exists, so keys held while it loses focus don't stay stuck down
    void watchFocus(Window window) {
        window.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                input.releaseAll();
            }
        });
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D) g;

        g2D.drawImage(backGround, 0, 0, null);
        if (showGallery) {
            drawGallery(g2D);
            return;
        }

        BufferedImage img = p.animation[player.state.animationIndex][player.frame];
        int x = (int) player.x;
        int y = (int) player.y;
        if (player.facingLeft) {
            g2D.drawImage(img, x + img.getWidth(), y, -img.getWidth(), img.getHeight(), null);
        }
        else {
            g2D.drawImage(img, x, y, img.getWidth(), img.getHeight(), null);
        }
    }


    // every animation playing at once in a grid; each is scaled down only if its biggest frame won't fit the cell
    private void drawGallery(Graphics2D g2D) {
        int cols = 7;
        int rows = (p.animation.length + cols - 1) / cols;
        int cellW = PANEL_WIDTH / cols;
        int cellH = PANEL_HEIGHT / rows;
        int galleryTick = (int) (galleryTime / Player.FRAME_TIME);
        g2D.setFont(new Font("SansSerif", Font.BOLD, 11));

        for (int a = 0; a < p.animation.length; a++) {
            int maxW = 1, maxH = 1;
            for (BufferedImage img : p.animation[a]) {
                maxW = Math.max(maxW, img.getWidth());
                maxH = Math.max(maxH, img.getHeight());
            }
            double scale = Math.min(1, Math.min((double) cellW / maxW, (cellH - 14.0) / maxH));
            BufferedImage img = p.animation[a][galleryTick % p.animation[a].length];
            int w = (int) (img.getWidth() * scale);
            int h = (int) (img.getHeight() * scale);
            int cellX = (a % cols) * cellW;
            int cellY = (a / cols) * cellH;

            g2D.drawImage(img, cellX + (cellW - w) / 2, cellY + cellH - 14 - h, w, h, null);
            g2D.setColor(Color.BLACK); // shadow keeps the label readable on light parts of the background
            g2D.drawString(p.names[a], cellX + 4, cellY + cellH - 2);
            g2D.setColor(Color.WHITE);
            g2D.drawString(p.names[a], cellX + 3, cellY + cellH - 3);
        }
    }

    private BufferedImage createImage(String filename){
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new File(filename));
            return bufferedImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // one game tick: read the keys, move the player, then redraw
    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double dt = Math.min((now - lastTickTime) / 1_000_000_000.0, 0.05); // cap it so a hiccup can't teleport Brad
        lastTickTime = now;

        if (input.wasPressed(KeyEvent.VK_G)) {
            showGallery = !showGallery;
        }

        // holding both opposite keys cancels out instead of one silently winning
        int dirX = 0;
        int dirY = 0;
        if (input.isDown(KeyEvent.VK_A, KeyEvent.VK_LEFT)) dirX -= 1;
        if (input.isDown(KeyEvent.VK_D, KeyEvent.VK_RIGHT)) dirX += 1;
        if (input.isDown(KeyEvent.VK_W, KeyEvent.VK_UP)) dirY -= 1;
        if (input.isDown(KeyEvent.VK_S, KeyEvent.VK_DOWN)) dirY += 1;
        boolean running = input.isDown(KeyEvent.VK_SHIFT);

        if (!showGallery) {
            player.update(dt, dirX, dirY, running, PANEL_WIDTH, PANEL_HEIGHT);
        }
        galleryTime += dt;

        input.endTick();
        repaint();
        Toolkit.getDefaultToolkit().sync(); // stops the animation stuttering on Linux
    }
}
