package test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Sprite {

    BufferedImage[][] animation;
    BufferedImage[] playerIdle;
    BufferedImage[] playerWalking;
    BufferedImage[] playerRunning;
    BufferedImage[] playerPunch;

    Sprite() {
        playerIdle = new BufferedImage[4];
        playerWalking = new BufferedImage[8];
        playerRunning = new BufferedImage[8];
        playerPunch = new BufferedImage[4];
        animation = new BufferedImage[4][];

        for (int i = 0; i < playerIdle.length; i++) {
            playerIdle[i] = createImage(".\\Sprites\\Brad\\Idle\\idle"+(i+1)+".png");
        }
        for (int i = 0; i < playerWalking.length; i++) {
            playerWalking[i] = createImage(".\\Sprites\\Brad\\Walk\\walking"+(i+1)+".png");
        }
        for (int i = 0; i < playerRunning.length; i++) {
            playerRunning[i] = createImage(".\\Sprites\\Brad\\Run\\run"+(i+1)+".png");
        }

        animation[0] = playerIdle;
        animation[1] = playerWalking;
        animation[2] = playerRunning;
        animation[3] = playerPunch;


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