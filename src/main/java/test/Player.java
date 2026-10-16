package test;

// Brad's position, movement and which animation frame to show. Knows nothing about keys or drawing.
public class Player {

    enum State {
        IDLE(0, 0.2), WALK(1, 0.1), RUN(2, 0.1), DODGE(3, 0.075);

        final int animationIndex; // index into Sprite.animation
        final double frameTime;   // seconds each animation frame stays on screen

        State(int animationIndex, double frameTime) {
            this.animationIndex = animationIndex;
            this.frameTime = frameTime;
        }
    }

    enum Facing { DOWN, UP, LEFT, RIGHT }

    static final double WALK_SPEED = 70;      // pixels per second
    static final double RUN_SPEED = 140;
    static final double DODGE_SPEED = 200;    // a roll lasts 8 frames x 0.075 s, so it covers about 120 px

    double x;
    double y;
    final int width;
    final int height;
    Facing facing = Facing.DOWN;
    State state = State.IDLE;
    int frame = 0;
    private double frameTimer = 0;
    private int dodgeX; // direction of the current roll
    private int dodgeY;
    private final int[] frameCounts; // frames in each animation, so the frame number wraps correctly

    Player(double x, double y, int width, int height, int[] frameCounts) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.frameCounts = frameCounts;
    }

    // dirX and dirY are -1, 0 or 1; dt is seconds since the last update
    void update(double dt, int dirX, int dirY, boolean running, boolean dodgePressed, int maxX, int maxY) {
        if (state != State.DODGE && dodgePressed) {
            startDodge(dirX, dirY);
        }
        if (state == State.DODGE) {
            move(dodgeX, dodgeY, DODGE_SPEED, dt, maxX, maxY);
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
        State newState = !moving ? State.IDLE : running ? State.RUN : State.WALK;

        if (moving) {
            move(dirX, dirY, running ? RUN_SPEED : WALK_SPEED, dt, maxX, maxY);
        }
        face(dirX, dirY);

        if (newState != state) {
            state = newState;
            frame = 0;
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
            switch (facing) {
                case LEFT: dirX = -1; break;
                case RIGHT: dirX = 1; break;
                case UP: dirY = -1; break;
                default: dirY = 1; break;
            }
        }
        face(dirX, dirY);
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

    // sideways wins on diagonals, since there are no diagonal sprites
    private void face(int dirX, int dirY) {
        if (dirX != 0) {
            facing = dirX < 0 ? Facing.LEFT : Facing.RIGHT;
        } else if (dirY != 0) {
            facing = dirY < 0 ? Facing.UP : Facing.DOWN;
        }
    }
}
