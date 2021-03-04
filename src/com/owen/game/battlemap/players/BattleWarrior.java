package com.owen.game.battlemap.players;

import com.owen.game.Game;
import com.owen.game.battlemap.BattleMap;
import com.owen.game.map.Position;
import com.owen.game.sprites.CharacterGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BattleWarrior extends BattlePlayer {
    public BattleWarrior(Position mapPosition, BufferedImage sprite, BattleMap map, Game game) {
        super(mapPosition, sprite, map, game);
        Graphics g = this.sprite.getGraphics();
        g.drawImage(CharacterGenerator.getWeapons().get(1), 0, 0, null);
        g.dispose();
    }
}
