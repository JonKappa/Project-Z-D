package test;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

// The window's drawing surface. Runs the game loop on a Swing timer and draws whatever Game holds.
public class MyPanel extends JPanel implements ActionListener {

    final Game game;
    final Input input;
    final Timer timer;
    long lastTickTime;

    MyPanel() {
        this.setPreferredSize(new Dimension(Game.WIDTH, Game.HEIGHT));
        game = new Game();
        input = new Input();

        lastTickTime = System.nanoTime();
        timer = new Timer(16, this); // about 60 updates a second
        timer.start();
    }

    // the window calls this once it exists, so keys held while it loses focus don't stay stuck down,
    // and the game pauses instead of carrying on while the player is in another window
    void watchFocus(Window window) {
        window.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                input.releaseAll();
                game.pause();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        game.draw((Graphics2D) g);
    }

    // one game tick: update the world, then redraw
    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double dt = Math.min((now - lastTickTime) / 1_000_000_000.0, 0.05); // cap it so a hiccup can't teleport the player
        lastTickTime = now;

        game.update(dt, input);

        input.endTick();
        repaint();
        Toolkit.getDefaultToolkit().sync(); // stops the animation stuttering on Linux
    }
}
