package test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Sprite {

    // folder, file prefix, frame count; order sets the animation index (0 idle, 1 walk, 2 run, 3 punch, ...)
    static final Object[][] ANIMATIONS = {
            {"Idle", "idle", 4}, {"Walk", "walking", 8}, {"Run", "run", 8}, {"Punch", "punch", 4},
            {"Kick", "kick", 5}, {"Dash", "dash", 9}, {"Taunt", "taunt", 13}, {"Slide", "slide", 5},
            {"JumpKick", "jumpkick", 8}, {"Combo", "combo", 12}, {"Uppercut", "uppercut", 8}, {"Palm", "palm", 8},
            {"Hurt", "hurt", 7}, {"Knockback", "knockback", 6}, {"KnockedDown", "knockeddown", 9}, {"GetUp", "getup", 10},
            {"Block", "block", 6}, {"Guard", "guard", 3}, {"GroundHurt", "groundhurt", 5}, {"GroundKick", "groundkick", 5},
            {"Grabbed", "grabbed", 2}, {"Stagger", "stagger", 5}, {"Thrown", "thrown", 6}, {"HeadScratch", "headscratch", 11},
            {"Stunned", "stunned", 4},
    };

    BufferedImage[][] animation;
    String[] names;

    Sprite() {
        animation = new BufferedImage[ANIMATIONS.length][];
        names = new String[ANIMATIONS.length];

        for (int a = 0; a < ANIMATIONS.length; a++) {
            names[a] = (String) ANIMATIONS[a][0];
            animation[a] = new BufferedImage[(int) ANIMATIONS[a][2]];
            for (int i = 0; i < animation[a].length; i++) {
                animation[a][i] = createImage("Sprites/Brad/" + names[a] + "/" + ANIMATIONS[a][1] + (i + 1) + ".png");
            }
        }
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
