package com.owen.game.battlemap.players;

import com.owen.game.Game;
import com.owen.game.battlemap.BattleMap;
import com.owen.game.battlemap.BattleMoveable;
import com.owen.game.battlemap.enemies.BattleEnemy;
import com.owen.game.instances.Instance;
import com.owen.game.map.Position;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

@SuppressWarnings("Duplicates")
public class BattlePlayer extends BattleMoveable {

    public BattlePlayer(Position mapPosition, BufferedImage sprite, BattleMap map, Game game) {
        super(mapPosition, sprite, 5, 3, map, game, new Color(0, 255, 0));
    }

    public ArrayList<Position> pathfindToAttackingRange(Position target) {
        BattleEnemy targetEnemy = map.enemyOn(target);
//        System.out.printf("Pathfinding %s from %s for %s\n", target, mapPosition, this);
        Position twp = map.getHex(target).getWorldPosition();

        if (map.getHex(target).getCost() == 0) {
            System.out.println("Searching Cancelled, Target is Unreachable");
            return null;
        }

        HashMap<Position,Integer> costSoFar = new HashMap<>();
        costSoFar.put(mapPosition, 0);

        HashMap<Position,Position> cameFrom = new HashMap<>();
        cameFrom.put(mapPosition, null);

        PriorityQueue<Position> frontier = new PriorityQueue<>(11, (Position o1, Position o2) -> {
            Position dwp1 = map.getHex(o1).getWorldPosition().sub(twp);
            Position dwp2 = map.getHex(o2).getWorldPosition().sub(twp);

            double d1 = Math.sqrt(dwp1.x*dwp1.x+dwp1.y*dwp1.y);
            double d2 = Math.sqrt(dwp2.x*dwp2.x+dwp2.y*dwp2.y);

            //System.out.printf("%.3f %d\n", (d1-d2)/(200), (costSoFar.get(o1)-costSoFar.get(o2)));
            return (int) ((d1-d2)/(200) + (costSoFar.get(o1)-costSoFar.get(o2)));
        });

        frontier.add(mapPosition);

        Position current = mapPosition;
        while (frontier.size() > 0) {
            current = frontier.remove();
            for (Position next : map.getHex(current).getNeighboursPos()) {
                int newCost = costSoFar.get(current) + map.getHex(next).getCost();
                if ((!costSoFar.containsKey(next) || costSoFar.get(next) > newCost) && map.getHex(next).getCost() != 0 && canMove(next, targetEnemy)) {
                    costSoFar.put(next, newCost);
                    cameFrom.put(next, current);
                    frontier.add(next);
                }
            }

            if (getPossibleAttacks(current).contains(target)) {
                break;
            }
        }

//        System.out.println("Searching Finished");
        if (frontier.size() == 0) {
            this.target = null;
            return null;
        }

        // Construct full path and restrict to movement for this turn
        ArrayList<Position> fullPath = constructPath(cameFrom, current), path = new ArrayList<>();
        for (Position pos : fullPath) {
            if (possibleMoves.contains(pos)) path.add(pos);
            else break;
        }

        return path;
//        return constructPath(cameFrom, current);
    }

    private boolean canMove(Position pos, BattleEnemy TE) {
        for (Instance inst : map.getInstances()) {
            if (inst == this || inst == TE) continue;
            if (inst instanceof BattleMoveable) {
                BattleMoveable bm = (BattleMoveable) inst;
                if (bm.getTarget() != null) {
                    if (bm.getTarget().equals(pos)) return false;
                } else {
                    if (bm.getMapPosition().equals(pos)) return false;
                }
            }
        }
        return true;
    }
}
