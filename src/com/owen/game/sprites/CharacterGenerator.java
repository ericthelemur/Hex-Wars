package com.owen.game.sprites;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Random;

public class CharacterGenerator {
    private static final Spritesheet clothesSS = new Spritesheet("pc_sprites.png", 32);

    private static final BufferedImage body = clothesSS.getSprite(1, 0);
    private static final ArrayList<BufferedImage> tops = getClothesItems(Rows.TOPS);
    private static final ArrayList<BufferedImage> jackets = getClothesItems(Rows.JACKETS);        // Loads jacket;
    private static final ArrayList<BufferedImage> bottoms = getClothesItems(Rows.BOTTOMS);        // Loads bottoms;
    private static final ArrayList<BufferedImage> eyes = getClothesItems(Rows.EYES);              // Loads eyes

    private static final Spritesheet hairSS = new Spritesheet("male_hair.png", 32);
    private static final ArrayList<BufferedImage>hats = hairSS.getStrip(0);
    private static final ArrayList<HairType> hairTypes = new ArrayList<>(getHairBeards());

    private static BufferedImage wolfSprite, attackSword;
    private static final ArrayList<BufferedImage> weapons = clothesSS.getStrip(10, 3);

    static {
        try {
            attackSword = ImageIO.read(Spritesheet.class.getResource("Spritesheets/attackSword.png"));
            wolfSprite = ImageIO.read(Spritesheet.class.getResource("Spritesheets/wolf.png"));

        } catch (IOException ignored) {}
    }


    private static BufferedImage randomClothes(ArrayList<BufferedImage> arr, Random rand) {
        return arr.get(rand.nextInt(arr.size()));
    }

    private static BufferedImage randomHair(Random rand) {
        BufferedImage output = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) output.getGraphics();           // Create base image

        HairType type = hairTypes.get(rand.nextInt(hairTypes.size()));      // Pick hair colour
        int hairNo = rand.nextInt(hats.size() + type.hair.size()*hairTypes.size());    // Pick hair style (4x more likely to have hair than hat)

        if (hairNo < hats.size()) g.drawImage(hats.get(hairNo), 0, 0,null);     // If hat, pick hat type and draw onto base
        else {
            hairNo = (hairNo-hats.size())/hairTypes.size();        // If hair, get index
            g.drawImage(type.hair.get(hairNo), 0, 0, null);     // And draw onto base
        }
        g.drawImage(type.beards.get(rand.nextInt(type.beards.size())), 0, 0, null);
                                                                                // Pick and draw random beard style
        g.dispose();        // Clean up
        return output;
    }

    public static BufferedImage generateCharacter(Random rand) {
        BufferedImage output = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) output.getGraphics();       // Create base image
        g.drawImage(body, 0, 0, null);          // Draw on body
        g.drawImage(randomClothes(eyes,     rand), 0, 0, null);       // Pick random items of clothing
        g.drawImage(randomClothes(bottoms,  rand), 0, 0, null);
        g.drawImage(randomClothes(tops,     rand), 0, 0, null);
        g.drawImage(randomClothes(jackets,  rand), 0, 0, null);
        g.drawImage(randomHair(             rand), 0, 0, null);              // Generates random hair
        g.dispose();            // Clean up
        return output;
    }

    private static ArrayList<BufferedImage> getClothesItems(Rows rows) {
        if (rows.getWidth() == 10) return clothesSS.getStrip(rows.getRows());
        else return clothesSS.getStrip(rows.getRows(), rows.getWidth());
    }

    private static ArrayList<HairType> getHairBeards() {
        ArrayList<HairType> output = new ArrayList<>();
        for (Hair val: Hair.values()) {
            output.add(new HairType(hairSS.getStrip(val.getHairRow(), 8), hairSS.getStrip(val.getBeardRow(), 5)));
        }
        return output;
    }

    public static BufferedImage generateDeconstCharacter(Random rand) {         // Debug display of every element in the character image
        BufferedImage output = new BufferedImage(32, 192, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) output.getGraphics();       // Create base image
        g.drawImage(body, 0, 0, null);          // Draw on body
        g.drawImage(randomClothes(eyes,     rand), 0, 32, null);       // Pick random items of clothing
        g.drawImage(randomClothes(bottoms,  rand), 0, 64, null);
        g.drawImage(randomClothes(tops,     rand), 0, 96, null);
        g.drawImage(randomClothes(jackets,  rand), 0, 128, null);
        g.drawImage(randomHair(             rand), 0, 160, null);              // Generates random hair
        g.dispose();            // Clean up
        return output;
    }

    public static BufferedImage getWolfSprite() {
        return wolfSprite;
    }

    public static ArrayList<BufferedImage> getWeapons() {
        return weapons;
    }

    public static BufferedImage getAttackSword() {
        return attackSword;
    }
}



class HairType {        // Stores hair types per colour in an object
    final ArrayList<BufferedImage> hair, beards;

    HairType(ArrayList<BufferedImage> hair, ArrayList<BufferedImage> beards) {
        this.hair = hair;
        this.beards = beards;
    }
}

enum Rows {
    TOPS            (new int[] {1, 2}),
    JACKETS         (new int[] {1, 2, 3, 4, 5}),
    BOTTOMS         (new int[] {6, 7 ,8}),
    EYES            (new int[] {9}, 7);


    private final int[] rows;
    private final int width;

    Rows(int[] rows) {
        this.rows = rows;
        this.width = 10;
    }

    Rows(int[] rows, int width) {
        this.rows = rows;
        this.width = width;
    }

    public int[] getRows() {
        return rows;
    }

    public int getWidth() {
        return width;
    }
}

enum Hair {
    BROWN           (1),
    DARKBLONDE      (4),
    LIGHTBLONDE     (7),
    BLACK           (10),
    RED             (13);

    private final int row;

    Hair(int row) {
        this.row = row;
    }

    public int getHairRow() {
        return this.row+2;
    }
    public int getBeardRow() {
        return this.row;
    }
}

