package test;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;


public class MyPanel extends JPanel implements ActionListener, KeyListener {

    boolean wPressed = false;
    boolean aPressed = false;
    boolean sPressed = false;
    boolean dPressed = false;

    BufferedImage backGround;
    final int PANEL_WIDTH = 640;
    final int PANEL_HEIGHT = 480;
    Timer timer;
    double xVelocity = 0;
    double yVelocity = 0;
    int walkingSpeed = 7;
    double x = 0;
    double y = 0;
    int frame = 0;
    int animationIndex = 0;
    Sprite p;
    boolean facingLeft = false;
    boolean isRunning = false;
    boolean controlsKeyPressed = false;



    MyPanel(){
        p = new Sprite();
        this.setPreferredSize(new Dimension(PANEL_WIDTH,PANEL_HEIGHT));

        backGround = createImage(".\\Sprites\\Background\\testBackground.png");
        timer = new Timer(100, this);
        timer.start();
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
    }


    public void paint(Graphics g) {
        super.paint(g); // paint background

        Graphics2D g2D = (Graphics2D) g;



        if (isRunning && controlsKeyPressed) {
            animationIndex = 2;
        }
        else if (controlsKeyPressed) {
            animationIndex = 1;
        }
        else if (xVelocity == 0 && yVelocity == 0) {
            animationIndex = 0;
        }

        int width = p.animation[animationIndex][frame].getWidth();
        int height = p.animation[animationIndex][frame].getHeight();

        g2D.drawImage(backGround, 0, 0, null);
        if (facingLeft) {
            g2D.drawImage(p.animation[animationIndex][frame], (int) x + width, (int) y, -width, height, null);
        }
        else {
            g2D.drawImage(p.animation[animationIndex][frame], (int) x, (int) y, width, height, null);
        }


        if (frame == p.animation[animationIndex].length - 1) {
            frame = 0;
        } else {
            frame += 1;
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

    public static void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
        //catches if InterruptedException happens
        // if it does, it interrupts the current thread (thread.sleep
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (wPressed || aPressed || sPressed || dPressed) {
            if (wPressed) {
                up();
            }
            if (sPressed) {
                down();
            }
            if (aPressed) {
                left();
            }
            if (dPressed) {
                right();
            }
            controlsKeyPressed = true;
        }
        x += xVelocity;
        y += yVelocity;
        repaint();
    }

    public void up() {
        yVelocity = -walkingSpeed;

        if (isRunning) {
            yVelocity = -walkingSpeed * 2;
        }
    }

    public void down() {
        yVelocity = walkingSpeed;

        if (isRunning) {
            yVelocity = walkingSpeed * 2;
        }
    }

    public void left() {
        facingLeft = true;
        xVelocity = -walkingSpeed;

        if (isRunning) {
            xVelocity = -walkingSpeed * 2;
        }

    }

    public void right() {
        facingLeft = false;
        xVelocity = walkingSpeed;

        if (isRunning) {
            xVelocity = walkingSpeed * 2;
        }
    }



    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            wPressed = true;
        }
        if (code == KeyEvent.VK_S) {
            sPressed = true;
        }
        if (code == KeyEvent.VK_A) {
            aPressed = true;
        }
        if (code == KeyEvent.VK_D) {
            right();
            dPressed = true;
        }
        if (code == KeyEvent.VK_SHIFT) {
            isRunning = true;
        }



    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            wPressed = false;
        }
        if (code == KeyEvent.VK_S) {
            sPressed = false;
        }
        if (code == KeyEvent.VK_A) {
            aPressed = false;
        }
        if (code == KeyEvent.VK_D) {
            dPressed = false;
        }
        if (code == KeyEvent.VK_SHIFT) {
            isRunning = false;
        }

        if (!wPressed) {
            yVelocity = 0;
        }
        if (!sPressed) {
            yVelocity = 0;
        }
        if (!aPressed) {
            xVelocity = 0;
        }
        if (!dPressed) {
            xVelocity = 0;
        }

        if (!wPressed && !aPressed && !sPressed && !dPressed) {
            controlsKeyPressed = false;
            frame = 0;
        }
    }

}

