package com.owen.game.map;

import com.owen.game.hex.Hex;

import java.util.ArrayList;

public class Landmass extends Position {
    private int number, size;
    private ArrayList<Hex> hexes = new ArrayList<>();

    public Landmass(int x, int y, int number) {
        super(x, y);
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ArrayList<Hex> getHexes() {
        return hexes;
    }

    public Hex getHex(int i) {
        return hexes.get(i);
    }

    public void addHex(Hex hex) {
        hexes.add(hex);
    }
}
