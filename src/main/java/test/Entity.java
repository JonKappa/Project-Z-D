package test;

import java.awt.Graphics2D;

// Anything that lives in the world and gets updated and drawn every tick: the player now,
// zombies, bullets and pickups later.
public abstract class Entity {

    double x; // top-left corner of the sprite, in pixels
    double y;
    final int width;
    final int height;
    int health;
    boolean removed; // set to true and Game drops it at the end of the tick

    Entity(double x, double y, int width, int height, int health) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = health;
    }

    // dt is seconds since the last tick
    abstract void update(double dt, Game game);

    abstract void draw(Graphics2D g, Game game);

    boolean isDead() {
        return health <= 0;
    }

    // things lower on the screen are nearer the camera, so they're drawn last
    double depth() {
        return y + height;
    }
}
