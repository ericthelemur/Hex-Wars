package com.owen.game.battlemap.enemies;

import com.owen.game.Game;
import com.owen.game.battlemap.BattleMap;
import com.owen.game.map.Position;
import com.owen.game.sprites.CharacterGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BattleEnemyWarrior extends BattleEnemy {
    public BattleEnemyWarrior(Position mapPosition, BufferedImage sprite, BattleMap map, Game game) {
        super(mapPosition, sprite, 4, 2, map, game);

        Graphics g = this.sprite.getGraphics();
        g.drawImage(CharacterGenerator.getWeapons().get(1), 0, 0, null);
        g.dispose();
    }
}
