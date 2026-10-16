package test;

// Brad's position, movement and which animation frame to show. Knows nothing about keys or drawing.
public class Player {

    enum State {
        IDLE(0), WALK(1), RUN(2);

        final int animationIndex; // index into Sprite.animation

        State(int animationIndex) {
            this.animationIndex = animationIndex;
        }
    }

    enum Facing { DOWN, UP, LEFT, RIGHT }

    static final double WALK_SPEED = 70;      // pixels per second
    static final double RUN_SPEED = 140;
    static final double FRAME_TIME = 0.1;     // seconds each animation frame stays on screen

    double x;
    double y;
    final int width;
    final int height;
    Facing facing = Facing.DOWN;
    State state = State.IDLE;
    int frame = 0;
    private double frameTimer = 0;
    private final int[] frameCounts; // frames in each animation, so the frame number wraps correctly

    Player(double x, double y, int width, int height, int[] frameCounts) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.frameCounts = frameCounts;
    }

    // dirX and dirY are -1, 0 or 1; dt is seconds since the last update
    void update(double dt, int dirX, int dirY, boolean running, int maxX, int maxY) {
        boolean moving = dirX != 0 || dirY != 0;
        State newState = !moving ? State.IDLE : running ? State.RUN : State.WALK;

        if (moving) {
            double speed = running ? RUN_SPEED : WALK_SPEED;
            double length = Math.sqrt(dirX * dirX + dirY * dirY); // keeps diagonals the same speed as straight lines
            x += dirX / length * speed * dt;
            y += dirY / length * speed * dt;
            x = Math.max(0, Math.min(x, maxX - width));
            y = Math.max(0, Math.min(y, maxY - height));
        }
        // sideways wins on diagonals, since there are no diagonal sprites
        if (dirX != 0) {
            facing = dirX < 0 ? Facing.LEFT : Facing.RIGHT;
        } else if (dirY != 0) {
            facing = dirY < 0 ? Facing.UP : Facing.DOWN;
        }

        if (newState != state) {
            state = newState;
            frame = 0;
            frameTimer = 0;
        }
        frameTimer += dt;
        while (frameTimer >= FRAME_TIME) {
            frameTimer -= FRAME_TIME;
            frame = (frame + 1) % frameCounts[state.animationIndex];
        }
    }
}
