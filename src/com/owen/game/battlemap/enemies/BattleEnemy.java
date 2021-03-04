package com.owen.game.battlemap.enemies;

import com.owen.game.Game;
import com.owen.game.battlemap.BattleMap;
import com.owen.game.battlemap.BattleMoveable;
import com.owen.game.map.Position;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class BattleEnemy extends BattleMoveable {
    private HashMap<Position,Double> ratings;
    private ArrayList<Position> rankings;

    public BattleEnemy(Position mapPosition, BufferedImage sprite, int maxDamage, int minDamage, BattleMap map, Game game) {
        super(mapPosition, sprite, maxDamage, minDamage, map, game, new Color(255, 0, 0));
    }

    public void pickTarget(Random rand) {
        int preferredDistance = 1;
        if (this instanceof BattleEnemyArcher) preferredDistance = 5;

        ratings = new HashMap<>();
        rankings = new ArrayList<>();

        double dist, chance;
        for (Position pos : possibleMoves) {
            dist = Math.abs(-preferredDistance+getPlayerDist(pos));
            chance = dist;
            ratings.put(pos, chance);
            rankings.add(pos);
        }
        rankings.sort((o1, o2) -> (int) ((ratings.get(o2) - ratings.get(o1))+(map.getHex(o2).getDistance(mapPosition.x, mapPosition.y) - map.getHex(o1).getDistance(mapPosition.x, mapPosition.y))));
        if (this instanceof BattleEnemyArcher) Collections.reverse(rankings);
        setTarget(rankings.get(rand.nextInt(3)));
        
    }

    private int getPlayerDist(Position pos) {
        for (int i = 1; i < 10; i++) {
            for (Position ringPos: map.getHex(pos).getRing(i)) {
                if (map.playerOn(ringPos) != null) {
                    return 10-i;
                }
            }
        }
        return 0;
    }
    
    public void pickAttacking(Random rand) {
        HashMap<Position, Integer> attackRatings = new HashMap<>();
        rankings = new ArrayList<>();
        
        for (Position pos : possibleAttacks) {
            if (map.playerOn(pos) != null) {
                attackRatings.put(pos, -map.playerOn(pos).getHealth());
                rankings.add(pos);
            }
        }
        if (attackRatings.size() == 0) return;
        
        rankings.sort((o1, o2) -> (attackRatings.get(o2) - attackRatings.get(o1)));
        for (Position ranking : rankings) {
            if (rand.nextFloat() > 0.5) {
                setAttacking(map.playerOn(ranking));
                return;
            }
        }
        setTarget(rankings.get(0));
    }
}
