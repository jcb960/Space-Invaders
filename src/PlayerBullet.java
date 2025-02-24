import java.awt.Image;

public class PlayerBullet extends Sprite2D{ // child class of Sprite2D, hence possesses methods/fields from superclass
    public PlayerBullet(Image i, int windowWidth) { // // only takes in an image, and window width
        super(i, null, windowWidth); // calling to superclass, setting to null since we don't need a second image for player bullet
    }

    public void move() {
        y = y - xSpeed; // since they're moving up the screen, we subtract xSpeed from the 'y' (height)
    }

}
