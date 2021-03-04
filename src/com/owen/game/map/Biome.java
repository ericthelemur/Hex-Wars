package com.owen.game.map;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Biome {
    private final String name;
    private final Color colour;
    private int cost = -1;
    private ArrayList<BufferedImage> sprites;

    public Biome(String name, Color colour) {
        this.name = name;
        this.colour = colour;
    }

    public Biome(String name, Color colour, int cost) {
        this.name = name;
        this.colour = colour;
        this.cost = cost;
    }

    public Biome(String name, Color colour, int cost, ArrayList<BufferedImage> sprites) {
        this.name = name;
        this.colour = colour;
        this.cost = cost;
        this.sprites = sprites;
    }

    public String getName() {
        return name;
    }

    public Color getColour() {
        return colour;
    }

    public int getCost() {
        return cost;
    }

    public BufferedImage getSprite() {
        return sprites.get(0);
    }

    public ArrayList<BufferedImage> getSprites() {
        return sprites;
    }

    public BufferedImage getRandomSprite() {
        return sprites.get(new Random().nextInt(sprites.size()));
    }
}
