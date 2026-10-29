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
import java.util.Map;
import javax.imageio.ImageIO;

// The game world: everything in it, which screen is showing, and one update step.
// Knows nothing about Swing timers or windows.
public class Game {

    static final int WIDTH = 640;
    static final int HEIGHT = 480;

    enum Screen { TITLE, PLAYING, PAUSED, GAME_OVER, CONTROLS, SETTINGS }

    // each row of the Controls screen: what it does, then the keys
    private static final String[][] CONTROLS = {
        {"Move", "W A S D or arrow keys"},
        {"Dodge roll", "Space"},
        {"Pause", "Esc"},
        {"Menus", "Arrows or W/S, Enter, or the mouse"},
    };

    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 48);
    private static final Font HEADING_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 36);
    private static final Font TEXT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
    private static final Font TEXT_BOLD_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 18);

    final Settings settings;
    final Sprite sprites;
    final BufferedImage background;
    final List<Entity> entities = new ArrayList<>();
    private final int[] playerFrameCounts;
    private final int playerWidth;
    private final int playerHeight;
    Player player;
    Screen screen = Screen.TITLE;
    Runnable onDisplayChange = () -> { }; // the window sets this to re-apply fullscreen and window size
    private final Map<Screen, Menu> menus;
    private Screen backTo = Screen.TITLE; // where Back goes from Controls or Settings (the title or pause menu)

    Game() {
        settings = Settings.load();
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

        menus = Map.of(
            Screen.TITLE, new Menu(230, List.of(
                new Menu.Item("Play", this::startNewGame),
                new Menu.Item("Controls", () -> openFrom(Screen.CONTROLS)),
                new Menu.Item("Settings", () -> openFrom(Screen.SETTINGS)),
                new Menu.Item("Quit", () -> System.exit(0)))),
            Screen.PAUSED, new Menu(230, List.of(
                new Menu.Item("Resume", () -> show(Screen.PLAYING)),
                new Menu.Item("Controls", () -> openFrom(Screen.CONTROLS)),
                new Menu.Item("Settings", () -> openFrom(Screen.SETTINGS)),
                new Menu.Item("Quit to title", () -> show(Screen.TITLE)))),
            Screen.GAME_OVER, new Menu(230, List.of(
                new Menu.Item("Play again", this::startNewGame),
                new Menu.Item("Title screen", () -> show(Screen.TITLE)))),
            Screen.CONTROLS, new Menu(390, List.of(
                new Menu.Item("Back", () -> goBack()))),
            Screen.SETTINGS, new Menu(150, 380, List.of(
                Menu.Item.setting(() -> "Fullscreen: " + (settings.fullscreen ? "On" : "Off"), dir -> {
                    settings.fullscreen = !settings.fullscreen;
                    settings.save();
                    onDisplayChange.run();
                }),
                Menu.Item.setting(() -> "Window size: " + settings.windowScale + "x", dir -> {
                    // wraps round: Right or Enter on 3x goes back to 1x
                    int count = Settings.MAX_WINDOW_SCALE;
                    settings.windowScale = (settings.windowScale - 1 + dir + count) % count + 1;
                    settings.save();
                    onDisplayChange.run();
                }),
                Menu.Item.slider("Music volume", () -> settings.musicVolume,
                        v -> { settings.musicVolume = v; settings.save(); }, Settings.VOLUME_STEP),
                Menu.Item.slider("Effects volume", () -> settings.effectsVolume,
                        v -> { settings.effectsVolume = v; settings.save(); }, Settings.VOLUME_STEP),
                new Menu.Item("Back", () -> goBack()))));
    }

    // opens a screen reached from the title or pause menu, remembering which one to go back to
    private void openFrom(Screen next) {
        backTo = screen;
        show(next);
    }

    // back to the menu Controls or Settings was opened from, with the same button still highlighted
    private void goBack() {
        screen = backTo;
    }

    // true while a game is under way, including its pause menu and the screens opened from it,
    // so the world is drawn behind them
    private boolean inGame() {
        return screen == Screen.PLAYING || screen == Screen.PAUSED || screen == Screen.GAME_OVER
                || ((screen == Screen.CONTROLS || screen == Screen.SETTINGS) && backTo == Screen.PAUSED);
    }

    // switches screen, and opens its menu (if it has one) with the first button highlighted
    void show(Screen next) {
        screen = next;
        Menu menu = menus.get(next);
        if (menu != null) menu.reset();
    }

    // clears the world and puts a fresh player in the middle of the screen
    void startNewGame() {
        entities.clear();
        player = new Player((WIDTH - playerWidth) / 2.0, (HEIGHT - playerHeight) / 2.0,
                playerWidth, playerHeight, playerFrameCounts);
        entities.add(player);
        show(Screen.PLAYING);
    }

    // pauses a game in progress; does nothing on the other screens
    void pause() {
        if (screen == Screen.PLAYING) show(Screen.PAUSED);
    }

    // one step of the game; dt is seconds since the last step
    void update(double dt, Input input) {
        switch (screen) {
            case PLAYING -> {
                if (input.wasPressed(KeyEvent.VK_ESCAPE)) {
                    show(Screen.PAUSED);
                    return;
                }
                updateWorld(dt, input);
                if (player.isDead()) show(Screen.GAME_OVER);
            }
            case PAUSED -> {
                // Esc goes back to the game; it does nothing on the title or game over screens,
                // so a double tap can't skip past a menu
                if (input.wasPressed(KeyEvent.VK_ESCAPE)) show(Screen.PLAYING);
                else menus.get(screen).update(input);
            }
            case CONTROLS, SETTINGS -> {
                if (input.wasPressed(KeyEvent.VK_ESCAPE)) goBack();
                else menus.get(screen).update(input);
            }
            case TITLE, GAME_OVER -> menus.get(screen).update(input);
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
        boolean dodge = input.wasPressed(KeyEvent.VK_SPACE);

        player.control(dirX, dirY, dodge);

        // a copy, so an entity can spawn others during its update
        for (Entity entity : new ArrayList<>(entities)) {
            entity.update(dt, this);
        }
        entities.removeIf(entity -> entity.removed);
    }

    void draw(Graphics2D g) {
        g.drawImage(background, 0, 0, null);
        if (inGame()) {
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
            }
            case PLAYING -> { }
            case PAUSED -> {
                shade(g, new Color(0, 0, 0, 140));
                centredText(g, "PAUSED", HEADING_FONT, 190);
            }
            case GAME_OVER -> {
                shade(g, new Color(90, 0, 0, 170));
                centredText(g, "YOU DIED", HEADING_FONT, 190);
            }
            case CONTROLS -> {
                shade(g, new Color(0, 0, 0, 170));
                centredText(g, "CONTROLS", HEADING_FONT, 90);
                drawControls(g);
            }
            case SETTINGS -> {
                shade(g, new Color(0, 0, 0, 170));
                centredText(g, "SETTINGS", HEADING_FONT, 90);
            }
        }
        Menu menu = menus.get(screen);
        if (menu != null) menu.draw(g);
    }

    // the action names right-aligned to the middle of the screen, the keys left-aligned after them
    private static void drawControls(Graphics2D g) {
        int middle = WIDTH / 2 - 70;
        for (int i = 0; i < CONTROLS.length; i++) {
            int y = 150 + i * 40;
            g.setFont(TEXT_BOLD_FONT);
            FontMetrics metrics = g.getFontMetrics();
            shadowText(g, CONTROLS[i][0], middle - metrics.stringWidth(CONTROLS[i][0]), y, Color.WHITE);
            g.setFont(TEXT_FONT);
            shadowText(g, CONTROLS[i][1], middle + 20, y, new Color(220, 220, 220));
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
        shadowText(g, text, (WIDTH - metrics.stringWidth(text)) / 2, y, Color.WHITE);
    }

    private static void shadowText(Graphics2D g, String text, int x, int y, Color color) {
        g.setColor(Color.BLACK);
        g.drawString(text, x + 2, y + 2);
        g.setColor(color);
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
