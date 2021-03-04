package com.owen.game.instances.moveable;

import com.owen.game.Game;
import com.owen.game.hex.Hex;
import com.owen.game.map.Position;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Wolf extends Enemy {
    private ArrayList<Position> poss = new ArrayList<>();

    public Wolf(Position mapPosition, Random rand, BufferedImage sprite, Game game) {
        super(mapPosition, rand, sprite, game);
        time = 7;
        if (game.getHexMap().getHex(mapPosition).landmass != null) {
            for (Hex hex : game.getHexMap().getHex(mapPosition).landmass.getHexes()) poss.add(hex.getMapPosition());
        }

    }

    @Override
    public void update() {
        super.update();
        if (target == null) {
            if (poss.size() == 0) setTarget(game.getHexMap().getRandomLand());
            else setTarget(game.getHexMap().getRandomLand(poss));
        }
    }

    @Override
    public void setSoldiers(int soldiers) {
        this.soldiers = soldiers;
        warriors = soldiers;
        archers = 0;
        updateMult();
    }
}
