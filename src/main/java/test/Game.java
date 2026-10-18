package test;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

// The game world: everything in it, and one update step. Knows nothing about Swing timers or windows.
public class Game {

    static final int WIDTH = 640;
    static final int HEIGHT = 480;

    final Sprite sprites;
    final BufferedImage background;
    final List<Entity> entities = new ArrayList<>();
    Player player;

    Game() {
        sprites = new Sprite();
        background = loadImage("./Sprites/Background/testBackground.png");

        // every direction has the same number of frames per animation
        BufferedImage[][] down = sprites.animation[Sprite.DOWN];
        int[] frameCounts = new int[down.length];
        for (int a = 0; a < frameCounts.length; a++) {
            frameCounts[a] = down[a].length;
        }
        BufferedImage idle = down[Player.State.IDLE.animationIndex][0];
        player = new Player(0, 0, idle.getWidth(), idle.getHeight(), frameCounts);
        entities.add(player);
    }

    // one step of the world; dt is seconds since the last step
    void update(double dt, Input input) {
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
        entities.sort(Comparator.comparingDouble(Entity::depth));
        for (Entity entity : entities) {
            entity.draw(g, this);
        }
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
