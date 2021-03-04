package com.owen.game.instances.buildings;

import com.owen.game.Game;
import com.owen.game.instances.Instance;
import com.owen.game.map.Position;

import java.awt.image.BufferedImage;

public class Building extends Instance {
    public Building(Position mapPosition, BufferedImage sprite, Game game) {
        super(mapPosition, sprite, game);
        game.getHexMap().addBuilding(this);
    }
}
