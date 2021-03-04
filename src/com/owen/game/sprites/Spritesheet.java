package com.owen.game.sprites;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Spritesheet {
    private BufferedImage sprite = null;
    private final int TILE_WIDTH, TILE_HEIGHT;

    public Spritesheet(String imagePath, int TileSize) {
        TILE_WIDTH = TILE_HEIGHT = TileSize;

        URL url = this.getClass().getResource("Spritesheets/"+imagePath);
        try {
            sprite = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Spritesheet(BufferedImage img, int tileWidth, int tileHeight) {
        sprite = img;
        TILE_WIDTH = tileWidth;
        TILE_HEIGHT = tileHeight;
    }

    public BufferedImage getSprite(int gridX, int gridY) {
        return sprite.getSubimage(gridX * TILE_WIDTH, gridY * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
    }

    public ArrayList<BufferedImage> getStrip(int gridY, int width) {
        ArrayList<BufferedImage> output = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            output.add(getSprite(i, gridY));
        }
        return output;
    }

    public ArrayList<BufferedImage> getStrip(int gridY) {
        return getStrip(gridY, sprite.getWidth()/TILE_WIDTH);
    }

    public ArrayList<BufferedImage> getStrip(int[] gridYList) {
        ArrayList<BufferedImage> output = new ArrayList<>();
        for (int gridY : gridYList) output.addAll(getStrip(gridY));
        return output;
    }

    public ArrayList<BufferedImage> getStrip(int[] gridYList, int endWidth) {
        ArrayList<BufferedImage> output = new ArrayList<>();
        for (int i = 0; i < gridYList.length; i++) {
            if (i == gridYList.length-1) output.addAll(getStrip(gridYList[i], endWidth));
            else                         output.addAll(getStrip(gridYList[i]));
        }
        return output;
    }

    public BufferedImage getFullImg() {
        return sprite;
    }
}