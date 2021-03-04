package com.owen.game.instances.moveable;

import com.owen.game.Game;
import com.owen.game.map.Position;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Enemy extends Moveable {
    public Enemy(Position mapPosition, Random rand, BufferedImage sprite, Game game) {
        super(mapPosition, sprite, game, new Color(255, 0, 0));
        time = 15;
        setSoldiers(2+rand.nextInt(4));
    }

    @Override
    public void update() {
        super.update();
        if (target == null) {
            setTarget(game.getHexMap().getRandomLand());
        }
    }
}
