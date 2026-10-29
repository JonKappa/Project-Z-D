package test;

import java.awt.*;
import javax.swing.*;

// The game window. Switches between fullscreen and a window sized from the settings.
public class MyFrame extends JFrame{

    MyPanel panel;

    MyFrame() {
        super("Project Z-D");
        panel = new MyPanel();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(panel);
        panel.watchFocus(this);
        panel.game.onDisplayChange = this::applyDisplaySettings;
        applyDisplaySettings();
    }

    // goes fullscreen, or back to a window of the chosen size, centred on the screen
    void applyDisplaySettings() {
        Settings settings = panel.game.settings;
        // the screen the window is on now, so fullscreen stays on that monitor
        Rectangle screen = getGraphicsConfiguration().getBounds();

        // the window border can only be added or removed while the window is hidden
        if (isDisplayable()) dispose();
        setUndecorated(settings.fullscreen);

        if (settings.fullscreen) {
            // a borderless window covering the screen, not Java's exclusive fullscreen mode: exclusive
            // mode made Swing's drawing flicker, and leaving it lost the background image (black screen)
            setBounds(screen);
            setVisible(true);
        } else {
            panel.setPreferredSize(windowSize(settings.windowScale));
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }
        panel.requestFocusInWindow();
    }

    // 640 x 480 times the scale, shrunk (keeping the shape) if that won't fit on the screen
    private Dimension windowSize(int scale) {
        Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int roomForBorder = 60; // title bar and edges, which pack adds outside the panel
        double fit = Math.min((usable.width - roomForBorder) / (double) Game.WIDTH,
                (usable.height - roomForBorder) / (double) Game.HEIGHT);
        double size = Math.max(1, Math.min(scale, fit));
        return new Dimension((int) (Game.WIDTH * size), (int) (Game.HEIGHT * size));
    }
}
