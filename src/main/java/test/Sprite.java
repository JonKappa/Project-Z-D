package test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Sprite {

    // which set of frames to draw; the left-side facings reuse the right-side frames mirrored
    static final int DOWN = 0;
    static final int DOWN_RIGHT = 1;
    static final int RIGHT = 2;
    static final int UP_RIGHT = 3;
    static final int UP = 4;

    // the middle of the character in every frame; the art sits left of the frame's own centre,
    // so mirroring around this keeps the character in place when it turns left or right
    static final int CENTER_X = 36;

    BufferedImage[][][] animation; // [direction][animation][frame], animation index is Player.State.animationIndex

    Sprite() {
        String[] directions = {"Down", "DownRight", "Right", "UpRight", "Up"}; // same order as the constants above
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