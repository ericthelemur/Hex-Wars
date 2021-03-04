package com.owen.game.battlemap.enemies;

import com.owen.game.Game;
import com.owen.game.battlemap.BattleMap;
import com.owen.game.map.Position;

import java.awt.image.BufferedImage;

public class BattleEnemyWolf extends BattleEnemy {
    public BattleEnemyWolf(Position mapPosition, BufferedImage sprite, BattleMap map, Game game) {
        super(mapPosition, sprite, 4, 2, map, game);
    }
}
