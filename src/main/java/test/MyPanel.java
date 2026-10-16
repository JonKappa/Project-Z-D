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
    double xVelocity = 0;
    double yVelocity = 0;
    int walkingSpeed = 7;
    double x = 0;
    double y = 0;
    int frame = 0;
    int animationIndex = 0;
    Sprite p;
    Input input;
    boolean facingLeft = false;
    boolean isRunning = false;
    boolean controlsKeyPressed = false;



    MyPanel(){
        p = new Sprite();
        this.setPreferredSize(new Dimension(PANEL_WIDTH,PANEL_HEIGHT));

        backGround = createImage("./Sprites/Background/testBackground.png");
        input = new Input();
        timer = new Timer(100, this);
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
        boolean wPressed = input.isDown(KeyEvent.VK_W, KeyEvent.VK_UP);
        boolean aPressed = input.isDown(KeyEvent.VK_A, KeyEvent.VK_LEFT);
        boolean sPressed = input.isDown(KeyEvent.VK_S, KeyEvent.VK_DOWN);
        boolean dPressed = input.isDown(KeyEvent.VK_D, KeyEvent.VK_RIGHT);
        isRunning = input.isDown(KeyEvent.VK_SHIFT);

        boolean moving = wPressed || aPressed || sPressed || dPressed;
        if (controlsKeyPressed && !moving) {
            frame = 0;
        }
        controlsKeyPressed = moving;

        xVelocity = 0;
        yVelocity = 0;
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
        x += xVelocity;
        y += yVelocity;
        input.endTick();
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

}
