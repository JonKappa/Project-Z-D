package test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// The player's position, movement, animation frame and drawing. Knows nothing about keys:
// Game reads them and passes them in with control() before each update.
public class Player extends Entity {

    enum State {
        IDLE(0, 0.2), RUN(1, 0.1), DODGE(2, 0.075);

        final int animationIndex; // index into Sprite.animation
        final double frameTime;   // seconds each animation frame stays on screen

        State(int animationIndex, double frameTime) {
            this.animationIndex = animationIndex;
            this.frameTime = frameTime;
        }
    }

    enum Facing {
        DOWN(0, 1), DOWN_RIGHT(1, 1), RIGHT(1, 0), UP_RIGHT(1, -1),
        UP(0, -1), UP_LEFT(-1, -1), LEFT(-1, 0), DOWN_LEFT(-1, 1);

        final int dirX; // the key direction that gives this facing, -1, 0 or 1 like update's dirX and dirY
        final int dirY;

        Facing(int dirX, int dirY) {
            this.dirX = dirX;
            this.dirY = dirY;
        }
    }

    static final double RUN_SPEED = 140;      // pixels per second
    static final double DODGE_SPEED = 200;    // a roll lasts 8 frames x 0.075 s, so it covers about 120 px
    // speed of each dodge frame as a share of DODGE_SPEED: slow while the boots are planted
    // (crouch, land, stand up) so they don't skate, fast while diving and rolling; averages 1
    static final double[] DODGE_PACE = {0.4, 1.3, 1.4, 1.4, 1.4, 1.3, 0.6, 0.2};
    static final double DIAGONAL_GRACE = 0.1; // seconds a diagonal facing survives one of its two keys being let go
    static final int MAX_HEALTH = 100;

    Facing facing = Facing.DOWN;
    State state = State.IDLE;
    int frame = 0;
    private double frameTimer = 0;
    private int dodgeX; // direction of the current roll
    private int dodgeY;
    private Facing pendingFacing; // straight facing waiting out DIAGONAL_GRACE after a diagonal
    private double pendingTimer;
    private final int[] frameCounts; // frames in each animation, so the frame number wraps correctly

    // this tick's controls, set by Game before update
    private int dirX; // -1, 0 or 1
    private int dirY;
    private boolean dodgePressed;

    Player(double x, double y, int width, int height, int[] frameCounts) {
        super(x, y, width, height, MAX_HEALTH);
        this.frameCounts = frameCounts;
    }

    // dirX and dirY are -1, 0 or 1
    void control(int dirX, int dirY, boolean dodgePressed) {
        this.dirX = dirX;
        this.dirY = dirY;
        this.dodgePressed = dodgePressed;
    }

    @Override
    void update(double dt, Game game) {
        int maxX = game.viewWidth;
        int maxY = game.viewHeight;
        if (state != State.DODGE && dodgePressed) {
            startDodge(dirX, dirY);
        }
        if (state == State.DODGE) {
            move(dodgeX, dodgeY, DODGE_SPEED * DODGE_PACE[frame], dt, maxX, maxY);
            frameTimer += dt;
            while (frameTimer >= state.frameTime) {
                frameTimer -= state.frameTime;
                frame++;
            }
            if (frame < frameCounts[state.animationIndex]) {
                return; // a roll can't be steered or cancelled once it starts
            }
            state = State.IDLE; // roll finished; fall through and pick up whatever keys are held now
            frame = 0;
            frameTimer = 0;
        }

        boolean moving = dirX != 0 || dirY != 0;
        State newState = moving ? State.RUN : State.IDLE;

        if (moving) {
            move(dirX, dirY, RUN_SPEED, dt, maxX, maxY);
        }
        face(dirX, dirY, dt);

        if (newState != state) {
            state = newState;
            // the left-side facings play their strides half a cycle on (see draw), so a stride
            // starting there starts half a cycle in, and every facing steps off on the same frame
            boolean mirroredStride = state == State.RUN && facing.dirX < 0;
            frame = mirroredStride ? frameCounts[state.animationIndex] / 2 : 0;
            frameTimer = 0;
        }
        frameTimer += dt;
        while (frameTimer >= state.frameTime) {
            frameTimer -= state.frameTime;
            frame = (frame + 1) % frameCounts[state.animationIndex];
        }
    }

    // rolls the way the keys point, or the way Brad is facing if none are held
    private void startDodge(int dirX, int dirY) {
        if (dirX == 0 && dirY == 0) {
            dirX = facing.dirX;
            dirY = facing.dirY;
        }
        facing = facingFor(dirX, dirY); // a roll turns straight away, no grace
        pendingFacing = null;
        dodgeX = dirX;
        dodgeY = dirY;
        state = State.DODGE;
        frame = 0;
        frameTimer = 0;
    }

    private void move(int dirX, int dirY, double speed, double dt, int maxX, int maxY) {
        double length = Math.sqrt(dirX * dirX + dirY * dirY); // keeps diagonals the same speed as straight lines
        x += dirX / length * speed * dt;
        y += dirY / length * speed * dt;
        x = Math.max(0, Math.min(x, maxX - width));
        y = Math.max(0, Math.min(y, maxY - height));
    }

    // Faces the way the keys point; with no keys held Brad keeps his last facing. Nobody lets go
    // of two keys in the same tick, so going from a diagonal to one of its own straight halves
    // waits a moment first, otherwise stopping after a diagonal would almost never stay diagonal.
    private void face(int dirX, int dirY, double dt) {
        Facing target = facingFor(dirX, dirY);
        if (target == null) {
            pendingFacing = null;
            return;
        }
        boolean diagonal = facing.dirX != 0 && facing.dirY != 0;
        boolean halfOfIt = (target.dirX == facing.dirX && target.dirY == 0)
                || (target.dirY == facing.dirY && target.dirX == 0);
        if (!diagonal || !halfOfIt) {
            facing = target;
            pendingFacing = null;
            return;
        }
        if (pendingFacing != target) {
            pendingFacing = target;
            pendingTimer = 0;
        }
        pendingTimer += dt;
        if (pendingTimer >= DIAGONAL_GRACE) {
            facing = target;
            pendingFacing = null;
        }
    }

    // the facing that matches a key direction, or null when no keys are held
    private static Facing facingFor(int dirX, int dirY) {
        for (Facing f : Facing.values()) {
            if (f.dirX == dirX && f.dirY == dirY) {
                return f;
            }
        }
        return null;
    }

    @Override
    void draw(Graphics2D g, Game game) {
        // the left-side facings have no frames of their own; they draw their right-side twin mirrored
        int direction = switch (facing) {
            case DOWN -> Sprite.DOWN;
            case DOWN_RIGHT, DOWN_LEFT -> Sprite.DOWN_RIGHT;
            case RIGHT, LEFT -> Sprite.RIGHT;
            case UP_RIGHT, UP_LEFT -> Sprite.UP_RIGHT;
            case UP -> Sprite.UP;
        };
        boolean mirrored = facing.dirX < 0;
        BufferedImage[] frames = game.sprites.animation[direction][state.animationIndex];
        int shown = frame;
        if (mirrored && state == State.RUN) {
            // mirroring swaps which foot is in front, so a mirrored stride is shifted by half a cycle;
            // that keeps the same foot forward when turning between down and down-left, or up and up-left
            shown = (shown + frames.length / 2) % frames.length;
        }
        BufferedImage img = frames[shown];
        int drawX = (int) x;
        int drawY = (int) y;
        if (mirrored) {
            g.drawImage(img, drawX + 2 * Sprite.CENTER_X, drawY, -img.getWidth(), img.getHeight(), null);
        }
        else {
            g.drawImage(img, drawX, drawY, img.getWidth(), img.getHeight(), null);
        }
    }
}
