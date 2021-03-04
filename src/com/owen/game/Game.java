package com.owen.game;

import com.owen.game.hex.Hex;
import com.owen.game.instances.moveable.Moveable;
import com.owen.game.map.Biome;
import com.owen.game.map.Map;
import com.owen.game.map.MapGenerator;
import com.owen.game.map.Position;
import com.owen.game.modules.*;
import com.owen.game.sprites.Spritesheet;
import com.owen.game.states.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class Game implements Runnable {
    private int width = 1920, height = 1080;

    private final int MAPWIDTH = 200, MAPHEIGHT = 200, HEXWIDTH = 57;
    private final int HEXHEIGHT = (int) (2*HEXWIDTH/Math.sqrt(3));

    private final int FPSTarget = 30;
    private final long SPFTarget = (long) (1e9 / FPSTarget);
    private int FPS = 0;

    public final KeyboardModule keyboardModule = new KeyboardModule();
    public final MouseModule mouseModule = new MouseModule();
    private Canvas canvas;
    private BufferStrategy strategy;
    private final Position blankPosition = new Position(-1, -1);
    private final Hex blankHex = new Hex(-1, -1, 57, null,this);
    private final Font smallFont = new Font("Segoe UI", Font.BOLD, 16), mediumFont = new Font("Segoe UI Semibold", Font.PLAIN, 24),
            largeFont = new Font("Segoe UI Semibold", Font.PLAIN, 32), XLFont = new Font("Segoe UI Semibold", Font.PLAIN, 50);

    private boolean running = false, paused = true;
    public boolean debug = false;

    private HashMap<String,BlankState> states;
    private BlankState state;

    private Map hexMap;
    private String loadMessage = "Hello";
    //private long seed = new Random().nextLong();
    private long seed = 2138388166526162848L;

    //private long seed = 2138388166526162848L;
    public Random rand = new Random(seed);

    public Game(){
        System.out.println(seed);
        init();
        run();
    }

    private void init() {
        // Frame
        JFrame frame = new JFrame();
        frame.setName("HexWars");

        Toolkit tk = Toolkit.getDefaultToolkit();     // use Javas built-in toolkit to make a custom cursor and get screen size

        BufferedImage cursorImage = loadImg("Spritesheets/cursor.png");
        Cursor customCursor = tk.createCustomCursor(cursorImage, new Point(16,16), "Hand");
        frame.setCursor(customCursor);

//        // Set size to be correct including insets, disabled for fullscreen
        Insets insets = frame.getInsets();
        Dimension frameSize = new Dimension(width + insets.left + insets.right, height + insets.bottom + insets.top);
        frame.setSize(frameSize);
        frame.setPreferredSize(frameSize);
        frame.setMinimumSize(frameSize);
        frame.setMaximumSize(frameSize);

        frame.setResizable(false);           // We don't want the user to be able to drag the window-edges
        frame.setUndecorated(false);         // We don't want to remove the edges, because then we can't move the frame

        // Set size to screen size and make fullscreen
//        Dimension dim = tk.getScreenSize();
//        width = dim.width;
//        height = dim.height;
//        frame.setSize(dim);
//        System.out.println(frame.getSize());
//        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
//        frame.setUndecorated(true);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);   // Nothing else happens on close

        // Creates window listener so close action can be listened to
        WindowListener wl = new WindowListener(){
            @Override
            public void windowClosing(WindowEvent arg0) {
                stop();
            }
            @Override
            public void windowActivated(WindowEvent arg0) {}
            @Override
            public void windowClosed(WindowEvent arg0) {}
            @Override
            public void windowDeactivated(WindowEvent arg0) {}
            @Override
            public void windowDeiconified(WindowEvent arg0) {}
            @Override
            public void windowIconified(WindowEvent arg0) {}
            @Override
            public void windowOpened(WindowEvent arg0) {}
        };

        // Add listener
        frame.addWindowListener(wl);
        JPanel fullPanel = new JPanel();        // Background JPanel
        fullPanel.setSize(width, height);       // Set to be full size of frame (without the edges).
        fullPanel.setLayout(null);              // Clear the layout, so we'll have full control over where things are placed.
        frame.setContentPane(fullPanel);        // We then set it to be the contentPane on the frame i.e. the background

        canvas = new Canvas();
        canvas.setBounds(0, 0, width, height);     // Set to fullscreen
        canvas.setIgnoreRepaint(true);          // No repaint on window move

        // Add listeners to the canvas.
        canvas.addKeyListener(keyboardModule);
        canvas.addMouseListener(mouseModule);
        canvas.addMouseMotionListener(mouseModule);
        canvas.addMouseWheelListener(mouseModule);

        // Set font
        canvas.setFont(smallFont);

        // Add the fullFramePanel
        fullPanel.add(canvas);
        frame.pack();
        frame.setVisible(true);

        // Set up double buffering on the canvas (Swing- and AWT-elements are already double-buffered).
        canvas.createBufferStrategy(2);
        strategy = canvas.getBufferStrategy();

        // Create states to switch to
        states = new HashMap<>();
        states.put("Game",   new GameState(this));
        states.put("Map",    new MapState(this));
        states.put("Battle", new BattleState(this));
        states.put("Menu",   new MenuState(this));

        switchState("Menu");
    }

    public void genMap() {   // Generate map
        updateLoadMessage("Generating map...");
        hexMap = new Map(MAPWIDTH, MAPHEIGHT, HEXWIDTH, this, rand);
        updateLoadMessage("");
        // Generation required after initialization
        MapGenerator.step2(hexMap, MAPWIDTH, MAPHEIGHT, this, rand);
        updateLoadMessage("Generating map... Done");
        getState().afterMapGen();
    }

    public void updateLoadMessage(String message) {     // Outputs loading messages
        loadMessage = message;
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        g.setColor(new Color(200, 100, 50));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);

        FontMetrics metrics = g.getFontMetrics();
        g.drawString(loadMessage, width/2-metrics.stringWidth(loadMessage)/2, height/2-metrics.getHeight()/2);
        g.dispose();
        canvas.getBufferStrategy().show();
    }

    public void stop() {
        // Sets running to false, so the gameloop exits, and run() ends, killing the thread.
        running = false;
    }

    public void drawPrompt(String prompt, Color colour, Graphics g) {
        g.setFont(largeFont);
        g.setColor(colour);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(prompt, width/2-metrics.stringWidth(prompt)/2, (int) (0.9*height-metrics.getHeight()/2));
        g.setFont(smallFont);
    }

    @Override
    public void run() {
        running = true;
        // For loop timings (limit FPS)
        long beginLoopTime=0, endLoopTime, deltaLoop, previousBeginTime;

        // Main game loop
        while(running && !keyboardModule.isPressed(KeyEvent.VK_ESCAPE)) {
            // Calculate FPS
            previousBeginTime = beginLoopTime;
            beginLoopTime = System.nanoTime();
            FPS = (int) (1e9 / (beginLoopTime-previousBeginTime));

            // React to keyboard and mouse-input
            state.manageControls();

            // Update logic
            state.update();

            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();     // Get the Graphics2D object from the canvas
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);  // Turn on anti-aliasing
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);  // and image interpolation
            g.setStroke(new BasicStroke(1));

            g.setColor(new Color( 68,  68, 122));       // Fill with ocean colour
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.setFont(smallFont);

            state.draw(g);                                               // Draw everything on the canvas in the draw method
            g.setColor(Color.white);
            g.dispose();                                                // Free up any resources the Graphics2D-object is using
            canvas.getBufferStrategy().show();                          // Show the buffered image we've been drawing

            endLoopTime = System.nanoTime();
            deltaLoop = endLoopTime - beginLoopTime;

            // Sleep for remaining time of the frame
            if (deltaLoop <= SPFTarget) {
                try{
                    Thread.sleep((SPFTarget - deltaLoop)/(1000*1000));
                }catch(InterruptedException ignored){}
            }
        }

        // Closes the entire application, when we get outside the gameloop
        System.exit(0);
    }

    public void switchState(String stateName) {     // Swtiches between states
        System.out.println("Switch to " + stateName);
        state = states.get(stateName);
        //state.resetCamera();
    }

    public void enterBattleState(Biome biome, Moveable aggressor, Moveable receiver) {  // Enters battle
        System.out.println("Entering battle between " + aggressor + " and " + receiver);
        switchState("Battle");
        ((BattleState) state).startBattle(biome, aggressor, receiver);
    }

    public void drawMultipleLines(String ss, int x, int y, Graphics g) {
        String[] strings = ss.split("\n");
        int stringHeight = g.getFontMetrics().getHeight();
        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];
            g.drawString(s, x, y+i*stringHeight);
        }
    }

    public BufferedImage loadImg(String path) {
        try {
            return toCompatibleImage(ImageIO.read(Spritesheet.class.getResource(path)));
        } catch (IOException e) {
            System.out.println("Cannot find Image: "+path);
        }
        return null;
    }

    private BufferedImage toCompatibleImage(BufferedImage image) {
        if (image == null) System.out.println("Image is null");
        // obtain the current system graphical settings
        GraphicsConfiguration gfxConfig = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();

        /*
         * if image is already compatible and optimized for current system
         * settings, simply return it
         */
        if (image.getColorModel().equals(gfxConfig.getColorModel()))
            return image;

        // otherwise image is not optimized, so create a new image that is
        BufferedImage newImage = gfxConfig.createCompatibleImage(
                image.getWidth(), image.getHeight(), image.getTransparency());

        // get the graphics context of the new image to draw the old image on
        Graphics2D g2d = (Graphics2D) newImage.getGraphics();

        // actually draw the new image and dispose of context no longer needed
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // return the new optimized image
        return newImage;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMAPWIDTH() {
        return MAPWIDTH;
    }

    public int getMAPHEIGHT() {
        return MAPHEIGHT;
    }

    public int getHEXWIDTH() {
        return HEXWIDTH;
    }

    public int getHEXHEIGHT() {
        return HEXHEIGHT;
    }

    public int getFPS() {
        return FPS;
    }

    public Map getHexMap() {
        return hexMap;
    }

    public BlankState getState() {
        return state;
    }

    public Position getBlankPosition() {
        return blankPosition;
    }

    public Hex getBlankHex() {
        return blankHex;
    }

    public HashMap<String, BlankState> getStates() {
        return states;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        System.out.println("Set paused: "+this.paused);
    }

    public void togglePaused() {
        paused = !paused;
        System.out.println("Toggle pause: "+paused);
    }

    public Font getSmallFont() {
        return smallFont;
    }

    public Font getMediumFont() {
        return mediumFont;
    }

    public Font getLargeFont() {
        return largeFont;
    }

    public Font getXLFont() {
        return XLFont;
    }
}
