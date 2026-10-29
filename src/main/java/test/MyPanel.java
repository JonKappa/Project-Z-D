package test;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

// The window's drawing surface. Runs the game loop on a Swing timer and draws whatever Game holds,
// scaled up to fill the panel, with black bars if the panel isn't the game's 16:9 shape.
public class MyPanel extends JPanel implements ActionListener {

    final Game game;
    final Input input;
    final Timer timer;
    long lastTickTime;

    MyPanel() {
        this.setPreferredSize(new Dimension(Game.WIDTH, Game.HEIGHT));
        this.setBackground(Color.BLACK);
        game = new Game();
        input = new Input();
        listenToMouse();

        lastTickTime = System.nanoTime();
        timer = new Timer(16, this); // about 60 updates a second
        timer.start();
    }

    // how much the game is scaled up to fill the panel
    private double scale() {
        return Math.min(getWidth() / (double) Game.WIDTH, getHeight() / (double) Game.HEIGHT);
    }

    // where the game's top-left corner sits in the panel, after centring it between the black bars
    private double offsetX() {
        return (getWidth() - Game.WIDTH * scale()) / 2;
    }

    private double offsetY() {
        return (getHeight() - Game.HEIGHT * scale()) / 2;
    }

    // passes the mouse to Input, turned from panel pixels into game pixels
    private void listenToMouse() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                input.mouseMoved(gameX(e), gameY(e));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                input.mouseMoved(gameX(e), gameY(e));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) input.mousePressed(gameX(e), gameY(e));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) input.mouseReleased();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private int gameX(MouseEvent e) {
        return (int) Math.floor((e.getX() - offsetX()) / scale());
    }

    private int gameY(MouseEvent e) {
        return (int) Math.floor((e.getY() - offsetY()) / scale());
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
        super.paintComponent(g); // fills the panel black, which leaves the bars
        Graphics2D g2D = (Graphics2D) g.create();
        // nearest neighbour keeps the pixel art sharp instead of blurring it when scaled up
        g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2D.translate(offsetX(), offsetY());
        g2D.scale(scale(), scale());
        g2D.clipRect(0, 0, Game.WIDTH, Game.HEIGHT);
        game.draw(g2D);
        g2D.dispose();
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
