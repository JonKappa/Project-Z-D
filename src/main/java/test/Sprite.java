package test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Sprite {

    // which set of frames to draw; facing left reuses SIDE (the down-right frames) mirrored
    static final int DOWN = 0;
    static final int SIDE = 1;
    static final int UP = 2;

    BufferedImage[][][] animation; // [direction][animation][frame], animation index is Player.State.animationIndex

    Sprite() {
        String[] directions = {"Down", "DownRight", "Up"};
        animation = new BufferedImage[directions.length][][];
        for (int d = 0; d < directions.length; d++) {
            String folder = "./Sprites/Survivor/" + directions[d];
            animation[d] = new BufferedImage[][] {
                loadFrames(folder + "/Idle/idle", 4),
                loadFrames(folder + "/Walk/walking", 8),
                loadFrames(folder + "/Run/run", 8),
                loadFrames(folder + "/Dodge/dodge", 8),
            };
        }
    }

    private BufferedImage[] loadFrames(String prefix, int count) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            frames[i] = createImage(prefix + (i + 1) + ".png");
        }
        return frames;
    }

    private BufferedImage createImage(String filename){
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new File(filename));
            return bufferedImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}