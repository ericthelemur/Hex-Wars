package com.owen.game.battlemap.players;

import com.owen.game.Game;
import com.owen.game.battlemap.BattleMap;
import com.owen.game.map.Position;
import com.owen.game.sprites.CharacterGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class BattleArcher extends BattlePlayer {
    public BattleArcher(Position mapPosition, BufferedImage sprite, BattleMap map, Game game) {
        super(mapPosition, sprite, map, game);
        Graphics g = this.sprite.getGraphics();
        g.drawImage(CharacterGenerator.getWeapons().get(0), 0, 0, null);
        g.dispose();
    }

    @Override
    public void select() {
        possibleMoves = getPossibilities(5);
        possibleAttacks = getPossibleAttacks(mapPosition);
    }

    public ArrayList<Position> getPossibleAttacks(Position pos) {
        ArrayList<Position> possible = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            possible.addAll(map.getHex(pos).getRing(i));
        }
        return possible;
    }
}
