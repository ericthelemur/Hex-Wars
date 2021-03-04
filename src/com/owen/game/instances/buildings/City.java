package com.owen.game.instances.buildings;

import com.owen.game.Game;
import com.owen.game.hex.Hex;
import com.owen.game.map.MapGenerator;
import com.owen.game.map.Position;

import java.awt.image.BufferedImage;

public class City extends Building {
    private int recruitCost;

    public City(Position mapPosition, BufferedImage sprite, Game game) {
        super(mapPosition, sprite, game);
        recruit();

        for (Hex hex: game.getHexMap().getHex(mapPosition).getNeighbours())
            if (hex.getBiome() != MapGenerator.getBiome("Ocean")) hex.setBuilding(new Farmland(hex.getMapPosition(), MapGenerator.getRandomSprite("Farmland"), game));
    }

    public int getRecruitCost() {
        return recruitCost;
    }

    public int recruit() {
        int val = recruitCost;
        recruitCost = 80+game.rand.nextInt(40);
        return val;
    }
}
