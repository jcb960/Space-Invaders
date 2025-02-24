import java.awt.Image;
import java.awt.*; // for paint method

public class Alien extends Sprite2D { // child class of Sprite2D
    private int framesDrawn;
    public boolean isAlive = true;

    public Alien(Image i, Image i2, int windowWidth) {
        super(i, i2, windowWidth);
    } // calling the superclass constructor from within a subclass constructor by using super()

    // public interface
    public boolean move() {
        if (x < 15) {
            return true; // true if the alien object has gone below 15 pixels i.e. hit the edge
        }
        else if (x > windowWidth - myImage.getWidth(null) - 15) {
            return true; // subtracting the width of image as well because (x, y) is the top left of the image
            // so if x is above 15 px (from the right), it has hit the edge, so return true
        }
        else { // the alien object is within the boundaries
            x += xSpeed; // so increase their position on the x-axis.
            // Note: if needed to go from right to left, xSpeed will be negative. So x += xSpeed will make the images go right to left
            return false; // not hit the edge
        }
    }

    public void reverseDirection() {
        xSpeed*=-1; // to indicate that we are moving in the opposite direction now, but keeping the same speed hence *-1
        x+=xSpeed; // surpassed the boundary, so get it back in the boundary, note += xSpeed, because xSpeed *= -1 has done been done before that
        y+=15; // move the alien objects further down the frame
    }
    @Override
    public void paint(Graphics g) { // When calling alien objects, this is the pain method they're going to use
        // to alternate between moving alien images
        framesDrawn++; // to determine which image needs to be used
        if (framesDrawn % 100 < 50) // image cycles every second i.e. hits 50 frames
            g.drawImage(myImage, (int)x, (int)y, null);
        else
            g.drawImage(myImage2, (int)x, (int)y, null);
    }
}
