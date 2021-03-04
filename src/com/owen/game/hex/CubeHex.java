package com.owen.game.hex;

import com.owen.game.battlemap.BattleHex;

public class CubeHex {
    private int q, r, s;
    public CubeHex(int q, int r, int s) {
        this.q = q;
        this.r = r;
        this.s = s;
    }

    public CubeHex(int x, int y) {  // Offset to Cube converter
        q = y;
        r = (int) (x - (y - (y&1)) / 2f);
        s = -q - r;
    }

    public CubeHex(Hex hex) {
        int x = hex.getMapPosition().x;
        int y = hex.getMapPosition().y;
        q = y;
        r = (int) (x - (y - (y&1)) / 2f);
        s = -q - r;
    }

    public CubeHex(BattleHex hex) {
        int x = hex.getMapPosition().x;
        int y = hex.getMapPosition().y;
        q = y;
        r = (int) (x - (y - (y&1)) / 2f);
        s = -q - r;
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    public int getS() {
        return s;
    }
}
