import java.awt.*;
import java.awt.event.*; // for keyboard
import java.awt.image.BufferStrategy; // Render all graphics to an off-screen memory buffer
// When finished drawing a frame of animation, flip the
// off-screen buffer onscreen during the ‘vertical sync’ period
import javax.swing.*;
import java.util.ArrayList; // to store bullets
import java.util.Iterator; // to remove bullets from the array while iterating through it, but it functions properly

// NOTE: collision checks between bullet and aliens happen in the paint method for convenience
// Allows multiple bullets in frame at once

public class InvadersApplication extends JFrame implements Runnable, KeyListener { //Runnable for threads, Key listener for keyboard events
    private static final Dimension WindowSize = new Dimension(800, 600);
    private static boolean isGraphicsInitialized = false; // used otherwise, it will try to paint uninitialized objects
    private final BufferStrategy strategy;
    private static final int NUMALIENS = 30;
    private final Alien[] AliensArray = new Alien[NUMALIENS];
    private final boolean[] hitEdgeArray = new boolean[NUMALIENS]; // used to determine which alien image object hit/surpassed the boundary, true if that's the case
    private final Spaceship PlayerShip;
    private boolean rightBool = false; // for the left array key (false = not pressed)
    private boolean leftBool = false; // for the right arrow key
    private boolean spaceBool = false; // player shooting bullets
    private boolean isGameRunning = false; // game loop condition
    private int bestScore = 0; // highest score gotten in one game (before GAME OVER appears)
    private int currentScore; // the current score of the game
    private final int scoreIncrementer; // for convenience, you only have to change the value of score gotten per alien kill once in the constructor
    private static final Font scoreFont = new Font("Times", Font.PLAIN,25); // static final int, because it'll be kept being used in paint()
    private ArrayList<PlayerBullet> bulletsList = new ArrayList<>(); // array to store the bullets
    Iterator iterator; // iterator "list" to convert the array of bullets to an "iterator array" so we can remove items while looping through the array
    private int numAliensKilled; // number of aliens killed, if it equals NUMALIENS, you passed that round
    private boolean isStartingFrame = true; // starting a new wave and starting a new game is different, so if it's first frame, we are starting a new game, resets after a loss condition happens

    private final String workingDirectory = System.getProperty("user.dir"); // gets the user path to the project to where the file is being run
    String image1PathAliens = workingDirectory + "\\src\\ct255-images\\alien_ship_1.png\\"; // Using intelliJ, the image path in the folder
    String image2PathAliens = workingDirectory + "\\src\\ct255-images\\alien_ship_2.png\\";
    String imagePathPlayerShip = workingDirectory + "\\src\\ct255-images\\player_ship.png\\";
    String imagePathBullet = workingDirectory + "\\src\\ct255-images\\bullet.png\\";
    // relative address not working with mine so had to use absolute address, hence getting the user path


    // constructor of ImageIcon (from javax.swing) loads an image from disk (.jpg, .gif or .png) and returns it as a new instance of the ImageIcon class
    // The getImage() method of this ImageIcon object gives you a usable Image class object, which can be displayed in your paint() method by the Graphics class
    Image alienImage1 = (new ImageIcon(image1PathAliens)).getImage();
    Image alienImage2 = (new ImageIcon(image2PathAliens)).getImage();
    Image playerImage = (new ImageIcon(imagePathPlayerShip)).getImage();
    Image bulletImage = (new ImageIcon(imagePathBullet)).getImage();

    // now got the images set up
    // so we can now call instantiate alien objects by passing in the image of the alien_ship (and the dimension), when starting the game

    // constructor
    public InvadersApplication() {
        //Create and set up the window
        this.setTitle("Week 5 Assignment"); // sets the title of the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // when trying to exit from the window, the JFrame window stops and closes

        scoreIncrementer = 10; // initializing the score per alien killed
        startGame(0); // to initialize the alien objects, so they wouldn't be null in the paint() method (if it's null, it will cause errors although those errors won't affect the program)
        PlayerShip = new Spaceship(playerImage, WindowSize.width); // creating the player-ship, image = player_ship
        setPlayerInitialPos(); // a new game = spaceship's position goes back to the middle

        //Display the window, centered on the screen
        Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize(); // gets the dimension of the computer's screen in pixels
        int x = screensize.width/2 - WindowSize.width/2; // we are doing this so that the window pops up in the center of the screen (device's)
        int y = screensize.height/2 - WindowSize.height/2;
        setBounds(x, y, WindowSize.width, WindowSize.height); // (x, y) where the top-left of the window will be, then extend 600 horizontal and vertical from that (x, y)
        setVisible(true); // makes the screen visible on the screen

        createBufferStrategy(2); // specifies that double-buffering should be used
        strategy = getBufferStrategy(); // after creating the buffer strategy, we retrieve it using this method

        // this must be after setBounds() and setVisible(), because doing it before this, the component doesn't have valid drawing face, so buffer creation would fail
        // also if you do it before the setBounds(), it might create buffers with incorrect or default (possibly 0) dimensions

        addKeyListener(this); // send keyboard events arriving into this JFrame to its own event handlers
        isGraphicsInitialized = true; // it's now safe to paint the images

        Thread t = new Thread(this); // create animation by using Threads for this class, this = reference variable that refers to the current object (class) it's in
        t.start(); // start the animation
    }

    public void startGame(double speed) { // a method to initialize the wave/game with a speed parameter, alien's positions gets back to the beginning with higher speeds as rounds go on
        numAliensKilled = 0; // resetting the kill counter to 0 for a new wave/game
        // the initial positions of the alien objects
        // getting the alien images in a grid formation, jy = 70 (to accommodate scores at the top of the window) so it is visible on the frame
        for (int i = 0, jx = 15, jy = 70; i < NUMALIENS; i++) {
            AliensArray[i] = new Alien(alienImage1, alienImage2, WindowSize.width);
            AliensArray[i].setPosition(jx, jy); // setting their positions
            jx += alienImage1.getWidth(null) + 20; // alien image's width + 20 px, is where the next alien image starts on the x-axis
            // i.e. 20 px gaps between the alien images
            if (i % 5 == 4) { // After every 5th image
                jy += alienImage1.getHeight(null) + 15; // move the alien objects to the next row
                jx = 15; // reset it back to what jx originally was to get that rectangular formation
            }
            AliensArray[i].setXSpeed(speed); // set the speed for every alien object, for that wave to whatever was passed in
        }
    }

    public void setPlayerInitialPos() {
        // used after a new game starts, not waves
        // centering the image in the bottom middle of the frame (getting the positions)
        double playerPosX = (double) WindowSize.width /2 - (double) playerImage.getWidth(null) /2; // putting the image in the center
        double playerPosY = WindowSize.height-playerImage.getHeight(null)-25; // putting the image in the bottom
        PlayerShip.setPosition(playerPosX, playerPosY); // then set the player ship image on the frame
    }

    // thread's entry point
    @Override
    public void run() { // invoked by JVM when doing t.start()
        boolean isEdgeHit = false; // if any ONE alien object hits edge, set it to true, reset every run
        int bulletGapInFrames = 10, numFramesBulletInterval = bulletGapInFrames; // it starts off with having the ability to shoot the bullet
        // you can change the minimum distance between continuous bullets so they can't just spam the bullets
        boolean hasShot = false; // bullet has been shot, so we can decide when the next bullet can be shot
        int i; // for-loop counter
        double speed = 5; // aliens start game off with speed of 5
        numAliensKilled = 0; // letting to 0 here as well just in case
        while (true) { // game loop until user exits game
            try {
                // a try/catch block is used because sleep can throw an InterruptedException
                // which is the error that is caused when a program is stopped during the sleep period
                Thread.sleep(20); // pauses the program for 20 milliseconds (not stop the program completely)
                // doing it this way so the player_ship movement rate matches the thread sleeping rate, 50 FPS
                if (isGameRunning) { // start game
                    if (isStartingFrame) { // starting a new game (not a new wave)
                        bulletsList.clear(); // clears the array from previous game (if there is a previous game)
                        setPlayerInitialPos(); // set the player's position back to the middle
                        speed = 5; // reset the starting speed back to 5
                        startGame(speed); // start the with the speed
                        currentScore = 0; // reset the currentScore counter
                        isStartingFrame = false; // no longer the starting frame i.e. a new game started
                    }
                    if (rightBool) { // right key, move right
                        PlayerShip.setXSpeed(6); // set the speed
                        PlayerShip.move(); // move right, positive = go right
                    }
                    if (leftBool) { // left key, move left
                        PlayerShip.setXSpeed(-6); // set the speed in the negative direction i.e. goes left
                        PlayerShip.move(); // spaceship move left
                    }
                    if (spaceBool && numFramesBulletInterval == bulletGapInFrames) {
                        // player wants to shoot the bullet, but it only shoots the bullet if the distance between bullets are correct
                        shootBullet(); // shoot the bullet
                        numFramesBulletInterval = 0; // set it to 0, and wait until it hits bulletGapInFrames before allowed to shoot again

                        hasShot = true; // the player has shot the bullet, so set it true
                    }
                    // alien's moving

                    // An array to record which alien objects hit the edge is used because
                    // after every reverseDirection(), the gap between alien objects increases/decreases
                    // due to adding xSpeed to the ones on the edge (if hit the edge, retract the image back in the boundary, like x -= xSpeed)
                    // So effectively 0 change in positions, but for the ones that didn't hit the edge, they shift towards the ones that hit making them closer
                    // so by determining which ones are false, we can subtract the xSpeed that was added and ignore the ones that are true

                    for (i = 0; i < NUMALIENS; i++) { // moving alien object left to right, if hit edge, reverse direction, if hit edge, reverse direction again and so on
                        if (AliensArray[i].move()) { // if any of them hits the edge
                            hitEdgeArray[i] = true; // record which alien image(s) has hit the edge (plural images because they're in a column so all left/right side will hit the edge)
                            isEdgeHit = true; // to show at least ONE has hit the edge
                        } else {
                            hitEdgeArray[i] = false; // recording that alien object hasn't hit the edge in the array
                        }
                    }
                    if (isEdgeHit) { // at least one of the object has hit the boundary, so reverse ALL of their direction
                        // value at index i in NUMALIENS corresponds to values at i in hitEdgeArray
                        for (i = 0; i < NUMALIENS; i++) {
                            if (!hitEdgeArray[i]) { // it the alien objects weren't part of the hit the boundary group
                                // subtract the xSpeed from object's x position, but keep the y position (why we have getter methods)
                                AliensArray[i].setPosition(AliensArray[i].x - AliensArray[i].xSpeed, AliensArray[i].y);
                            }
                            AliensArray[i].reverseDirection(); // reverse the direction (in the opposite direction)
                        }
                        isEdgeHit = false; // reset the value to false
                    }
                    for (PlayerBullet bullet : bulletsList) { // move the bullet upwards the screen
                        bullet.move();
                    }
                    if (currentScore > bestScore) bestScore = currentScore; // if new best score has been reached
                    if (hasShot) numFramesBulletInterval++; // only increment the counter if the bullet has been shot
                    if (numFramesBulletInterval == bulletGapInFrames) hasShot = false; // player allowed to shoot now once the interval has passed
                    if (numAliensKilled == NUMALIENS) { // start a new wave
                        speed += 5; // increase the difficulty with higher speeds
                        startGame(speed); // start the game with the new speed
                    }
                }
                this.repaint(); // call the paint() with the new member data's (their changed positions)
            } catch (InterruptedException e) { // if the program is stopped from running during the sleep period, it will print the stack trace to the console
                e.printStackTrace();
            }
        }
    }

    // Three mandatory Keyboard Event-Handler functions
    @Override
    // when a key is pressed
    // explained in 'keyReleased' method
    public void keyPressed(KeyEvent e) {
        // when the pressed key is the right key
        // set it true, to move it in the run() (by the threads) we can match the movement rate of keyboard rate with the thread rate
        // game mechanics' keyboard inputs only set to true if game is running as well
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && isGameRunning) { // only make the player move right, if the game is running as well
            rightBool = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT && isGameRunning) { // left key
            leftBool = true; // indicate left key should move now, hence = true
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE && isGameRunning) { // space key
            spaceBool = true; // indicate that the player wants to shoot (keyboard's rate), so set it true
        }
        isGameRunning = true; // start the game after any key has been pressed
    }

    @Override
    // key release
    public void keyReleased(KeyEvent e) {
        // 'e' provides the 'virtual keycode' of the key that has triggered the event and constants are defined to matches these values
        // again notice, game mechanics' inputs only set to true if game is running
        // for right key
        // so when the pressed key corresponds to the right key (and left key below that)
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && isGameRunning) { //e.getKeyCode gets the ASCII value but as a virtual key code, then the predefined constants are KeyEvents.VK_RIGHT = right key, virtual key code
            rightBool = false; // no longer moving right, so set it to false
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT && isGameRunning) { // for left key
            leftBool = false; // no longer moving left, so set it to false
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE && isGameRunning) { // for space key (shooting)
            spaceBool = false; // spaceship no longer wants to shoot, so set it to false
        }
    }

    @Override // mandatory to override it, but we don't need it hence nothing in it
    public void keyTyped(KeyEvent e) {
    }

    // application's paint method
    @Override
    public void paint(Graphics g) {
        if (!isGraphicsInitialized) return; // to not try to paint null objects
        g = strategy.getDrawGraphics(); // redirect our drawing calls to the off-screen buffer
        if (isGameRunning) { // game started? if yes, go in here
            g.setColor(Color.BLACK); // set it to black
            g.fillRect(0, 0, getWidth(), getHeight()); // paint the whole canvas black
            g.setColor(Color.white); // for the text
            String scoreBoard = "Score: " + currentScore + "    Best: " + bestScore; // the text
            drawCenteredString(g, scoreBoard, scoreFont, WindowSize.height/10-5); // method to get put the text in the middle given the size of the text

            //doing this in the paint method for convenience
            iterator = bulletsList.iterator(); // and iterator is used so we can remove an item from the array while iterating through it
            // note that we have the while loop and then the for loop, this is for convenience, because even though the alien might be dead, we still go through the bullets (provided the alien is alive) leading to extra score
            while (iterator.hasNext()) { // while there is an item in the array
                PlayerBullet bullet = (PlayerBullet) iterator.next(); // cast the item to a PlayerBullet
                for (Alien alien : AliensArray) { // go through aliens
                    if (!alien.isAlive) continue; // continue, go to the next iteration, just ignore the ones that are dead
                    if (hasCollisionHappened1(alien, bullet)) { // collision between alien and bullet
                        alien.isAlive = false; // now dead
                        iterator.remove(); // remove item
                        numAliensKilled++; // increment aliens killed by 1
                        currentScore+=scoreIncrementer; // score added to the total current score
                    }
                }
            }
            checkCollisionAlienPlayerOrHitBottom(); // if the collision between alien and spaceship, or the alien reached the bottom
            // added the condition where alien it hits the bottom, otherwise the spaceship can dodge the bullets, resulting in an endless game loop

            for (Alien alien : AliensArray) {
                if (alien.isAlive) alien.paint(g); // paint the living aliens
            }
            for (PlayerBullet bullet : bulletsList) {
                bullet.paint(g); // paint the bullets
            }
            PlayerShip.paint(g); // paint the spaceship
        }
        else { // GAME OVER screen
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight()); // paint the whole canvas black, clears the screen
            g.setColor(Color.WHITE); // for text

            Font f1 = new Font("Times", Font.PLAIN,60); // set the font
            g.setFont(f1);
            drawCenteredString(g, "GAME OVER", f1, WindowSize.height/100*40); // draws the text in the middle of the screen

            Font f2 = new Font("Times", Font.PLAIN,20);
            g.setFont(f2);
            drawCenteredString(g, "Press any key to play", f2, WindowSize.height/100*58); // draws the text, in the middle of the screen

            g.setColor(Color.lightGray); // a lighter version for this text
            drawCenteredString(g, "[Arrow keys to move, space to fire]", f2, WindowSize.height/100*65); // draws the text, in the middle
        }
        strategy.show(); // indicate that we want to flip the buffers
    }

    public static void drawCenteredString(Graphics g, String text, Font font, int y) {
        // to draw text centered on the screen
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font); // to get the size of the font
        // Determine the X coordinate for the text
        int x = (WindowSize.width - metrics.stringWidth(text)) / 2; // the position where the text will be centered
        // Set the font
        g.setFont(font);
        // Draw the String, with the given x and y positions
        g.drawString(text, x, y);
    }

    public void shootBullet() {
        // add a new bullet to our list
        // to make the bullet come out of the middle of the spaceship
        double xx = PlayerShip.x + (double)playerImage.getWidth(null)/2 - (double)bulletImage.getWidth(null)/2;
        double yy = PlayerShip.y - (double)bulletImage.getHeight(null);
        PlayerBullet b = new PlayerBullet(bulletImage, WindowSize.width); // new bullet
        b.setPosition(xx, yy); // with those x and y positions
        b.setXSpeed(10); // set the speed of the bullet
        bulletsList.add(b); // add the bullet to the array
    }

    public boolean hasCollisionHappened1(Alien alien, PlayerBullet bullet) {
        // collision method between alien and bullets
        // assigning the fields
        double x1 = alien.x, y1 = alien.y, w1 = alienImage1.getWidth(null), h1 = alienImage1.getHeight(null);
        double x2 = bullet.x, y2 = bullet.y, w2 = bulletImage.getWidth(null), h2 = bulletImage.getHeight(null);

        // if alien image and bullet image overlap, collision occurred, hence return true, otherwise return false
        return ((x1 < x2 && x1 + w1 > x2) || (x2 < x1 && x2 + w2 > x1)) &&
                ((y1 < y2 && y1 + h1 > y2) || (y2 < y1 && y2 + h2 > y1));
    }

    public boolean hasCollisionHappened2(Alien alien, Spaceship spaceship) {
        // collision method between alien and spaceship
        double x1 = alien.x, y1 = alien.y, w1 = alienImage1.getWidth(null), h1 = alienImage1.getHeight(null);
        double x2 = spaceship.x, y2 = spaceship.y, w2 = playerImage.getWidth(null), h2 = playerImage.getHeight(null);

        // if alien image and spaceship image overlap, collision occurred, hence return true, otherwise return false
        return ((x1 < x2 && x1 + w1 > x2) || (x2 < x1 && x2 + w2 > x1)) &&
                ((y1 < y2 && y1 + h1 > y2) || (y2 < y1 && y2 + h2 > y1));
    }

    public void checkCollisionAlienPlayerOrHitBottom() {
        // a method to use the collision method between aliens and spaceship, essentially if a game losing condition has been met
        for (Alien alien : AliensArray) { // go through aliens
            if (!alien.isAlive) continue; // ignore the dead aliens
            if (hasCollisionHappened2(alien, PlayerShip) || alien.y+alienImage1.getHeight(null) >= WindowSize.height) {
                // if alien and spaceship collided, or alien touched the bottom of the window
                isGameRunning = false; // game exit
                isStartingFrame = true; // set to true to indicate, a new game is going to start once user clicks any key on the GAME OVER screen, best score remains the highest score (doesn't get reset)
            }
        }
    }

    // application entry point
    public static void main(String[] args) {
        new InvadersApplication(); // start the program
    }
}
