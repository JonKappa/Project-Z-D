package test;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

// The game world: everything in it, which screen is showing, and one update step.
// Knows nothing about Swing timers or windows.
public class Game {

    static final int WIDTH = 640;
    static final int HEIGHT = 480;

    enum Screen { TITLE, PLAYING, PAUSED, GAME_OVER }

    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 48);
    private static final Font HEADING_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 36);
    private static final Font HINT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 18);

    final Sprite sprites;
    final BufferedImage background;
    final List<Entity> entities = new ArrayList<>();
    private final int[] playerFrameCounts;
    private final int playerWidth;
    private final int playerHeight;
    Player player;
    Screen screen = Screen.TITLE;

    Game() {
        sprites = new Sprite();
        background = loadImage("./Sprites/Background/testBackground.png");

        // every direction has the same number of frames per animation
        BufferedImage[][] down = sprites.animation[Sprite.DOWN];
        playerFrameCounts = new int[down.length];
        for (int a = 0; a < playerFrameCounts.length; a++) {
            playerFrameCounts[a] = down[a].length;
        }
        BufferedImage idle = down[Player.State.IDLE.animationIndex][0];
        playerWidth = idle.getWidth();
        playerHeight = idle.getHeight();
    }

    // clears the world and puts a fresh player in the middle of the screen
    void startNewGame() {
        entities.clear();
        player = new Player((WIDTH - playerWidth) / 2.0, (HEIGHT - playerHeight) / 2.0,
                playerWidth, playerHeight, playerFrameCounts);
        entities.add(player);
        screen = Screen.PLAYING;
    }

    // pauses a game in progress; does nothing on the other screens
    void pause() {
        if (screen == Screen.PLAYING) screen = Screen.PAUSED;
    }

    // one step of the game; dt is seconds since the last step
    void update(double dt, Input input) {
        switch (screen) {
            case TITLE -> {
                // Esc does nothing here, so tapping it twice on game over can't close the whole game
                if (input.wasPressed(KeyEvent.VK_ENTER)) startNewGame();
            }
            case PLAYING -> {
                if (input.wasPressed(KeyEvent.VK_ESCAPE)) {
                    screen = Screen.PAUSED;
                    return;
                }
                updateWorld(dt, input);
                if (player.isDead()) screen = Screen.GAME_OVER;
            }
            case PAUSED -> {
                if (input.wasPressed(KeyEvent.VK_ESCAPE)) screen = Screen.PLAYING;
                else if (input.wasPressed(KeyEvent.VK_Q)) screen = Screen.TITLE;
            }
            case GAME_OVER -> {
                if (input.wasPressed(KeyEvent.VK_ENTER)) startNewGame();
                else if (input.wasPressed(KeyEvent.VK_ESCAPE)) screen = Screen.TITLE;
            }
        }
    }

    private void updateWorld(double dt, Input input) {
        // holding both opposite keys cancels out instead of one silently winning
        int dirX = 0;
        int dirY = 0;
        if (input.isDown(KeyEvent.VK_A, KeyEvent.VK_LEFT)) dirX -= 1;
        if (input.isDown(KeyEvent.VK_D, KeyEvent.VK_RIGHT)) dirX += 1;
        if (input.isDown(KeyEvent.VK_W, KeyEvent.VK_UP)) dirY -= 1;
        if (input.isDown(KeyEvent.VK_S, KeyEvent.VK_DOWN)) dirY += 1;
        boolean running = input.isDown(KeyEvent.VK_SHIFT);
        boolean dodge = input.wasPressed(KeyEvent.VK_SPACE);

        player.control(dirX, dirY, running, dodge);

        // a copy, so an entity can spawn others during its update
        for (Entity entity : new ArrayList<>(entities)) {
            entity.update(dt, this);
        }
        entities.removeIf(entity -> entity.removed);
    }

    void draw(Graphics2D g) {
        g.drawImage(background, 0, 0, null);
        if (screen != Screen.TITLE) {
            entities.sort(Comparator.comparingDouble(Entity::depth));
            for (Entity entity : entities) {
                entity.draw(g, this);
            }
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        switch (screen) {
            case TITLE -> {
                shade(g, new Color(0, 0, 0, 160));
                centredText(g, "PROJECT Z-D", TITLE_FONT, 190);
                centredText(g, "Enter to play", HINT_FONT, 260);
            }
            case PLAYING -> { }
            case PAUSED -> {
                shade(g, new Color(0, 0, 0, 140));
                centredText(g, "PAUSED", HEADING_FONT, 200);
                centredText(g, "Esc to resume", HINT_FONT, 250);
                centredText(g, "Q to quit to the title screen", HINT_FONT, 280);
            }
            case GAME_OVER -> {
                shade(g, new Color(90, 0, 0, 170));
                centredText(g, "YOU DIED", HEADING_FONT, 200);
                centredText(g, "Enter to play again", HINT_FONT, 250);
                centredText(g, "Esc for the title screen", HINT_FONT, 280);
            }
        }
    }

    private static void shade(Graphics2D g, Color color) {
        g.setColor(color);
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    // draws text centred across the screen with its baseline at y, with a drop shadow so it reads on any background
    private static void centredText(Graphics2D g, String text, Font font, int y) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        int x = (WIDTH - metrics.stringWidth(text)) / 2;
        g.setColor(Color.BLACK);
        g.drawString(text, x + 2, y + 2);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    private static BufferedImage loadImage(String filename) {
        try {
            return ImageIO.read(new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
