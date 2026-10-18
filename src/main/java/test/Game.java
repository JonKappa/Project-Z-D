package test;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// The game world: everything in it, and one update step. Knows nothing about Swing timers or windows.
public class Game {

    static final int WIDTH = 640;
    static final int HEIGHT = 480;

    final Sprite sprites;
    final BufferedImage background;
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

        player.update(dt, dirX, dirY, running, dodge, WIDTH, HEIGHT);
    }

    void draw(Graphics2D g) {
        g.drawImage(background, 0, 0, null);
        drawPlayer(g);
    }

    private void drawPlayer(Graphics2D g) {
        // the left-side facings have no frames of their own; they draw their right-side twin mirrored
        int direction = switch (player.facing) {
            case DOWN -> Sprite.DOWN;
            case DOWN_RIGHT, DOWN_LEFT -> Sprite.DOWN_RIGHT;
            case RIGHT, LEFT -> Sprite.RIGHT;
            case UP_RIGHT, UP_LEFT -> Sprite.UP_RIGHT;
            case UP -> Sprite.UP;
        };
        boolean mirrored = player.facing.dirX < 0;
        BufferedImage[] frames = sprites.animation[direction][player.state.animationIndex];
        int frame = player.frame;
        if (mirrored && (player.state == Player.State.WALK || player.state == Player.State.RUN)) {
            // mirroring swaps which foot is in front, so a mirrored stride is shifted by half a cycle;
            // that keeps the same foot forward when turning between down and down-left, or up and up-left
            frame = (frame + frames.length / 2) % frames.length;
        }
        BufferedImage img = frames[frame];
        int x = (int) player.x;
        int y = (int) player.y;
        if (mirrored) {
            g.drawImage(img, x + 2 * Sprite.CENTER_X, y, -img.getWidth(), img.getHeight(), null);
        }
        else {
            g.drawImage(img, x, y, img.getWidth(), img.getHeight(), null);
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
