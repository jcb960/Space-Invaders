import java.awt.*;

public class Sprite2D {
    // there are going to be subclasses: Alien and Spaceship and PlayerBullet, so general data will be stored here that is common to both
    // member data
    // 'protected' so its child classes can access them
    protected double x, y; // x and y positions
    protected double xSpeed = 0; // all object start off with speed 0 unless set to a different one
    protected Image myImage; // to alternate between images for aliens
    protected Image myImage2;
    int windowWidth; // to get the images in the specified frame only

    // constructor
    public Sprite2D(Image i, Image i2, int windowWidth) { // takes in 2 images (for aliens only), and the window width
        myImage = i;
        myImage2 = i2;
        this.windowWidth = windowWidth;
    }

    public void setPosition(double xx, double yy) { // set the initial position for the image objects
        x = xx;
        y = yy;
    }

    public void setXSpeed(double dx) {
        xSpeed = dx; // setting the speed to 6/-6 or whatever when moving right/left
    }

    public void paint(Graphics g) { // paint the image of the object
        g.drawImage(myImage, (int)x, (int)y, null);
        // draw the image, where it's top left is at (x, y)
    }
    // getter methods no longer needed (used in Assignment 4)
}
