package com.owen.game.battlemap;

import com.owen.game.Game;
import com.owen.game.map.Biome;

public class BattleMapGenerator {
    public static BattleHex[][] generateBattleMap(Biome biome, int width, int height, int hexWidth, BattleMap map, Game game) {
        BattleHex[][] data = new BattleHex[width][height];
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) {
            data[x][y] = new BattleHex(x, y, hexWidth, map, game);
            data[x][y].setBiome(biome);
        }
        return data;
    }
}
