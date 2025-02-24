import java.awt.Image;

public class Spaceship extends Sprite2D { // child class of Sprite2D, hence possesses methods/fields from superclass
    public Spaceship(Image i, int windowWidth) { // only takes in an image, and window width
        super(i, null, windowWidth); // calling to superclass, setting to null since we don't need a second image for spaceship
    }

    public void move() {
        x+=xSpeed; // move them left/right
        if (x < 15) { // out of boundary
            x = 15; // set it to the boundary, (technically) that's the furthest left you could go with the spaceship
        }

        else if (x > windowWidth - myImage.getWidth(null) - 15) { // out of boundary
            x = windowWidth - myImage.getWidth(null) - 15; // this is the furthest right you could go with the spaceship
        }
    }
}
